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

package org.apache.geronimo.console.core.keystore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.geronimo.crypto.asn1.ASN1Set;
import org.apache.geronimo.crypto.asn1.DEROutputStream;
import org.apache.geronimo.crypto.asn1.x509.X509Name;
import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.crypto.jce.PKCS10CertificationRequest;
import org.apache.geronimo.crypto.jce.X509Principal;
import org.apache.geronimo.crypto.jce.X509V1CertificateGenerator;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreGBean implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(KeyStoreGBean.class);

    private String keyStoreType;

    private String keyStoreProvider;

    private String keyStoreLocation;

    private String keyStorePassword;

    private String keyPassword;

    private KeyStore keystore;

    // Used to resolve keystore path.
    private ServerInfo serverInfo;

    public KeyStoreGBean() {
    }

    public void doStart() throws WaitingException, Exception {

        //Security.addProvider(new BouncyCastleProvider());

        this.keystore = KeyStore.getInstance(keyStoreType);

        boolean keystoreExistsFlag = true;
        InputStream is = null;

        try {
            File keyStore = serverInfo.resolveServer(this.keyStoreLocation);
            log.debug("loading keystore from " + keyStore);
            is = new java.io.FileInputStream(keyStore);
            this.keystore.load(is, this.keyStorePassword.toCharArray());
        } catch (java.io.FileNotFoundException e) {
            keystoreExistsFlag = false;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
            }
        }

        if (keystoreExistsFlag == false) {
            keystore.load(null, keyStorePassword.toCharArray());
        }
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getKeyStoreType() {
        return this.keyStoreType;
    }

    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    public String getKeyStoreProvider() {
        return this.keyStoreProvider;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getKeyStoreLocation() {
        return this.keyStoreLocation;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStorePassword() {
        return this.keyStorePassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    public String getKeyPassword() {
        return this.keyPassword;
    }

    public int getKeyStoreSize() throws KeyStoreException {
        return this.keystore.size();
    }

    public KeyEntryInfo getKeyEntryInfo(String alias) throws KeyStoreException {
        KeyEntryInfo info = null;

        if (this.keystore.isCertificateEntry(alias)) {
            // certificate entry
            info = new KeyEntryInfo(alias, "trusted certificate", keystore
                    .getCreationDate(alias));
        } else if (this.keystore.isKeyEntry(alias)) {
            // private key entry
            info = new KeyEntryInfo(alias, "private key", keystore
                    .getCreationDate(alias));
        } else {
            throw new KeyStoreException("invalid key entry type");
        }
        return info;
    }

    public List getKeyStoreEntries() throws KeyStoreException {
        List list = new ArrayList();

        Enumeration aliases = this.keystore.aliases();

        while (aliases.hasMoreElements()) {
            String alias = (String) aliases.nextElement();
            list.add(getKeyEntryInfo(alias));
        }
        return list;
    }

    public Certificate[] getCertificateChain(String alias)
            throws KeyStoreException {
        Certificate[] certs = null;

        if (keystore.isCertificateEntry(alias)) {
            Certificate cert = keystore.getCertificate(alias);
            certs = new Certificate[1];
            certs[0] = cert;
        } else if (keystore.isKeyEntry(alias)) {
            certs = keystore.getCertificateChain(alias);
        } else if (keystore.containsAlias(alias)) {
            throw new KeyStoreException("Unsupported key-store-entry, alias = "
                    + alias);
        } else {
            throw new KeyStoreException(
                    "Key-store-entry alias not found, alias = " + alias);
        }

        return certs;
    }

    public String generateCSR(String alias) throws Exception {

        // find certificate by alias
        X509Certificate cert = (X509Certificate) keystore.getCertificate(alias);

        // find private key by alias
        PrivateKey key = (PrivateKey) keystore.getKey(alias, keyPassword.toCharArray());

        // generate csr
        String csr = generateCSR(cert, key);
        return csr;
    }

    public String generateCSR(X509Certificate cert, PrivateKey signingKey)
            throws Exception {

        String sigalg = cert.getSigAlgName();
        X509Name subject = new X509Name(cert.getSubjectDN().toString());
        PublicKey publicKey = cert.getPublicKey();
        ASN1Set attributes = null;

        PKCS10CertificationRequest csr = new PKCS10CertificationRequest(sigalg,
                subject, publicKey, attributes, signingKey);

        if (!csr.verify()) {
            throw new KeyStoreException("CSR verification failed");
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DEROutputStream deros = new DEROutputStream(os);
        deros.writeObject(csr.getDERObject());
        String b64 = new String(Base64.encode(os.toByteArray()));

        final String BEGIN_CERT_REQ = "-----BEGIN CERTIFICATE REQUEST-----";
        final String END_CERT_REQ = "-----END CERTIFICATE REQUEST-----";
        final int CERT_REQ_LINE_LENGTH = 70;

        StringBuilder sbuf = new StringBuilder(BEGIN_CERT_REQ).append('\n');

        int idx = 0;
        while (idx < b64.length()) {

            int len = (idx + CERT_REQ_LINE_LENGTH > b64.length()) ? b64
                    .length()
                    - idx : CERT_REQ_LINE_LENGTH;

            String chunk = b64.substring(idx, idx + len);

            sbuf.append(chunk).append('\n');
            idx += len;
        }

        sbuf.append(END_CERT_REQ);
        return sbuf.toString();
    }

    public void generateKeyPair(String alias, String keyalg, Integer keysize,
            String sigalg, Integer validity, String cn, String ou, String o,
            String l, String st, String c)
            throws java.security.NoSuchAlgorithmException,
            java.security.KeyStoreException, java.security.SignatureException,
            java.security.InvalidKeyException,
            java.security.cert.CertificateException, java.io.IOException {

        KeyPairGenerator kpgen = KeyPairGenerator.getInstance(keyalg);

        kpgen.initialize(keysize.intValue());

        KeyPair keyPair = kpgen.generateKeyPair();

        X509Certificate cert = generateCert(keyPair.getPublic(), keyPair
                .getPrivate(), sigalg, validity.intValue(), cn, ou, o, l, st, c);

        keystore.setKeyEntry(alias, keyPair.getPrivate(), keyPassword.toCharArray(), new Certificate[] { cert });

        saveKeyStore();
    }

    public void saveKeyStore() throws java.io.IOException,
            java.security.KeyStoreException,
            java.security.cert.CertificateException,
            java.security.NoSuchAlgorithmException {

        FileOutputStream os = null;

        try {
            File keyStore = serverInfo.resolveServer(this.keyStoreLocation);
            os = new FileOutputStream(keyStore);

            keystore.store(os, keyStorePassword.toCharArray());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public X509Certificate generateCert(PublicKey publicKey,
            PrivateKey privateKey, String sigalg, int validity, String cn,
            String ou, String o, String l, String st, String c)
            throws java.security.SignatureException,
            java.security.InvalidKeyException {
        X509V1CertificateGenerator certgen = new X509V1CertificateGenerator();

        // issuer dn
        Vector order = new Vector();
        Hashtable attrmap = new Hashtable();

        if (cn != null) {
            attrmap.put(X509Principal.CN, cn);
            order.add(X509Principal.CN);
        }

        if (ou != null) {
            attrmap.put(X509Principal.OU, ou);
            order.add(X509Principal.OU);
        }

        if (o != null) {
            attrmap.put(X509Principal.O, o);
            order.add(X509Principal.O);
        }

        if (l != null) {
            attrmap.put(X509Principal.L, l);
            order.add(X509Principal.L);
        }

        if (st != null) {
            attrmap.put(X509Principal.ST, st);
            order.add(X509Principal.ST);
        }

        if (c != null) {
            attrmap.put(X509Principal.C, c);
            order.add(X509Principal.C);
        }

        X509Principal issuerDN = new X509Principal(order, attrmap);
        certgen.setIssuerDN(issuerDN);

        // validity
        long curr = System.currentTimeMillis();
        long untill = curr + (long) validity * 24 * 60 * 60 * 1000;

        certgen.setNotBefore(new Date(curr));
        certgen.setNotAfter(new Date(untill));

        // subject dn
        certgen.setSubjectDN(issuerDN);

        // public key
        certgen.setPublicKey(publicKey);

        // signature alg
        certgen.setSignatureAlgorithm(sigalg);

        // serial number
        certgen.setSerialNumber(new BigInteger(String.valueOf(curr)));

        // make certificate
        X509Certificate cert = certgen.generateX509Certificate(privateKey);
        return cert;
    }

    public void importTrustedX509Certificate(String alias, String certfile)
            throws java.io.FileNotFoundException,
            java.security.cert.CertificateException,
            java.security.KeyStoreException, java.io.IOException,
            java.security.NoSuchAlgorithmException,
            java.security.NoSuchProviderException {
        InputStream is = null;

        try {
            if (keyStoreProvider.equalsIgnoreCase("Default"))
            {
                keyStoreProvider = new String(System.getProperty("java.security.Provider"));
            }
            CertificateFactory cf = CertificateFactory.getInstance("X.509", keyStoreProvider);

            is = new FileInputStream(certfile);
            Certificate cert = cf.generateCertificate(is);

            if(alias == null || alias.equals("")) {
                // Generate an alias for this certificate
                X509Certificate xcert = (X509Certificate)cert;
                alias = xcert.getIssuerDN().toString()+":"+xcert.getSerialNumber();
            }

            keystore.setCertificateEntry(alias, cert);

            saveKeyStore();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public void importPKCS7Certificate(String alias, String certbuf)
            throws java.security.cert.CertificateException,
            java.security.NoSuchProviderException,
            java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException,
            java.security.UnrecoverableKeyException, java.io.IOException {

        InputStream is = null;

        try {
            is = new ByteArrayInputStream(certbuf.getBytes());
            importPKCS7Certificate(alias, is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public void importPKCS7Certificate(String alias, InputStream is)
            throws java.security.cert.CertificateException,
            java.security.NoSuchProviderException,
            java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException,
            java.security.UnrecoverableKeyException, java.io.IOException {

        if (keyStoreProvider.equalsIgnoreCase("Default"))
        {
            keyStoreProvider = new String(System.getProperty("java.security.Provider"));
        }
        CertificateFactory cf = CertificateFactory.getInstance("X.509",keyStoreProvider);
        Collection certcoll = cf.generateCertificates(is);

        Certificate[] chain = new Certificate[certcoll.size()];

        Iterator iter = certcoll.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            chain[i] = (Certificate) iter.next();
        }

        char[] password = keyPassword.toCharArray();
        keystore.setKeyEntry(alias, keystore.getKey(alias, password), password,
                chain);

        saveKeyStore();
    }

    public void deleteEntry(String alias)
            throws KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException, IOException {

        keystore.deleteEntry(alias);

        saveKeyStore();
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(KeyStoreGBean.class);

        infoFactory.addAttribute("keyStoreType", String.class, true);
        infoFactory.addAttribute("keyStoreProvider", String.class, true);
        infoFactory.addAttribute("keyStoreLocation", String.class, true);
        infoFactory.addAttribute("keyStorePassword", String.class, true);
        infoFactory.addAttribute("keyPassword", String.class, true);

        infoFactory.addReference("serverInfo", ServerInfo.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        infoFactory.addOperation("getKeyEntryInfo",
                new Class[] { String.class });
        infoFactory.addOperation("getKeyStoreSize");
        infoFactory.addOperation("getKeyStoreEntries");
        infoFactory.addOperation("getCertificateChain",
                new Class[] { String.class });
        infoFactory.addOperation("generateCSR", new Class[] { String.class });

        infoFactory.addOperation("generateKeyPair", new Class[] { String.class,
                String.class, Integer.class, String.class, Integer.class,
                String.class, String.class, String.class, String.class,
                String.class, String.class });

        infoFactory.addOperation("importTrustedX509Certificate", new Class[] {
                String.class, String.class });
        infoFactory.addOperation("importPKCS7Certificate", new Class[] {
                String.class, String.class });
        infoFactory.addOperation("deleteEntry", new Class[] {String.class });

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
