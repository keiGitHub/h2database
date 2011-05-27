/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.index;

import java.sql.SQLException;
import java.util.Iterator;
import org.h2.engine.Session;
import org.h2.result.Row;
import org.h2.result.SearchRow;

/**
 * The cursor implementation for the scan index.
 */
public class ScanCursor implements Cursor {
    private ScanIndex scan;
    private Row row;
    private final Session session;
    private final boolean multiVersion;
    private Iterator delta;

    ScanCursor(Session session, ScanIndex scan, boolean multiVersion) {
        this.session = session;
        this.scan = scan;
        this.multiVersion = multiVersion;
        if (multiVersion) {
            delta = scan.getDelta();
        }
        row = null;
    }

    public Row get() {
        return row;
    }

    public SearchRow getSearchRow() {
        return row;
    }

    public int getPos() {
        return row.getPos();
    }

    public boolean next() throws SQLException {
        if (multiVersion) {
            while (true) {
                if (delta != null) {
                    if (!delta.hasNext()) {
                        delta = null;
                        row = null;
                        continue;
                    } else {
                        row = (Row) delta.next();
                        if (!row.getDeleted() || row.getSessionId() == session.getId()) {
                            continue;
                        }
                    }
                } else {
                    row = scan.getNextRow(session, row);
                    if (row != null && row.getSessionId() != 0 && row.getSessionId() != session.getId()) {
                        continue;
                    }
                }
                break;
            }
            return row != null;
        }
        row = scan.getNextRow(session, row);
        return row != null;
    }
}
