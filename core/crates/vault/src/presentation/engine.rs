use crate::application::growth::use_cases::list_by_range::{
    ListGrowthByRangeCommand, ListGrowthByRangeUseCase,
};
use crate::application::growth::use_cases::log::{LogGrowthCommand, LogGrowthUseCase};
use crate::application::media::use_cases::list_metadata::ListMediaMetadataUseCase;
use crate::application::media::use_cases::read_decrypted::ReadDecryptedMediaUseCase;
use crate::application::media::use_cases::store_encrypted::{
    StoreEncryptedMediaCommand, StoreEncryptedMediaUseCase,
};
use crate::application::milestone::use_cases::create::{
    CreateMilestoneCommand, CreateMilestoneUseCase,
};
use crate::application::milestone::use_cases::delete::DeleteMilestoneUseCase;
use crate::application::milestone::use_cases::detail::DetailMilestoneUseCase;
use crate::application::milestone::use_cases::list::{
    ListMilestonesCommand, ListMilestonesUseCase,
};
use crate::application::vault::use_cases::init_master_key::InitMasterKeyUseCase;
use crate::application::vault::use_cases::unlock::{UnlockCommand, UnlockUseCase};
use crate::infrastructure::crypto::chacha20_engine::ChaCha20Engine;
use crate::infrastructure::repository::sqlite_growth::SqliteGrowthRepository;
use crate::infrastructure::repository::sqlite_media::SqliteMediaRepository;
use crate::infrastructure::repository::sqlite_milestone::SqliteMilestoneRepository;
use crate::infrastructure::repository::sqlite_pool;
use crate::presentation::dto::{GrowthLogDto, MediaItemDto, MilestoneDto};
use crate::presentation::error::FfiError;
use crate::presentation::mappers;
use chrono::{TimeZone, Utc};
use std::sync::Mutex;
use uuid::Uuid;
use zeroize::Zeroizing;

#[derive(uniffi::Object)]
pub struct VaultEngine {
    master_key: Mutex<Option<Zeroizing<Vec<u8>>>>,
    milestone_repo: SqliteMilestoneRepository,
    growth_repo: SqliteGrowthRepository,
    media_repo: SqliteMediaRepository,
    crypto: ChaCha20Engine,
    storage_dir: String,
}

#[uniffi::export]
impl VaultEngine {
    #[uniffi::constructor]
    pub fn new(db_path: String, storage_dir: String) -> Result<Self, FfiError> {
        let pool = sqlite_pool::open(&db_path)
            .map_err(|e| FfiError::Internal { message: e.to_string() })?;

        Ok(Self {
            master_key: Mutex::new(None),
            milestone_repo: SqliteMilestoneRepository::new(pool.clone()),
            growth_repo: SqliteGrowthRepository::new(pool.clone()),
            media_repo: SqliteMediaRepository::new(pool),
            crypto: ChaCha20Engine,
            storage_dir,
        })
    }

    pub fn engine_version(&self) -> String {
        format!("vault {}", env!("CARGO_PKG_VERSION"))
    }

    pub fn generate_master_key(&self) -> Result<Vec<u8>, FfiError> {
        InitMasterKeyUseCase::execute().map_err(FfiError::from)
    }

    pub fn unlock(&self, raw_key: Vec<u8>) -> Result<(), FfiError> {
        let key = UnlockUseCase::execute(UnlockCommand { raw_key }).map_err(FfiError::from)?;
        let mut guard = self
            .master_key
            .lock()
            .map_err(|e| FfiError::Internal { message: e.to_string() })?;
        *guard = Some(key);
        Ok(())
    }

    // --- Milestones ---

    pub fn create_milestone(
        &self,
        title: String,
        description: String,
        occurred_at_millis: i64,
    ) -> Result<MilestoneDto, FfiError> {
        let occurred_at = Utc
            .timestamp_millis_opt(occurred_at_millis)
            .single()
            .ok_or_else(|| FfiError::Validation { message: "invalid occurred_at".into() })?;

        let uc = CreateMilestoneUseCase::new(&self.milestone_repo);
        let m = uc
            .execute(CreateMilestoneCommand { title, description, occurred_at })
            .map_err(FfiError::from)?;
        Ok(mappers::milestone_to_dto(m))
    }

