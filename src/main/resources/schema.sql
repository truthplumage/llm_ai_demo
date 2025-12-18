-- Enable pgvector (requires superuser privileges on some setups)
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding column to existing product table
ALTER TABLE IF EXISTS public."product"
  ADD COLUMN IF NOT EXISTS embedding vector(1536);

-- Similarity search index for embeddings
CREATE INDEX IF NOT EXISTS product_embedding_idx
  ON public."product" USING ivfflat (embedding vector_l2_ops)
  WITH (lists = 100);
