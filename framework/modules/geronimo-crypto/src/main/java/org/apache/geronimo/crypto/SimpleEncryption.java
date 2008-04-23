/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.crypto;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import org.apache.geronimo.crypto.encoders.Base64;

/**
 * This class protects some value BY ENCRYPTING WITH A KNOWN KEY.  That is
 * to say, it's only safe against anyone who can't read the source code.
 * So the main idea is to protect against casual observers.
 *
 * If someone has a better idea for how to implement encryption with a
 * non-obvious key that the user isn't likely to change during the normal
 * course of working with the server, I'd be happy to hear it.  (But I
 * assume the SSL keystore is likely to be changed, which would result
 * in losing all the "encrypted" data.
 *
 * @version $Rev$ $Date$
 */
public final class SimpleEncryption extends AbstractEncryption {

    public final static SimpleEncryption INSTANCE = new SimpleEncryption();
    
    private final static SecretKeySpec SECRET_KEY = new SecretKeySpec(new byte[]{(byte)-45,(byte)-15,(byte)100,(byte)-34,(byte)70,(byte)83,(byte)75,(byte)-100,(byte)-75,(byte)61,(byte)26,(byte)114,(byte)-20,(byte)-58,(byte)114,(byte)77}, "AES");

    private SimpleEncryption() {
    }

    protected SecretKeySpec getSecretKeySpec() {
        return SECRET_KEY;
    }
}
