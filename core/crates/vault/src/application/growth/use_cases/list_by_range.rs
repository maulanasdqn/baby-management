use crate::application::growth::error::GrowthError;
use crate::domain::growth::entity::GrowthLog;
use crate::domain::growth::repository::GrowthRepository;
use chrono::{DateTime, Utc};

pub struct ListGrowthByRangeCommand {
    pub from: DateTime<Utc>,
    pub to: DateTime<Utc>,
}

pub struct ListGrowthByRangeUseCase<R> {
    repository: R,
}

impl<R: GrowthRepository> ListGrowthByRangeUseCase<R> {
    pub fn new(repository: R) -> Self {
        Self { repository }
    }

    pub fn execute(&self, cmd: ListGrowthByRangeCommand) -> Result<Vec<GrowthLog>, GrowthError> {
        self.repository
            .list_by_range(cmd.from, cmd.to)
            .map_err(GrowthError::from)
    }
}
