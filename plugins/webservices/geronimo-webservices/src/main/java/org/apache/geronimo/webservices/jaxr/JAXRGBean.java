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
package org.apache.geronimo.webservices.jaxr;

import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * Simple GBean to provide access to a JAXR ConnectionFactory
 *
 * @version $Rev$ $Date$
 */
public class JAXRGBean implements ResourceSource {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ClassLoader cl;
    private final String connectionFactoryClass;

    public JAXRGBean(String connectionFactoryClass, ClassLoader cl) {
        this.cl = cl;
        this.connectionFactoryClass = connectionFactoryClass;
    }

    /**
     * Returns a fresh, new implementation of javax.xml.registry.ConnectionFactory
     *
     * @return ConnectionFactory
     */
    public Object $getResource() {
        if (connectionFactoryClass != null) {
            System.setProperty("javax.xml.registry.ConnectionFactoryClass", connectionFactoryClass);
            //TODO consider whether we should bypass the code below and just construct it ourselves, like it does.
        }
        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(cl);
        try {
            return ConnectionFactory.newInstance();
        } catch (JAXRException e) {
            log.error("Error creating ConnectionFactory", e);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

        return null;
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(JAXRGBean.class,
                NameFactory.JAXR_CONNECTION_FACTORY);

        infoFactory.addAttribute("connectionFactoryClass", String.class, true, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addOperation("$getResource");

        infoFactory.setConstructor(new String[] {"connectionFactoryClass", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
