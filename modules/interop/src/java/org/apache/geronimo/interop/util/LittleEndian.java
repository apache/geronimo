/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

public abstract class LittleEndian {
    public static byte[] getShortBytes(short value) {
        byte[] bytes = new byte[2];
        setShort(bytes, value);
        return bytes;
    }

    public static byte[] getIntBytes(int value) {
        byte[] bytes = new byte[4];
        setInt(bytes, value);
        return bytes;
    }

    public static byte[] getLongBytes(long value) {
        byte[] bytes = new byte[8];
        setLong(bytes, value);
        return bytes;
    }

    public static short getShort(byte[] bytes) {
        return getShort(bytes, 0);
    }

    public static short getShort(byte[] bytes, int offset) {
        int b1 = (bytes[offset + 1] << 8) & 0x0000ff00;
        int b0 = bytes[offset] & 0x000000ff;
        return (short) (b1 | b0);
    }

    public static int getInt(byte[] bytes) {
        return getInt(bytes, 0);
    }

    public static int getInt(byte[] bytes, int offset) {
        int b3 = (bytes[offset + 3] << 24) & 0xff000000;
        int b2 = (bytes[offset + 2] << 16) & 0x00ff0000;
        int b1 = (bytes[offset + 1] << 8) & 0x0000ff00;
        int b0 = bytes[offset] & 0x000000ff;
        return b3 | b2 | b1 | b0;
    }

    public static long getLong(byte[] bytes) {
        return getLong(bytes, 0);
    }

    public static long getLong(byte[] bytes, int offset) {
        long hi = getInt(bytes, offset + 4) & 0xffffffffL;
        long lo = getInt(bytes, offset) & 0xffffffffL;
        return (lo << 32) | hi;
    }

    public static void setShort(byte[] bytes, short value) {
        setShort(bytes, 0, value);
    }

    public static void setShort(byte[] bytes, int offset, short value) {
        bytes[offset] = (byte) (value & 0xff);
        bytes[offset + 1] = (byte) ((value >>> 8) & 0xff);
    }

    public static void setInt(byte[] bytes, int value) {
        setInt(bytes, 0, value);
    }

    public static void setInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) (value & 0xff);
        bytes[offset + 1] = (byte) ((value >>> 8) & 0xff);
        bytes[offset + 2] = (byte) ((value >>> 16) & 0xff);
        bytes[offset + 3] = (byte) ((value >>> 24) & 0xff);
    }

    public static void setLong(byte[] bytes, long value) {
        setLong(bytes, 0, value);
    }

    public static void setLong(byte[] bytes, int offset, long value) {
        int hi = (int) (value >>> 32);
        int lo = (int) value;
        setInt(bytes, offset + 4, hi);
        setInt(bytes, offset, lo);
    }
}
