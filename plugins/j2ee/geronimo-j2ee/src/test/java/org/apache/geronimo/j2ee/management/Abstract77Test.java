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

package org.apache.geronimo.j2ee.management;

import java.util.HashSet;
import java.util.Set;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.HashMap;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.management.impl.J2EEDomainImpl;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.j2ee.management.impl.JVMImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.AppClientModule;
import org.apache.geronimo.management.geronimo.WebModule;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;
import org.apache.geronimo.management.geronimo.J2EEApplication;
import org.apache.geronimo.management.geronimo.J2EEServer;
import org.apache.geronimo.management.geronimo.JVM;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public abstract class Abstract77Test extends TestCase {
    protected static final GBeanData SERVER_INFO_DATA = buildGBeanData(new String[] {"role"}, new String[] {"ServerInfo"}, BasicServerInfo.class);

    protected static final String DOMAIN = "geronimo.test";
    protected static final GBeanData DOMAIN_DATA = buildGBeanData(new String[] {"j2eeType", "name"}, new String[] {"J2EEDomain", DOMAIN}, J2EEDomainImpl.class);
    protected static final GBeanData SERVER_DATA = buildGBeanData(new String[] {"j2eeType", "name"}, new String[] {"J2EEServer", "test"}, J2EEServerImpl.class);
    protected static final GBeanData JVM_DATA = buildGBeanData(new String[] {"j2eeType", "J2EEServer", "name"}, new String[] {"JVM", "test", "JVM"}, JVMImpl.class);

    protected Kernel kernel;

    private static GBeanData buildGBeanData(String[] key, String[] value, Class info) {
        AbstractName abstractName = buildAbstractName(key, value);
        return new GBeanData(abstractName, info);
    }

    private static AbstractName buildAbstractName(String[] key, String value[]) {
        Hashtable names = new Hashtable();
        for (int i = 0; i < key.length; i++) {
            String k = key[i];
            String v = value[i];
            names.put(k, v);
        }

        ObjectName objectName;
        try {
            objectName = new ObjectName(DOMAIN, names);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Malformed ObjectName: " + DOMAIN + ":" + names);
        }
        return new AbstractName(new Artifact("test", "foo", "1", "car"), names, objectName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);
        kernel = KernelFactory.newInstance(bundleContext).createKernel(DOMAIN);
        kernel.boot(bundleContext);

        // server info
        SERVER_INFO_DATA.setAttribute("baseDirectory", System.getProperty("java.io.tmpdir"));
        kernel.loadGBean(SERVER_INFO_DATA, bundleContext);

        // domain
        DOMAIN_DATA.setReferencePatterns("Servers", new ReferencePatterns(new AbstractNameQuery(J2EEServer.class.getName())));
        kernel.loadGBean(DOMAIN_DATA, bundleContext);

        // server
        SERVER_DATA.setReferencePattern("ServerInfo", SERVER_INFO_DATA.getAbstractName());
        SERVER_DATA.setReferencePatterns("JVMs", new ReferencePatterns(new AbstractNameQuery(JVM.class.getName())));
        LinkedHashSet resourcePatterns = new LinkedHashSet();
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JAVA_MAIL_RESOURCE), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JCA_CONNECTION_FACTORY), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JDBC_RESOURCE), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JMS_RESOURCE), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JNDI_RESOURCE), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.JTA_RESOURCE), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.RMI_IIOP_RESOURCE), J2EEResource.class.getName()));
        resourcePatterns.add(new AbstractNameQuery(null, Collections.singletonMap(NameFactory.J2EE_TYPE, NameFactory.URL_RESOURCE), J2EEResource.class.getName()));
        SERVER_DATA.setReferencePatterns("Resources", resourcePatterns);
        SERVER_DATA.setReferencePatterns("Applications", new ReferencePatterns(new AbstractNameQuery(J2EEApplication.class.getName())));
        SERVER_DATA.setReferencePatterns("AppClientModules", new ReferencePatterns(new AbstractNameQuery(AppClientModule.class.getName())));
        SERVER_DATA.setReferencePatterns("EJBModules", new ReferencePatterns(new AbstractNameQuery(EJBModule.class.getName())));
        SERVER_DATA.setReferencePatterns("ResourceAdapterModules", new ReferencePatterns(new AbstractNameQuery(ResourceAdapterModule.class.getName())));
        SERVER_DATA.setReferencePatterns("WebModules", new ReferencePatterns(new AbstractNameQuery(WebModule.class.getName())));
        // Can't test, there are none of these available
        kernel.loadGBean(SERVER_DATA, bundleContext);

        // JVM
        kernel.loadGBean(JVM_DATA, bundleContext);

        // start um
        kernel.startGBean(SERVER_INFO_DATA.getAbstractName());
        kernel.startGBean(DOMAIN_DATA.getAbstractName());
        kernel.startGBean(SERVER_DATA.getAbstractName());
        kernel.startGBean(JVM_DATA.getAbstractName());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        kernel.stopGBean(JVM_DATA.getAbstractName());
        kernel.stopGBean(SERVER_DATA.getAbstractName());
        kernel.stopGBean(DOMAIN_DATA.getAbstractName());
        kernel.stopGBean(SERVER_INFO_DATA.getAbstractName());
        kernel.unloadGBean(JVM_DATA.getAbstractName());
        kernel.unloadGBean(SERVER_DATA.getAbstractName());
        kernel.unloadGBean(DOMAIN_DATA.getAbstractName());
        kernel.unloadGBean(SERVER_INFO_DATA.getAbstractName());
        kernel.shutdown();
        kernel = null;
    }

    protected void assertObjectNamesEqual(String[] expected, String[] test) throws Exception {
        Set expectedSet = new HashSet(expected.length);
        for (int i = 0; i < expected.length; i++) {
            expectedSet.add(new ObjectName(expected[i]));
        }
        Set testSet = new HashSet(test.length);
        for (int i = 0; i < test.length; i++) {
            testSet.add(new ObjectName(test[i]));
        }
        assertEquals(expectedSet, testSet);
    }
}
