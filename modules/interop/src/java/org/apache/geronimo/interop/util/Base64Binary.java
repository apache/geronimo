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

public class Base64Binary {
    /**
     * * Convert from byte array to base 64 encoded string.
     */
    public static String toString(byte[] bytes) {
        return toString(bytes, 0, bytes.length);
    }

    public static String toString(byte[] bytes, int offset, int length) {
        StringBuffer s = new StringBuffer((length * 4) / 3 + 1);
        int n = offset + length;
        for (int i = offset; i < n; i += 3) {
            /* TODO: make this optional???
            if (i != 0 && i % 18 == 0)
            {
                // Must have at most 76 characters per line.
                s.append('\n');
            }
            */
            int value;
            int chars;
            if (i < n - 2) {
                value = (0x00FF0000 & (bytes[i] << 16))
                        | (0x0000FF00 & (bytes[i + 1] << 8))
                        | (0x000000FF & bytes[i + 2]);
                chars = 4;
            } else if (i < n - 1) {
                value = (0x00FF0000 & (bytes[i] << 16))
                        | (0x0000FF00 & (bytes[i + 1] << 8));
                chars = 3;
            } else {
                value = (0x00FF0000 & (bytes[i] << 16));
                chars = 2;
            }
            while (chars-- > 0) {
                int x = (0x00FC0000 & value) >> 18;
                char c = getChar(x);
                s.append(c);
                value = value << 6;
            }
            if (i == n - 1) {
                s.append("==");
            } else if (i == n - 2) {
                s.append('=');
            }
        }
        return s.toString();
    }

    private static char getChar(int c) {
        if (c < 26) {
            return (char) ('A' + c);
        } else if (c < 52) {
            return (char) ('a' + (c - 26));
        } else if (c < 62) {
            return (char) ('0' + (c - 52));
        } else if (c == 62) {
            return '+';
        } else // c == 63
        {
            return '/';
        }
    }
}
