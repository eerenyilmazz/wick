CREATE TABLE discovery_preferences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    min_age INTEGER NOT NULL,
    max_age INTEGER NOT NULL,
    province VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE discovery_preference_genders (
    preference_id UUID NOT NULL REFERENCES discovery_preferences(id) ON DELETE CASCADE,
    gender VARCHAR(20) NOT NULL
);
