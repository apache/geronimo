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
package org.apache.geronimo.tomcat;

import org.apache.geronimo.management.geronimo.SecureConnector;

/**
 * @version $Rev$ $Date$
 */
public interface TomcatSecureConnector extends SecureConnector {
    /**
     * Gets the name of the keystore file that holds the trusted CA certificates
     * used for client certificate authentication.
     * This is relative to the Geronimo home directory.
     */
    public String getTruststoreFileName();
    /**
     * Sets the name of the keystore file that holds the trusted CA certificates
     * used for client certificate authentication.
     * This is relative to the Geronimo home directory.
     */
    public void setTruststoreFileName(String name);
    /**
     * Sets the password used to verify integrity of truststore.
     */
    public void setTruststorePassword(String password);
    /**
     * Gets the format of the entries in the keystore.  The default format for
     * Java keystores is JKS, though some connector implementations support
     * PCKS12 (and possibly other formats).
     */
    public String getTruststoreType();
    /**
     * Sets the format of the entries in the keystore.  The default format for
     * Java keystores is JKS, though some connector implementations support
     * PCKS12 (and possibly other formats).
     */
    public void setTruststoreType(String type);

    /**
     * Gets a comma seperated list of the encryption ciphers that may be used. If not
     * specified, then any available cipher may be used.
     */
    public String getCiphers();

    /**
     * Sets a comma seperated list of the encryption ciphers that may be used. If not
     * specified, then any available cipher may be used.
     */
    public void setCiphers(String ciphers);

    /**
     * Sets a keyAlias if one is being used
     * @param keyAlias
     */
    public void setKeyAlias(String keyAlias);

    /**
     * Gets the key alias
     * @return key alias
     */
    public String getKeyAlias();
}
