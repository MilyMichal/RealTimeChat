CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(500),
    sender VARCHAR(255),
    date TIMESTAMPTZ,
    type VARCHAR(255),
    send_to VARCHAR(255)
    );