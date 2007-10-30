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
package org.apache.geronimo.ca.helper.util;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.management.geronimo.CertificateRequestStore;
import org.apache.geronimo.management.geronimo.CertificateStore;
import org.apache.geronimo.management.geronimo.SecureConnector;

/**
 * This class implements some methods used by the CA Helper Application.
 *
 * @version $Rev$ $Date$
 */
public class CAHelperUtils {
    /**
     * This method removes a certificate request stored in the CertificateRequestStore.
     * @param csrId Id of the CSR to be removed.
     * @param sNo Serial number of the certificate issued in response to the CSR to be removed.
     */
    public static void removeRequest(String csrId, BigInteger sNo) {
        getCertificateRequestStore().removeRequestStatus(csrId, sNo);
    }
    
    /**
     * This method returns the CertificateRequestStore.
     */
    public static CertificateRequestStore getCertificateRequestStore() {
        Kernel kernel = KernelRegistry.getSingleKernel();
        
        AbstractNameQuery certReqStoreQuery = new AbstractNameQuery(org.apache.geronimo.management.geronimo.CertificateRequestStore.class.getName());
        Set set = kernel.listGBeans(certReqStoreQuery);
        try {
            CertificateRequestStore certReqStore = (CertificateRequestStore)kernel.getGBean((AbstractName)set.iterator().next());
            return certReqStore;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * This method returns the CertificateStore.
     */
    public static CertificateStore getCertificateStore() {
        Kernel kernel = KernelRegistry.getSingleKernel();
        
        AbstractNameQuery certStoreQuery = new AbstractNameQuery(org.apache.geronimo.management.geronimo.CertificateStore.class.getName());
        Set set = kernel.listGBeans(certStoreQuery);
        try {
            CertificateStore certStore = (CertificateStore)kernel.getGBean((AbstractName)set.iterator().next());
            return certStore;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method returns a port configured for HTTPS ClientAuthentication.
     * 
     * @return Port configured for HTTPS Client Authentication.
     * @return -1 if no HTTPS Client Authentication Connector is configured.
     */
    public static int getHttpsClientAuthPort() {
        Kernel kernel = KernelRegistry.getSingleKernel();
        
        AbstractNameQuery connectorQuery = new AbstractNameQuery(SecureConnector.class.getName());
        Set set = kernel.listGBeans(connectorQuery);
        for(Iterator itr = set.iterator(); itr.hasNext(); ){
            try {
                SecureConnector connector = (SecureConnector)kernel.getGBean((AbstractName)itr.next());
                if(connector.isClientAuthRequired())
                    return connector.getPort();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
