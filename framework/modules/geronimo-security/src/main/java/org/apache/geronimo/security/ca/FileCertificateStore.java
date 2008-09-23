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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.geronimo.CertificateStore;
import org.apache.geronimo.management.geronimo.CertificateStoreException;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.crypto.CaUtils;
import org.apache.geronimo.security.SecurityNames;

/**
 * A certificate store implementation using disk files.
 *
 * @version $Rev$ $Date$
 */
public class FileCertificateStore implements CertificateStore, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(FileCertificateStore.class);

    private ServerInfo serverInfo;
    private Kernel kernel;
    private AbstractName abstractName;
    private URI directoryPath;
    
    // File name for storing the highest serial number in the store
    private static final String SERIAL_NUMBER_FILE = "highest-serial-number.txt";
    // Extension for certificate files.  Filename would be <serial-number>+CERT_FILE_SUFFIX
    private static final String CERT_FILE_SUFFIX = ".txt";
    // File name for storing CA's certificate
    private static final String CA_CERT_FILE = "ca-cert.txt";
    // File name for storing Certificate Challenges
    private static final String CHALLENGE_FILENAME = "challenge.properties";
    private static final String CHALLENGE_FILE_HEADER = "Challenge File";
    
    // directory for the certificate store
    private File storeDir = null;
    
    // File object of SERIAL_NUMBER_FILE cached
    private File highestSerialFile = null;
    // highest serial number cached
    private BigInteger highestSerialNumber = null;
    // Cerificate Challenges
    private Properties challenges = null;
    
    /**
     * Constructor
     * @param directoryPath directory for the certificate store
     */
    public FileCertificateStore(ServerInfo serverInfo, URI directoryPath, Kernel kernel, AbstractName abstractName) {
        this.serverInfo = serverInfo;
        this.kernel = kernel;
        this.abstractName = abstractName;
        this.directoryPath = directoryPath;
    }
    /**
     * This method stores a given certificate.
     * 
     * @param cert Certificate to be stored
     */
    public void storeCertificate(Certificate cert) throws CertificateStoreException {
        BigInteger sNo = ((X509Certificate)cert).getSerialNumber();
        File certFile = new File(storeDir, sNo+CERT_FILE_SUFFIX);
        try {
            // Check if the highest serial number is less than the serial number of certificate to be stored.
            if(sNo.compareTo(getHighestSerialNumber()) == 1) {
                // store the current serial number so that getNextSerialNumber() will not result in duplicate
                // serial number
                setHighestSerialNumber(sNo);
            }
            
            // Store the certificate to disk in base64 format
            FileOutputStream fout = new FileOutputStream(certFile);
            CaUtils.storeInBase64(fout, cert.getEncoded(), CaUtils.CERT_HEADER, CaUtils.CERT_FOOTER, CaUtils.B64_LINE_SIZE);
            fout.close();
        } catch (Exception e) {
            throw new CertificateStoreException("Error while storing certificate.", e);
        }
    }

    /**
     * This method returns a Certificate with a given serial number (if it exists in the store)
     * 
     * @param sNo Serial Number of the certificate to be retrieved.
     */
    public Certificate getCertificate(BigInteger sNo) throws CertificateStoreException {
        File certFile = new File(storeDir, sNo+CERT_FILE_SUFFIX);
        if(!certFile.exists()) {
            // No such certificate in the store.
            throw new CertificateStoreException("No certificate with serial number "+sNo+" found.");
        }
        
        // Read the certificate from disk and generate a java.security.cert.Certificate
        try {
            FileInputStream fin = new FileInputStream(certFile);
            CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            Certificate cert = certFac.generateCertificate(fin);
            fin.close();
            return cert;
        } catch (Exception e) {
            throw new CertificateStoreException("Error while retrieving certificate.", e);
        }
    }

    /**
     * This method returns base64 encoded certificate with a given serial number (if it exists in the store)
     * 
     * @param sNo Serial Number of the certificate to be retrieved.
     */
    public String getCertificateBase64Text(BigInteger sNo) throws CertificateStoreException {
        File certFile = new File(storeDir, sNo+CERT_FILE_SUFFIX);
        if(!certFile.exists()) {
            throw new CertificateStoreException("No certificate with serial number "+sNo+" found.");
        }
        FileInputStream fin;
        try {
            fin = new FileInputStream(certFile);
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fin.close();
            return new String(data);
        } catch (Exception e) {
            throw new CertificateStoreException("Error while retrieving certificate.", e);
        }
    }
    
    /**
     * This method returns the highest certificate serial number in the store.
     */
    public BigInteger getHighestSerialNumber() throws CertificateStoreException{
        if(highestSerialNumber == null) {
            // Value has not been cached.  Read from the disk.
            try {
                FileInputStream finp = new FileInputStream(highestSerialFile);
                byte[] data = new byte[finp.available()];
                finp.read(data);
                finp.close();
                highestSerialNumber = new BigInteger(new String(data).trim());
            } catch (Exception e) {
                throw new CertificateStoreException("Error while getting serial number.", e);
            }
        }
        return highestSerialNumber;
    }

    /**
     * This method returns the 'highest certificate serial number plus ONE' and increments the highest
     * serial number in the store.
     */
    public BigInteger getNextSerialNumber() throws CertificateStoreException{
        setHighestSerialNumber(getHighestSerialNumber().add(BigInteger.ONE));
        return highestSerialNumber;
    }

    /**
     * This method checks if a certificate with a given serial number exists in the store.
     * 
     * @param sNo Serial number of the certificate to be checked
     */
    public boolean containsCertificate(BigInteger sNo) {
        File certFile = new File(storeDir, sNo+CERT_FILE_SUFFIX);
        return certFile.exists();
    }
    
    /**
     * This method sets the highest serial number to a given value and updates the same to disk.
     * @param sNo The serial number to be set
     */
    private void setHighestSerialNumber(BigInteger sNo) throws CertificateStoreException{
        try {
            highestSerialNumber = sNo;
            FileOutputStream fout = new FileOutputStream(highestSerialFile);
            fout.write(highestSerialNumber.toString().getBytes());
            fout.close();
        } catch (Exception e) {
            throw new CertificateStoreException("Error while setting highest serial number.", e);
        }
    }
    
    /**
     * This method stores the CA's certificate in the store.
     * @param cert CA's certificate
     */
    public boolean storeCACertificate(Certificate cert) throws CertificateStoreException{
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(new File(storeDir, CA_CERT_FILE));
            CaUtils.storeInBase64(fout, cert.getEncoded(), CaUtils.CERT_HEADER, CaUtils.CERT_FOOTER, CaUtils.B64_LINE_SIZE);
            fout.close();
            return true;
        } catch (Exception e) {
            throw new CertificateStoreException("Exception in storing CA certificate", e);
        }
    }

    /**
     * This method returns the CA's certificate stored in the store.
     */
    public Certificate getCACertificate() throws CertificateStoreException {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(new File(storeDir, CA_CERT_FILE));
            CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            Certificate cert = certFac.generateCertificate(fin);
            fin.close();
            return cert;
        } catch (Exception e) {
            throw new CertificateStoreException("Exception in getting CA certificate", e);
        }
    }
    
    /**
     * This method stores the challenge phrase against the specified certificate serial number
     * @param sNo  Serial number of the certificate
     * @param challenge Challenge phrase
     */
    public boolean setCertificateChallenge(BigInteger sNo, String challenge) {
        if(challenges == null) {
            loadChallenges();
        }
        if(!challenges.containsKey(sNo.toString())) {
            challenges.setProperty(sNo.toString(), challenge);
            storeChallenges();
            return true;
        }
        return false;
    }

    /**
     * This methods stores the challenges map to disk
     */
    private void storeChallenges() {
        if(challenges == null) loadChallenges();
        File chFile = new File(storeDir, CHALLENGE_FILENAME);
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(chFile);
            challenges.store(fout, CHALLENGE_FILE_HEADER);
            fout.close();
        } catch (Exception e) {
            log.error("Exceptions while storing challenges file. File = "+chFile.getAbsolutePath(), e);
        }
        
    }
    
    /**
     * This method loads the challenges map from disk.
     */
    private void loadChallenges() {
        File chFile = new File(storeDir, CHALLENGE_FILENAME);
        FileInputStream fin = null;
        try {
            if(!chFile.exists())
                chFile.createNewFile();
            fin = new FileInputStream(chFile);
            challenges = new Properties();
            challenges.load(fin);
            fin.close();
        } catch (IOException e) {
            log.error("Exceptions while loading challenges file. File = "+chFile.getAbsolutePath(), e);
        }
    }
    
    public void doFail() {
    }

    public void doStart() throws Exception {
        serverInfo.resolveServer(directoryPath);
        URI dirURI;
        if (serverInfo != null) {
            dirURI = serverInfo.resolve(directoryPath);
        } else {
            dirURI = directoryPath;
        }
        if (!dirURI.getScheme().equals("file")) {
            throw new IllegalStateException("FileCertificateStore must have a root that's a local directory (not " + dirURI + ")");
        }
        storeDir = new File(dirURI);
        if(!storeDir.exists()) {
            storeDir.mkdirs();
            log.debug("Created directory "+storeDir.getAbsolutePath());
        } else if(!storeDir.isDirectory() || !storeDir.canRead()) {
            throw new IllegalStateException("FileCertificateStore must have a root that's a valid readable directory (not " + storeDir.getAbsolutePath() + ")");
        }
        log.debug("CertificateStore directory is " + storeDir.getAbsolutePath());
        highestSerialFile = new File(storeDir, SERIAL_NUMBER_FILE);
        if(!highestSerialFile.exists()) {
            // If the file does not exist, it means the certificate store is a new one.
            // Start with ZERO
            try {
                setHighestSerialNumber(BigInteger.ZERO);
            } catch(CertificateStoreException e) {
                log.error("Error initializing certificate store. storeDir="+storeDir, e);
            }
        }
        loadChallenges();
    }

    public void doStop() throws Exception {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileCertificateStore.class, SecurityNames.CERTIFICATE_STORE);
        infoFactory.addAttribute("directoryPath", URI.class, true, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addInterface(CertificateStore.class);
        infoFactory.setConstructor(new String[]{"ServerInfo", "directoryPath", "kernel", "abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
