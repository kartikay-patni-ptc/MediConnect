-- Fix appointment table schema to support longer AI summaries
ALTER TABLE appointment ALTER COLUMN ai_summary TYPE TEXT;
ALTER TABLE appointment ALTER COLUMN notes TYPE TEXT;

-- Verify the changes
\d appointment;
