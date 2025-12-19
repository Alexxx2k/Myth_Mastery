
CREATE TABLE auto_personal (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    father_name VARCHAR(50)
);

CREATE TABLE auto (
    id SERIAL PRIMARY KEY,
    num VARCHAR(20) NOT NULL UNIQUE,
    color VARCHAR(30) NOT NULL,
    mark VARCHAR(30) NOT NULL,
    personal_id INTEGER NOT NULL
);

CREATE TABLE routes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE journal (
    id SERIAL PRIMARY KEY,
    time_in TIMESTAMP(0),
    auto_id INTEGER NOT NULL,
    route_id INTEGER NOT NULL,
    time_out TIMESTAMP(0)
);

CREATE TABLE public.users (
    id SERIAL PRIMARY KEY,
    username character varying(50) NOT NULL UNIQUE,
    password_hash character varying(255) NOT NULL,
    role character varying(20) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE auto
    ADD CONSTRAINT fk_auto_auto_personal
        FOREIGN KEY (personal_id) REFERENCES auto_personal(id) ON DELETE CASCADE;

ALTER TABLE journal
    ADD CONSTRAINT fk_journal_auto
        FOREIGN KEY (auto_id) REFERENCES auto(id) ON DELETE CASCADE;

ALTER TABLE journal
    ADD CONSTRAINT fk_journal_routes
        FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE;