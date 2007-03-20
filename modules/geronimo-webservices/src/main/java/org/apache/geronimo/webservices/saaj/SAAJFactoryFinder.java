/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.webservices.saaj;

import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.SOAPException;

class SAAJFactoryFinder {
    
    private static final Map<String, Map<String,String>> SAAJ_FACTORIES = 
        new HashMap<String, Map<String, String>>();
                                                     
    static {
        SAAJ_FACTORIES.put(SAAJUniverse.Type.AXIS1.toString(), 
                           createSAAJInfo("org.apache.axis.soap.MessageFactoryImpl", 
                                          "org.apache.axis.soap.SOAPFactoryImpl",
                                          "org.apache.axis.soap.SOAPConnectionFactoryImpl",
                                          "org.apache.axis.soap.SAAJMetaFactoryImpl"));
        SAAJ_FACTORIES.put(SAAJUniverse.Type.AXIS2.toString(), 
                           createSAAJInfo("org.apache.axis2.saaj.MessageFactoryImpl", 
                                          "org.apache.axis2.saaj.SOAPFactoryImpl", 
                                          "org.apache.axis2.saaj.SOAPConnectionFactoryImpl", 
                                          "org.apache.axis2.saaj.SAAJMetaFactoryImpl"));
        SAAJ_FACTORIES.put(SAAJUniverse.Type.SUN.toString(), 
                           createSAAJInfo("com.sun.xml.messaging.saaj.soap.ver1_1.SOAPMessageFactory1_1Impl", 
                                          "com.sun.xml.messaging.saaj.soap.ver1_1.SOAPFactory1_1Impl",
                                          "com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory", 
                                          "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl"));
        
        // set the default to SUN
        SAAJ_FACTORIES.put(SAAJUniverse.Type.DEFAULT.toString(), 
                           SAAJ_FACTORIES.get(SAAJUniverse.Type.SUN.toString()));
    }
    
    private static Map<String, String> createSAAJInfo(String messageFactory,
                                                      String soapFactory,
                                                      String soapConnectionFactory,
                                                      String metaFactory) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("javax.xml.soap.MessageFactory", messageFactory);
        map.put("javax.xml.soap.SOAPFactory", soapFactory);
        map.put("javax.xml.soap.SOAPConnectionFactory", soapConnectionFactory);
        map.put("javax.xml.soap.MetaFactory", metaFactory);
        return map;
    }
    
    static Object find(String factoryPropertyName) throws SOAPException {
        String factoryClassName = getFactoryClass(factoryPropertyName);
        if (factoryClassName == null) {
            throw new SOAPException(
                    "Provider for " + factoryPropertyName + " cannot be found",
                    null);
        } else {
            return newInstance(factoryClassName);
        }        
    }
    
    private static String getFactoryClass(String factoryName) {
        SAAJUniverse.Type universe = SAAJUniverse.getCurrentUniverse();
        if (universe == null) {
            return SAAJ_FACTORIES.get(SAAJUniverse.Type.DEFAULT.toString()).get(factoryName);
        } else {
            return SAAJ_FACTORIES.get(universe.toString()).get(factoryName);
        }
    }
    
    // a bit of code from axis2 saaj-api
    private static Object newInstance(String factoryClassName) throws SOAPException {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (Exception exception) {
            throw new SOAPException(exception.toString(), exception);
        }

        try {
            Class factory = null;
            if (classloader == null) {
                factory = Class.forName(factoryClassName);
            } else {
                try {
                    factory = classloader.loadClass(factoryClassName);
                } catch (ClassNotFoundException cnfe) {
                }
            }
            if (factory == null) {
                classloader = SAAJFactoryFinder.class.getClassLoader();
                factory = classloader.loadClass(factoryClassName);
            }
            return factory.newInstance();
        } catch (ClassNotFoundException classnotfoundexception) {
            throw new SOAPException("Provider " + factoryClassName + " not found",
                    classnotfoundexception);
        } catch (Exception exception) {
            throw new SOAPException("Provider " + factoryClassName + " could not be instantiated: "
                                    + exception, exception);
        }
    }
       
}
