/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.management.impl.J2EEDomainImpl;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.j2ee.management.impl.JVMImpl;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;

/**
 * @version $Rev: 386505 $ $Date$
 */
public abstract class Abstract77Test extends TestCase {
    protected static final GBeanData SERVER_INFO_DATA = buildGBeanData(new String[] {"role"}, new String[] {"ServerInfo"}, BasicServerInfo.getGBeanInfo());

    protected static final String DOMAIN = "geronimo.test";
    protected static final GBeanData DOMAIN_DATA = buildGBeanData(new String[] {"j2eeType", "name"}, new String[] {"J2EEDomain", DOMAIN}, J2EEDomainImpl.GBEAN_INFO);
    protected static final GBeanData SERVER_DATA = buildGBeanData(new String[] {"j2eeType", "name"}, new String[] {"J2EEServer", "test"}, J2EEServerImpl.GBEAN_INFO);
    protected static final GBeanData JVM_DATA = buildGBeanData(new String[] {"j2eeType", "J2EEServer", "name"}, new String[] {"JVM", "test", "JVM"}, JVMImpl.GBEAN_INFO);

    protected Kernel kernel;

    private static GBeanData buildGBeanData(String[] key, String[] value, GBeanInfo info) {
        AbstractName abstractName = buildAbstractName(key, value, info);
        return new GBeanData(abstractName, info);
    }

    private static AbstractName buildAbstractName(String[] key, String value[], GBeanInfo info) {
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
        Logger.getRootLogger().setLevel(Level.WARN);
        Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%p [%t] %m %n")));
        super.setUp();
        kernel = KernelFactory.newInstance().createKernel(DOMAIN);
        kernel.boot();

        ClassLoader classLoader = getClass().getClassLoader();
        SERVER_INFO_DATA.setAttribute("baseDirectory", System.getProperty("java.io.tmpdir"));
        kernel.loadGBean(SERVER_INFO_DATA, classLoader);

        kernel.loadGBean(DOMAIN_DATA, classLoader);

        SERVER_DATA.setReferencePattern("ServerInfo", SERVER_INFO_DATA.getAbstractName());
        kernel.loadGBean(SERVER_DATA, classLoader);


        kernel.loadGBean(JVM_DATA, classLoader);
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
