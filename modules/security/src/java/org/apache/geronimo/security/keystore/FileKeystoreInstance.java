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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.DynamicGBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.util.jce.X509Principal;
import org.apache.geronimo.util.jce.X509V1CertificateGenerator;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Implementation of KeystoreInstance that accesses a keystore file on the
 * local filesystem, identified by the file's name (the last component of
 * the name only, not the full path).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class FileKeystoreInstance implements KeystoreInstance, DynamicGBean {
    private static final Log log = LogFactory.getLog(FileKeystoreInstance.class);
    private final static String MAGIC_KEYSTORE_PWD_KEY = "#KeystorePW";
    final static String JKS = "JKS";
    private File keystoreFile;
    private String keystoreName;
    private Map unlockKeyPasswords = new HashMap();
    private Kernel kernel;
    private ObjectName objectName;
    private char[] openPassword;
    // The following variables are the state of the keystore, which should be chucked if the file on disk changes
    private List privateKeys = new ArrayList();
    private List trustCerts = new ArrayList();
    private KeyStore keystore;
    private long keystoreReadDate = Long.MIN_VALUE;

    public FileKeystoreInstance(File keystoreFile, String keystoreName, Kernel kernel, String objectName) throws MalformedObjectNameException {
        this.keystoreFile = keystoreFile;
        this.keystoreName = keystoreName;
        this.kernel = kernel;
        this.objectName = ObjectName.getInstance(objectName);
    }

    public String getKeystoreName() {
        return keystoreName;
    }

    public boolean unlockKeystore(char[] password) {
        //todo: test whether password is correct and if not return false
        try {
            kernel.setAttribute(objectName, MAGIC_KEYSTORE_PWD_KEY, password);
        } catch (Exception e) {
            log.error("Unable to save keystore password for keystore '"+keystoreName+"'", e);
            return false;
        }
        return true;
    }

    public void lockKeystore() {
        try {
            kernel.setAttribute(objectName, MAGIC_KEYSTORE_PWD_KEY, null);
        } catch (Exception e) {
            log.error("Unable to clear keystore password for keystore '"+keystoreName+"'", e);
        }
    }

    public boolean isKeystoreLocked() {
        return unlockKeyPasswords.get(MAGIC_KEYSTORE_PWD_KEY) == null;
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
        try {
            kernel.setAttribute(objectName, alias, password);
        } catch (Exception e) {
            log.error("Unable to save key password for key '"+alias+"' in keystore '"+keystoreName+"'", e);
            return false;
        }
        return true;
    }

    public void lockPrivateKey(String alias) {
        try {
            kernel.setAttribute(objectName, alias, null);
        } catch (Exception e) {
            log.error("Unable to clear keystore password for keystore '"+keystoreName+"'", e);
        }
    }

    public boolean isKeyUnlocked(String alias) {
        return unlockKeyPasswords.get(alias) == null;
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

    // ==================== Should only be accessed by the Kernel =================

    public Object getAttribute(String name) throws Exception {
        return unlockKeyPasswords.get(name);
    }

    public void setAttribute(String name, Object value) throws Exception {
        unlockKeyPasswords.put(name, value);
    }

    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        throw new UnsupportedOperationException();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileKeystoreInstance.class, NameFactory.KEYSTORE_INSTANCE);
        infoFactory.addAttribute("keystoreFile", File.class, true, false);
        infoFactory.addAttribute("keystoreName", String.class, true, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addInterface(KeystoreInstance.class);
        infoFactory.setConstructor(new String[]{"keystoreFile", "keystoreName", "kernel", "objectName"});

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

    private char[] getKeystorePassword() {
        return (char[])unlockKeyPasswords.get(MAGIC_KEYSTORE_PWD_KEY);
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
