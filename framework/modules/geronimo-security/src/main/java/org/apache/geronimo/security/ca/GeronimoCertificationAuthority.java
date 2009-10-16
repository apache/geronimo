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
package org.apache.geronimo.security.ca;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.geronimo.CertificateRequestStore;
import org.apache.geronimo.management.geronimo.CertificateStore;
import org.apache.geronimo.management.geronimo.CertificateStoreException;
import org.apache.geronimo.management.geronimo.CertificationAuthority;
import org.apache.geronimo.management.geronimo.CertificationAuthorityException;
import org.apache.geronimo.management.geronimo.KeystoreException;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.crypto.CaUtils;
import org.apache.geronimo.crypto.asn1.ASN1InputStream;
import org.apache.geronimo.crypto.asn1.DERBitString;
import org.apache.geronimo.crypto.asn1.DEREncodableVector;
import org.apache.geronimo.crypto.asn1.DERInteger;
import org.apache.geronimo.crypto.asn1.DERObject;
import org.apache.geronimo.crypto.asn1.DERSequence;
import org.apache.geronimo.crypto.asn1.pkcs.PKCSObjectIdentifiers;
import org.apache.geronimo.crypto.asn1.x509.AlgorithmIdentifier;
import org.apache.geronimo.crypto.asn1.x509.SubjectPublicKeyInfo;
import org.apache.geronimo.crypto.asn1.x509.TBSCertificateStructure;
import org.apache.geronimo.crypto.asn1.x509.Time;
import org.apache.geronimo.crypto.asn1.x509.V3TBSCertificateGenerator;
import org.apache.geronimo.crypto.asn1.x509.X509Name;
import org.apache.geronimo.security.SecurityNames;

/**
 * A Certification Authority implementation using KeystoreInstance to store CA's private key, 
 * CertificateStore to store issued certificates and CertificateRequestStore to store certificate requests
 *
 * @version $Rev$ $Date$
 */
