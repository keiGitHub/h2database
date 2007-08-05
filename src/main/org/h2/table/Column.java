/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.table;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import org.h2.command.Parser;
import org.h2.engine.Constants;
import org.h2.engine.Mode;
import org.h2.engine.Session;
import org.h2.expression.ConditionAndOr;
import org.h2.expression.Expression;
import org.h2.expression.SequenceValue;
import org.h2.expression.ValueExpression;
import org.h2.message.Message;
import org.h2.result.Row;
import org.h2.schema.Schema;
import org.h2.schema.Sequence;
import org.h2.util.StringUtils;
import org.h2.value.DataType;
import org.h2.value.Value;
import org.h2.value.ValueInt;
import org.h2.value.ValueLong;
import org.h2.value.ValueNull;
import org.h2.value.ValueString;
import org.h2.value.ValueTime;
import org.h2.value.ValueTimestamp;
import org.h2.value.ValueUuid;

/**
 * @author Thomas
 */
public class Column {
    private final int type;
    private final long precision;
    private final int scale;
    private Table table;
    private String name;
    private int columnId;
    private boolean nullable = true;
    private Expression defaultExpression;
    private Expression checkConstraint;
    private String checkConstraintSQL;
    private String originalSQL;
    private boolean autoIncrement;
    private long start;
    private long increment;
    private boolean convertNullToDefault;
    private Sequence sequence;
    private boolean isComputed;
    private TableFilter computeTableFilter;
    private int selectivity;
    private SingleColumnResolver resolver;
    private String comment;
    private boolean primaryKey;

    // must be equal to ResultSetMetaData columnNoNulls, columnNullable, columnNullableUnknown
    public static final int NOT_NULLABLE = 0, NULLABLE = 1, NULLABLE_UNKNOWN = 2;

    public Column(String name, int type, long precision, int scale) {
        this.name = name;
        this.type = type;
        this.precision = precision;
        this.scale = scale;
    }
    
    public Column getClone() {
        Column newColumn = new Column(name, type, precision, scale);
        // table is not set
        // columnId is not set
        newColumn.nullable = nullable;
        newColumn.defaultExpression = defaultExpression;
        newColumn.originalSQL = originalSQL;
        // autoIncrement, start, increment is not set
        newColumn.convertNullToDefault = convertNullToDefault;
        newColumn.sequence = sequence;
        newColumn.comment = comment;
        newColumn.isComputed = isComputed;
        newColumn.selectivity = selectivity;
        newColumn.primaryKey = primaryKey;
        return newColumn;
    }

    public boolean getComputed() {
        return isComputed;
    }

    public Value computeValue(Session session, Row row) throws SQLException {
        computeTableFilter.setSession(session);
        computeTableFilter.set(row);
        return defaultExpression.getValue(session);
    }

    public void setComputed(boolean computed, Expression expression) {
        this.isComputed = computed;
        this.defaultExpression = expression;
    }

    void setTable(Table table, int columnId) {
        this.table = table;
        this.columnId = columnId;
    }

    public Table getTable() {
        return table;
    }

    public void setDefaultExpression(Session session, Expression defaultExpression) throws SQLException {
        // also to test that no column names are used
        if(defaultExpression != null) {
            defaultExpression = defaultExpression.optimize(session);
            if(defaultExpression.isConstant()) {
                defaultExpression = ValueExpression.get(defaultExpression.getValue(session));
            }
        }
        this.defaultExpression = defaultExpression;
    }

    public int getColumnId() {
        return columnId;
    }

    public String getSQL() {
        return Parser.quoteIdentifier(name);
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public long getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }

    public void setNullable(boolean b) {
        nullable = b;
    }

