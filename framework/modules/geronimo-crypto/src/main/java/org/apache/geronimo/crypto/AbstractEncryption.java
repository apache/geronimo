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


package org.apache.geronimo.crypto;

import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

import org.apache.geronimo.crypto.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractEncryption implements Encryption {
    private static final Logger log = LoggerFactory.getLogger(AbstractEncryption.class);
    
    /**
     * Gets a String which contains the Base64-encoded form of the source,
     * encrypted with the key from getSecretKeySpec().
     */
    public String encrypt(Serializable source) {
        SecretKeySpec spec = getSecretKeySpec();
        try {
            Cipher c = Cipher.getInstance(spec.getAlgorithm());
            c.init(Cipher.ENCRYPT_MODE, spec);
            SealedObject so = new SealedObject(source, c);
            ByteArrayOutputStream store = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(store);
            out.writeObject(so);
            out.close();
            byte[] data = store.toByteArray();
            byte[] textData = Base64.encode(data);
            return new String(textData, "US-ASCII");
        } catch (Exception e) {
            log.error("Unable to encrypt", e);
            return null;
        }
    }

    /**
     * Given a String which is the Base64-encoded encrypted data, retrieve
     * the original Object.
     */
    public Serializable decrypt(String source) {
        SecretKeySpec spec = getSecretKeySpec();
        try {
            byte[] data = Base64.decode(source);
            Cipher c = Cipher.getInstance(spec.getAlgorithm());
            c.init(Cipher.DECRYPT_MODE, spec);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
            SealedObject so = (SealedObject) in.readObject();
            return (Serializable) so.getObject(c);
        } catch (Exception e) {
            log.error("Unable to decrypt", e);
            return null;
        }
    }

    protected abstract SecretKeySpec getSecretKeySpec();
}
