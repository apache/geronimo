/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.gbean.annotation;

import org.apache.geronimo.crypto.EncryptionManager;

public enum EncryptionSetting {
    ENCRYPTED {
        public Object encrypt(Object plaintext) {
            if (plaintext == null) return null;
            if (plaintext.equals("")) return "";
            return EncryptionManager.encrypt((String)plaintext);
        }
        public Object decrypt(Object encrypted) {
            if (encrypted == null) return null;
            return EncryptionManager.decrypt((String)encrypted);
        }},
    PLAINTEXT {

        public Object encrypt(Object plaintext) {
            return plaintext;
        }
        public Object decrypt(Object encrypted) {
            return encrypted;
        }},
    // Default is to encrypt attributes whose name contains "password"
    DEFAULT {

        public Object encrypt(Object plaintext) {
            throw new RuntimeException("dont call this");
        }
        public Object decrypt(Object encrypted) {
            throw new RuntimeException("dont call this");
        }};

    public abstract Object encrypt(Object plaintext);

    public abstract Object decrypt(Object encrypted);

    public static EncryptionSetting defaultEncryption(String name, String type) {
        if (name == null) throw new NullPointerException("Name missing");
        if (type == null) throw new NullPointerException("type missing");
        if (!String.class.getName().equals(type)) return PLAINTEXT;
        return (name.toLowerCase().contains("password") || name.toLowerCase().contains("keystorepass") || name.toLowerCase().contains("truststorepass"))? ENCRYPTED: PLAINTEXT;
    }

}
