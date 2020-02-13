
CREATE OR REPLACE FUNCTION LOG3900.registerAccount(in_username VARCHAR(20), in_password VARCHAR(100)) RETURNS void AS $$
    DECLARE
        account_id INT;
    BEGIN
       IF EXISTS( SELECT A.username FROM LOG3900.Account as A WHERE A.username = in_username) THEN
            RAISE EXCEPTION 'Username exist already.';
       END IF;
       INSERT INTO LOG3900.Account VALUES(DEFAULT, in_username, in_password) RETURNING id INTO account_id;
       INSERT INTO LOG3900.accountchannel VALUES(account_id, 'general');
    END;
$$LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LOG3900.loginAccount(in_username VARCHAR(20), in_password VARCHAR(100)) RETURNS void AS $$
    BEGIN
        IF NOT EXISTS( SELECT A.username FROM LOG3900.Account as A WHERE A.username = in_username) THEN
            RAISE EXCEPTION 'Username is incorrect.';
        END IF;
        IF NOT EXISTS( SELECT A.hashPwd FROM LOG3900.Account as A WHERE A.hashPwd = in_password) THEN
            RAISE EXCEPTION 'Password is incorrect.';
        END IF;
    END;
$$LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LOG3900.getMessagesWithChannelId(in_id VARCHAR(20))
RETURNS TABLE (out_username VARCHAR(20), out_content TEXT, out_times VARCHAR(8)) AS $$
    BEGIN
        RETURN QUERY
        WITH RECURSIVE messageOrder(parent_id, id, content, level, ts, account_id)
        AS (
            SELECT parent_id, id, content, 0, times, account_id
            FROM LOG3900.MESSAGES as messages
            WHERE parent_id IS NULL
            AND channel_id = in_id

            UNION ALL

            SELECT messages.parent_id, messages.id, messages.content, messageOrder.level+1, messageOrder.ts, messageOrder.account_id
            FROM LOG3900.MESSAGES as messages
            JOIN messageOrder ON (messages.parent_id = messageOrder.id)
        )
        SELECT a.username, content, ts
        FROM messageOrder, LOG3900.account as a
        WHERE account_id = a.id
        ORDER BY level;
    END;
$$LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LOG3900.createChannelWithAccountId(in_id VARCHAR(20)) RETURNS VOID AS $$
    DECLARE
        channel_id VARCHAR(20);
    BEGIN
        INSERT INTO LOG3900.Channel VALUES(DEFAULT) RETURNING id INTO channel_id;
        INSERT INTO LOG3900.accountchannel VALUES(in_id, channel_id);
    END;
$$LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION LOG3900.insertChannelMessage(in_channel_id VARCHAR(20), in_account_id INT, in_content TEXT, in_ts VARCHAR(8)) RETURNS VOID AS $$
    DECLARE
        last_id INT;
    BEGIN
        SELECT m.id
        FROM LOG3900.messages as m
        WHERE m.channel_id = in_channel_id
        ORDER BY m.id DESC
        LIMIT 1
        INTO last_id;

        IF last_id IS NULL THEN
            INSERT INTO LOG3900.messages VALUES(DEFAULT, null, in_ts, in_content, in_channel_id, in_account_id);
        ELSE
            INSERT INTO LOG3900.messages VALUES(DEFAULT, last_id, in_ts, in_content, in_channel_id, in_account_id);
        END IF;
    END;
$$LANGUAGE plpgsql;