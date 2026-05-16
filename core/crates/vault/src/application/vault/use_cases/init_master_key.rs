use crate::application::vault::error::VaultError;
use rand::RngCore;

pub struct InitMasterKeyUseCase;

impl InitMasterKeyUseCase {
    pub fn execute() -> Result<Vec<u8>, VaultError> {
        let mut key = vec![0u8; 32];
        rand::thread_rng().fill_bytes(&mut key);
        Ok(key)
    }
}
