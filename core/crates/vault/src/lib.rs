uniffi::setup_scaffolding!();

pub mod application;
pub mod domain;
pub mod infrastructure;
pub mod presentation;

pub use presentation::dto::{GrowthLogDto, MediaItemDto, MilestoneDto, SyncStatusDto};
pub use presentation::engine::VaultEngine;
pub use presentation::error::FfiError;
