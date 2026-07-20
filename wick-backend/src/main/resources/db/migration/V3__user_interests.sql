CREATE TABLE user_interests (
    user_id uuid NOT NULL,
    interest character varying(50) NOT NULL,
    CONSTRAINT user_interests_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX user_interests_user_id_idx ON user_interests (user_id);
