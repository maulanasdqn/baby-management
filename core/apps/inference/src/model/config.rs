use serde::Deserialize;
#[derive(Debug, Clone, Deserialize)]
pub struct GemmaConfig {
    pub vocab_size: usize,
    pub hidden_size: usize,
    pub intermediate_size: usize,
    pub num_hidden_layers: usize,
    pub num_attention_heads: usize,
    pub num_key_value_heads: usize,
    pub head_dim: usize,
    pub max_position_embeddings: usize,
    pub rms_norm_eps: f64,
    pub rope_theta: f64,
}
impl Default for GemmaConfig {
    fn default() -> Self {
        Self {
            vocab_size: 256000,
            hidden_size: 1152,
            intermediate_size: 6912,
            num_hidden_layers: 18,
            num_attention_heads: 4,
            num_key_value_heads: 1,
            head_dim: 256,
            max_position_embeddings: 8192,
            rms_norm_eps: 1e-6,
            rope_theta: 10000.0,
        }
    }
}
impl GemmaConfig {
    pub fn from_file(path: &str) -> Result<Self, String> {
        let json = std::fs::read_to_string(path)
            .map_err(|e| format!("read config: {e}"))?;
        serde_json::from_str(&json).map_err(|e| format!("parse config: {e}"))
    }
}
