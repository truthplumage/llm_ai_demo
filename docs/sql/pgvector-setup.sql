-- Enable pgvector extension (run once per database)
CREATE EXTENSION IF NOT EXISTS vector;

-- Add embedding column to existing product table (1536 dims for OpenAI 4o/3.5 embeddings)
ALTER TABLE public."product"
  ADD COLUMN IF NOT EXISTS embedding vector(1536);

-- IVFFlat index for efficient similarity search (tune lists as needed)
CREATE INDEX IF NOT EXISTS product_embedding_idx
  ON public."product" USING ivfflat (embedding vector_l2_ops)
  WITH (lists = 100);

-- Optional: constraint to ensure embedding is set (uncomment if you want to enforce)
-- ALTER TABLE public."product" ADD CONSTRAINT product_embedding_not_null CHECK (embedding IS NOT NULL);
