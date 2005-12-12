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
package org.apache.geronimo.interop.rmi.iiop;

import java.util.Random;

import org.apache.geronimo.interop.util.BigEndian;
import org.apache.geronimo.interop.util.UTF8;


public class SecurityInfo {
    // public data

    public static final int TAG_USERNAME = BigEndian.getInt(new byte[]
    {
        (byte) 'U', (byte) 'S', (byte) 'E', (byte) 'R'
    });

    public static final int TAG_PASSWORD = BigEndian.getInt(new byte[]
    {
        (byte) 'P', (byte) 'A', (byte) 'S', (byte) 'S'
    });

    public static Random        seedFactory = new Random();
    public String               username;
    public String               password;

    private static ThreadLocal  current = new ThreadLocal();

    public static SecurityInfo getCurrent() {
        return (SecurityInfo) current.get();
    }

    public static void setCurrent(SecurityInfo info) {
        current.set(info);
    }

    /**
     * * Encode a username or password to prevent accidental disclosure
     * * by packet sniffers etc. The intention is not to provide strong
     * * encryption, SSL should be used for that.
     * *
     * * Note: this algorithm is not to be changed, or it will cause
     * * version incompatibilites between client and server. See also
     * * similar requirements in Random.java.
     */
    public static byte[] encode(String plainText) {
        int seed = seedFactory.nextInt(); // data race, but we don't care
        Random random = new Random(seed);
        byte[] utf8 = UTF8.fromString(plainText);
        int n = utf8.length;
        int pad = 0;
        // Try to mask password length by padding to 4 byte boundaries.
        while ((1 + n + pad) % 4 != 0) {
            pad++;
        }
        byte[] data = new byte[6 + n + pad];
        data[0] = (byte) 'E'; // Can be overwritten by caller after return.
        BigEndian.setInt(data, 1, seed);
        data[5] = (byte) (pad + random.nextInt());
        for (int i = 0; i < n + pad; i++) {
            if (i < n) {
                data[6 + i] = (byte) (utf8[i] + random.nextInt());
            } else {
                data[6 + i] = (byte) random.nextInt(); // random padding.
            }
        }
        return data;
    }

    /**
     * * Inverse of encode.
     */
    public static String decode(byte[] data) {
        int n = data.length - 6;
        if (n < 0) {
            throw new IllegalArgumentException("data.length = " + data.length);
        }
        int seed = BigEndian.getInt(data, 1);
        Random random = new Random(seed);
        int pad = ((data[5] - random.nextInt()) + 0x100) & 0xff;
        if (pad < 0 || pad > 3) {
            throw new IllegalArgumentException("pad = " + pad);
        }
        n -= pad;
        byte[] utf8 = new byte[n];
        for (int i = 0; i < n; i++) {
            utf8[i] = (byte) (data[i + 6] - random.nextInt());
        }
        String plainText = UTF8.toString(utf8);
        return plainText;
    }
}
