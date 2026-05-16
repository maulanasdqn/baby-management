// UniFFI-compatible data transfer objects. Uuid → String, DateTime<Utc> → i64 millis.

#[derive(uniffi::Record)]
pub struct MilestoneDto {
    pub id: String,
    pub title: String,
    pub description: String,
    pub occurred_at_millis: i64,
    pub created_at_millis: i64,
}

#[derive(uniffi::Record)]
pub struct GrowthLogDto {
    pub id: String,
    pub weight_grams: Option<u32>,
    pub height_mm: Option<u32>,
    pub notes: String,
    pub logged_at_millis: i64,
}

#[derive(uniffi::Record)]
pub struct MediaItemDto {
    pub id: String,
    pub title: String,
    pub encrypted_path: String,
    pub size_bytes: u64,
    pub created_at_millis: i64,
}

#[derive(uniffi::Record)]
pub struct SyncStatusDto {
    pub pending_milestones: u32,
    pub pending_growth_logs: u32,
    pub pending_media_items: u32,
    pub last_synced_at_millis: Option<i64>,
    pub is_configured: bool,
}
