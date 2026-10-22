-- Brings an existing database created from the original INTEGER/TEXT schema
-- into alignment with the current JPA entities. New databases should already
-- receive the correct types from V1__initial_schema.sql.
--
-- Flyway versioned migrations: https://documentation.red-gate.com/flyway/reference/migrations
-- PostgreSQL ALTER TABLE: https://www.postgresql.org/docs/current/sql-altertable.html
--
-- Codex was used to help generate this corrective migration from the observed
-- Hibernate validation failure and the current entity type definitions.
-- The assistance focused on preserving existing rows while converting keys,
-- the admin flag, dates, and foreign-key relationships.

-- Drop foreign keys before changing the referenced and referencing key types.
-- The loop handles PostgreSQL's generated constraint names as well as names
-- created by an earlier version of the schema.
DO $$
DECLARE
    foreign_key RECORD;
BEGIN
    FOR foreign_key IN
        SELECT conrelid::regclass AS table_name, conname
        FROM pg_constraint
        WHERE contype = 'f'
          AND conrelid IN ('registry_items'::regclass, 'recalled_foods'::regclass)
    LOOP
        EXECUTE format(
            'ALTER TABLE %s DROP CONSTRAINT %I',
            foreign_key.table_name,
            foreign_key.conname
        );
    END LOOP;
END $$;

-- Remove the old integer-only admin check before converting the column.
DO $$
DECLARE
    check_constraint RECORD;
BEGIN
    FOR check_constraint IN
        SELECT conname
        FROM pg_constraint
        WHERE contype = 'c'
          AND conrelid = 'users'::regclass
          AND pg_get_constraintdef(oid) ILIKE '%is_admin%'
    LOOP
        EXECUTE format(
            'ALTER TABLE users DROP CONSTRAINT %I',
            check_constraint.conname
        );
    END LOOP;
END $$;

-- Convert the integer identifiers to BIGINT, matching Java Long fields.
ALTER TABLE users
    ALTER COLUMN id TYPE BIGINT USING id::BIGINT;

ALTER TABLE registry_items
    ALTER COLUMN id TYPE BIGINT USING id::BIGINT,
    ALTER COLUMN user_id TYPE BIGINT USING user_id::BIGINT;

ALTER TABLE recalled_foods
    ALTER COLUMN id TYPE BIGINT USING id::BIGINT,
    ALTER COLUMN registry_item_id TYPE BIGINT USING registry_item_id::BIGINT;

-- Convert the legacy 0/1 admin flag to the boolean used by the User entity.
ALTER TABLE users
    ALTER COLUMN is_admin TYPE BOOLEAN USING (is_admin <> 0);

ALTER TABLE users
    ALTER COLUMN is_admin SET DEFAULT FALSE;

-- The application maps these fields to LocalDate. This expects the existing
-- text values to use PostgreSQL's ISO date format: YYYY-MM-DD.
ALTER TABLE recalled_foods
    ALTER COLUMN recall_date TYPE DATE USING recall_date::DATE,
    ALTER COLUMN manu_date TYPE DATE USING manu_date::DATE;

-- Restore the relationships with the delete behavior defined by the schema.
ALTER TABLE registry_items
    ADD CONSTRAINT registry_items_user_id_fkey
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE;

ALTER TABLE recalled_foods
    ADD CONSTRAINT recalled_foods_registry_item_id_fkey
    FOREIGN KEY (registry_item_id)
    REFERENCES registry_items(id)
    ON DELETE SET NULL;

-- Restore the boolean invariant after the type conversion.
ALTER TABLE users
    ADD CONSTRAINT users_is_admin_check
    CHECK (is_admin IN (FALSE, TRUE));
