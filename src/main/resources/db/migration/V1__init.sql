-- ============================================================================
-- V1 — initial sample schema.
--
-- Migration rules:
--   1. A migration that has already been applied is NEVER edited. It is fixed
--      with a new one.
--   2. Name: V<n>__<description_in_snake_case>.sql
--   3. Every migration must be able to run against a database with data, not
--      only an empty one.
--   4. Destructive changes (DROP COLUMN) go in their own migration, deployed
--      after no live version uses the column any more.
--
-- INIT: delete this table and write your service's real schema.
-- ============================================================================

CREATE TABLE samples (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(500)
);

COMMENT ON TABLE samples IS 'Sample table — remove when initializing the repository';
