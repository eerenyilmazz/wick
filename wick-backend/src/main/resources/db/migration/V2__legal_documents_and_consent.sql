CREATE TABLE legal_documents (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    type character varying(255) NOT NULL,
    version integer NOT NULL,
    content text NOT NULL,
    published_at timestamp(6) with time zone NOT NULL,
    CONSTRAINT legal_documents_pkey PRIMARY KEY (id),
    CONSTRAINT legal_documents_type_version_key UNIQUE (type, version),
    CONSTRAINT legal_documents_type_check CHECK (type IN ('PRIVACY_POLICY', 'TERMS_OF_SERVICE'))
);

CREATE TABLE user_consents (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    user_id uuid NOT NULL,
    document_type character varying(255) NOT NULL,
    version integer NOT NULL,
    CONSTRAINT user_consents_pkey PRIMARY KEY (id),
    CONSTRAINT user_consents_user_fk FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT user_consents_document_type_check CHECK (document_type IN ('PRIVACY_POLICY', 'TERMS_OF_SERVICE'))
);

INSERT INTO legal_documents (id, created_at, updated_at, type, version, content, published_at)
VALUES (gen_random_uuid(), now(), now(), 'PRIVACY_POLICY', 1, $$[TASLAK - henuz hukuk incelemesinden gecmedi. Gercek KVKK aydinlatma metni yayinlanmadan once bir avukata onaylatilmalidir.]

Bu metin yer tutucudur.$$, now());

INSERT INTO legal_documents (id, created_at, updated_at, type, version, content, published_at)
VALUES (gen_random_uuid(), now(), now(), 'TERMS_OF_SERVICE', 1, $$[TASLAK - henuz hukuk incelemesinden gecmedi. Gercek kullanim sartlari metni yayinlanmadan once bir avukata onaylatilmalidir.]

Bu metin yer tutucudur.$$, now());
