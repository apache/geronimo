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
package org.apache.geronimo.security.keystore;

import javax.net.ServerSocketFactory;

/**
 * Management interface for working with keystores.  Mostly this is used to
 * identify KeystoreInstances to work with individual keystores.
 *
 * @see KeystoreInstance
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface KeystoreManager {
    /**
     * Gets the names of the keystores available in the server.
     */
    public String[] listKeystores();

    /**
     * Gets a specific keystore instance by name
     * @param name The name as provided by listKeystores
     */
    public KeystoreInstance getKeystore(String name);

    /**
     * Gets a ServerSocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     * @param keyStore The key keystore name as provided by listKeystores.  The
     *                 KeystoreInstance for this keystore must be unlocked.
     * @param keyAlias The name of the private key in the keystore.  The
     *                 KeystoreInstance for this keystore must have unlocked
     *                 this key.
     * @param trustStore The trust keystore name as provided by listKeystores.
     *                   The KeystoreInstance for this keystore must have
     *                   unlocked this key.
     *
     * @throws KeystoreIsLocked Occurs when the requested key keystore cannot
     *                          be used because it has not been unlocked.
     * @throws KeyIsLocked Occurs when the requested private key in the key
     *                     keystore cannot be used because it has not been
     *                     unlocked.
     */
    public ServerSocketFactory createSSLFactory(String keyStore, String keyAlias, String trustStore)
            throws KeystoreIsLocked, KeyIsLocked;

    /**
     * Creates a new, empty keystore.  The name should be a valid file name
     * with no path separator characters.
     *
     * @param name The name of the keystore to create
     * @param password The password to use to protect the new keystore
     */
    public KeystoreInstance createKeystore(String name, char[] password);
}
