CREATE TABLE users (
                       id UUID NOT NULL,
                       created_at TIMESTAMP(6) WITHOUT TIME ZONE,
                       email VARCHAR(255) NOT NULL,
                       enabled BOOLEAN NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL,
                       updated_at TIMESTAMP(6) WITHOUT TIME ZONE,
                       CONSTRAINT users_pkey PRIMARY KEY (id),
                       CONSTRAINT uk_users_email UNIQUE (email),
                       CONSTRAINT users_role_check
                           CHECK (role IN ('USER', 'ADMIN', 'PROVIDER'))
);

CREATE TABLE refresh_tokens (
                                id UUID NOT NULL,
                                expiry_date TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
                                revoked BOOLEAN NOT NULL,
                                token VARCHAR(500) NOT NULL,
                                user_id UUID NOT NULL,
                                CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id),
                                CONSTRAINT uk_refresh_tokens_token UNIQUE (token)
);