    public Value validateConvertUpdateSequence(Session session, Value value) throws SQLException {
        if(value == null) {
            if(defaultExpression == null) {
                value = ValueNull.INSTANCE;
            } else {
                value = defaultExpression.getValue(session).convertTo(type);
                if(primaryKey) {
                    session.setLastIdentity(value);
                }
            }
        }
        if (value == ValueNull.INSTANCE) {
            if(convertNullToDefault) {
                value = defaultExpression.getValue(session).convertTo(type);
            }
            if (value == ValueNull.INSTANCE && !nullable) {
                if(Mode.getCurrentMode().convertInsertNullToZero) {
                    DataType dt = DataType.getDataType(type);
                    if(dt.decimal) {
                        value = ValueInt.get(0).convertTo(type);
                    } else if(dt.type==Value.TIMESTAMP) {
                        value = ValueTimestamp.getNoCopy(new Timestamp(System.currentTimeMillis()));
                    } else if(dt.type==Value.TIME) {
                        // need to normalize
                        value = ValueTime.get(Time.valueOf("0:0:0"));
                    } else if(dt.type==Value.DATE) {
                        value = ValueTimestamp.getNoCopy(new Timestamp(System.currentTimeMillis())).convertTo(dt.type);
                    } else {
                        value = ValueString.get("").convertTo(type);
                    }
                } else {
                    throw Message.getSQLException(Message.NULL_NOT_ALLOWED, name);
                }
            }
        }
        if(checkConstraint != null) {
            resolver.setValue(value);
            Value v = checkConstraint.getValue(session);
            // Both TRUE and NULL are ok
            if(Boolean.FALSE.equals(v.getBoolean())) {
                throw Message.getSQLException(Message.CHECK_CONSTRAINT_VIOLATED_1, checkConstraint.getSQL());
            }
        }
        value = value.convertScale(Mode.getCurrentMode().convertOnlyToSmallerScale, scale);
        if (precision > 0) {
            if(!value.checkPrecision(precision)) {
                throw Message.getSQLException(Message.VALUE_TOO_LONG_1, name);
            }
        }
        updateSequenceIfRequired(session, value);
        return value;
    }

    private void updateSequenceIfRequired(Session session, Value value) throws SQLException {
        if(sequence != null) {
            long current = sequence.getCurrentValue();
            long increment = sequence.getIncrement();
            long now = value.getLong();
            boolean update = false;
            if(increment > 0 && now > current) {
                update = true;
            } else if(increment < 0 && now < current) {
                update = true;
            }
            if(update) {
                sequence.setStartValue(now + increment);
                session.setLastIdentity(ValueLong.get(now));
                sequence.flush();
            }
        }
    }

    public void convertAutoIncrementToSequence(Session session, Schema schema, int id, boolean temporary) throws SQLException {
        if(!autoIncrement) {
            throw Message.getInternalError();
        }
        if("IDENTITY".equals(originalSQL)) {
            originalSQL = "BIGINT";
        }
        String sequenceName;
        for(int i=0; ; i++) {
            ValueUuid uuid = ValueUuid.getNewRandom();
            String s = uuid.getString();
            s = s.replace('-', '_').toUpperCase();
            sequenceName = "SYSTEM_SEQUENCE_" + s;
            if(schema.findSequence(sequenceName) == null) {
                break;
            }
        }
        Sequence sequence = new Sequence(schema, id, sequenceName, true);
        sequence.setStartValue(start);
        sequence.setIncrement(increment);
        if(!temporary) {
            session.getDatabase().addSchemaObject(session, sequence);
        }
        setAutoIncrement(false, 0, 0);
        SequenceValue seq = new SequenceValue(sequence);
        setDefaultExpression(session, seq);
        setSequence(sequence);
    }

    public void prepareExpression(Session session) throws SQLException {
        if(defaultExpression != null) {
            computeTableFilter = new TableFilter(session, table, null, false, null);
            defaultExpression.mapColumns(computeTableFilter, 0);
            defaultExpression = defaultExpression.optimize(session);
        }
    }

