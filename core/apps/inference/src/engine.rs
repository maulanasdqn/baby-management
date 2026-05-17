use burn_ndarray::{NdArray, NdArrayDevice};
use burn::prelude::*;
use safetensors::SafeTensors;
use std::collections::HashMap;
use std::path::Path;
use memmap2::Mmap;
use std::fs::File;
use crate::model::{config::GemmaConfig, gemma::GemmaModel};
use crate::tokenizer::GemmaTokenizer;
type B = NdArray<f32>;
pub struct Engine {
    model: GemmaModel<B>,
    tokenizer: GemmaTokenizer,
    config: GemmaConfig,
    device: NdArrayDevice,
    eos_id: u32,
}
impl Engine {
    pub fn load(model_dir: &str) -> Result<Self, String> {
        let dir = Path::new(model_dir);
        let config_path = dir.join("config.json");
        let tokenizer_path = dir.join("tokenizer.json");
        let weights_path = dir.join("model.safetensors");
        let config = GemmaConfig::from_file(config_path.to_str().unwrap())
            .map_err(|e| format!("config error: {e}"))?;
        let tokenizer = GemmaTokenizer::from_file(tokenizer_path.to_str().unwrap())?;
        let eos_id = tokenizer.token_to_id("<eos>").unwrap_or(1);
        let device = NdArrayDevice::Cpu;
        let model = Self::load_weights(&config, &weights_path.to_str().unwrap(), &device)?;
        Ok(Self { model, tokenizer, config, device, eos_id })
    }
    fn load_weights(cfg: &GemmaConfig, path: &str, device: &NdArrayDevice) -> Result<GemmaModel<B>, String> {
        let file = File::open(path).map_err(|e| format!("open weights: {e}"))?;
        let mmap = unsafe { Mmap::map(&file).map_err(|e| format!("mmap: {e}"))? };
        let st = SafeTensors::deserialize(&mmap).map_err(|e| format!("parse safetensors: {e}"))?;
        let model = GemmaModel::new(cfg, device);
        let _ = st.names();
        Ok(model)
    }
    pub fn generate(
        &self,
        prompt: &str,
        max_tokens: u32,
        on_token: impl Fn(String),
        on_done: impl Fn(),
    ) -> Result<(), String> {
        let mut ids: Vec<i64> = self.tokenizer.encode(prompt)?;
        let mut generated = 0u32;
        while generated < max_tokens {
            let seq_len = ids.len();
            let input = Tensor::<B, 2, Int>::from_data(
                burn::tensor::TensorData::new(ids.clone(), [1, seq_len]),
                &self.device,
            );
            let logits = self.model.forward(input, 0);
            let last = logits.slice([0..1, (seq_len - 1)..seq_len, 0..self.config.vocab_size]);
            let next_id = last.squeeze::<2>(0).squeeze::<1>(0).argmax(0);
            let next_id_val: i64 = next_id.into_scalar().elem();
            if next_id_val as u32 == self.eos_id {
                break;
            }
            ids.push(next_id_val);
            generated += 1;
            let decoded = self.tokenizer.decode(&[next_id_val as u32])?;
            on_token(decoded);
        }
        on_done();
        Ok(())
    }
}
