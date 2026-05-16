use crate::application::milestone::error::MilestoneError;
use crate::domain::milestone::entity::Milestone;
use crate::domain::milestone::repository::MilestoneRepository;
use uuid::Uuid;

pub struct DetailMilestoneUseCase<R> {
    repository: R,
}

impl<R: MilestoneRepository> DetailMilestoneUseCase<R> {
    pub fn new(repository: R) -> Self {
        Self { repository }
    }

    pub fn execute(&self, id: Uuid) -> Result<Milestone, MilestoneError> {
        self.repository
            .find_by_id(id)?
            .ok_or(MilestoneError::NotFound)
    }
}