public class GeronimoCertificationAuthority implements CertificationAuthority, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(GeronimoCertificationAuthority.class);

    private ServerInfo serverInfo;
    private Kernel kernel;
    private AbstractName abstractName;

    // KeystoreInstance with CA's private key and certificate
    private KeystoreInstance caKeystore = null;
    // CertificateStore used to store all certificates issued by the CA
    private CertificateStore certStore = null;
    // Password for CA's keystore and private-key
    private char[] password;
    // CertificateRequestStore used to store certificate requests
    private CertificateRequestStore certReqStore = null;
    
    // Cache variables
    // Key alias
    private String alias;
    // CA's private key
    private PrivateKey caPrivateKey;
    // CA's public key
    private PublicKey caPublicKey;
    // CA's own certificate
    private Certificate caCert;
    // CA's name
    private X509Name caName;
    
    /**
     * Constructor
     * 
     * @param caKeystore KeystoreInstance containing CA's private-key and certificate
     * @param certStore CertificateStore for storing certificates issued by this CA
     * @param certReqStore CeetificateRequestStore for storing certificates requests
     */
    public GeronimoCertificationAuthority(ServerInfo serverInfo, KeystoreInstance caKeystore, CertificateStore certStore, CertificateRequestStore certReqStore, Kernel kernel, AbstractName abstractName) {
        if(caKeystore == null) throw new IllegalArgumentException("caKeystore is null.");
        if(certStore == null) throw new IllegalArgumentException("certStore is null");
        if(certReqStore == null) throw new IllegalArgumentException("certReqStore is null");
        this.serverInfo = serverInfo;
        this.kernel = kernel;
        this.abstractName = abstractName;
        this.caKeystore = caKeystore;
        this.certStore = certStore;
        this.certReqStore = certReqStore;
    }

    /**
     * This method checks if the CA is locked.
     * @return true if CA is locked, false otherwise.
     */
    public boolean isLocked() {
        return password == null;
    }
    
    /**
     * This method locks the CA.
     */
     public void lock() {
        try {
            caKeystore.lockKeystore(password);
        } catch (KeystoreException e) {
            log.error("Error locking CA.", e);
        }
        password = null;
        caName = null;
        caCert = null;
        caPrivateKey = null;
        alias = null;
    }
    
     /**
      * This method unlocks the CA.
      * @param password Password to unlock the CA.
      */
    public void unlock(char[] password) throws CertificationAuthorityException{
        try {
            this.password = password;
            caKeystore.unlockKeystore(password);
            alias = caKeystore.listPrivateKeys(password)[0];
            caKeystore.unlockPrivateKey(alias, password, password);
            caCert = caKeystore.getCertificate(alias, password);
            caName = CaUtils.getSubjectX509Name(caCert);
            caPrivateKey = caKeystore.getPrivateKey(alias, password, password);
            caPublicKey = caCert.getPublicKey();
        } catch(Exception e) {
            throw new CertificationAuthorityException("Errors in unlocking CA.", e);
        }
    }
    
    /**
     * This method returns CA's name.
     * @throws Exception if CA is locked.
     */
    public X500Principal getName() throws CertificationAuthorityException {
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            return new X500Principal(caName.getEncoded());
        } catch (IOException e) {
            throw new CertificationAuthorityException("Error in getting CA name.", e);
        }
    }

    /**
     * This method returns CA's own certificate.
     * @throws Exception if CA is locked.
     */
    public Certificate getCertificate() throws CertificationAuthorityException {
        if(caCert == null) throw new CertificationAuthorityException("CA Certificate is null. CA may be locked.");
        try {
            return caCert = caKeystore.getCertificate(alias, password);
        } catch (KeystoreException e) {
            log.error("Error getting CA's certificate.", e);
        }
        return null;
    }
    
    /**
     * This method makes the CA issue a self-signed certificate with given details.  This method is usually
     * called while initializing the CA.
     * 
     * @param sNo Serial number for self-signed certificate
     * @param validFromDate Certificate validity period start date
     * @param validToDate Certificate validity period end date
     * @param algorithm Signature algorithm for self-signed certificate
     */
    public void issueOwnCertificate(BigInteger sNo, Date validFromDate, Date validToDate, String algorithm) throws CertificationAuthorityException{
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            PublicKey publicKey = caCert.getPublicKey();
            Certificate cert = issueCertificate(getName(), publicKey, sNo, validFromDate, validToDate, algorithm);
            caKeystore.importPKCS7Certificate(alias, CaUtils.base64Certificate(cert), password);
            caCert = cert;
        } catch(Exception e) {
            throw new CertificationAuthorityException("Error in issuing own certificate.", e);
        }
    }
    
    /**
     * This method issues a certificate.
     * 
     * @param subject Subject X500Principal
     * @param publicKey Subject's public key 
     * @param sNo Serial number for the certificate to be issued
     * @param validFromDate Certificate validity period start date
     * @param validToDate Certificate validity period end date
     * @param algorithm Signature algorithm for the certificate
     * @return newly issued certificate
     */
    public Certificate issueCertificate(X500Principal subject, PublicKey publicKey, BigInteger sNo, Date validFromDate, Date validToDate, String algorithm) throws CertificationAuthorityException{
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            X509Name subName = CaUtils.getX509Name(subject);
            Certificate cert = issueCertificate(subName, caName, sNo, publicKey, caPrivateKey, validFromDate, validToDate, algorithm);
            cert.verify(caPublicKey);
            certStore.storeCertificate(cert);
            return cert;
        } catch(Exception e) {
            throw new CertificationAuthorityException("Error in issuing certificate.", e);
        }
    }
    
    /**
     * This method returns the highest serial number used by the CA.
     */
    public BigInteger getHighestSerialNumber() throws CertificationAuthorityException {
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            return certStore.getHighestSerialNumber();
        } catch (CertificateStoreException e) {
            throw new CertificationAuthorityException("Error in getting highest serial number for CA.", e);
        }
    }
    
    /**
     * This method checks if a Certificate with a given serial number is already issued.
     * @param sNo The serial number of the the certificate to be looked for
     * @return true if a certificate with the specified serial number has already been issued
     */
    public boolean isCertificateIssued(BigInteger sNo) throws CertificationAuthorityException {
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        return certStore.containsCertificate(sNo);
    }
    
    /**
     * This method returns the next serial number that can be used to issue a certificate and increments the
     * highest serial number.
     */
    public BigInteger getNextSerialNumber() throws CertificationAuthorityException {
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            return certStore.getNextSerialNumber();
        } catch (CertificateStoreException e) {
            throw new CertificationAuthorityException("Error in getting next serial number for CA.", e);
        }
    }
    
    /**
     * This method retrieves a certificate with the specified serial number.
     * @param sNo The serial number of the certificate to be retrieved
     * @return java.security.cert.Certificate instance of the certificate
     */
    public Certificate getCertificate(BigInteger sNo) throws CertificationAuthorityException {
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            return certStore.getCertificate(sNo);
        } catch (CertificateStoreException e) {
            throw new CertificationAuthorityException("Error getting certificate. serial number = "+sNo, e);
        }
    }

    /**
     * This method retrieves a certificate with the specified serial number.
     * @param sNo The serial number of the certificate to be retrieved
     * @return base64 encoded certificate text
     */
     public String getCertificateBase64Text(BigInteger sNo) throws CertificationAuthorityException {
        if(isLocked()) throw new CertificationAuthorityException("CA is locked.");
        try {
            return certStore.getCertificateBase64Text(sNo);
        } catch (CertificateStoreException e) {
            throw new CertificationAuthorityException("Error getting certificate. serial number = "+sNo, e);
        }
    }
    
    /**
     * This method issues a certificate.
     * @param subName Subject's name
     * @param caName Issuer's name
     * @param serialNum Serial number for the certificate
     * @param subPubKey Subject's public key
     * @param caPriKey Issuer's private key
     * @param validFromDate Certificate validity period start date
     * @param validToDate Certificate validity period end date
     * @param algorithm Signature algorithm for the certificate
     * @return issued certificate
     */
    private Certificate issueCertificate(X509Name subName, X509Name caName, BigInteger serialNum, PublicKey subPubKey, PrivateKey caPriKey, Date validFromDate, Date validToDate, String algorithm) throws Exception {
        AlgorithmIdentifier algId = null;
        if("MD2withRSA".equalsIgnoreCase(algorithm))
            algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.md2WithRSAEncryption);
        else if("MD5withRSA".equalsIgnoreCase(algorithm))
            algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.md5WithRSAEncryption);
        else if("SHA1withRSA".equalsIgnoreCase(algorithm))
            algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption);
        else
            throw new CertificationAuthorityException("Signature algorithm "+algorithm+" is not supported.");
        
        ASN1InputStream ais = new ASN1InputStream(subPubKey.getEncoded());
        DERObject subPubkeyDerObj = ais.readObject();
        SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(subPubkeyDerObj);
        
        // Create certificate generator and initialize fields
        // Certificate version is v3
        V3TBSCertificateGenerator v3certGen = new V3TBSCertificateGenerator();
        // Subject info
        v3certGen.setSubject(subName);
        v3certGen.setSubjectPublicKeyInfo(subPubKeyInfo);
        // Issuer info
        v3certGen.setIssuer(caName);
        // serial number
        v3certGen.setSerialNumber(new DERInteger(serialNum));
        // validity
        v3certGen.setStartDate(new Time(validFromDate));
        v3certGen.setEndDate(new Time(validToDate));
        // signature algorithm
        v3certGen.setSignature(algId);
        
        // Get the certificate info to be signed
        TBSCertificateStructure tbsCert = v3certGen.generateTBSCertificate();
        byte[] tobesigned = tbsCert.getEncoded();
        
        // Create the signature
        Signature signatureObj = Signature.getInstance(algorithm);
        signatureObj.initSign(caPriKey);
        signatureObj.update(tobesigned);
        byte[] signature = signatureObj.sign();
        
        // Compose tbsCert, algId and signature into a DER sequence.
        // This will be the certificate in DER encoded form
        DEREncodableVector certDerVec = new DEREncodableVector();
        certDerVec.add(tbsCert);
        certDerVec.add(algId);
        certDerVec.add(new DERBitString(signature));
        DERSequence certDerSeq = new DERSequence(certDerVec);
        byte[] certData = certDerSeq.getEncoded();
        
        // Create a java.security.cert.Certificate object
        Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(certData));

        return certificate;
    }

    public void doFail() {
    }

    public void doStart() throws Exception {
        if(caKeystore.isKeystoreLocked()) {
            lock();
        }
    }

    public void doStop() throws Exception {
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(GeronimoCertificationAuthority.class, SecurityNames.CERTIFICATION_AUTHORITY);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addReference("KeystoreInstance", KeystoreInstance.class, SecurityNames.KEYSTORE_INSTANCE);
        infoFactory.addReference("CertificateStore", CertificateStore.class, "CertificateStore");
        infoFactory.addReference("CertificateRequestStore", CertificateRequestStore.class, "CertificateRequestStore");
        infoFactory.addInterface(CertificationAuthority.class);
        infoFactory.setConstructor(new String[]{"ServerInfo", "KeystoreInstance", "CertificateStore", "CertificateRequestStore", "kernel", "abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
