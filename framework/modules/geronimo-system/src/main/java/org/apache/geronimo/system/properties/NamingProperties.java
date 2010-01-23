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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/** java.naming.factory.initial=com.sun.jndi.rmi.registry.RegistryContextFactory
java.naming.factory.url.pkgs=org.apache.geronimo.naming
java.naming.provider.url=rmi://localhost:1099

 */
public class NamingProperties {

    static final String JAVA_NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    static final String JAVA_NAMING_FACTORY_URL_PKGS = "java.naming.factory.url.pkgs";
    static final String JAVA_NAMING_PROVIDER_URL = "java.naming.provider.url";

    public NamingProperties(String namingFactoryInitial, String namingFactoryUrlPkgs, String namingProviderUrl) {
        setNamingFactoryInitial(namingFactoryInitial);
        if (namingFactoryUrlPkgs != null) {
            setNamingFactoryUrlPkgs(namingFactoryUrlPkgs);
        }
        setNamingProviderUrl(namingProviderUrl);

        try {
            // Calling this causes the System properties we just set
            // to be read in and cached by the vm ensuring we can't
            // be booted out by another module in the system.
            new InitialContext().lookup("java:");
        } catch (Throwable ignore) {
        }
    }

    public String getNamingFactoryInitial() {
        return System.getProperty(JAVA_NAMING_FACTORY_INITIAL);
    }

    public void setNamingFactoryInitial(String namingFactoryInitial) {
        System.setProperty(JAVA_NAMING_FACTORY_INITIAL, namingFactoryInitial);
    }

    public String getNamingFactoryUrlPkgs() {
        return System.getProperty(JAVA_NAMING_FACTORY_URL_PKGS);
    }

    public void setNamingFactoryUrlPkgs(String namingFactoryUrlPkgs) {
        if (namingFactoryUrlPkgs != null) {
            System.setProperty(JAVA_NAMING_FACTORY_URL_PKGS, namingFactoryUrlPkgs);
        } else {
            System.getProperties().remove(JAVA_NAMING_FACTORY_URL_PKGS);
        }
    }

    public String getNamingProviderUrl() {
        return System.getProperty(JAVA_NAMING_PROVIDER_URL);
    }

    public void setNamingProviderUrl(String namingProviderUrl) {
        System.setProperty(JAVA_NAMING_PROVIDER_URL, namingProviderUrl);
    }

    public static final GBeanInfo gbeanInfo;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(NamingProperties.class);
        infoFactory.addAttribute("namingFactoryInitial", String.class, true);
        infoFactory.addAttribute("namingFactoryUrlPkgs", String.class, true);
        infoFactory.addAttribute("namingProviderUrl", String.class, true, true);

        infoFactory.setConstructor(new String[] {"namingFactoryInitial", "namingFactoryUrlPkgs", "namingProviderUrl"});

        gbeanInfo = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return gbeanInfo;
    }
}
