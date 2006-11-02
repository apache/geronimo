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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.management.geronimo.KeystoreIsLocked;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.util.asn1.ASN1Set;
import org.apache.geronimo.util.asn1.DEROutputStream;
import org.apache.geronimo.util.asn1.x509.X509Name;
import org.apache.geronimo.util.encoders.Base64;
import org.apache.geronimo.util.jce.PKCS10CertificationRequest;
import org.apache.geronimo.util.jce.X509Principal;
import org.apache.geronimo.util.jce.X509V1CertificateGenerator;

/**
 * Implementation of KeystoreInstance that accesses a keystore file on the
 * local filesystem, identified by the file's name (the last component of
 * the name only, not the full path).
 *
 * @version $Rev$ $Date$
 */
public class FileKeystoreInstance implements KeystoreInstance, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(FileKeystoreInstance.class);
    final static String JKS = "JKS";
    private URI keystorePath; // relative path
    private ServerInfo serverInfo; // used to decode relative path
    private File keystoreFile; // Only valid after startup
    private String keystoreName;
    private char[] keystorePassword; // Used to "unlock" the keystore for other services
    private Map keyPasswords = new HashMap();
    private Kernel kernel;
    private AbstractName abstractName;
    private char[] openPassword; // The password last used to open the keystore for editing
    // The following variables are the state of the keystore, which should be chucked if the file on disk changes
    private List privateKeys = new ArrayList();
    private List trustCerts = new ArrayList();
    private KeyStore keystore;
    private long keystoreReadDate = Long.MIN_VALUE;

    public FileKeystoreInstance(ServerInfo serverInfo, URI keystorePath, String keystoreName, String keystorePassword, String keyPasswords, Kernel kernel, AbstractName abstractName) {
        this.serverInfo = serverInfo;
        this.keystorePath = keystorePath;
        this.keystoreName = keystoreName;
        this.kernel = kernel;
        this.abstractName = abstractName;
        this.keystorePassword = keystorePassword == null ? null : keystorePassword.toCharArray();
        if(keyPasswords != null) {
            String[] keys = keyPasswords.split("\\]\\!\\[");
            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                int pos = key.indexOf('=');
                this.keyPasswords.put(key.substring(0, pos), key.substring(pos+1).toCharArray());
            }
        }
    }

    public void doStart() throws Exception {
        keystoreFile = new File(serverInfo.resolveServer(keystorePath));
        if(!keystoreFile.exists() || !keystoreFile.canRead()) {
            throw new IllegalArgumentException("Invalid keystore file ("+keystorePath+" = "+keystoreFile.getAbsolutePath()+")");
        }
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public String getKeystoreName() {
        return keystoreName;
    }

    public boolean unlockKeystore(char[] password) {
        //todo: test whether password is correct and if not return false
        try {
            kernel.setAttribute(abstractName, "keystorePassword", password == null ? null : new String(password));
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Unable to set attribute keystorePassword on myself!").initCause(e);
        }
        return true;
    }

    public void setKeystorePassword(String password) {
        keystorePassword = password == null ? null : password.toCharArray();
    }

    public void lockKeystore() {
        try {
            kernel.setAttribute(abstractName, "keystorePassword", null);
            keyPasswords.clear();
            storePasswords();
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Unable to set attribute keystorePassword on myself!").initCause(e);
        }
    }

    public boolean isKeystoreLocked() {
        return keystorePassword == null;
    }

    public String[] listPrivateKeys(char[] storePassword) {
        if(!isLoaded(storePassword)) {
            if(!loadKeystoreData(storePassword)) {
                return null;
            }
        }
        return (String[]) privateKeys.toArray(new String[privateKeys.size()]);
    }

    public boolean unlockPrivateKey(String alias, char[] password) throws KeystoreIsLocked {
        if(isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+keystoreName+"' is locked!");
        }
        //todo: test whether password is correct and if not return false
        keyPasswords.put(alias, password);
        storePasswords();
        return true;
    }

    public String[] getUnlockedKeys() throws KeystoreIsLocked {
        if(isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+keystoreName+"' is locked; please unlock it in the console.");
        }
        if(keystore == null || keystoreReadDate < keystoreFile.lastModified()) {
            loadKeystoreData(keystorePassword);
        }
        return (String[]) keyPasswords.keySet().toArray(new String[keyPasswords.size()]);
    }

    public boolean isTrustStore() throws KeystoreIsLocked {
        if(isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+keystoreName+"' is locked; please unlock it in the console.");
        }
        if(keystore == null || keystoreReadDate < keystoreFile.lastModified()) {
            loadKeystoreData(keystorePassword);
        }
        return trustCerts.size() > 0;
    }

    public void lockPrivateKey(String alias) {
        keyPasswords.remove(alias);
        storePasswords();
    }

    private void storePasswords() {
        StringBuffer buf = new StringBuffer();
        for (Iterator it = keyPasswords.entrySet().iterator(); it.hasNext();) {
            if(buf.length() > 0) {
                buf.append("]![");
            }
            Map.Entry entry = (Map.Entry) it.next();
            buf.append(entry.getKey()).append("=").append((char[])entry.getValue());
        }
        try {
            kernel.setAttribute(abstractName, "keyPasswords", buf.length() == 0 ? null : buf.toString());
        } catch (Exception e) {
            log.error("Unable to save key passwords in keystore '"+keystoreName+"'", e);
        }
    }

    public void setKeyPasswords(String passwords) {} // Just so the kernel sees the new value

    /**
     * Checks whether the specified private key is locked, which is to say,
     * available for other components to use to generate socket factories.
     * Does not check whether the unlock password is actually correct.
     */
    public boolean isKeyLocked(String alias) {
        return keyPasswords.get(alias) == null;
    }

    public String[] listTrustCertificates(char[] storePassword) {
        if(!isLoaded(storePassword)) {
            if(!loadKeystoreData(storePassword)) {
                return null;
            }
        }
        return (String[]) trustCerts.toArray(new String[trustCerts.size()]);
    }

    public Certificate getCertificate(String alias, char[] storePassword) {
        if(!isLoaded(storePassword)) {
            if(!loadKeystoreData(storePassword)) {
                return null;
            }
        }
        try {
            return keystore.getCertificate(alias);
        } catch (KeyStoreException e) {
            log.error("Unable to read certificate from keystore", e);
        }
        return null;
    }

    public boolean importTrustCertificate(Certificate cert, String alias, char[] storePassword) {
        if(!isLoaded(storePassword)) {
            if(!loadKeystoreData(storePassword)) {
                return false;
            }
        }
        try {
            keystore.setCertificateEntry(alias, cert);
            trustCerts.add(alias);
            return saveKeystore(storePassword);
        } catch (KeyStoreException e) {
            log.error("Unable to import certificate", e);
        }
        return false;
    }

    public boolean generateKeyPair(String alias, char[] storePassword, char[] keyPassword, String keyAlgorithm, int keySize, String signatureAlgorithm, int validity, String commonName, String orgUnit, String organization, String locality, String state, String country) {
        if(!isLoaded(storePassword)) {
            if(!loadKeystoreData(storePassword)) {
                return false;
            }
        }
        try {
            KeyPairGenerator kpgen = KeyPairGenerator.getInstance(keyAlgorithm);
            kpgen.initialize(keySize);
            KeyPair keyPair = kpgen.generateKeyPair();
            X509Certificate cert = generateCertificate(keyPair.getPublic(), keyPair.getPrivate(), signatureAlgorithm,
                    validity, commonName, orgUnit, organization, locality, state, country);

            keystore.setKeyEntry(alias, keyPair.getPrivate(), keyPassword, new Certificate[] { cert });
            privateKeys.add(alias);
            return saveKeystore(storePassword);
        } catch (SignatureException e) {
            log.error("Unable to generate key pair", e);
        } catch (InvalidKeyException e) {
            log.error("Unable to generate key pair", e);
        } catch (KeyStoreException e) {
            log.error("Unable to generate key pair", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to generate key pair", e);
        }
        return false;
    }


    public String generateCSR(String alias) {
        // find certificate by alias
        X509Certificate cert = null;
        try {
            cert = (X509Certificate) keystore.getCertificate(alias);
        } catch (KeyStoreException e) {
            log.error("Unable to generate CSR", e);
        }

        // find private key by alias
        PrivateKey key = null;
        try {
            key = (PrivateKey) keystore.getKey(alias, (char[])keyPasswords.get(alias));
        } catch (KeyStoreException e) {
            log.error("Unable to generate CSR", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to generate CSR", e);
        } catch (UnrecoverableKeyException e) {
            log.error("Unable to generate CSR", e);
        }

        // generate csr
        String csr = null;
        try {
            csr = generateCSR(cert, key);
        } catch (Exception e) {
            log.error("Unable to generate CSR", e);
        }
        return csr;
    }

    private String generateCSR(X509Certificate cert, PrivateKey signingKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, KeyStoreException, IOException {
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
        
        StringBuffer sbuf = new StringBuffer(BEGIN_CERT_REQ).append('\n');
        
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

    private void importPKCS7Certificate(String alias, InputStream is)
        throws java.security.cert.CertificateException,
        java.security.NoSuchProviderException,
        java.security.KeyStoreException,
        java.security.NoSuchAlgorithmException,
        java.security.UnrecoverableKeyException, java.io.IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Collection certcoll = cf.generateCertificates(is);
        
        Certificate[] chain = new Certificate[certcoll.size()];
        
        Iterator iter = certcoll.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            chain[i] = (Certificate) iter.next();
        }
        
        if(keystore.getCertificate(alias).getPublicKey().equals(chain[0].getPublicKey())) {
            char[] keyPassword = (char[])keyPasswords.get(alias);
            keystore.setKeyEntry(alias, keystore.getKey(alias, keyPassword), keyPassword, chain);
            saveKeystore(keystorePassword);
        } else {
            log.error("Error in importPKCS7Certificate.  PublicKey in the certificate received is not related to the PrivateKey in the keystore. keystore = "+keystoreName+", alias = "+alias);
        }
    }

    public void deleteEntry(String alias) {
        try {
            keystore.deleteEntry(alias);
            privateKeys.remove(alias);
            trustCerts.remove(alias);
            keyPasswords.remove(alias);
        } catch (KeyStoreException e) {
            log.error("Unable to delete entry:"+alias, e);
        }
        saveKeystore(keystorePassword);
    }
    
    public KeyManager[] getKeyManager(String algorithm, String alias) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeystoreIsLocked {
        if(isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+keystoreName+"' is locked; please unlock it in the console.");
        }
        if(keystore == null || keystoreReadDate < keystoreFile.lastModified()) {
            loadKeystoreData(keystorePassword);
        }
        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(algorithm);
        keyFactory.init(keystore, (char[]) keyPasswords.get(alias));
        return keyFactory.getKeyManagers();
    }

    public TrustManager[] getTrustManager(String algorithm) throws KeyStoreException, NoSuchAlgorithmException, KeystoreIsLocked {
        if(isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+keystoreName+"' is locked; please unlock it in the console.");
        }
        if(keystore == null || keystoreReadDate < keystoreFile.lastModified()) {
            loadKeystoreData(keystorePassword);
        }
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(algorithm);
        trustFactory.init(keystore);
        return trustFactory.getTrustManagers();
    }

    private boolean saveKeystore(char[] password) {
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(keystoreFile));
            keystore.store(out, password);
            out.flush();
            out.close();
            keystoreReadDate = System.currentTimeMillis();
            return true;
        } catch (KeyStoreException e) {
            log.error("Unable to save keystore", e);
        } catch (IOException e) {
            log.error("Unable to save keystore", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to save keystore", e);
        } catch (CertificateException e) {
            log.error("Unable to save keystore", e);
        }
        return false;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileKeystoreInstance.class, NameFactory.KEYSTORE_INSTANCE);
        infoFactory.addAttribute("keystorePath", URI.class, true, false);
        infoFactory.addAttribute("keystoreName", String.class, true, false);
        infoFactory.addAttribute("keystorePassword", String.class, true, true);
        infoFactory.addAttribute("keyPasswords", String.class, true, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addInterface(KeystoreInstance.class);
        infoFactory.setConstructor(new String[]{"ServerInfo","keystorePath", "keystoreName", "keystorePassword", "keyPasswords", "kernel", "abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    // ==================== Internals =====================

    private boolean loadKeystoreData(char[] password) {
        try {
            keystoreReadDate = System.currentTimeMillis();
            privateKeys.clear();
            trustCerts.clear();
            if(keystore == null) {
                keystore = KeyStore.getInstance(JKS);
            }
            InputStream in = new BufferedInputStream(new FileInputStream(keystoreFile));
            keystore.load(in, password);
            in.close();
            openPassword = password;
            Enumeration aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                if(keystore.isKeyEntry(alias)) {
                    privateKeys.add(alias);
                } else if(keystore.isCertificateEntry(alias)) {
                    trustCerts.add(alias);
                }
            }
            return true;
        } catch (KeyStoreException e) {
            log.error("Unable to open keystore with provided password", e);
        } catch (IOException e) {
            log.error("Unable to open keystore with provided password", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to open keystore with provided password", e);
        } catch (CertificateException e) {
            log.error("Unable to open keystore with provided password", e);
        }
        return false;
    }

    private boolean isLoaded(char[] password) {
        if(openPassword == null || openPassword.length != password.length) {
            return false;
        }
        if(keystoreReadDate < keystoreFile.lastModified()) {
            return false;
        }
        for (int i = 0; i < password.length; i++) {
            if(password[i] != openPassword[i]) {
                return false;
            }
        }
        return true;
    }

    private X509Certificate generateCertificate(PublicKey publicKey, PrivateKey privateKey, String algorithm, int validity, String commonName, String orgUnit, String organization, String locality, String state, String country) throws SignatureException, InvalidKeyException {
        X509V1CertificateGenerator certgen = new X509V1CertificateGenerator();
        Vector order = new Vector();
        Hashtable attrmap = new Hashtable();

        if (commonName != null) {
            attrmap.put(X509Principal.CN, commonName);
            order.add(X509Principal.CN);
        }

        if (orgUnit != null) {
            attrmap.put(X509Principal.OU, orgUnit);
            order.add(X509Principal.OU);
        }

        if (organization != null) {
            attrmap.put(X509Principal.O, organization);
            order.add(X509Principal.O);
        }

        if (locality != null) {
            attrmap.put(X509Principal.L, locality);
            order.add(X509Principal.L);
        }

        if (state != null) {
            attrmap.put(X509Principal.ST, state);
            order.add(X509Principal.ST);
        }

        if (country != null) {
            attrmap.put(X509Principal.C, country);
            order.add(X509Principal.C);
        }

        X509Principal issuerDN = new X509Principal(order, attrmap);

        // validity
        long curr = System.currentTimeMillis();
        long untill = curr + (long) validity * 24 * 60 * 60 * 1000;

        certgen.setNotBefore(new Date(curr));
        certgen.setNotAfter(new Date(untill));
        certgen.setIssuerDN(issuerDN);
        certgen.setSubjectDN(issuerDN);
        certgen.setPublicKey(publicKey);
        certgen.setSignatureAlgorithm(algorithm);
        certgen.setSerialNumber(new BigInteger(String.valueOf(curr)));

        // make certificate
        return certgen.generateX509Certificate(privateKey);
    }
}
