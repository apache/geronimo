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

public abstract class UTF8 {
    public static byte[] fromString(String value) {
        int n = value.length(), u = 0;
        for (int i = 0; i < n; i++) {
            int c = value.charAt(i);
            if (c >= 0x0001 && c <= 0x007F) {
                u++;
            } else if (c > 0x07FF) {
                u += 3;
            } else {
                u += 2;
            }
        }
        byte[] bytes = new byte[u];
        for (int i = 0, j = 0; i < n; i++) {
            int c = value.charAt(i);
            if (c >= 0x0001 && c <= 0x007F) {
                bytes[j++] = (byte) c;
            } else if (c > 0x07FF) {
                bytes[j++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytes[j++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytes[j++] = (byte) (0x80 | (c & 0x3F));
            } else {
                bytes[j++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytes[j++] = (byte) (0x80 | (c & 0x3F));
            }
        }
        return bytes;
    }

    /**
     * * If there is sufficient space in buffer from offset to convert value
     * * without allocating a new byte array, do so now and return the number
     * * of bytes written. Otherwise return -1. This method is intended for
     * * use in optimized string marshalling.
     */
    public static int fromString(String value, byte[] buffer, int offset, int length) {
        int n = value.length(), j = offset;
        for (int i = 0; i < n; i++) {
            if (j + 3 > length) {
                return -1;
            }
            int c = value.charAt(i);
            if (c >= 0x0001 && c <= 0x007F) {
                buffer[j++] = (byte) c;
            } else if (c > 0x07FF) {
                buffer[j++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                buffer[j++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                buffer[j++] = (byte) (0x80 | (c & 0x3F));
            } else {
                buffer[j++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                buffer[j++] = (byte) (0x80 | (c & 0x3F));
            }
        }
        return j - offset;
    }

    public static String toString(byte[] value) {
        return toString(value, 0, value.length);
    }

    public static String toString(byte[] value, int offset, int length) {
        int n = offset + length, j = 0;
        char[] chars = new char[length]; // May be more than we need, but not less
        for (int i = offset; i < n; i++) {
            int c = (value[i] + 256) & 255; // byte is signed, we need unsigned
            int c2, c3;

            switch (c >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    // 0xxx xxxx
                    chars[j++] = (char) c;
                    break;

                case 12:
                case 13:
                    // 110x xxxx  10xx xxxx
                    if (i + 1 >= n) {
                        badUtf8Data();
                    }
                    c2 = (value[++i] + 256) & 255; // byte is signed, we need unsigned
                    if ((c2 & 0xC0) != 0x80) {
                        badUtf8Data();
                    }
                    chars[j++] = (char) (((c & 0x1F) << 6) | (c2 & 0x3F));
                    break;

                case 14:
                    // 1110 xxxx  10xx xxxx  10xx xxxx
                    if (i + 2 >= n) {
                        badUtf8Data();
                    }
                    c2 = (value[++i] + 256) & 255; // byte is signed, we need unsigned
                    c3 = (value[++i] + 256) & 255; // byte is signed, we need unsigned
                    if ((c2 & 0xC0) != 0x80 || (c3 & 0xC0) != 0x80) {
                        badUtf8Data();
                    }
                    chars[j++] = (char) (((c & 0x0F) << 12)
                                         | ((c2 & 0x3F) << 6)
                                         | (c3 & 0x3F));
                    break;

                default:
                    badUtf8Data();
            }
        }
        return new String(chars, 0, j);
    }

    private static void badUtf8Data() {
        throw new org.omg.CORBA.MARSHAL("bad UTF-8 data");
    }
}
