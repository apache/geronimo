/**
 *
 * Copyright 2005 The Apache Software Foundation
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.util;

import java.io.Serializable;

/**
 * A static class that handles storing and reading values, potentially using
 * encryption.  This can be used as the interface to any back-end encryption
 * services.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class EncryptionManager {
    private final static String SIMPLE_ENCRYPTION_PREFIX = "{Simple}";

    /**
     * Gets a String which contains the Base64-encoded form of the
     * encrypted form of the source.
     */
    public static String encrypt(Serializable source) {
        return SIMPLE_ENCRYPTION_PREFIX +SimpleEncryption.encrypt(source);
    }

    /**
     * Given a String which is the Base64-encoded encrypted data, retrieve
     * the original Object.
     */
    public static Object decrypt(String source) {
        if(source.startsWith(SIMPLE_ENCRYPTION_PREFIX)) {
            return SimpleEncryption.decrypt(source.substring(SIMPLE_ENCRYPTION_PREFIX.length()));
        }
        return source;
    }
}
