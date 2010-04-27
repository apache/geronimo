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

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;

/**
 * Management interface for working with keystores.  Mostly this is used to
 * identify KeystoreInstances to work with individual keystores.
 *
 * @see KeystoreInstance
 *
 * @version $Rev$ $Date$
 */
public interface KeystoreManager {
    /**
     * Dummy method to add the keystore created through Java provided
     * Keytool.exe as gbeans in geronimo.
     */
    public void initializeKeystores();
    
    /**
     * Gets the names of the keystores available in the server.
     */
    public KeystoreInstance[] getKeystores();

    /**
     * Gets a ServerSocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     * @param provider The SSL provider to use, or null for the default
     * @param protocol The SSL protocol to use
     * @param algorithm The SSL algorithm to use
     * @param keyStore The key keystore name as provided by listKeystores.  The
     *                 KeystoreInstance for this keystore must be unlocked.
     * @param keyAlias The name of the private key in the keystore.  The
     *                 KeystoreInstance for this keystore must have unlocked
     *                 this key.
     * @param trustStore The trust keystore name as provided by listKeystores.
     *                   The KeystoreInstance for this keystore must have
     *                   unlocked this key.
     * @param loader     The class loader used to resolve factory classes.
     *
     * @throws KeystoreIsLocked Occurs when the requested key keystore cannot
     *                          be used because it has not been unlocked.
     * @throws KeyIsLocked Occurs when the requested private key in the key
     *                     keystore cannot be used because it has not been
     *                     unlocked.
     */
    public SSLServerSocketFactory createSSLServerFactory(String provider, String protocol, String algorithm,
                                                   String keyStore, String keyAlias, String trustStore, ClassLoader loader)
            throws KeystoreException;


    /**
     * Gets a SocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     * @param provider The SSL provider to use, or null for the default
     * @param protocol The SSL protocol to use
     * @param algorithm The SSL algorithm to use
     * @param keyStore The key keystore name as provided by listKeystores.  The
     *                 KeystoreInstance for this keystore must be unlocked.
     * @param keyAlias The name of the private key in the keystore.  The
     *                 KeystoreInstance for this keystore must have unlocked
     *                 this key.
     * @param trustStore The trust keystore name as provided by listKeystores.
     *                   The KeystoreInstance for this keystore must have
     *                   unlocked this key.
     * @param loader     The class loader used to resolve factory classes.
     *
     * @throws KeystoreIsLocked Occurs when the requested key keystore cannot
     *                          be used because it has not been unlocked.
     * @throws KeyIsLocked Occurs when the requested private key in the key
     *                     keystore cannot be used because it has not been
     *                     unlocked.
     */
    public SSLSocketFactory createSSLFactory(String provider, String protocol, String algorithm,
                                                   String keyStore, String keyAlias, String trustStore, ClassLoader loader)
            throws KeystoreException;


    /**
     * Gets a SocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     * @param provider The SSL provider to use, or null for the default
     * @param protocol The SSL protocol to use
     * @param algorithm The SSL algorithm to use
     * @param trustStore The trust keystore name as provided by listKeystores.
     *                   The KeystoreInstance for this keystore must have
     *                   unlocked this key.
     * @param loader     The class loader used to resolve factory classes.
     *
     * @throws KeystoreIsLocked Occurs when the requested key keystore cannot
     *                          be used because it has not been unlocked.
     * @throws KeyIsLocked Occurs when the requested private key in the key
     *                     keystore cannot be used because it has not been
     *                     unlocked.
     */
    public SSLSocketFactory createSSLFactory(String provider, String protocol, String algorithm,
                                                   String trustStore, ClassLoader loader)
            throws KeystoreException;

    /**
     * Creates a new, empty keystore.  The name should be a valid file name
     * with no path separator characters.
     *
     * @param name The name of the keystore to create
     * @param password The password to use to protect the new keystore
     * @param type The keystore type
     */
    public KeystoreInstance createKeystore(String name, char[] password, String type) throws KeystoreException;

    /**
     * Gets the aliases for any keystores that are available to be used as
     * private key keystores for an SSL factory.  This means the keystore is
     * unlocked and contains at least one private key that's unlocked.
     */
    public KeystoreInstance[] getUnlockedKeyStores();

    /**
     * Gets the aliases for any keystores that are available to be used as
     * trusted certificate keystores for an SSL factory.  This means the
     * keystore is unlocked and contains at least one trust certificate.
     */
    public KeystoreInstance[] getUnlockedTrustStores();

    SSLContext createSSLContext(String provider, String protocol, String algorithm, String keyStore, String keyAlias, String trustStore, ClassLoader loader) throws KeystoreException;
}
