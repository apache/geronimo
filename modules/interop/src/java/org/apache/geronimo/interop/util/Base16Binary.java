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

public class Base16Binary {
    /**
     * * Convert from hexadecimal string to byte array.
     */
    public static byte[] fromString(String string) {
        return fromString(string, 0, string.length());
    }

    /**
     * * Convert from hexadecimal string to byte array.
     */
    public static byte[] fromString(String string, int offset, int length) {
        byte[] bytes = new byte[length / 2];
        for (int j = 0, k = 0; k < length; j++, k += 2) {
            int hi = Character.digit(string.charAt(offset + k), 16);
            int lo = Character.digit(string.charAt(offset + k + 1), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException(string);
            }
            bytes[j] = (byte) (16 * hi + lo);
        }
        return bytes;
    }

    /**
     * * Convert from byte array to hexadecimal string.
     */
    public static String toString(byte[] bytes) {
        return toString(bytes, 0, bytes.length);
    }

    /**
     * * Convert from byte array to hexadecimal string.
     */
    public static String toString(byte[] bytes, int offset, int length) {
        char[] chars = new char[length * 2];
        for (int j = 0, k = 0; j < length; j++, k += 2) {
            int value = (bytes[offset + j] + 256) & 255;
            chars[k] = Character.forDigit(value >> 4, 16);
            chars[k + 1] = Character.forDigit(value & 15, 16);
        }
        return new String(chars);
    }
}
