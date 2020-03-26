INSERT INTO log3900.channel VALUES('general', DEFAULT);
INSERT INTO log3900.channel VALUES('pute', DEFAULT);
INSERT INTO log3900.channel VALUES('george', DEFAULT);
INSERT INTO log3900.channel VALUES('channel', DEFAULT);

SELECT LOG3900.registerAccount('username', 'password', 'Use', 'Rname');
SELECT LOG3900.registerAccount('jeremy', 'password', 'Je', 'rem');
SELECT LOG3900.registerAccount('asd', 'asd', 'alpha', 'bravo');
SELECT LOG3900.registerAccount('a', 'a', 'fuck', 'you');
SELECT LOG3900.registerAccount('b', 'b', 'fucka', 'youa');

INSERT INTO log3900.accountChannel VALUES(1, 'pute');
INSERT INTO log3900.accountChannel VALUES(2, 'pute');
INSERT INTO log3900.accountChannel VALUES(2, 'channel');

SELECT LOG3900.registerGame('FFA', '23/03/2020 16:32:35', 40, 'username', '[{"username": "username", "point":50}, {"username": "asd", "point":69}]');
SELECT LOG3900.registerGame('SOLO', '23/03/2020 16:32:35', 23, 'username', '[{"username": "username", "point":87}]');
SELECT LOG3900.registerGame('SOLO', '23/03/2020 16:32:35', 45, 'asd', '[{"username": "asd", "point":420}]');
SELECT LOG3900.registerGame('SOLO', '23/03/2020 16:32:35', 45, 'asd', '[{"username": "asd", "point":23}]');
