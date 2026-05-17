use std::sync::{Arc, Mutex};
use std::sync::mpsc;

use crate::engine::InferenceEngine;

#[derive(uniffi::Object)]
pub struct TokenStream {
    rx: Mutex<mpsc::Receiver<Option<String>>>,
}

#[uniffi::export]
impl TokenStream {
    pub fn next_token(&self) -> Option<String> {
        self.rx.lock().ok()?.recv().unwrap_or(None)
    }
}

#[derive(uniffi::Object)]
pub struct NativeInferenceEngine {
    engine: Arc<Mutex<Option<InferenceEngine>>>,
}

#[uniffi::export]
impl NativeInferenceEngine {
    #[uniffi::constructor]
    pub fn new(model_dir: String) -> Arc<Self> {
        let engine = InferenceEngine::load(&model_dir).ok();
        Arc::new(Self {
            engine: Arc::new(Mutex::new(engine)),
        })
    }

    pub fn is_ready(&self) -> bool {
        self.engine.lock().map(|g| g.is_some()).unwrap_or(false)
    }

    pub fn generate_stream(&self, prompt: String, max_tokens: u32) -> Arc<TokenStream> {
        let (tx, rx) = mpsc::channel::<Option<String>>();
        let engine = Arc::clone(&self.engine);

        std::thread::spawn(move || {
            let mut guard = match engine.lock() {
                Ok(g) => g,
                Err(_) => {
                    let _ = tx.send(None);
                    return;
                }
            };
            if let Some(eng) = guard.as_mut() {
                let _ = eng.generate(&prompt, max_tokens as usize, |token| {
                    let _ = tx.send(Some(token));
                });
            }
            let _ = tx.send(None);
        });

        Arc::new(TokenStream {
            rx: Mutex::new(rx),
        })
    }
}
