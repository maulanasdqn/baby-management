use burn::prelude::*;
use burn::nn::Linear;
use burn::tensor::activation::gelu;
use super::config::GemmaConfig;

#[derive(Module, Debug)]
pub struct GemmaMlp<B: Backend> {
    gate_proj: Linear<B>,
    up_proj: Linear<B>,
    down_proj: Linear<B>,
}

impl<B: Backend> GemmaMlp<B> {
    pub fn new(cfg: &GemmaConfig, device: &B::Device) -> Self {
        let h = cfg.hidden_size;
        let i = cfg.intermediate_size;
        let no_bias = |d_in, d_out| burn::nn::LinearConfig::new(d_in, d_out).with_bias(false);
        Self {
            gate_proj: Linear::new(&no_bias(h, i), device),
            up_proj: Linear::new(&no_bias(h, i), device),
            down_proj: Linear::new(&no_bias(i, h), device),
        }
    }

    pub fn forward(&self, x: Tensor<B, 3>) -> Tensor<B, 3> {
        let gate = gelu(self.gate_proj.forward(x.clone()));
        let up = self.up_proj.forward(x);
        self.down_proj.forward(gate.mul(up))
    }
}
