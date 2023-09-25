CREATE TABLE stocks_user
(
    id     INTEGER GENERATED BY DEFAULT AS IDENTITY
        CONSTRAINT user_pkey
            PRIMARY KEY,
    first_name  VARCHAR NOT NULL,
    second_name VARCHAR,
    birthday    DATE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW(),
    is_deleted  BOOLEAN   DEFAULT FALSE
);

CREATE TABLE security_info
(
    id  INTEGER NOT NULL
        PRIMARY KEY
        REFERENCES stocks_user,
    username VARCHAR NOT NULL
        UNIQUE,
    password VARCHAR NOT NULL,
    email    VARCHAR NOT NULL
        CONSTRAINT security_info_pk
            UNIQUE
);

