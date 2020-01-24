DROP SCHEMA IF EXISTS LOG3900 CASCADE;

CREATE SCHEMA IF NOT EXISTS LOG3900;

CREATE TABLE IF NOT EXISTS LOG3900.Account (
    id          SERIAL PRIMARY KEY NOT NULL,
    username    VARCHAR(20) NOT NULL,
    password    VARCHAR(100) NOT NULL
);

CREATE OR REPLACE FUNCTION LOG3900.registerAccount(in_username VARCHAR(20), in_password VARCHAR(100)) RETURNS void AS $$
    BEGIN
       IF EXISTS( SELECT A.username FROM LOG3900.Account as A WHERE A.username = in_username) THEN
            RAISE EXCEPTION 'Username exist already.';
       END IF;
       INSERT INTO LOG3900.Account VALUES(DEFAULT, in_username, in_password);
    END;
$$LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LOG3900.loginAccount(in_username VARCHAR(20), in_password VARCHAR(100)) RETURNS void AS $$
    BEGIN
        IF NOT EXISTS( SELECT A.username FROM LOG3900.Account as A WHERE A.username = in_username) THEN
            RAISE EXCEPTION 'Username is incorrect.';
        END IF;
        IF NOT EXISTS( SELECT A.password FROM LOG3900.Account as A WHERE A.password = in_password) THEN
            RAISE EXCEPTION 'Password is incorrect.';
        END IF;
    END;
$$LANGUAGE plpgsql;