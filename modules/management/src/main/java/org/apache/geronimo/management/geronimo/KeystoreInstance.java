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
package org.apache.geronimo.management.geronimo;

import java.security.cert.Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.KeyStoreException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * Management interface for dealing with a specific Keystore
 *
 * @version $Rev$ $Date$
 */
public interface KeystoreInstance {
    /**
     * Returns the name of the keystore as known to the keystore manager.
     */
    public String getKeystoreName();

    /**
     * Saves a password to access the keystore as a whole.  This means that any
     * other server component can use this keystore to create a socket factory.
     * However, the relevant private key in the keystore must also be unlocked.
     *
     * @return True if the keystore was unlocked successfully
     */
    public boolean unlockKeystore(char[] password);

    /**
     * Clears any saved password, meaning this keystore cannot be used by other
     * server components.  You can still query and update it by passing the
     * password to other functions,
     */
    public void lockKeystore();

    /**
     * Checks whether this keystore is unlocked, which is to say, available for
     * other components to use to generate socket factories.
     * Does not check whether the unlock password is actually correct.
     */
    public boolean isKeystoreLocked();

    /**
     * Gets the aliases of all private key entries in the keystore
     *
     * @param storePassword Used to open the keystore.
     */
    public String[] listPrivateKeys(char[] storePassword);

    /**
     * Saves a password to access a private key.  This means that if the
     * keystore is also unlocked, any server component can create an SSL
     * socket factory using this private key.  Note that the keystore
     * must be unlocked before this can be called.
     *
     * @param password The password to save.
     * @return True if the key was unlocked successfully
     */
    public boolean unlockPrivateKey(String alias, char[] password) throws KeystoreIsLocked;

    /**
     * Gets the aliases for all the private keys that are currently unlocked.
     * This only works if the keystore is unlocked.
     */
    public String[] getUnlockedKeys() throws KeystoreIsLocked;

    /**
     * Checks whether this keystore can be used as a trust store (e.g. has at
     * least one trust certificate).  This only works if the keystore is
     * unlocked.
     */
    public boolean isTrustStore() throws KeystoreIsLocked;

    /**
     * Clears any saved password for the specified private key, meaning this
     * key cannot be used for a socket factory by other server components.
     * You can still query and update it by passing the password to other
     * functions,
     */
    public void lockPrivateKey(String alias);

    /**
     * Checks whether the specified private key is locked, which is to say,
     * available for other components to use to generate socket factories.
     * Does not check whether the unlock password is actually correct.
     */
    public boolean isKeyLocked(String alias);

    /**
     * Gets the aliases of all trusted certificate entries in the keystore.
     *
     * @param storePassword Used to open the keystore.
     */
    public String[] listTrustCertificates(char[] storePassword);

    /**
     * Gets a particular certificate from the keystore.  This may be a trust
     * certificate or the certificate corresponding to a particular private
     * key.
     * @param alias The certificate to look at
     * @param storePassword The password to use to access the keystore
     */
    public Certificate getCertificate(String alias, char[] storePassword);

    /**
     * Adds a certificate to this keystore as a trusted certificate.
     * @param cert The certificate to add
     * @param alias The alias to list the certificate under
     * @param storePassword The password for the keystore
     * @return True if the certificate was imported successfully
     */
    public boolean importTrustCertificate(Certificate cert, String alias, char[] storePassword);

    /**
     * Generates a new private key and certificate pair in this keystore.
     * @param alias The alias to store the new key pair under
     * @param storePassword The password used to access the keystore
     * @param keyPassword The password to use to protect the new key
     * @param keyAlgorithm The algorithm used for the key (e.g. RSA)
     * @param keySize The number of bits in the key (e.g. 1024)
     * @param signatureAlgorithm The algorithm used to sign the key (e.g. MD5withRSA)
     * @param validity The number of days the certificate should be valid for
     * @param commonName The CN portion of the identity on the certificate
     * @param orgUnit The OU portion of the identity on the certificate
     * @param organization The O portion of the identity on the certificate
     * @param locality The L portion of the identity on the certificate
     * @param state The ST portion of the identity on the certificate
     * @param country The C portion of the identity on the certificate
     * @return True if the key was generated successfully
     */
    public boolean generateKeyPair(String alias, char[] storePassword, char[] keyPassword, String keyAlgorithm, int keySize,
                                   String signatureAlgorithm, int validity, String commonName, String orgUnit,
                                   String organization, String locality, String state, String country);


    /**
     * Gets a KeyManager for a key in this Keystore.  This only works if both
     * the keystore and the private key in question have been unlocked,
     * allowing other components in the server to access them.
     * @param algorithm The SSL algorithm to use for this key manager
     * @param alias     The alias of the key to use in the keystore
     */
    public KeyManager[] getKeyManager(String algorithm, String alias) throws NoSuchAlgorithmException,
            UnrecoverableKeyException, KeyStoreException, KeystoreIsLocked;

    /**
     * Gets a TrustManager for this keystore.  This only works if the keystore
     * has been unlocked, allowing other components in the server to access it.
     * @param algorithm The SSL algorithm to use for this trust manager
     */
    public TrustManager[] getTrustManager(String algorithm) throws KeyStoreException, NoSuchAlgorithmException, KeystoreIsLocked;
    
    public String generateCSR(String alias);
    
    public void importPKCS7Certificate(String alias, String certbuf)
    throws java.security.cert.CertificateException,
    java.security.NoSuchProviderException,
    java.security.KeyStoreException,
    java.security.NoSuchAlgorithmException,
    java.security.UnrecoverableKeyException, java.io.IOException;
    
    public void deleteEntry(String alias);
}
