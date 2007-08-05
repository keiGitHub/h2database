/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.command.ddl;

import java.sql.SQLException;

import org.h2.engine.Database;
import org.h2.engine.Session;
import org.h2.message.Message;
import org.h2.schema.Schema;
import org.h2.schema.TriggerObject;
import org.h2.table.Table;

/**
 * @author Thomas
 */

public class CreateTrigger extends SchemaCommand {
    // TODO implement drop trigger

    private String triggerName;
    private boolean ifNotExists;

    private boolean before;
    private int typeMask;
    private boolean rowBased;
    private int queueSize = TriggerObject.DEFAULT_QUEUE_SIZE;
    private boolean noWait;
    private String tableName;
    private String triggerClassName;

    public CreateTrigger(Session session, Schema schema) {
        super(session, schema);
    }

    public void setBefore(boolean before) {
        this.before = before;
    }

    public void setTriggerClassName(String triggerClassName) {
        this.triggerClassName = triggerClassName;
    }

    public void setTypeMask(int typeMask) {
        this.typeMask = typeMask;
    }

    public void setRowBased(boolean rowBased) {
        this.rowBased = rowBased;
    }

    public void setQueueSize(int size) {
        this.queueSize = size;
    }

    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setTriggerName(String name) {
        this.triggerName = name;
    }

    public void setIfNotExists(boolean ifNotExists) {
        // TODO trigger: if exists - probably better use 'or replace'
        this.ifNotExists = ifNotExists;
    }

    public int update() throws SQLException {
        // TODO rights: what rights are required to create a trigger?
        session.commit(true);
        Database db = session.getDatabase();
        if(getSchema().findTrigger(triggerName)!=null) {
            if (ifNotExists) {
                return 0;
            }
            throw Message.getSQLException(Message.TRIGGER_ALREADY_EXISTS_1,
                    triggerName);
        }
        int id = getObjectId(false, true);
        Table table = getSchema().getTableOrView(session, tableName);
        TriggerObject trigger = new TriggerObject(getSchema(), id, triggerName, table);
        trigger.setBefore(before);
        trigger.setNoWait(noWait);
        trigger.setQueueSize(queueSize);
        trigger.setRowBased(rowBased);
        trigger.setTriggerClassName(session, triggerClassName);
        trigger.setTypeMask(typeMask);
        db.addSchemaObject(session, trigger);
        table.addTrigger(trigger);
        return 0;
    }

}
