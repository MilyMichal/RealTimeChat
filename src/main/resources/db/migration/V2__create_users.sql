CREATE TABLE IF NOT EXISTS users (
    id  BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(20) NOT NULL UNIQUE,
    email    VARCHAR(255) NOT NULL UNIQUE,
    roles VARCHAR(255),
    pictureURL VARCHAR(255),
    is_non_banned BOOLEAN NOT NULL,
    ban_expiration TIMESTAMP
    );