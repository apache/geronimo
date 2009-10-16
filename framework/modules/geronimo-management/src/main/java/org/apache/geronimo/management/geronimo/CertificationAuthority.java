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
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.apache.geronimo.management.geronimo.CertificationAuthorityException;

/**
 * Management interface for dealing with a specific CertificationAuthority.
 *
 * @version $Rev$ $Date$
 */
public interface CertificationAuthority {

    /**
     * This method checks if the CA is locked.
     * @return true if CA is locked, false otherwise.
     */
    public abstract boolean isLocked();

    /**
     * This method locks the CA.
     */
    public abstract void lock();

    /**
     * This method unlocks the CA.
     * @param password Password to unlock the CA.
     */
    public abstract void unlock(char[] password) throws CertificationAuthorityException;

    /**
     * This method returns CA's name.
     * @throws Exception if CA is locked.
     */
    public abstract X500Principal getName() throws CertificationAuthorityException;

    /**
     * This method returns CA's own certificate.
     * @throws Exception if CA is locked.
     */
    public abstract Certificate getCertificate() throws CertificationAuthorityException;

    /**
     * This method makes the CA issue a self-signed certificate with given details.  This method is usually
     * called while initializing the CA.
     * 
     * @param sNo Serial number for self-signed certificate
     * @param validFromDate Certificate validity period start date
     * @param validToDate Certificate validity period end date
     * @param algorithm Signature algorithm for self-signed certificate
     */
    public abstract void issueOwnCertificate(BigInteger sNo, Date validFromDate, Date validToDate, String algorithm) throws CertificationAuthorityException;

    /**
     * This method issues a certificate.
     * 
     * @param subject Subject name
     * @param publicKey Subject's public key 
     * @param sNo Serial number for the certificate to be issued
     * @param validFromDate Certificate validity period start date
     * @param validToDate Certificate validity period end date
     * @param algorithm Signature algorithm for the certificate
     * @return newly issued certificate
     */
    public abstract Certificate issueCertificate(X500Principal subject, PublicKey publicKey, BigInteger sNo, Date validFromDate, Date validToDate, String algorithm) throws CertificationAuthorityException;

    /**
     * This method returns the highest serial number used by the CA.
     */
    public abstract BigInteger getHighestSerialNumber() throws CertificationAuthorityException;

    /**
     * This method checks if a Certificate with a given serial number is already issued.
     * @param sNo The serial number of the the certificate to be looked for
     * @return true if a certificate with the specified serial number has already been issued
     */
    public abstract boolean isCertificateIssued(BigInteger sNo) throws CertificationAuthorityException;

    /**
     * This method returns the next serial number that can be used to issue a certificate and increments the
     * highest serial number.
     */
    public abstract BigInteger getNextSerialNumber() throws CertificationAuthorityException;

    /**
     * This method retrieves a certificate with the specified serial number.
     * @param sNo The serial number of the certificate to be retrieved
     * @return java.security.cert.Certificate instance of the certificate
     */
    public abstract Certificate getCertificate(BigInteger sNo) throws CertificationAuthorityException;

    /**
     * This method retrieves a certificate with the specified serial number.
     * @param sNo The serial number of the certificate to be retrieved
     * @return base64 encoded certificate text
     */
    public abstract String getCertificateBase64Text(BigInteger sNo) throws CertificationAuthorityException;
}
