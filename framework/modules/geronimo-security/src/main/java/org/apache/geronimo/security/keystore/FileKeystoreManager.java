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
package org.apache.geronimo.security.keystore;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.geronimo.crypto.KeystoreUtil;
import org.apache.geronimo.crypto.jce.X509Principal;
import org.apache.geronimo.crypto.jce.X509V1CertificateGenerator;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.util.InputUtils;
import org.apache.geronimo.management.geronimo.KeyIsLocked;
import org.apache.geronimo.management.geronimo.KeystoreException;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.management.geronimo.KeystoreIsLocked;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of KeystoreManager that assumes every file in a specified
 * directory is a keystore.
 *
 * @version $Rev$ $Date$
 */
public class FileKeystoreManager implements KeystoreManager, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(FileKeystoreManager.class);
    private File directory;
    private ServerInfo serverInfo;
    private URI configuredDir;
    private Collection keystores;
    private Kernel kernel;

    public FileKeystoreManager(URI keystoreDir, ServerInfo serverInfo, Collection keystores, Kernel kernel) {
        configuredDir = keystoreDir;
        this.serverInfo = serverInfo;
        this.keystores = keystores;
        this.kernel = kernel;
    }

    public void doStart() throws Exception {
        URI rootURI;
        if (serverInfo != null) {
            rootURI = serverInfo.resolveServer(configuredDir);
        } else {
            rootURI = configuredDir;
        }
        if (!rootURI.getScheme().equals("file")) {
            throw new IllegalStateException("FileKeystoreManager must have a root that's a local directory (not " + rootURI + ")");
        }
        directory = new File(rootURI);
        if (!directory.exists()) {
        	if (directory.mkdirs()) {
        		log.warn("The keystore directory: " + directory.getAbsolutePath() + " does not exist. System automatically created one.");
        	}
        }       	
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            throw new IllegalStateException("FileKeystoreManager must have a root that's a valid readable directory (not " + directory.getAbsolutePath() + ")");
        }
        log.debug("Keystore directory is " + directory.getAbsolutePath());
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public void initializeKeystores() {
        getKeystores();
    }
    public String[] listKeystoreFiles() {
        File[] files = directory.listFiles();
        List list = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.canRead() && !file.isDirectory()) {
                String name = file.getName();
                if (name.lastIndexOf(".") == -1) {
                    list.add(file.getName());
                } else {
                    String type = name.substring(name.lastIndexOf(".") + 1);    
                    if (file.length()> 0){
                        for(String ktype : KeystoreUtil.keystoreTypes ){
                            if (ktype.toLowerCase().equals(type.toLowerCase())){                                
                                list.add(file.getName());
                            }
                        }
                    } else if (file.length() == 0){
                        for (String ktype : KeystoreUtil.emptyKeystoreTypes){
                            if (ktype.toLowerCase().equals(type.toLowerCase())){                        
                                list.add(file.getName());
                            }
                        }
                    }
                }
                            
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public KeystoreInstance[] getKeystores() {
        String[] names = listKeystoreFiles();
        KeystoreInstance[] result = new KeystoreInstance[names.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getKeystore(names[i], null);
            if(result[i] == null) {
                return null;
            }
        }
        return result;
    }

    public KeystoreInstance getKeystore(String name, String type) {
        for (Iterator it = keystores.iterator(); it.hasNext();) {
            KeystoreInstance instance = (KeystoreInstance) it.next();
            if(instance.getKeystoreName().equals(name)) {
                return instance;
            }
        }
        File test = new File(directory, name);
        if(!test.exists() || !test.canRead()) {
            throw new IllegalArgumentException("Cannot access keystore "+test.getAbsolutePath()+"!");
        }
        AbstractName aName;
        AbstractName myName = kernel.getAbstractNameFor(this);
        aName = kernel.getNaming().createSiblingName(myName, name, SecurityNames.KEYSTORE_INSTANCE);
        GBeanData data = new GBeanData(aName, FileKeystoreInstance.getGBeanInfo());
        try {
            String path = configuredDir.toString();
            if(!path.endsWith("/")) {
                path += "/";
            }
            data.setAttribute("keystorePath", new URI(path +name));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Can't resolve keystore path: "+e.getMessage(), e);
        }
        data.setReferencePattern("ServerInfo", kernel.getAbstractNameFor(serverInfo));
        data.setAttribute("keystoreName", name);
        if(type == null) {
            if(name.lastIndexOf(".") == -1) {
                type = KeystoreUtil.defaultType;
                log.warn("keystoreType for new keystore \""+name+"\" set to default type \""+type+"\".");
            } else {
                type = name.substring(name.lastIndexOf(".")+1);
                log.warn("keystoreType for new keystore \""+name+"\" set to \""+type+"\" based on file extension.");
            }
        }
        data.setAttribute("keystoreType", type);
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if(mgr != null) {
            try {
                mgr.addGBeanToConfiguration(myName.getArtifact(), data, true);
                return (KeystoreInstance) kernel.getProxyManager().createProxy(aName, KeystoreInstance.class);
            } catch (InvalidConfigException e) {
                log.error("Should never happen", e);
                throw new IllegalStateException("Unable to add Keystore GBean ("+e.getMessage()+")", e);
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
            }
        } else {
            log.warn("The ConfigurationManager in the kernel does not allow changes at runtime");
            return null;
        }
    }

    /**
     * Gets a SocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     *
     * @param provider   The SSL provider to use, or null for the default
     * @param protocol   The SSL protocol to use
     * @param algorithm  The SSL algorithm to use
     * @param trustStore The trust keystore name as provided by listKeystores.
     *                   The KeystoreInstance for this keystore must have
     *                   unlocked this key.
     * @param loader     The class loader used to resolve factory classes.
     *
     * @return A created SSLSocketFactory item created from the KeystoreManager.
     * @throws KeystoreIsLocked
     *                Occurs when the requested key keystore cannot
     *                be used because it has not been unlocked.
     * @throws KeyIsLocked
     *                Occurs when the requested private key in the key
     *                keystore cannot be used because it has not been
     *                unlocked.
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     * @throws KeyManagementException
     * @throws NoSuchProviderException
     */
    public SSLSocketFactory createSSLFactory(String provider, String protocol, String algorithm, String trustStore, ClassLoader loader) throws KeystoreException {
        // typically, the keyStore and the keyAlias are not required if authentication is also not required.
        return createSSLFactory(provider, protocol, algorithm, null, null, trustStore, loader);
    }

    /**
     * Gets a SocketFactory using one Keystore to access the private key
     * and another to provide the list of trusted certificate authorities.
     *
     * @param provider   The SSL provider to use, or null for the default
     * @param protocol   The SSL protocol to use
     * @param algorithm  The SSL algorithm to use
     * @param keyStore   The key keystore name as provided by listKeystores.  The
     *                   KeystoreInstance for this keystore must be unlocked.
     * @param keyAlias   The name of the private key in the keystore.  The
     *                   KeystoreInstance for this keystore must have unlocked
     *                   this key.
     * @param trustStore The trust keystore name as provided by listKeystores.
     *                   The KeystoreInstance for this keystore must have
     *                   unlocked this key.
     * @param loader     The class loader used to resolve factory classes.
     *
     * @return A created SSLSocketFactory item created from the KeystoreManager.
     * @throws KeystoreIsLocked
     *                Occurs when the requested key keystore cannot
     *                be used because it has not been unlocked.
     * @throws KeyIsLocked
     *                Occurs when the requested private key in the key
     *                keystore cannot be used because it has not been
     *                unlocked.
     * @throws KeystoreException
     */
    public SSLSocketFactory createSSLFactory(String provider, String protocol, String algorithm, String keyStore, String keyAlias, String trustStore, ClassLoader loader) throws KeystoreException {
        // the keyStore is optional.
        KeystoreInstance keyInstance = null;
        if (keyStore != null) {
            keyInstance = getKeystore(keyStore, null);
            if(keyInstance.isKeystoreLocked()) {
                throw new KeystoreIsLocked("Keystore '"+keyStore+"' is locked; please use the keystore page in the admin console to unlock it");
            }
            if(keyInstance.isKeyLocked(keyAlias)) {
                throw new KeystoreIsLocked("Key '"+keyAlias+"' in keystore '"+keyStore+"' is locked; please use the keystore page in the admin console to unlock it");
            }
        }
        KeystoreInstance trustInstance = trustStore == null ? null : getKeystore(trustStore, null);
        if(trustInstance != null && trustInstance.isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+trustStore+"' is locked; please use the keystore page in the admin console to unlock it");
        }

        // OMG this hurts, but it causes ClassCastExceptions elsewhere unless done this way!
        try {
            Class cls = loader.loadClass("javax.net.ssl.SSLContext");
            Object ctx = cls.getMethod("getInstance", new Class[] {String.class}).invoke(null, new Object[]{protocol});
            Class kmc = Class.forName("[Ljavax.net.ssl.KeyManager;", false, loader);
            Class tmc = Class.forName("[Ljavax.net.ssl.TrustManager;", false, loader);            Class src = loader.loadClass("java.security.SecureRandom");
            cls.getMethod("init", new Class[]{kmc, tmc, src}).invoke(ctx, new Object[]{
                                                                            keyInstance == null ? null : keyInstance.getKeyManager(algorithm, keyAlias, null),
                                                                            trustInstance == null ? null : trustInstance.getTrustManager(algorithm, null),
                                                                            new java.security.SecureRandom()});
            Object result = cls.getMethod("getSocketFactory", new Class[0]).invoke(ctx, new Object[0]);
            return (SSLSocketFactory) result;
        } catch (Exception e) {
            throw new KeystoreException("Unable to create SSL Factory", e);
        }
    }

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
    public SSLServerSocketFactory createSSLServerFactory(String provider, String protocol, String algorithm, String keyStore, String keyAlias, String trustStore, ClassLoader loader) throws KeystoreException {
        SSLContext sslContext = createSSLContext(provider, protocol, algorithm, keyStore, keyAlias, trustStore, loader);
        // OMG this hurts, but it causes ClassCastExceptions elsewhere unless done this way!
        try {
            Object result = sslContext.getClass().getMethod("getServerSocketFactory", new Class[0]).invoke(sslContext, new Object[0]);
            return (SSLServerSocketFactory) result;
        } catch (Exception e) {
            throw new KeystoreException("Unable to create SSL Server Factory", e);
        }
    }

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
     * @return SSLContext using the security info provided
     * @throws KeystoreIsLocked Occurs when the requested key keystore cannot
     *                          be used because it has not been unlocked.
     * @throws KeyIsLocked Occurs when the requested private key in the key
     *                     keystore cannot be used because it has not been
     *                     unlocked.
     */
    public SSLContext createSSLContext(String provider, String protocol, String algorithm, String keyStore, String keyAlias, String trustStore, ClassLoader loader) throws KeystoreException {
        KeystoreInstance keyInstance = getKeystore(keyStore, null);
        if(keyInstance.isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+keyStore+"' is locked; please use the keystore page in the admin console to unlock it");
        }
        if(keyInstance.isKeyLocked(keyAlias)) {
            throw new KeystoreIsLocked("Key '"+keyAlias+"' in keystore '"+keyStore+"' is locked; please use the keystore page in the admin console to unlock it");
        }
        KeystoreInstance trustInstance = trustStore == null ? null : getKeystore(trustStore, null);
        if(trustInstance != null && trustInstance.isKeystoreLocked()) {
            throw new KeystoreIsLocked("Keystore '"+trustStore+"' is locked; please use the keystore page in the admin console to unlock it");
        }

        // OMG this hurts, but it causes ClassCastExceptions elsewhere unless done this way!
        try {
            Class cls = loader.loadClass("javax.net.ssl.SSLContext");
            Object ctx = cls.getMethod("getInstance", new Class[] {String.class}).invoke(null, new Object[]{protocol});
            Class kmc = Class.forName("[Ljavax.net.ssl.KeyManager;", false, loader);
            Class tmc = Class.forName("[Ljavax.net.ssl.TrustManager;", false, loader);
            Class src = loader.loadClass("java.security.SecureRandom");
            cls.getMethod("init", new Class[]{kmc, tmc, src}).invoke(ctx, new Object[]{keyInstance.getKeyManager(algorithm, keyAlias, null),
                                                                            trustInstance == null ? null : trustInstance.getTrustManager(algorithm, null),
                                                                            new java.security.SecureRandom()});
            return (SSLContext) ctx;
        } catch (Exception e) {
            throw new KeystoreException("Unable to create SSL Context", e);
        }
    }

    public KeystoreInstance createKeystore(String name, char[] password, String keystoreType) throws KeystoreException {

        // ensure there are no illegal chars in DB name
        InputUtils.validateSafeInput(name);

        File test = new File(directory, name);
        if(test.exists()) {
            throw new IllegalArgumentException("Keystore already exists "+test.getAbsolutePath()+"!");
        }
        try {
            KeyStore keystore = KeyStore.getInstance(keystoreType);
            keystore.load(null, password);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(test));
            keystore.store(out, password);
            out.flush();
            out.close();
            return getKeystore(name, keystoreType);
        } catch (KeyStoreException e) {
            throw new KeystoreException("Unable to create keystore", e);
        } catch (IOException e) {
            throw new KeystoreException("Unable to create keystore", e);
        } catch (NoSuchAlgorithmException e) {
            throw new KeystoreException("Unable to create keystore", e);
        } catch (CertificateException e) {
            throw new KeystoreException("Unable to create keystore", e);
        }
    }

    public KeystoreInstance[] getUnlockedKeyStores() {
        List results = new ArrayList();
        for (Iterator it = keystores.iterator(); it.hasNext();) {
            KeystoreInstance instance = (KeystoreInstance) it.next();
            try {
                if(!instance.isKeystoreLocked() && instance.getUnlockedKeys(null).length > 0) {
                    results.add(instance);
                }
            } catch (KeystoreException e) {}
        }
        return (KeystoreInstance[]) results.toArray(new KeystoreInstance[results.size()]);
    }

    public KeystoreInstance[] getUnlockedTrustStores() {
        List results = new ArrayList();
        for (Iterator it = keystores.iterator(); it.hasNext();) {
            KeystoreInstance instance = (KeystoreInstance) it.next();
            try {
                if(!instance.isKeystoreLocked() && instance.isTrustStore(null)) {
                    results.add(instance);
                }
            } catch (KeystoreException e) {}
        }
        return (KeystoreInstance[]) results.toArray(new KeystoreInstance[results.size()]);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileKeystoreManager.class);
        infoFactory.addAttribute("keystoreDir", URI.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("KeystoreInstances", KeystoreInstance.class, SecurityNames.KEYSTORE_INSTANCE);
        infoFactory.addInterface(KeystoreManager.class);
        infoFactory.setConstructor(new String[]{"keystoreDir", "ServerInfo", "KeystoreInstances", "kernel"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    // ===================== Move this to a unitiy class or something ====================

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
        return certgen.generateX509Certificate(privateKey);
    }
}
