use tokenizers::Tokenizer;
pub struct GemmaTokenizer {
    inner: Tokenizer,
}
impl GemmaTokenizer {
    pub fn from_file(path: &str) -> Result<Self, String> {
        let inner = Tokenizer::from_file(path)
            .map_err(|e| format!("tokenizer load error: {e}"))?;
        Ok(Self { inner })
    }
    pub fn encode(&self, text: &str) -> Result<Vec<i64>, String> {
        let enc = self.inner.encode(text, true)
            .map_err(|e| format!("encode error: {e}"))?;
        Ok(enc.get_ids().iter().map(|&id| id as i64).collect())
    }
    pub fn decode(&self, ids: &[u32]) -> Result<String, String> {
        self.inner.decode(ids, true)
            .map_err(|e| format!("decode error: {e}"))
    }
    pub fn token_to_id(&self, token: &str) -> Option<u32> {
        self.inner.token_to_id(token)
    }
}
