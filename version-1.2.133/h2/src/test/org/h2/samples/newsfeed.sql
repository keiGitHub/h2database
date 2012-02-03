/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

CREATE TABLE CHANNEL(TITLE VARCHAR, LINK VARCHAR, DESC VARCHAR,
    LANGUAGE VARCHAR, PUB TIMESTAMP, LAST TIMESTAMP, AUTHOR VARCHAR);

INSERT INTO CHANNEL VALUES('H2 Database Engine' ,
    'http://www.h2database.com/', 'H2 Database Engine', 'en-us', NOW(), NOW(), 'Thomas Mueller');

CREATE TABLE ITEM(ID INT PRIMARY KEY, TITLE VARCHAR, ISSUED TIMESTAMP, DESC VARCHAR);

INSERT INTO ITEM VALUES(83,
'New version available: 1.2.133 (2010-04-10)', '2010-04-10 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(82,
'New version available: 1.2.132 (2010-03-21)', '2010-03-21 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(81,
'New version available: 1.2.131 (2010-03-05)', '2010-03-05 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(80,
'New version available: 1.2.130 (2010-02-26)', '2010-02-26 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(79,
'New version available: 1.2.129 (2010-02-19)', '2010-02-19 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(78,
'New version available: 1.2.128 (2010-01-30)', '2010-01-30 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(77,
'New version available: 1.2.127 (2010-01-15)', '2010-01-15 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(76,
'New version available: 1.2.126 (2009-12-18)', '2009-12-18 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(75,
'New version available: 1.2.125 (2009-12-06)', '2009-12-06 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(74,
'New version available: 1.2.124 (2009-11-20)', '2009-11-20 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(73,
'New version available: 1.2.123 (2009-11-08)', '2009-11-08 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

INSERT INTO ITEM VALUES(72,
'New version available: 1.2.122 (2009-10-28)', '2009-10-28 12:00:00',
$$A new version of H2 is available for
<a href="http://www.h2database.com">download</a>.
(You may have to click 'Refresh').
<br />
For details, see the
<a href="http://www.h2database.com/html/changelog.html">change log</a>.
<br />
For future plans, see the
<a href="http://www.h2database.com/html/roadmap.html">roadmap</a>.
$$);

SELECT 'newsfeed-rss.xml' FILE,
    XMLSTARTDOC() ||
    XMLNODE('rss', XMLATTR('version', '2.0'),
        XMLNODE('channel', NULL,
            XMLNODE('title', NULL, C.TITLE) ||
            XMLNODE('link', NULL, C.LINK) ||
            XMLNODE('description', NULL, C.DESC) ||
            XMLNODE('language', NULL, C.LANGUAGE) ||
            XMLNODE('pubDate', NULL, FORMATDATETIME(C.PUB, 'EEE, d MMM yyyy HH:mm:ss z', 'en', 'GMT')) ||
            XMLNODE('lastBuildDate', NULL, FORMATDATETIME(C.LAST, 'EEE, d MMM yyyy HH:mm:ss z', 'en', 'GMT')) ||
            GROUP_CONCAT(
                XMLNODE('item', NULL,
                    XMLNODE('title', NULL, I.TITLE) ||
                    XMLNODE('link', NULL, C.LINK) ||
                    XMLNODE('description', NULL, XMLCDATA(I.TITLE))
                )
            ORDER BY I.ID DESC SEPARATOR '')
        )
    ) CONTENT
FROM CHANNEL C, ITEM I
UNION
SELECT 'newsfeed-atom.xml' FILE,
    XMLSTARTDOC() ||
    XMLNODE('feed', XMLATTR('xmlns', 'http://www.w3.org/2005/Atom') || XMLATTR('xml:lang', C.LANGUAGE),
        XMLNODE('title', XMLATTR('type', 'text'), C.TITLE) ||
        XMLNODE('id', NULL, XMLTEXT(C.LINK)) ||
        XMLNODE('author', NULL, XMLNODE('name', NULL, C.AUTHOR)) ||
        XMLNODE('link', XMLATTR('rel', 'self') || XMLATTR('href', 'http://www.h2database.com/html/newsfeed-atom.xml'), NULL) ||
        XMLNODE('updated', NULL, FORMATDATETIME(C.LAST, 'yyyy-MM-dd''T''HH:mm:ss''Z''', 'en', 'GMT')) ||
        GROUP_CONCAT(
            XMLNODE('entry', NULL,
                XMLNODE('title', XMLATTR('type', 'text'), I.TITLE) ||
                XMLNODE('link', XMLATTR('rel', 'alternate') || XMLATTR('type', 'text/html') || XMLATTR('href', C.LINK), NULL) ||
                XMLNODE('id', NULL, XMLTEXT(C.LINK || '/' || I.ID)) ||
                XMLNODE('updated', NULL, FORMATDATETIME(I.ISSUED, 'yyyy-MM-dd''T''HH:mm:ss''Z''', 'en', 'GMT')) ||
                XMLNODE('content', XMLATTR('type', 'html'), XMLCDATA(I.DESC))
            )
        ORDER BY I.ID DESC SEPARATOR '')
    ) CONTENT
FROM CHANNEL C, ITEM I
UNION
SELECT 'newsletter.txt' FILE, I.DESC CONTENT FROM ITEM I WHERE I.ID = (SELECT MAX(ID) FROM ITEM)