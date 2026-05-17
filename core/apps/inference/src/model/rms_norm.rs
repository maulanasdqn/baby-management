use burn::prelude::*;
#[derive(Module, Debug)]
pub struct RmsNorm<B: Backend> {
    weight: Param<Tensor<B, 1>>,
    eps: f64,
}
impl<B: Backend> RmsNorm<B> {
    pub fn new(size: usize, eps: f64, device: &B::Device) -> Self {
        let weight = Param::from_tensor(Tensor::ones([size], device));
        Self { weight, eps }
    }
    pub fn forward(&self, x: Tensor<B, 3>) -> Tensor<B, 3> {
        let [b, s, d] = x.dims();
        let variance = x.clone().powi_scalar(2).mean_dim(2).add_scalar(self.eps as f32);
        let x_norm = x.div(variance.sqrt().unsqueeze_dim(2).expand([b, s, d]));
        let w = self.weight.val().unsqueeze::<3>().expand([b, s, d]);
        x_norm.mul(w)
    }
}
