use burn::prelude::*;
use burn::nn::Linear;
use super::config::GemmaConfig;

#[derive(Module, Debug)]
pub struct GemmaAttention<B: Backend> {
    q_proj: Linear<B>,
    k_proj: Linear<B>,
    v_proj: Linear<B>,
    o_proj: Linear<B>,
    num_heads: usize,
    num_kv_heads: usize,
    head_dim: usize,
    rope_theta: f64,
}

impl<B: Backend> GemmaAttention<B> {
    pub fn new(cfg: &GemmaConfig, device: &B::Device) -> Self {
        let h = cfg.hidden_size;
        let hd = cfg.head_dim;
        let nq = cfg.num_attention_heads;
        let nk = cfg.num_key_value_heads;
        Self {
            q_proj: Linear::new(&burn::nn::LinearConfig::new(h, nq * hd).with_bias(false), device),
            k_proj: Linear::new(&burn::nn::LinearConfig::new(h, nk * hd).with_bias(false), device),
            v_proj: Linear::new(&burn::nn::LinearConfig::new(h, nk * hd).with_bias(false), device),
            o_proj: Linear::new(&burn::nn::LinearConfig::new(nq * hd, h).with_bias(false), device),
            num_heads: nq,
            num_kv_heads: nk,
            head_dim: hd,
            rope_theta: cfg.rope_theta,
        }
    }

    pub fn forward(&self, x: Tensor<B, 3>, offset: usize) -> Tensor<B, 3> {
        let [b, s, _] = x.dims();
        let q = self.split_heads(self.q_proj.forward(x.clone()), self.num_heads);
        let k = self.split_heads(self.k_proj.forward(x.clone()), self.num_kv_heads);
        let v = self.split_heads(self.v_proj.forward(x), self.num_kv_heads);

        let q = apply_rope(q, offset, self.rope_theta);
        let k = apply_rope(k, offset, self.rope_theta);

        // Repeat k/v for GQA
        let groups = self.num_heads / self.num_kv_heads;
        let k = k.repeat_dim(1, groups);
        let v = v.repeat_dim(1, groups);

        let scale = (self.head_dim as f32).sqrt().recip();
        let scores = q.matmul(k.swap_dims(2, 3)).mul_scalar(scale);
        let scores = causal_mask(scores, s);
        let attn = burn::tensor::activation::softmax(scores, 3);
        let out = attn.matmul(v);

        let out = out.swap_dims(1, 2).reshape([b, s, self.num_heads * self.head_dim]);
        self.o_proj.forward(out)
    }

    fn split_heads(&self, x: Tensor<B, 3>, n_heads: usize) -> Tensor<B, 4> {
        let [b, s, _] = x.dims();
        x.reshape([b, s, n_heads, self.head_dim]).swap_dims(1, 2)
    }
}

fn apply_rope<B: Backend>(x: Tensor<B, 4>, offset: usize, theta: f64) -> Tensor<B, 4> {
    let [b, h, s, d] = x.dims();
    let half = d / 2;
    let device = x.device();

    let positions: Vec<f32> = (0..s).map(|i| (i + offset) as f32).collect();
    let freqs: Vec<f32> = (0..half)
        .map(|i| 1.0 / (theta as f32).powf(2.0 * i as f32 / d as f32))
        .collect();

    let pos = Tensor::<B, 1>::from_floats(positions.as_slice(), &device).reshape([s, 1]);
    let frq = Tensor::<B, 1>::from_floats(freqs.as_slice(), &device).reshape([1, half]);
    let angles = pos.matmul(frq).reshape([1, 1, s, half]).expand([b, h, s, half]);

    let cos = angles.clone().cos();
    let sin = angles.sin();

    let x1 = x.clone().slice([0..b, 0..h, 0..s, 0..half]);
    let x2 = x.slice([0..b, 0..h, 0..s, half..d]);
    let rot_x1 = x1.clone().mul(cos.clone()).sub(x2.clone().mul(sin.clone()));
    let rot_x2 = x2.mul(cos).add(x1.mul(sin));
    Tensor::cat(vec![rot_x1, rot_x2], 3)
}

fn causal_mask<B: Backend>(scores: Tensor<B, 4>, seq: usize) -> Tensor<B, 4> {
    if seq == 1 {
        return scores;
    }
    let device = scores.device();
    let [b, h, s, _] = scores.dims();
    let mask_vals: Vec<f32> = (0..s)
        .flat_map(|i| (0..s).map(move |j| if j <= i { 0.0f32 } else { f32::NEG_INFINITY }))
        .collect();
    let mask = Tensor::<B, 2>::from_floats(mask_vals.as_slice(), &device)
        .reshape([1, 1, s, s])
        .expand([b, h, s, s]);
    scores.add(mask)
}
