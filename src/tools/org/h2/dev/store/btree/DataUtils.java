/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.dev.store.btree;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Utility methods
 */
public class DataUtils {

    /**
     * The type for leaf page.
     */
    public static final int PAGE_TYPE_LEAF = 0;

    /**
     * The type for node page.
     */
    public static final int PAGE_TYPE_NODE = 1;

    /**
     * The bit mask for compressed pages.
     */
    public static final int PAGE_COMPRESSED = 2;

    /**
     * The maximum length of a variable size int.
     */
    public static final int MAX_VAR_INT_LEN = 5;

    /**
     * The maximum length of a variable size long.
     */
    public static final int MAX_VAR_LONG_LEN = 10;

    /**
     * Get the length of the variable size int.
     *
     * @param x the value
     * @return the length in bytes
     */
    public static int getVarIntLen(int x) {
        if ((x & (-1 << 7)) == 0) {
            return 1;
        } else if ((x & (-1 << 14)) == 0) {
            return 2;
        } else if ((x & (-1 << 21)) == 0) {
            return 3;
        } else if ((x & (-1 << 28)) == 0) {
            return 4;
        }
        return 5;
    }

    /**
     * Get the length of the variable size long.
     *
     * @param x the value
     * @return the length in bytes
     */
    public static int getVarLongLen(long x) {
        int i = 1;
        while (true) {
            x >>>= 7;
            if (x == 0) {
                return i;
            }
            i++;
        }
    }

    /**
     * Read a variable size int.
     *
     * @param buff the source buffer
     * @return the value
     */
    public static int readVarInt(ByteBuffer buff) {
        int b = buff.get();
        if (b >= 0) {
            return b;
        }
        // a separate function so that this one can be inlined
        return readVarIntRest(buff, b);
    }

    private static int readVarIntRest(ByteBuffer buff, int b) {
        int x = b & 0x7f;
        b = buff.get();
        if (b >= 0) {
            return x | (b << 7);
        }
        x |= (b & 0x7f) << 7;
        b = buff.get();
        if (b >= 0) {
            return x | (b << 14);
        }
        x |= (b & 0x7f) << 14;
        b = buff.get();
        if (b >= 0) {
            return x | b << 21;
        }
        x |= ((b & 0x7f) << 21) | (buff.get() << 28);
        return x;
    }

    /**
     * Read a variable size long.
     *
     * @param buff the source buffer
     * @return the value
     */
    public static long readVarLong(ByteBuffer buff) {
        long x = buff.get();
        if (x >= 0) {
            return x;
        }
        x &= 0x7f;
        for (int s = 7; s < 64; s += 7) {
            long b = buff.get();
            x |= (b & 0x7f) << s;
            if (b >= 0) {
                break;
            }
        }
        return x;
    }

    /**
     * Write a variable size int.
     *
     * @param buff the target buffer
     * @param x the value
     */
    public static void writeVarInt(ByteBuffer buff, int x) {
        while ((x & ~0x7f) != 0) {
            buff.put((byte) (0x80 | (x & 0x7f)));
            x >>>= 7;
        }
        buff.put((byte) x);
    }

    /**
     * Write a variable size int.
     *
     * @param buff the target buffer
     * @param x the value
     */
    public static void writeVarLong(ByteBuffer buff, long x) {
        while ((x & ~0x7f) != 0) {
            buff.put((byte) (0x80 | (x & 0x7f)));
            x >>>= 7;
        }
        buff.put((byte) x);
    }

    /**
     * Copy the elements of an array, with a gap.
     *
     * @param src the source array
     * @param dst the target array
     * @param oldSize the size of the old array
     * @param gapIndex the index of the gap
     */
    public static void copyWithGap(Object src, Object dst, int oldSize, int gapIndex) {
        if (gapIndex > 0) {
            System.arraycopy(src, 0, dst, 0, gapIndex);
        }
        if (gapIndex < oldSize) {
            System.arraycopy(src, gapIndex, dst, gapIndex + 1, oldSize - gapIndex);
        }
    }

    /**
     * Copy the elements of an array, and remove one element.
     *
     * @param src the source array
     * @param dst the target array
     * @param oldSize the size of the old array
     * @param removeIndex the index of the entry to remove
     */
    public static void copyExcept(Object src, Object dst, int oldSize, int removeIndex) {
        if (removeIndex > 0 && oldSize > 0) {
            System.arraycopy(src, 0, dst, 0, removeIndex);
        }
        if (removeIndex < oldSize) {
            System.arraycopy(src, removeIndex + 1, dst, removeIndex, oldSize - removeIndex - 1);
        }
    }

    static void readFully(FileChannel file, ByteBuffer buff) throws IOException {
        do {
            int len = file.read(buff);
            if (len < 0) {
                break;
            }
        } while (buff.remaining() > 0);
        buff.rewind();
    }

    /**
     * Convert the length to a length code 0..31. 31 means more than 1 MB.
     *
     * @param len the length
     * @return the length code
     */
    public static int encodeLength(int len) {
        if (len <= 32) {
            return 0;
        }
        int x = len;
        int shift = 0;
        while (x > 3) {
            shift++;
            x = (x >> 1) + (x & 1);
        }
        shift = Math.max(0,  shift - 4);
        int code = (shift << 1) + (x & 1);
        return Math.min(31, code);
    }

    /**
     * Get the chunk id from the position.
     *
     * @param pos the position
     * @return the chunk id
     */
    public static int getPageChunkId(long pos) {
        return (int) (pos >>> 38);
    }

    /**
     * Get the maximum length for the given code.
     * For the code 31, Integer.MAX_VALUE is returned.
     *
     * @param pos the position
     * @return the maximum length
     */
    public static int getPageMaxLength(long pos) {
        int code = (int) ((pos >> 1) & 31);
        if (code == 31) {
            return Integer.MAX_VALUE;
        }
        return (2 + (code & 1)) << ((code >> 1) + 4);
    }

    /**
     * Get the offset from the position.
     *
     * @param pos the position
     * @return the offset
     */
    public static int getPageOffset(long pos) {
        return (int) (pos >> 6);
    }

    /**
     * Get the page type from the position.
     *
     * @param pos the position
     * @return the page type (PAGE_TYPE_NODE or PAGE_TYPE_LEAF)
     */
    public static int getPageType(long pos) {
        return ((int) pos) & 1;
    }

    /**
     * Get the position of this page. The following information is encoded in
     * the position: the chunk id, the offset, the maximum length, and the type
     * (node or leaf).
     *
     * @param chunkId the chunk id
     * @param offset the offset
     * @param length the length
     * @param type the page type (1 for node, 0 for leaf)
     * @return the position
     */
    public static long getPagePos(int chunkId, int offset, int length, int type) {
        long pos = (long) chunkId << 38;
        pos |= (long) offset << 6;
        pos |= encodeLength(length) << 1;
        pos |= type;
        return pos;
    }

    /**
     * Calculate a check value for the given integer. A check value is mean to
     * verify the data is consistent with a high probability, but not meant to
     * protect against media failure or deliberate changes.
     *
     * @param x the value
     * @return the check value
     */
    public static short getCheckValue(int x) {
        return (short) ((x >> 16) ^ x);
    }

}
