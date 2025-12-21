CREATE EXTENSION IF NOT EXISTS vector;

-- Create product table if missing (matches provided DDL, with defaults for not-null columns)
CREATE TABLE IF NOT EXISTS public."product" (
  id UUID PRIMARY KEY,
  seller_id UUID NOT NULL,
  "name" VARCHAR(100) NOT NULL,
  description TEXT,
  price NUMERIC(15,2) NOT NULL,
  stock INT DEFAULT 0 NOT NULL,
  status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
  reg_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
  reg_dt TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  modify_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
  modify_dt TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  embedding vector(1536)
);

-- Similarity search index for embeddings
CREATE INDEX IF NOT EXISTS product_embedding_idx
  ON public."product" USING ivfflat (embedding vector_l2_ops)
  WITH (lists = 100);
