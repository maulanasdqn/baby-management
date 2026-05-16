use crate::application::growth::error::GrowthError;
use crate::domain::growth::entity::{GrowthLog, NewGrowthLog};
use crate::domain::growth::repository::GrowthRepository;
use chrono::{DateTime, Utc};
use tracing::info;
use uuid::Uuid;

pub struct LogGrowthCommand {
    pub weight_grams: Option<u32>,
    pub height_mm: Option<u32>,
    pub notes: String,
    pub logged_at: DateTime<Utc>,
}

pub struct LogGrowthUseCase<R> {
    repository: R,
}

impl<R: GrowthRepository> LogGrowthUseCase<R> {
    pub fn new(repository: R) -> Self {
        Self { repository }
    }

    pub fn execute(&self, cmd: LogGrowthCommand) -> Result<GrowthLog, GrowthError> {
        if cmd.weight_grams.is_none() && cmd.height_mm.is_none() {
            return Err(GrowthError::NoMeasurementProvided);
        }

        let id = Uuid::new_v4();
        let log = self
            .repository
            .create(NewGrowthLog {
                id,
                weight_grams: cmd.weight_grams,
                height_mm: cmd.height_mm,
                notes: cmd.notes,
                logged_at: cmd.logged_at,
            })
            .map_err(GrowthError::from)?;

        info!(growth_id = %log.id, "growth log created");
        Ok(log)
    }
}
