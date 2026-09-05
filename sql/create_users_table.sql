CREATE TABLE IF NOT EXISTS public.users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT chk_users_role
    CHECK (role IN ('USER', 'ADMIN'))
)