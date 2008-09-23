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
import java.io.FilenameFilter;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.geronimo.CertificateRequestStore;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.security.SecurityNames;

/**
 * A certificate request store implementation using disk files.
 *
 * @version $Rev$ $Date$
 */
public class FileCertificateRequestStore implements CertificateRequestStore, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(FileCertificateRequestStore.class);
    
    // File name to store certificate request status
    private static final String CSR_STATUS_FILENAME = "csr-status.properties";
    // File header for certificate request status file
    private static final String CSR_STATUS_FILE_HEADER = "CSR Status File";
    // Status showing the request as received
    private static final String STATUS_RECEIVED = "R";
    // Status showing the request as verified
    private static final String STATUS_VERIFIED = "V";
    // Prefix for certificate request files
    private static final String CERT_REQ_FILE_PREFIX = "csr";
    // Extension for certificate request files
    private static final String CERT_REQ_FILE_SUFFIX = ".txt";
    
    private ServerInfo serverInfo;
    private Kernel kernel;
    private AbstractName abstractName;
    private URI directoryPath;
    private File dir;
    private Properties requestStatus;

    /**
     * Constructor
     */
    public FileCertificateRequestStore(ServerInfo serverInfo, URI directoryPath, Kernel kernel, AbstractName abstractName) {
        this.serverInfo = serverInfo;
        this.kernel = kernel;
        this.abstractName = abstractName;
        this.directoryPath = directoryPath;
    }

    /**
     * This method deletes a certificate request with the specified id.
     * @param id Id of the certificate request to be deleted.
     * @return True if the request is deleted succssfully
     */
    public boolean deleteRequest(String id) {
        if(requestStatus.containsKey(id)) {
            requestStatus.remove(id);
            storeRequestStatusFile();
        }
        return new File(dir, id+CERT_REQ_FILE_SUFFIX).delete();
    }

    /**
     * This method returns the ids of all certificate requests in the store.
     */
    public String[] getAllRequestIds() {
        File[] results = dir.listFiles(new FilenameFilter(){
                            public boolean accept(File dir, String name) {
                                return name.endsWith(CERT_REQ_FILE_SUFFIX);
                            }});
        String[] reqIds = new String[results.length];
        int suffixLength = CERT_REQ_FILE_SUFFIX.length();
        for(int i = 0; i < results.length; ++i) {
            String name = results[i].getName();
            reqIds[i] = name.substring(0, name.length() - suffixLength);
        }
        return reqIds;
    }

    /**
     * This method returns the ids of all certificate requests with verification due.
     */
    public String[] getVerificatonDueRequestIds() {
        ArrayList ids = new ArrayList();
        for(Iterator itr = requestStatus.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            if(entry.getValue().equals(STATUS_RECEIVED)) {
                ids.add(entry.getKey());
            }
        }
        
        return (String[]) ids.toArray(new String[0]);
    }

    /**
     * This method returns the ids of all certificate requests that are verified.
     */
    public String[] getVerifiedRequestIds() {
        ArrayList ids = new ArrayList();
        for(Iterator itr = requestStatus.entrySet().iterator(); itr.hasNext();) {
            Map.Entry entry = (Map.Entry) itr.next();
            if(entry.getValue().equals(STATUS_VERIFIED)) {
                ids.add(entry.getKey());
            }
        }
        
        return (String[]) ids.toArray(new String[0]);
    }
    
    /**
     * This method sets the status of the specifed certificate request as verified.
     * @param id Id of the certificate request
     * @return True if the status is set successfully.
     */
    public boolean setRequestVerified(String id) {
        if(requestStatus.containsKey(id)) {
            requestStatus.setProperty(id, STATUS_VERIFIED);
            storeRequestStatusFile();
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * This method sets the status of a certificate request as fulfilled.
     * @param id Id of the certificate request
     * @param sNo Serial number of the certificate issued against the certificate request.
     * @return True if the operation is successfull.
     */
    public boolean setRequestFulfilled(String id, BigInteger sNo) {
        if(requestStatus.containsKey(id)) {
            deleteRequest(id);
            requestStatus.setProperty(id, sNo.toString());
            storeRequestStatusFile();
            return true;
        } else {
            return false;
        }
    }

    /**
     * This method returns the certificate request text corresponding to a specified id.
     * @param id Id of the certificate request.
     */
    public String getRequest(String id) {
        try {
            FileInputStream fin = new FileInputStream(new File(dir, id+CERT_REQ_FILE_SUFFIX));
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fin.close();
            return new String(data);
        } catch (Exception e) {
            log.error("Error reading CSR. id = "+id, e);
        }
        return null;
    }

    /**
     * This method stores the given certificate request under the given id.  If a request with the id
     * exists in the store, it will generate a new id and store the request under that id.
     * @param id Id under which the certificate request is to be stored
     * @param csr Certificate Request text
     * @return Id under which the certificate request is stored
     */
    public String storeRequest(String id, String csr) {
        try {
            File csrFile = null;
            if(id == null || new File(dir, id+CERT_REQ_FILE_SUFFIX).exists()) {
                csrFile = File.createTempFile(CERT_REQ_FILE_PREFIX, CERT_REQ_FILE_SUFFIX, dir);
                id = csrFile.getName().substring(0, csrFile.getName().length() - CERT_REQ_FILE_SUFFIX.length());
            } else {
                csrFile = new File(dir, id+CERT_REQ_FILE_SUFFIX);
            }
            FileOutputStream fout = new FileOutputStream(csrFile);
            fout.write(csr.getBytes());
            requestStatus.setProperty(id, STATUS_RECEIVED);
            storeRequestStatusFile();
            fout.close();
            return id;
        } catch(Exception e) {
            log.error("Error storing CSR. id = "+id, e);
        }
        return null;
    }
    
    /**
     * This method returns the Serial number of the certificate issued against the certificate request
     * specified by the given id.
     * @param id Id of the certificate request
     * @return Serial number of the certificate issued.
     * @return null if there is no such certificate request or the certificate request is not fulfilled.
     */
    public BigInteger getSerialNumberForRequest(String id) {
        BigInteger sNo = null;
        if(requestStatus.getProperty(id) == null) {
            // No such request
            return null;
        }
        try {
            sNo = new BigInteger(requestStatus.getProperty(id));
        } catch(NumberFormatException e) {
            // happens if the certificate request is not fulfilled
        }
        return sNo;
    }

    /**
     * This method removes the certificate request id from the status list.
     * @param id Id of the certificate request to be removed.
     * @param sNo Serial number of certificate issued against the certificate request whose Id is to be removed.
     */
    public void removeRequestStatus(String id, BigInteger sNo) {
        if(id != null && requestStatus.containsKey(id)) {
            requestStatus.remove(id);
            storeRequestStatusFile();
        } else if(sNo != null && requestStatus.containsValue(sNo.toString())) {
            String sNoTemp = sNo.toString();
            for(Iterator itr = requestStatus.entrySet().iterator(); itr.hasNext(); ) {
                Map.Entry entry = (Map.Entry)itr.next();
                if(sNoTemp.equals(entry.getValue())) {
                    requestStatus.remove(entry.getKey());
                    break;
                }
            }
            storeRequestStatusFile();
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
            throw new IllegalStateException("FileCertificateRequestStore must have a root that's a local directory (not " + dirURI + ")");
        }
        dir = new File(dirURI);
        if(!dir.exists()) {
            dir.mkdirs();
            log.debug("Created directory "+dir.getAbsolutePath());
        } else if(!dir.isDirectory() || !dir.canRead()) {
            throw new IllegalStateException("FileCertificateRequestStore must have a root that's a valid readable directory (not " + dir.getAbsolutePath() + ")");
        }
        log.debug("CertificateRequestStore directory is " + dir.getAbsolutePath());
        File statusFile = new File(dir, CSR_STATUS_FILENAME);
        if(!statusFile.exists()) {
            statusFile.createNewFile();
            log.debug("Created request status file "+statusFile.getAbsolutePath());
        }
        requestStatus = new Properties();
        FileInputStream fin = new FileInputStream(statusFile);
        requestStatus.load(fin);
        fin.close();
    }

    public void doStop() throws Exception {
    }
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(FileCertificateRequestStore.class, SecurityNames.CERTIFICATE_REQUEST_STORE);
        infoFactory.addAttribute("directoryPath", URI.class, true, false);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addInterface(CertificateRequestStore.class);
        infoFactory.setConstructor(new String[]{"ServerInfo", "directoryPath", "kernel", "abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     * This methods stores the certificate request status file to disk.
     */
    private void storeRequestStatusFile() {
        File statusFile = new File(dir, CSR_STATUS_FILENAME);
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(statusFile);
            requestStatus.store(fout, CSR_STATUS_FILE_HEADER);
            fout.close();
        } catch (Exception e) {
            log.error("Errors while storing request status file "+statusFile.getAbsolutePath(), e);
        }
    }
}
