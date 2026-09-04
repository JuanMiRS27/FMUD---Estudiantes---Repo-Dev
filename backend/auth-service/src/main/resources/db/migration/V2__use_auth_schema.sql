CREATE SCHEMA IF NOT EXISTS auth;

DO $$
BEGIN
    IF to_regclass('public.users') IS NOT NULL AND to_regclass('auth.users') IS NULL THEN
        ALTER TABLE public.users SET SCHEMA auth;
    END IF;
END $$;

ALTER TABLE auth.users
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN enabled SET DEFAULT TRUE;

ALTER TABLE auth.users DROP CONSTRAINT IF EXISTS ck_users_role;
ALTER TABLE auth.users
    ADD CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'SECRETARIO'));

DROP INDEX IF EXISTS public.idx_users_role;
DROP INDEX IF EXISTS public.idx_users_enabled;
CREATE INDEX IF NOT EXISTS idx_users_role ON auth.users(role);
CREATE INDEX IF NOT EXISTS idx_users_enabled ON auth.users(enabled);
