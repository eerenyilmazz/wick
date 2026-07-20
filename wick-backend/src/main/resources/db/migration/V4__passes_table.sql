CREATE TABLE passes (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    passer_id uuid NOT NULL,
    passed_id uuid NOT NULL,
    CONSTRAINT passes_pkey PRIMARY KEY (id),
    CONSTRAINT passes_passer_passed_key UNIQUE (passer_id, passed_id),
    CONSTRAINT passes_passer_fk FOREIGN KEY (passer_id) REFERENCES users(id),
    CONSTRAINT passes_passed_fk FOREIGN KEY (passed_id) REFERENCES users(id)
);
