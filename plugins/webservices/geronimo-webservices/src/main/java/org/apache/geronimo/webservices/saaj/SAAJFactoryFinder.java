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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.soap.SOAPException;

import org.apache.geronimo.osgi.registry.api.ProviderRegistry;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SAAJFactoryFinder {

    private static final Logger LOG = LoggerFactory.getLogger(SAAJFactoryFinder.class);

    private static final String SAAJ_PROVIDER_PROPERTY = "org.apache.geronimo.saaj.provider";

    private static final Map<String, Map<String, String>> SAAJ_FACTORIES = new HashMap<String, Map<String, String>>();

    private static SAAJUniverse.Type DEFAULT_SAAJ_UNIVERSE = null;

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

        initDefaultSAAJProvider();
    }

    private static void initDefaultSAAJProvider() {
        String provider = System.getProperty(SAAJ_PROVIDER_PROPERTY);
        if (provider != null) {
            if (provider.equalsIgnoreCase("axis2")) {
                DEFAULT_SAAJ_UNIVERSE = SAAJUniverse.Type.AXIS2;
            } else if (provider.equalsIgnoreCase("sun")) {
                DEFAULT_SAAJ_UNIVERSE = SAAJUniverse.Type.SUN;
            } else {
                throw new RuntimeException("Invalid SAAJ universe specified: " + provider);
            }

            LOG.info("Default SAAJ universe: " + DEFAULT_SAAJ_UNIVERSE);
        } else {
            LOG.info("Default SAAJ universe not set");
        }
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
            throw new SOAPException("Provider for " + factoryPropertyName + " cannot be found", null);
        } else {
            return newInstance(factoryPropertyName, factoryClassName);
        }
    }

    private static String getFactoryClass(String factoryName) {
        SAAJUniverse.Type universe = SAAJUniverse.getCurrentUniverse();
        if (universe == null || universe == SAAJUniverse.Type.DEFAULT) {
            if (DEFAULT_SAAJ_UNIVERSE == null) {
                // Default SAAJ universe not set.
                // Prefer Axis2 SAAJ if it is in class loader, otherwise use Sun's
                if (isAxis2Available()) {
                    universe = SAAJUniverse.Type.AXIS2;
                } else {
                    universe = SAAJUniverse.Type.SUN;
                }
            } else {
                // Use default SAAJ universe
                universe = DEFAULT_SAAJ_UNIVERSE;
            }
        }

        return SAAJ_FACTORIES.get(universe.toString()).get(factoryName);
    }

    private static boolean isAxis2Available() {
        try {
            loadClass("javax.xml.soap.MessageFactory", "org.apache.axis2.saaj.MessageFactoryImpl");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Class<?> loadClass(String providerId, String className) throws ClassNotFoundException {
        //1. Use the traditional classLoader search strategy
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (classLoader != null) {
                return classLoader.loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e1) {
            }
        }
        //2. Use the bundle search strategy
        List<Class<?>> serviceClasses = getServiceClasses(providerId);
        for (Class<?> cls : serviceClasses) {
            if (cls.getName().equals(className)) {
                return cls;
            }
        }
        throw new ClassNotFoundException("class " + className + " could not be founded in both classpath and bundle registry");
    }

    private static List<Class<?>> getServiceClasses(String providerid) {
        Bundle bundle = BundleUtils.getContextBundle(true);
        if (bundle == null || bundle.getBundleContext() == null) {
            bundle = BundleUtils.getBundle(SAAJFactoryFinder.class.getClassLoader(), true);
        }
        if (bundle == null || bundle.getBundleContext() == null) {
            return Collections.<Class<?>>emptyList();
        }
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference serviceReference = null;
        try {
            serviceReference = bundleContext.getServiceReference("org.apache.geronimo.osgi.registry.api.ProviderRegistry");
            if (serviceReference != null) {
                ProviderRegistry registry = (ProviderRegistry) bundleContext.getService(serviceReference);
                return registry.getServiceClasses(providerid);
            }
            return Collections.<Class<?>>emptyList();
        } finally {
            if (serviceReference != null) {
                try {
                    bundleContext.ungetService(serviceReference);
                } catch (Exception e) {
                }
            }
        }

    }

    private static Object newInstance(String providerId, String factoryClassName) throws SOAPException {
        try {
            Class<?> factory = loadClass(providerId, factoryClassName);
            return factory.newInstance();
        } catch (ClassNotFoundException e) {
            throw new SOAPException(e);
        } catch (Exception e) {
            throw new SOAPException("Provider " + factoryClassName + " could not be instantiated: " + e.getMessage(), e);
        }
    }
}
