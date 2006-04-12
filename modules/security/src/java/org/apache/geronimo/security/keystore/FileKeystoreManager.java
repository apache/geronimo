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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.net.ServerSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.EditableConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.management.geronimo.KeyIsLocked;
import org.apache.geronimo.management.geronimo.KeystoreInstance;
import org.apache.geronimo.management.geronimo.KeystoreIsLocked;
import org.apache.geronimo.management.geronimo.KeystoreManager;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.util.jce.X509Principal;
import org.apache.geronimo.util.jce.X509V1CertificateGenerator;

/**
 * An implementation of KeystoreManager that assumes every file in a specified
 * directory is a keystore.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class FileKeystoreManager implements KeystoreManager, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(FileKeystoreManager.class);
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
            rootURI = serverInfo.resolve(configuredDir);
        } else {
            rootURI = configuredDir;
        }
        if (!rootURI.getScheme().equals("file")) {
            throw new IllegalStateException("FileKeystoreManager must have a root that's a local directory (not " + rootURI + ")");
        }
        directory = new File(rootURI);
        if (!directory.exists() || !directory.isDirectory() || !directory.canRead()) {
            throw new IllegalStateException("FileKeystoreManager must have a root that's a valid readable directory (not " + directory.getAbsolutePath() + ")");
        }
        log.debug("Keystore directory is " + directory.getAbsolutePath());
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public String[] listKeystoreFiles() {
        File[] files = directory.listFiles();
        List list = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if(file.canRead() && !file.isDirectory()) {
                list.add(file.getName());
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public KeystoreInstance[] getKeystores() {
        String[] names = listKeystoreFiles();
        KeystoreInstance[] result = new KeystoreInstance[names.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getKeystore(names[i]);
            if(result[i] == null) {
                return null;
            }
        }
        return result;
    }

    public KeystoreInstance getKeystore(String name) {
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
        aName = kernel.getNaming().createChildName(myName, name, NameFactory.KEYSTORE_INSTANCE);
        GBeanData data = new GBeanData(aName, FileKeystoreInstance.getGBeanInfo());
        data.setAttribute("keystoreFile", test);
        data.setAttribute("keystoreName", name);
        EditableConfigurationManager mgr = ConfigurationUtil.getEditableConfigurationManager(kernel);
        if(mgr != null) {
            try {
                mgr.addGBeanToConfiguration(myName.getArtifact(), data, true);
                return (KeystoreInstance) kernel.getProxyManager().createProxy(aName, KeystoreInstance.class);
            } catch (InvalidConfigException e) {
                log.error("Should never happen", e);
                throw new IllegalStateException("Unable to add Keystore GBean ("+e.getMessage()+")");
            } finally {
                ConfigurationUtil.releaseConfigurationManager(kernel, mgr);
            }
        } else {
            log.warn("The ConfigurationManager in the kernel does not allow changes at runtime");
            return null;
        }
    }

    public ServerSocketFactory createSSLFactory(String keyStore, String keyAlias, String trustStore) throws KeystoreIsLocked, KeyIsLocked {
        throw new UnsupportedOperationException();
    }

    public KeystoreInstance createKeystore(String name, char[] password) {
        File test = new File(directory, name);
        if(test.exists()) {
            throw new IllegalArgumentException("Keystore already exists "+test.getAbsolutePath()+"!");
        }
        try {
            KeyStore keystore = KeyStore.getInstance(FileKeystoreInstance.JKS);
            keystore.load(null, password);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(test));
            keystore.store(out, password);
            out.flush();
            out.close();
            return getKeystore(name);
        } catch (KeyStoreException e) {
            log.error("Unable to create keystore", e);
        } catch (IOException e) {
            log.error("Unable to create keystore", e);
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to create keystore", e);
        } catch (CertificateException e) {
            log.error("Unable to create keystore", e);
        }
        return null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileKeystoreManager.class);
        infoFactory.addAttribute("keystoreDir", URI.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addReference("KeystoreInstances", KeystoreInstance.class, NameFactory.KEYSTORE_INSTANCE);
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
