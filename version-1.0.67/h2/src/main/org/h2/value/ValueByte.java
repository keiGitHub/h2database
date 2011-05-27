/*
 * Copyright 2004-2008 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.value;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.constant.ErrorCode;
import org.h2.constant.SysProperties;
import org.h2.message.Message;
import org.h2.util.ObjectUtils;

/**
 * Implementation of the BYTE data type.
 */
public class ValueByte extends Value {
    public static final int PRECISION = 3;
    public static final int DISPLAY_SIZE = 4;

    private final byte value;

    private ValueByte(byte value) {
        this.value = value;
    }

    public Value add(Value v) throws SQLException {
        ValueByte other = (ValueByte) v;
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(value + other.value);
        }
        return ValueByte.get((byte) (value + other.value));
    }

    private ValueByte checkRange(int value) throws SQLException {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            throw Message.getSQLException(ErrorCode.OVERFLOW_FOR_TYPE_1, DataType.getDataType(Value.BYTE).name);
        } else {
            return ValueByte.get((byte) value);
        }
    }

    public int getSignum() {
        return value == 0 ? 0 : (value < 0 ? -1 : 1);
    }

    public Value negate() throws SQLException {
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(-(int) value);
        }
        return ValueByte.get((byte) (-value));
    }

    public Value subtract(Value v) throws SQLException {
        ValueByte other = (ValueByte) v;
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(value - other.value);
        }
        return ValueByte.get((byte) (value - other.value));
    }

    public Value multiply(Value v) throws SQLException {
        ValueByte other = (ValueByte) v;
        if (SysProperties.OVERFLOW_EXCEPTIONS) {
            return checkRange(value * other.value);
        }
        return ValueByte.get((byte) (value * other.value));
    }

    public Value divide(Value v) throws SQLException {
        ValueByte other = (ValueByte) v;
        if (other.value == 0) {
            throw Message.getSQLException(ErrorCode.DIVISION_BY_ZERO_1, getSQL());
        }
        return ValueByte.get((byte) (value / other.value));
    }

    public String getSQL() {
        return getString();
    }

    public int getType() {
        return Value.BYTE;
    }

    public byte getByte() {
        return value;
    }

    protected int compareSecure(Value o, CompareMode mode) {
        ValueByte v = (ValueByte) o;
        if (value == v.value) {
            return 0;
        }
        return value > v.value ? 1 : -1;
    }

    public String getString() {
        return String.valueOf(value);
    }

    public long getPrecision() {
        return PRECISION;
    }

    public int hashCode() {
        return value;
    }

    public Object getObject() {
        return ObjectUtils.getByte(value);
    }

    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        prep.setByte(parameterIndex, value);
    }

    public static ValueByte get(byte i) {
        return (ValueByte) Value.cache(new ValueByte(i));
    }

    public int getDisplaySize() {
        return DISPLAY_SIZE;
    }

    protected boolean isEqual(Value v) {
        return v instanceof ValueByte && value == ((ValueByte) v).value;
    }

}
