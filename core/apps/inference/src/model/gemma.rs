use burn::prelude::*;
use burn::nn::{Embedding, EmbeddingConfig};
use super::{config::GemmaConfig, layer::GemmaLayer, rms_norm::RmsNorm};

#[derive(Module, Debug)]
pub struct GemmaModel<B: Backend> {
    embed: Embedding<B>,
    layers: Vec<GemmaLayer<B>>,
    norm: RmsNorm<B>,
    hidden_size: usize,
}

impl<B: Backend> GemmaModel<B> {
    pub fn new(cfg: &GemmaConfig, device: &B::Device) -> Self {
        let embed = Embedding::new(
            &EmbeddingConfig::new(cfg.vocab_size, cfg.hidden_size),
            device,
        );
        let layers = (0..cfg.num_hidden_layers)
            .map(|_| GemmaLayer::new(cfg, device))
            .collect();
        let norm = RmsNorm::new(cfg.hidden_size, cfg.rms_norm_eps, device);
        Self { embed, layers, norm, hidden_size: cfg.hidden_size }
    }

    /// Returns logits [batch, seq, vocab_size].
    pub fn forward(&self, token_ids: Tensor<B, 2, Int>, offset: usize) -> Tensor<B, 3> {
        let scale = (self.hidden_size as f32).sqrt();
        let mut x = self.embed.forward(token_ids).mul_scalar(scale);
        for layer in &self.layers {
            x = layer.forward(x, offset);
        }
        let x = self.norm.forward(x);
        // Tie weights: logits = x @ embed.weight^T
        let w = self.embed.weight.val();
        let [v, d] = w.dims();
        let [b, s, _] = x.dims();
        x.reshape([b * s, d]).matmul(w.transpose()).reshape([b, s, v])
    }
}
