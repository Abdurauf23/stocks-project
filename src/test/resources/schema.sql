CREATE TABLE stocks_user
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY
        CONSTRAINT user_pkey
            PRIMARY KEY,
    first_name  VARCHAR NOT NULL,
    second_name VARCHAR,
    birthday    DATE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW(),
    is_deleted  BOOLEAN   DEFAULT FALSE
);

CREATE TABLE role
(
    role_id   INTEGER NOT NULL
        PRIMARY KEY,
    role_name VARCHAR NOT NULL
);

CREATE TABLE security_info
(
    id       INTEGER NOT NULL
        PRIMARY KEY
        REFERENCES stocks_user
            ON DELETE CASCADE,
    username VARCHAR NOT NULL
        UNIQUE,
    password VARCHAR NOT NULL,
    role_id  INTEGER NOT NULL
        REFERENCES role,
    email    VARCHAR NOT NULL
        CONSTRAINT security_info_pk
            UNIQUE
);

CREATE TABLE stock_meta
(
    id                INTEGER GENERATED BY DEFAULT AS IDENTITY
        PRIMARY KEY,
    symbol            VARCHAR,
    data_interval     VARCHAR,
    currency          VARCHAR(5),
    exchange_timezone VARCHAR,
    exchange          VARCHAR,
    mic_code          VARCHAR(4),
    type_             VARCHAR,
    stock_status      VARCHAR
);

CREATE TABLE stock_value
(
    id        INTEGER GENERATED BY DEFAULT AS IDENTITY
        PRIMARY KEY,
    meta_id   INTEGER
        REFERENCES stock_meta
            ON DELETE CASCADE,
    date_time TIMESTAMP NOT NULL,
    open      NUMERIC(10, 5),
    high      NUMERIC(10, 5),
    low       NUMERIC(10, 5),
    close     NUMERIC(10, 5),
    volume    INTEGER
);

CREATE TABLE stock_users_fav_stocks
(
    user_id INTEGER NOT NULL
        REFERENCES stocks_user
            ON DELETE CASCADE,
    meta_id INTEGER NOT NULL
        REFERENCES stock_meta
            ON DELETE CASCADE,
    PRIMARY KEY (user_id, meta_id)
);
