uniffi::include_scaffolding!("inference");
pub mod engine;
pub mod model;
pub mod tokenizer;
use engine::Engine;
use std::sync::{Arc, Mutex, atomic::{AtomicBool, Ordering}};
pub trait StreamCallback: Send + Sync {
    fn on_token(&self, token: String);
    fn on_done(&self);
    fn on_error(&self, message: String);
}
#[derive(Debug)]
pub enum InferenceError {
    ModelNotFound,
    TokenizerError,
    InferenceError,
    InvalidInput,
}
impl std::fmt::Display for InferenceError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            Self::ModelNotFound => write!(f, "Model not found at path"),
            Self::TokenizerError => write!(f, "Tokenizer error"),
            Self::InferenceError => write!(f, "Inference failed"),
            Self::InvalidInput => write!(f, "Invalid input"),
        }
    }
}
impl std::error::Error for InferenceError {}
pub struct InferenceEngine {
    inner: Mutex<Option<Engine>>,
    ready: AtomicBool,
    info: String,
}
impl InferenceEngine {
    pub fn new(model_dir: String) -> Result<Arc<Self>, InferenceError> {
        let eng = Engine::load(&model_dir).map_err(|_| InferenceError::InferenceError)?;
        Ok(Arc::new(Self {
            inner: Mutex::new(Some(eng)),
            ready: AtomicBool::new(true),
            info: format!("Gemma 3 1B — NdArray CPU — {}", model_dir),
        }))
    }
    pub fn generate(
        &self,
        prompt: String,
        callback: Arc<dyn StreamCallback>,
        max_tokens: u32,
    ) -> Result<(), InferenceError> {
        let guard = self.inner.lock().map_err(|_| InferenceError::InferenceError)?;
        let engine = guard.as_ref().ok_or(InferenceError::ModelNotFound)?;
        engine
            .generate(&prompt, max_tokens, |tok| callback.on_token(tok), || callback.on_done())
            .map_err(|_| InferenceError::InferenceError)
    }
    pub fn is_ready(&self) -> bool {
        self.ready.load(Ordering::Relaxed)
    }
    pub fn model_info(&self) -> String {
        self.info.clone()
    }
}