    pub fn list_milestones(
        &self,
        limit: u32,
        offset: u32,
    ) -> Result<Vec<MilestoneDto>, FfiError> {
        let uc = ListMilestonesUseCase::new(&self.milestone_repo);
        let items = uc
            .execute(ListMilestonesCommand { limit, offset })
            .map_err(FfiError::from)?;
        Ok(items.into_iter().map(mappers::milestone_to_dto).collect())
    }

    pub fn get_milestone(&self, id: String) -> Result<MilestoneDto, FfiError> {
        let uuid = Uuid::parse_str(&id)
            .map_err(|_| FfiError::Validation { message: "invalid id".into() })?;
        let uc = DetailMilestoneUseCase::new(&self.milestone_repo);
        uc.execute(uuid).map(mappers::milestone_to_dto).map_err(FfiError::from)
    }

    pub fn delete_milestone(&self, id: String) -> Result<(), FfiError> {
        let uuid = Uuid::parse_str(&id)
            .map_err(|_| FfiError::Validation { message: "invalid id".into() })?;
        let uc = DeleteMilestoneUseCase::new(&self.milestone_repo);
        uc.execute(uuid).map_err(FfiError::from)
    }

    // --- Growth ---

    pub fn log_growth(
        &self,
        weight_grams: Option<u32>,
        height_mm: Option<u32>,
        notes: String,
        logged_at_millis: i64,
    ) -> Result<GrowthLogDto, FfiError> {
        let logged_at = Utc
            .timestamp_millis_opt(logged_at_millis)
            .single()
            .ok_or_else(|| FfiError::Validation { message: "invalid logged_at".into() })?;

        let uc = LogGrowthUseCase::new(&self.growth_repo);
        let g = uc
            .execute(LogGrowthCommand { weight_grams, height_mm, notes, logged_at })
            .map_err(FfiError::from)?;
        Ok(mappers::growth_log_to_dto(g))
    }

    pub fn list_growth_by_range(
        &self,
        from_millis: i64,
        to_millis: i64,
    ) -> Result<Vec<GrowthLogDto>, FfiError> {
        let from = Utc
            .timestamp_millis_opt(from_millis)
            .single()
            .ok_or_else(|| FfiError::Validation { message: "invalid from".into() })?;
        let to = Utc
            .timestamp_millis_opt(to_millis)
            .single()
            .ok_or_else(|| FfiError::Validation { message: "invalid to".into() })?;

        let uc = ListGrowthByRangeUseCase::new(&self.growth_repo);
        let items = uc
            .execute(ListGrowthByRangeCommand { from, to })
            .map_err(FfiError::from)?;
        Ok(items.into_iter().map(mappers::growth_log_to_dto).collect())
    }

    // --- Media ---

    pub fn store_media(
        &self,
        title: String,
        plaintext_bytes: Vec<u8>,
    ) -> Result<MediaItemDto, FfiError> {
        let key = self.key()?;
        let uc = StoreEncryptedMediaUseCase::new(&self.media_repo, &self.crypto);
        let item = uc
            .execute(StoreEncryptedMediaCommand {
                title,
                plaintext_bytes,
                storage_dir: self.storage_dir.clone(),
                master_key: key.to_vec(),
            })
            .map_err(FfiError::from)?;
        Ok(mappers::media_item_to_dto(item))
    }

    pub fn read_media(&self, id: String) -> Result<Vec<u8>, FfiError> {
        let key = self.key()?;
        let uuid = Uuid::parse_str(&id)
            .map_err(|_| FfiError::Validation { message: "invalid id".into() })?;
        let uc = ReadDecryptedMediaUseCase::new(&self.media_repo, &self.crypto);
        uc.execute(uuid, &key).map_err(FfiError::from)
    }

    pub fn list_media(
        &self,
        limit: u32,
        offset: u32,
    ) -> Result<Vec<MediaItemDto>, FfiError> {
        let uc = ListMediaMetadataUseCase::new(&self.media_repo);
        let items = uc.execute(limit, offset).map_err(FfiError::from)?;
        Ok(items.into_iter().map(mappers::media_item_to_dto).collect())
    }
}

impl VaultEngine {
    fn key(&self) -> Result<Zeroizing<Vec<u8>>, FfiError> {
        self.master_key
            .lock()
            .map_err(|e| FfiError::Internal { message: e.to_string() })?
            .as_ref()
            .cloned()
            .ok_or(FfiError::Validation {
                message: "vault is locked — call unlock() first".into(),
            })
    }
}
