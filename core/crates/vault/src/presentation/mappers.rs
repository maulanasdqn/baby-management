use crate::domain::growth::entity::GrowthLog;
use crate::domain::media::entity::MediaItem;
use crate::domain::milestone::entity::Milestone;
use crate::presentation::dto::{GrowthLogDto, MediaItemDto, MilestoneDto};

pub fn milestone_to_dto(m: Milestone) -> MilestoneDto {
    MilestoneDto {
        id: m.id.to_string(),
        title: m.title,
        description: m.description,
        occurred_at_millis: m.occurred_at.timestamp_millis(),
        created_at_millis: m.created_at.timestamp_millis(),
    }
}

pub fn growth_log_to_dto(g: GrowthLog) -> GrowthLogDto {
    GrowthLogDto {
        id: g.id.to_string(),
        weight_grams: g.weight_grams,
        height_mm: g.height_mm,
        notes: g.notes,
        logged_at_millis: g.logged_at.timestamp_millis(),
    }
}

pub fn media_item_to_dto(m: MediaItem) -> MediaItemDto {
    MediaItemDto {
        id: m.id.to_string(),
        title: m.title,
        encrypted_path: m.encrypted_path,
        size_bytes: m.size_bytes,
        created_at_millis: m.created_at.timestamp_millis(),
    }
}
