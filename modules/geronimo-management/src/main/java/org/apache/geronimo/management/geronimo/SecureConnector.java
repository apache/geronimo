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
package org.apache.geronimo.management.geronimo;

/**
 * Common configuration settings for connectors that use SSL/TLS to conduct
 * secure communications with clients.
 *
 * http://jakarta.apache.org/tomcat/tomcat-5.5-doc/ssl-howto.html
 * http://mortbay.org/javadoc/org/mortbay/http/SslListener.html
 * 
 * @version $Rev$ $Date$
 */
public interface SecureConnector extends WebConnector {
    public final static String KEYSTORE_TYPE_JKS = "JKS";
    public final static String KEYSTORE_TYPE_PKCS12 = "PKCS12";
    public final static String ALGORITHM_TYPE_SUN = "SunX509";
    public final static String ALGORITHM_TYPE_IBM = "IbmX509";
    public final static String SECURE_PROTOCOL_TYPE_TLS = "TLS";
    public final static String SECURE_PROTOCOL_TYPE_SSL = "SSL";

    /**
     * Gets the name of the keystore file that holds the server certificate
     * (and by default, the trusted CA certificates used for client certificate
     * authentication).  This is relative to the Geronimo home directory.
     */
    public String getKeystoreFileName();
    /**
     * Sets the name of the keystore file that holds the server certificate
     * (and by default, the trusted CA certificates used for client certificate
     * authentication).  This is relative to the Geronimo home directory.
     */
    public void setKeystoreFileName(String name);
    /**
     * Sets the password used to access the keystore, and by default, used to
     * access the server private key inside the keystore.  Not all connectors
     * support configuring different passwords for those two features; if so,
     * a separate PrivateKeyPassword should be defined in an
     * implementation-specific connector interface.
     */
    public void setKeystorePassword(String password);
    /**
     * Gets the format of the entries in the keystore.  The default format for
     * Java keystores is JKS, though some connector implementations support
     * PCKS12 (and possibly other formats).
     */
    public String getKeystoreType();
    /**
     * Sets the format of the entries in the keystore.  The default format for
     * Java keystores is JKS, though some connector implementations support
     * PCKS12 (and possibly other formats).
     */
    public void setKeystoreType(String type);
    /**
     * Gets the certificate algorithm used to access the keystore.  This may
     * be different for different JVM vendors, but should not usually be
     * changed otherwise.
     */
    public String getAlgorithm();
    /**
     * Sets the certificate algorithm used to access the keystore.  This may
     * be different for different JVM vendors, but should not usually be
     * changed otherwise.
     */
    public void setAlgorithm(String algorithm);
    /**
     * Gets the protocol used for secure communication.  This should usually
     * be TLS, though some JVM implementations (particularly some of IBM's)
     * may not be compatible with popular browsers unless this is changed to
     * SSL.
     */
    public String getSecureProtocol();
    /**
     * Gets the protocol used for secure communication.  This should usually
     * be TLS, though some JVM implementations (particularly some of IBM's)
     * may not be compatible with popular browsers unless this is changed to
     * SSL.  Don't change it if you're not having problems.
     */
    public void setSecureProtocol(String protocol);
    /**
     * Checks whether clients are required to authenticate using client
     * certificates in order to connect using this connector.  If enabled,
     * client certificates are validated using the trust store, which defaults
     * to the same keystore file, keystore type, and keystore password as the
     * regular keystore.  Some connector implementations may allow you to
     * configure those 3 values separately to use a different trust store.
     *
     * todo: confirm that Jetty defaults to keystore not JVM default trust store
     */
    public boolean isClientAuthRequired();
    /**
     * Checks whether clients are required to authenticate using client
     * certificates in order to connect using this connector.  If enabled,
     * client certificates are validated using the trust store, which defaults
     * to the same keystore file, keystore type, and keystore password as the
     * regular keystore.  Some connector implementations may allow you to
     * configure those 3 values separately to use a different trust store.
     *
     * todo: confirm that Jetty defaults to keystore not JVM default trust store
     */
    public void setClientAuthRequired(boolean clientCert);

    // Jetty: integral/confidential separation
    // Tomcat: trust keystore, trust password, trust keystore type, ciphers
}
