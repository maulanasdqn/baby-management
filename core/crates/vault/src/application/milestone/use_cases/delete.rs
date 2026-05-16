use crate::application::milestone::error::MilestoneError;
use crate::domain::milestone::repository::MilestoneRepository;
use uuid::Uuid;

pub struct DeleteMilestoneUseCase<R> {
    repository: R,
}

impl<R: MilestoneRepository> DeleteMilestoneUseCase<R> {
    pub fn new(repository: R) -> Self {
        Self { repository }
    }

    pub fn execute(&self, id: Uuid) -> Result<(), MilestoneError> {
        self.repository.find_by_id(id)?.ok_or(MilestoneError::NotFound)?;
        self.repository.delete(id).map_err(MilestoneError::from)
    }
}
