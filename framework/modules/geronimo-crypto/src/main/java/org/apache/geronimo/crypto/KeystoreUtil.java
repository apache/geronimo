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
package org.apache.geronimo.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class KeystoreUtil {
    /**
     * All KeyStore types available.
     */
    public static final Set<String> keystoreTypes;
    /**
     * The keystore types which allow an empty keystore saved to disk.
     */
    public static final Set<String> emptyKeystoreTypes;
    /**
     * The keystore types which allow certificate entries.
     */
    public static final Set<String> certKeystoreTypes;
    /**
     * The default keystore type.
     */
    public static final String defaultType;

    static {
        Set<String> ignoreKeystores = new HashSet<String>();
        ignoreKeystores.add("windows-my");
        ignoreKeystores.add("windows-root");
        
        TreeSet<String> tempKeystoreTypes = new TreeSet<String>();
        TreeSet<String> tempEmptyKeystoreTypes = new TreeSet<String>();
        TreeSet<String> tempCertKeystoreTypes = new TreeSet<String>();
        String tempDefaultType = null;
        Provider[] providers = Security.getProviders();
        char[] password = "emptypassword".toCharArray();

        // Certificate used to check if a keystore allows storing trusted
        String sampleCertText = "-----BEGIN CERTIFICATE-----\n"
            +"MIIBpzCCAVECBgEV+CystzANBgkqhkiG9w0BAQQFADBcMQswCQYDVQQDEwJNZTEQMA4GA1UECxMH\n"
            +"TXkgVW5pdDEPMA0GA1UEChMGTXkgT3JnMRAwDgYDVQQHEwdNeSBDaXR5MQswCQYDVQQIEwJBUDEL\n"
            +"MAkGA1UEBhMCSU4wHhcNMDcxMDMxMjIyNjU4WhcNMTcxMDI4MjIyNjU4WjBcMQswCQYDVQQDEwJN\n"
            +"ZTEQMA4GA1UECxMHTXkgVW5pdDEPMA0GA1UEChMGTXkgT3JnMRAwDgYDVQQHEwdNeSBDaXR5MQsw\n"
            +"CQYDVQQIEwJBUDELMAkGA1UEBhMCSU4wXDANBgkqhkiG9w0BAQEFAANLADBIAkEAlN7IscUYq5U9\n"
            +"d1TYVJaj5RQJLg39Gz9R9hB0hhOULSHOxeE0utTJvgBQcf+f39FgbGIdriJniyoubtCXGfSpxwID\n"
            +"AQABMA0GCSqGSIb3DQEBBAUAA0EACQN6ScbxzAjrrQ3Ciy8I7/qsgpQo4Nuhfo5cAU4rvcKnujs6\n"
            +"uGHAJrHMF/ROGl6kPZvFeoGXk5qjyKs8Kx5MJA==\n"
            +"-----END CERTIFICATE-----";
        Certificate sampleCert = null;

        try {
            CertificateFactory certFac = CertificateFactory.getInstance("X.509");
            sampleCert = certFac.generateCertificate(new ByteArrayInputStream(sampleCertText.getBytes()));
        } catch (Throwable ignored) {
        }
        for(Provider provider: providers) {
            for(Provider.Service service: provider.getServices()) {
                String type = service.getAlgorithm();
                if (service.getType().equals("KeyStore") && 
                    !ignoreKeystores.contains(type.toLowerCase())) {

                    tempKeystoreTypes.add(type);
                    if(type.equalsIgnoreCase(KeyStore.getDefaultType())) {
                        tempDefaultType = type;
                    }

                    ByteArrayOutputStream baos = null;
                    KeyStore ks = null;
                    try {
                        ks = KeyStore.getInstance(type);
                        ks.load(null);
                        baos = new ByteArrayOutputStream();
                        // Check if an empty keystore can be saved.
                        ks.store(baos, password);
                        tempEmptyKeystoreTypes.add(type);
                    } catch(Throwable ignored) {
                    } finally {
                        if(baos != null) {
                            try {baos.close();} catch(IOException ignored){}
                        }
                    }

                    try {
                        // Check if the keystore allows storing of certificate entries.
                        ks.setCertificateEntry("samplecert", sampleCert);
                        tempCertKeystoreTypes.add(type);
                    } catch(Throwable ignored) {
                    }
                }
            }
        }
        
        keystoreTypes = Collections.unmodifiableSortedSet(tempKeystoreTypes);
        emptyKeystoreTypes = Collections.unmodifiableSortedSet(tempEmptyKeystoreTypes);
        certKeystoreTypes = Collections.unmodifiableSortedSet(tempCertKeystoreTypes);
        defaultType = tempDefaultType;
    }
}
