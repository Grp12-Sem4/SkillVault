-- SQL Schema for Time Capsule Feature

CREATE TABLE time_capsule_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    credit_balance INT NOT NULL,
    total_trades INT NOT NULL,
    average_rating DECIMAL(3,2) NOT NULL
);

CREATE TABLE time_capsule_skill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    skill_name VARCHAR(255) NOT NULL,
    skill_score INT NOT NULL,
    confidence_index INT NOT NULL,
    practice_hours INT NOT NULL,
    FOREIGN KEY (snapshot_id) REFERENCES time_capsule_snapshot(id) ON DELETE CASCADE
);

CREATE TABLE time_capsule_knowledge (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    mastery_level INT NOT NULL,
    decay_status VARCHAR(50) NOT NULL,
    revision_status VARCHAR(50) NOT NULL,
    FOREIGN KEY (snapshot_id) REFERENCES time_capsule_snapshot(id) ON DELETE CASCADE
);