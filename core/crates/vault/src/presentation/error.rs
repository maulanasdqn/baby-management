use crate::application::growth::error::GrowthError;
use crate::application::media::error::MediaError;
use crate::application::milestone::error::MilestoneError;
use crate::application::vault::error::VaultError;
use std::fmt;

#[derive(Debug, uniffi::Error)]
pub enum FfiError {
    NotFound,
    Validation { message: String },
    Crypto { message: String },
    Internal { message: String },
}

impl fmt::Display for FfiError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            Self::NotFound => write!(f, "not found"),
            Self::Validation { message } => write!(f, "validation error: {message}"),
            Self::Crypto { message } => write!(f, "crypto error: {message}"),
            Self::Internal { message } => write!(f, "internal error: {message}"),
        }
    }
}

impl std::error::Error for FfiError {}

impl From<MilestoneError> for FfiError {
    fn from(e: MilestoneError) -> Self {
        match e {
            MilestoneError::NotFound => Self::NotFound,
            MilestoneError::InvalidTitle | MilestoneError::InvalidOccurredAt => {
                Self::Validation { message: e.to_string() }
            }
            MilestoneError::Internal(m) => Self::Internal { message: m },
        }
    }
}

impl From<GrowthError> for FfiError {
    fn from(e: GrowthError) -> Self {
        match e {
            GrowthError::NotFound => Self::NotFound,
            GrowthError::NoMeasurementProvided => Self::Validation { message: e.to_string() },
            GrowthError::Internal(m) => Self::Internal { message: m },
        }
    }
}

impl From<MediaError> for FfiError {
    fn from(e: MediaError) -> Self {
        match e {
            MediaError::NotFound => Self::NotFound,
            MediaError::TitleEmpty => Self::Validation { message: e.to_string() },
            MediaError::EncryptionFailed(m) | MediaError::DecryptionFailed(m) => {
                Self::Crypto { message: m }
            }
            MediaError::Internal(m) => Self::Internal { message: m },
        }
    }
}

impl From<VaultError> for FfiError {
    fn from(e: VaultError) -> Self {
        match e {
            VaultError::NotInitialized | VaultError::AlreadyInitialized => {
                Self::Validation { message: e.to_string() }
            }
            VaultError::KeyDerivationFailed(m) | VaultError::CryptoFailed(m) => {
                Self::Crypto { message: m }
            }
            VaultError::Internal(m) => Self::Internal { message: m },
        }
    }
}
