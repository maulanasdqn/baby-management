use burn::prelude::*;
use super::{attention::GemmaAttention, mlp::GemmaMlp, rms_norm::RmsNorm, config::GemmaConfig};

#[derive(Module, Debug)]
pub struct GemmaLayer<B: Backend> {
    self_attn: GemmaAttention<B>,
    mlp: GemmaMlp<B>,
    input_norm: RmsNorm<B>,
    post_attn_norm: RmsNorm<B>,
}

impl<B: Backend> GemmaLayer<B> {
    pub fn new(cfg: &GemmaConfig, device: &B::Device) -> Self {
        Self {
            self_attn: GemmaAttention::new(cfg, device),
            mlp: GemmaMlp::new(cfg, device),
            input_norm: RmsNorm::new(cfg.hidden_size, cfg.rms_norm_eps, device),
            post_attn_norm: RmsNorm::new(cfg.hidden_size, cfg.rms_norm_eps, device),
        }
    }

    pub fn forward(&self, x: Tensor<B, 3>, offset: usize) -> Tensor<B, 3> {
        let h = self.self_attn.forward(self.input_norm.forward(x.clone()), offset);
        let x = x.add(h);
        let m = self.mlp.forward(self.post_attn_norm.forward(x.clone()));
        x.add(m)
    }
}
