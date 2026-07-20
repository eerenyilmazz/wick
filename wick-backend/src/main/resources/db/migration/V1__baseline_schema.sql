CREATE TABLE users (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    email character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    display_name character varying(255) NOT NULL,
    date_of_birth date,
    gender character varying(255),
    bio character varying(1000),
    fcm_token character varying(255),
    premium boolean NOT NULL,
    verified boolean NOT NULL,
    deleted_at timestamp(6) with time zone,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_email_key UNIQUE (email),
    CONSTRAINT users_gender_check CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'))
);

CREATE TABLE likes (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    liker_id uuid NOT NULL,
    liked_id uuid NOT NULL,
    CONSTRAINT likes_pkey PRIMARY KEY (id),
    CONSTRAINT likes_liker_liked_key UNIQUE (liker_id, liked_id),
    CONSTRAINT likes_liker_fk FOREIGN KEY (liker_id) REFERENCES users(id),
    CONSTRAINT likes_liked_fk FOREIGN KEY (liked_id) REFERENCES users(id)
);

CREATE TABLE matches (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_one_id uuid NOT NULL,
    user_two_id uuid NOT NULL,
    CONSTRAINT matches_pkey PRIMARY KEY (id),
    CONSTRAINT matches_user_one_two_key UNIQUE (user_one_id, user_two_id),
    CONSTRAINT matches_user_one_fk FOREIGN KEY (user_one_id) REFERENCES users(id),
    CONSTRAINT matches_user_two_fk FOREIGN KEY (user_two_id) REFERENCES users(id)
);

CREATE TABLE invitations (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    match_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    status character varying(255),
    responded_at timestamp(6) with time zone,
    CONSTRAINT invitations_pkey PRIMARY KEY (id),
    CONSTRAINT invitations_match_fk FOREIGN KEY (match_id) REFERENCES matches(id),
    CONSTRAINT invitations_sender_fk FOREIGN KEY (sender_id) REFERENCES users(id),
    CONSTRAINT invitations_status_check CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED'))
);

CREATE TABLE chat_sessions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    match_id uuid NOT NULL,
    status character varying(255),
    started_at timestamp(6) with time zone,
    ends_at timestamp(6) with time zone,
    CONSTRAINT chat_sessions_pkey PRIMARY KEY (id),
    CONSTRAINT chat_sessions_match_fk FOREIGN KEY (match_id) REFERENCES matches(id),
    CONSTRAINT chat_sessions_status_check CHECK (status IN ('ACTIVE', 'LOCKED'))
);

CREATE TABLE messages (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    session_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    content character varying(2000) NOT NULL,
    CONSTRAINT messages_pkey PRIMARY KEY (id),
    CONSTRAINT messages_session_fk FOREIGN KEY (session_id) REFERENCES chat_sessions(id),
    CONSTRAINT messages_sender_fk FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE token_balances (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL,
    balance integer NOT NULL,
    CONSTRAINT token_balances_pkey PRIMARY KEY (id),
    CONSTRAINT token_balances_user_key UNIQUE (user_id),
    CONSTRAINT token_balances_user_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE token_transactions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL,
    amount integer NOT NULL,
    reason character varying(255),
    CONSTRAINT token_transactions_pkey PRIMARY KEY (id),
    CONSTRAINT token_transactions_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT token_transactions_reason_check CHECK (reason IN ('DAILY_LOGIN', 'VERIFICATION_BONUS', 'PREMIUM_GRANT', 'SESSION_EXTENSION', 'EXTRA_INVITATION', 'PURCHASED'))
);

CREATE TABLE purchases (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL,
    product_id character varying(255) NOT NULL,
    purchase_token character varying(255) NOT NULL,
    CONSTRAINT purchases_pkey PRIMARY KEY (id),
    CONSTRAINT purchases_token_key UNIQUE (purchase_token),
    CONSTRAINT purchases_user_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE event_logs (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    event_type character varying(255) NOT NULL,
    user_id uuid,
    detail text,
    CONSTRAINT event_logs_pkey PRIMARY KEY (id),
    CONSTRAINT event_logs_event_type_check CHECK (event_type IN ('USER_REGISTERED', 'USER_LOGIN', 'MATCH_CREATED', 'SESSION_STARTED', 'SESSION_LOCKED', 'SESSION_EXTENDED', 'PURCHASE_COMPLETED', 'PREMIUM_GRANTED', 'PREMIUM_REVOKED', 'NOTIFICATION_SENT', 'ACCOUNT_DELETED'))
);

CREATE TABLE user_photos (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL,
    storage_key character varying(255) NOT NULL,
    display_order integer NOT NULL,
    CONSTRAINT user_photos_pkey PRIMARY KEY (id),
    CONSTRAINT user_photos_user_fk FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE blocks (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    blocker_id uuid NOT NULL,
    blocked_id uuid NOT NULL,
    CONSTRAINT blocks_pkey PRIMARY KEY (id),
    CONSTRAINT blocks_blocker_blocked_key UNIQUE (blocker_id, blocked_id),
    CONSTRAINT blocks_blocker_fk FOREIGN KEY (blocker_id) REFERENCES users(id),
    CONSTRAINT blocks_blocked_fk FOREIGN KEY (blocked_id) REFERENCES users(id)
);

CREATE TABLE reports (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    reporter_id uuid NOT NULL,
    reported_id uuid NOT NULL,
    reason character varying(255) NOT NULL,
    details character varying(1000),
    CONSTRAINT reports_pkey PRIMARY KEY (id),
    CONSTRAINT reports_reporter_fk FOREIGN KEY (reporter_id) REFERENCES users(id),
    CONSTRAINT reports_reported_fk FOREIGN KEY (reported_id) REFERENCES users(id),
    CONSTRAINT reports_reason_check CHECK (reason IN ('INAPPROPRIATE_CONTENT', 'FAKE_PROFILE', 'HARASSMENT', 'SPAM', 'OTHER'))
);
