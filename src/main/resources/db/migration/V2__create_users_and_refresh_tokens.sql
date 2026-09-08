CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    business_id BIGINT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_business FOREIGN KEY (business_id) REFERENCES businesses (id),
    CONSTRAINT chk_users_email_normalized CHECK (
        CHAR_LENGTH(TRIM(email)) > 3 AND email = LOWER(TRIM(email))
    ),
    CONSTRAINT chk_users_password_hash_not_blank CHECK (CHAR_LENGTH(TRIM(password_hash)) > 0),
    CONSTRAINT chk_users_role CHECK (
        role IN ('PLATFORM_ADMIN', 'BUSINESS_OWNER', 'EMPLOYEE', 'CUSTOMER')
    ),
    CONSTRAINT chk_users_business_scope CHECK (
        (role = 'PLATFORM_ADMIN' AND business_id IS NULL)
        OR (role <> 'PLATFORM_ADMIN' AND business_id IS NOT NULL)
    )
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_users_business_id ON users (business_id);

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    family_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_refresh_tokens_hash_length CHECK (CHAR_LENGTH(token_hash) = 64)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family_active ON refresh_tokens (family_id, revoked_at);
