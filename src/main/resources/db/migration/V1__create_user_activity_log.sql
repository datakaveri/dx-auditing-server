-- Create custom enum type for http_method
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'http_method') THEN
    CREATE TYPE http_method AS ENUM ('GET', 'POST', 'PUT', 'DELETE', 'PATCH');
  END IF;
END$$;

-- Create custom enum type for user_role
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
    CREATE TYPE user_role AS ENUM ('consumer', 'provider', 'cos_admin', 'org_admin', 'compute');
  END IF;
END$$;

-- Create custom enum type for origin_system
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'origin_system') THEN
    CREATE TYPE origin_system AS ENUM ('Catalogue', 'AAA', 'File', 'ACL');
  END IF;
END$$;

-- Create the user_activity_log table
CREATE TABLE IF NOT EXISTS user_activity_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    asset_name VARCHAR NOT NULL,
    asset_type VARCHAR(60) NOT NULL,
    operation VARCHAR(60) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    asset_id UUID NOT NULL,
    api VARCHAR NOT NULL,
    method http_method NOT NULL,
    size BIGINT NOT NULL DEFAULT 0,
    role user_role NOT NULL,
    user_id UUID NOT NULL,
    origin_server origin_system NOT NULL,
    myactivity_enabled BOOLEAN NOT NULL DEFAULT FALSE
);

-- Grant access
GRANT SELECT, INSERT ON TABLE user_activity_log TO ${auditUser};
