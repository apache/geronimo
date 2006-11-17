/**
 *
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

import java.math.BigInteger;
import java.security.cert.Certificate;

/**
 * Management interface for dealing with a specific Certificate Store
 *
 * @version $Rev$ $Date$
 */
public interface CertificateStore {
    /**
     * This method stores a given certificate.
     * 
     * @param cert Certificate to be stored
     */
    public void storeCertificate(Certificate cert) throws CertificateStoreException;

    /**
     * This method returns a Certificate with a given serial number (if it exists in the store)
     * 
     * @param sNo Serial Number of the certificate to be retrieved.
     */
    public Certificate getCertificate(BigInteger sNo) throws CertificateStoreException;

    /**
     * This method returns base64 encoded certificate with a given serial number (if it exists in the store)
     * 
     * @param sNo Serial Number of the certificate to be retrieved.
     */
    public String getCertificateBase64Text(BigInteger sNo) throws CertificateStoreException;

    /**
     * This method returns the highest certificate serial number in the store.
     */
    public BigInteger getHighestSerialNumber() throws CertificateStoreException;

    /**
     * This method returns the 'highest certificate serial number plus ONE' and increments the highest
     * serial number in the store.
     */
    public BigInteger getNextSerialNumber() throws CertificateStoreException;

    /**
     * This method checks if a certificate with a given serial number exists in the store.
     * 
     * @param sNo Serial number of the certificate to be checked
     */
    public boolean containsCertificate(BigInteger sNo);

    /**
     * This method stores the CA's certificate in the store.
     * @param cert CA's certificate
     */
    public boolean storeCACertificate(Certificate cert) throws CertificateStoreException;

    /**
     * This method returns the CA's certificate stored in the store.
     */
    public Certificate getCACertificate() throws CertificateStoreException;

    /**
     * This method stores the challenge phrase against the specified certificate serial number
     * @param sNo  Serial number of the certificate
     * @param challenge Challenge phrase
     */
    public boolean setCertificateChallenge(BigInteger sNo, String challenge) throws CertificateStoreException;
}