    public String getCreateSQL() {
        StringBuffer buff = new StringBuffer();
        if(name != null) {
            buff.append(Parser.quoteIdentifier(name));
            buff.append(' ');
        }
        if(originalSQL != null) {
            buff.append(originalSQL);
        } else {
            buff.append(DataType.getDataType(type).name);
            switch (type) {
            case Value.DECIMAL:
                buff.append("(");
                buff.append(precision);
                buff.append(", ");
                buff.append(scale);
                buff.append(")");
                break;
            case Value.BYTES:
            case Value.STRING:
            case Value.STRING_IGNORECASE:
            case Value.STRING_FIXED:
                if(precision < Integer.MAX_VALUE) {
                    buff.append("(");
                    buff.append(precision);
                    buff.append(")");
                }
                break;
            }
        }
        if(defaultExpression != null) {
            String sql = defaultExpression.getSQL();
            if(sql != null) {
                if(isComputed) {
                    buff.append(" AS ");
                    buff.append(sql);
                } else if(defaultExpression != null) {
                    buff.append(" DEFAULT ");
                    buff.append(sql);
                }
            }
        }
        if (!nullable) {
            buff.append(" NOT NULL");
        }
        if (convertNullToDefault) {
            buff.append(" NULL_TO_DEFAULT");
        }
        if(sequence != null) {
            buff.append(" SEQUENCE ");
            buff.append(sequence.getSQL());
        }
        if(selectivity != 0) {
            buff.append(" SELECTIVITY ");
            buff.append(selectivity);
        }
        if(checkConstraint != null) {
            buff.append(" CHECK ");
            buff.append(checkConstraintSQL);
        }
        if(comment != null) {
            buff.append(" COMMENT ");
            buff.append(StringUtils.quoteStringSQL(comment));
        }
        return buff.toString();
    }

    public boolean getNullable() {
        return nullable;
    }

    public void setOriginalSQL(String original) {
        originalSQL = original;
    }

    public String getOriginalSQL() {
        return originalSQL;
    }

    public Expression getDefaultExpression() {
        return defaultExpression;
    }

    public boolean getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoInc, long start, long increment) {
        this.autoIncrement = autoInc;
        this.start = start;
        this.increment = increment;
        this.nullable = false;
        if(autoInc) {
            convertNullToDefault = true;
        }
    }

    public void setConvertNullToDefault(boolean convert) {
        this.convertNullToDefault = convert;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public Sequence getSequence() {
        return sequence;
    }

    public int getSelectivity() {
        return selectivity == 0 ? Constants.SELECTIVITY_DEFAULT : selectivity;
    }

    public void setSelectivity(int selectivity) {
        selectivity = selectivity < 0 ? 0 : (selectivity > 100 ? 100 : selectivity);
        this.selectivity = selectivity;
    }

    public void addCheckConstraint(Session session, Expression expr) throws SQLException {
        resolver = new SingleColumnResolver(this);
        synchronized(this) {
            String oldName = name;
            if(name == null) {
                name = "VALUE";
            }
            expr.mapColumns(resolver, 0);
            name = oldName;
        }
        expr = expr.optimize(session);
        resolver.setValue(ValueNull.INSTANCE);
        // check if the column is mapped
        expr.getValue(session);
        if(checkConstraint == null) {
            checkConstraint = expr;
        } else {
            checkConstraint = new ConditionAndOr(ConditionAndOr.AND, checkConstraint, expr);
        }
        checkConstraintSQL = getCheckConstraintSQL(session, name);
    }

    public Expression getCheckConstraint(Session session, String asColumnName) throws SQLException {
        if(checkConstraint == null) {
            return null;
        }
        Parser parser = new Parser(session);
        String sql;
        synchronized(this) {
            String oldName = name;
            name = asColumnName;
            sql = checkConstraint.getSQL();
            name = oldName;
        }
        Expression expr = parser.parseExpression(sql);
        return expr;
    }

    public String getDefaultSQL() {
        return defaultExpression == null ? "" : defaultExpression.getSQL();
    }

    public int getPrecisionAsInt() {
        return precision > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)precision;
    }

    public DataType getDataType() {
        return DataType.getDataType(type);
    }

    public String getCheckConstraintSQL(Session session, String name) throws SQLException {
        Expression constraint = getCheckConstraint(session, name);
        return constraint == null ? "" : constraint.getSQL();
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

}
