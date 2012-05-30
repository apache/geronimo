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
package org.apache.geronimo.system.properties;

import java.security.Provider;
import java.security.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev: 5066 $ $Date: 2007-04-27 12:07:23 -0400 (Fri, 27 Apr 2007) $
 */
public class JvmVendor {

    private static final Logger log = LoggerFactory.getLogger(JvmVendor.class);

    private static final String JVM_VENDOR_PROPERTY_NAME = "java.vm.vendor";

    private static final boolean sun;

    private static final boolean ibm;

    private static final boolean apache;
    
    private static final boolean ibmHybrid;

    private JvmVendor () {
    }

    static {
        String fullVendorName = getFullName();
        boolean bApache = fullVendorName.regionMatches(true, 0, "Apache", 0, 6);    // aka. Apache Harmony
        boolean bIBM = fullVendorName.regionMatches(true, 0, "IBM", 0, 3);          // aka. IBM, but not IBM Hybrid
        boolean bSun = !bIBM && !bApache;                                           // default all others to Sun
        boolean bHP = fullVendorName.regionMatches(true, 0, "Hewlett", 0, 7);       // aka. Hewlett-Packard Company
        boolean bIBMHybrid = false;
        
        // Special code for IBM Hybrid SDK (Sun JVM with IBM extensions on Solaris and HP-UX)
        if ( ((bSun == true) && (System.getProperty("os.name").equalsIgnoreCase("SunOS") == true)) ||
             ((bHP == true) && (System.getProperty("os.name").equalsIgnoreCase("HP-UX") == true)) )
        {
            log.debug("Looking for the IBM Hybrid SDK Extensions");
            // Check if provider IBMJSSE Provider is installed.
            try {
                if (Security.getProvider("com.ibm.jsse2.IBMJSSEProvider2") == null) {
                    // IBMJSSE Provider is not installed, install it
                    log.debug("Trying to load IBM JSSE2 Provider.");
                    Class c = Class.forName("com.ibm.jsse2.IBMJSSEProvider2");
                    Provider p = (Provider) c.newInstance();
                    Security.addProvider(p);
                    // Security.addProvider(new com.ibm.jsse2.IBMJSSEProvider2());
                    log.debug("Loaded the IBM JSSE2 Provider");
                } else {
                    log.debug("Found the IBM JSSE2 Provider: {}", Security.getProvider("com.ibm.jsse2.IBMJSSEProvider2"));
                }
                
                if (Security.getProvider("IBMCertPath") == null) {
                    // If we found IBMJSSE but not this one, then the JAVA_OPTS are probably messed up
                    log.debug("No IBMCertPath provider found.");
                    throw new RuntimeException("Could not find the IBMCertPath provider.");
                } else {
                    log.debug("Found the IBMCertPath Provider: {}", Security.getProvider("IBMCertPath"));
                }
                
                if (Security.getProvider("IBMJCE") == null) {
                    // If we found IBMJSSE but not this one, then the JAVA_OPTS are probably messed up
                    log.debug("No IBMJCE provider found.");
                    throw new RuntimeException("Could not find the IBMJCE provider.");
                } else {
                    log.debug("Found the IBMJCE Provider {}", Security.getProvider("IBMJCE"));
                }
                
                System.setProperty("java.protocol.handler.pkgs", "com.ibm.net.ssl.www2.protocol");
                
                // All of the expected IBM Extensions were found, so we must be using the IBM Hybrid JDK
                bSun = false;
                bApache = false;
                bIBM = true;
                bIBMHybrid = true;
            } catch (ClassNotFoundException e) {
                // Couldn't load the IBMJSSE Provider, so we must not be using the IBM Hybrid SDK
                log.debug("Caught Exception: {}", e.toString());
                log.debug("Could not load the IBM JSSE Provider.  Must be using the OS provider's Java.");
            } catch (Throwable t) {
                // Couldn't load the IBMJSSE Provider, so we must not be using the IBM Hybrid SDK
                log.debug("Caught Throwable: {}", t.toString());
                log.debug("Assume we could not load the IBM JSSE Provider and that we are using the OS provider's Java.");
            }
        }
        // now, set our statics
        apache = bApache;
        ibm = bIBM;
        ibmHybrid = bIBMHybrid;
        sun = bSun;
        // log what we found
        log.info(getJvmInfo());
    }

    public static String getFullName() {
        return System.getProperty(JVM_VENDOR_PROPERTY_NAME);
    }

    public static boolean isSun() {
        return sun;
    }

    public static boolean isIBM() {
        return ibm;
    }

    public static boolean isIBMHybrid() {
        return ibmHybrid;
    }

    public static boolean isApache() {
        return apache;
    }

    public static String getJvmInfo() {
        if (sun == true) {
            return new String("Sun JVM " + System.getProperty("java.version"));
        } else if (apache == true) {
            return new String("Apache Harmony JVM " + System.getProperty("java.version"));
        } else if (ibm == true) {
            return new String("IBM JVM " + System.getProperty("java.version"));
        } else if (ibmHybrid == true) {
            return new String("IBM Hybrid JVM " + System.getProperty("java.version") + " on " + System.getProperty("os.name"));
        } else {
            // should never happen
            return new String("Unknown JVM detected - " + getFullName());
        }
    }

}
