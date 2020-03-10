INSERT INTO log3900.channel VALUES('general', DEFAULT);
INSERT INTO log3900.channel VALUES('pute', DEFAULT);
INSERT INTO log3900.channel VALUES('george', DEFAULT);
INSERT INTO log3900.channel VALUES('channel', DEFAULT);

SELECT LOG3900.registerAccount('username', 'password', 'Use', 'Rname');
SELECT LOG3900.registerAccount('jeremy', 'password', 'Je', 'rem');
SELECT LOG3900.registerAccount('asd', 'asd', 'alpha', 'bravo');

INSERT INTO log3900.accountChannel VALUES(1, 'pute');
INSERT INTO log3900.accountChannel VALUES(2, 'pute');
INSERT INTO log3900.accountChannel VALUES(2, 'channel');
