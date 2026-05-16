use crate::application::milestone::error::MilestoneError;
use crate::domain::milestone::entity::Milestone;
use crate::domain::milestone::repository::MilestoneRepository;

pub struct ListMilestonesCommand {
    pub limit: u32,
    pub offset: u32,
}

pub struct ListMilestonesUseCase<R> {
    repository: R,
}

impl<R: MilestoneRepository> ListMilestonesUseCase<R> {
    pub fn new(repository: R) -> Self {
        Self { repository }
    }

    pub fn execute(&self, cmd: ListMilestonesCommand) -> Result<Vec<Milestone>, MilestoneError> {
        self.repository
            .list(cmd.limit, cmd.offset)
            .map_err(MilestoneError::from)
    }
}
