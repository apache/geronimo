/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.security.realm.providers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mx4j.util.Base64Codec;


/**
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:59:26 $
 */

public class SQLSecurityRealmPasswordDigested extends SQLSecurityRealm {
    String algorithm;
    MessageDigest digest;

    public SQLSecurityRealmPasswordDigested() {
        setAlgorithm("MD5");
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
        }
    }

    String obfuscate(String password) {
        if (digest != null) {
            byte[] digestedBytes = digest.digest(password.getBytes());
            byte[] obfuscatedBytes = Base64Codec.encodeBase64(digestedBytes);
            password = new String(obfuscatedBytes);
        }
        return password;
    }
}
