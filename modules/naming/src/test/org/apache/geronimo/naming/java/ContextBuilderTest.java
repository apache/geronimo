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

package org.apache.geronimo.naming.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.ObjectName;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.deployment.RefAdapter;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.naming.ReferenceFactory;
import org.apache.xmlbeans.XmlObject;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class ContextBuilderTest extends TestCase {
    private ComponentContextBuilder builder;

    private List proxy;

    public void testFreeze() {
        ReadOnlyContext context = builder.getContext();
        assertTrue(context.isFrozen());
        try {
            builder.addEnvEntry(null, null, null);
            fail();
        } catch (IllegalStateException e) {
            // ok
        } catch (NamingException e) {
            fail();
        }
        try {
            builder.addUserTransaction(null);
            fail();
        } catch (IllegalStateException e) {
            // ok
        } catch (NamingException e) {
            fail();
        }
    }

    public void testEnvEntries() throws Exception {
        String stringVal = "Hello World";
        Character charVal = new Character('H');
        Byte byteVal = new Byte((byte) 12);
        Short shortVal = new Short((short) 12345);
        Integer intVal = new Integer(12345678);
        Long longVal = new Long(1234567890123456L);
        Float floatVal = new Float(123.456);
        Double doubleVal = new Double(12345.6789);
        Boolean booleanVal = Boolean.TRUE;
        builder.addEnvEntry("string", String.class.getName(), stringVal);
        builder.addEnvEntry("char", Character.class.getName(), charVal.toString());
        builder.addEnvEntry("byte", Byte.class.getName(), byteVal.toString());
        builder.addEnvEntry("short", Short.class.getName(), shortVal.toString());
        builder.addEnvEntry("int", Integer.class.getName(), intVal.toString());
        builder.addEnvEntry("long", Long.class.getName(), longVal.toString());
        builder.addEnvEntry("float", Float.class.getName(), floatVal.toString());
        builder.addEnvEntry("double", Double.class.getName(), doubleVal.toString());
        builder.addEnvEntry("boolean", Boolean.class.getName(), booleanVal.toString());

        ReadOnlyContext context = builder.getContext();
        Set actual = new HashSet();
        for (NamingEnumeration e = context.listBindings("env"); e.hasMore();) {
            NameClassPair pair = (NameClassPair) e.next();
            actual.add(pair.getName());
        }
        Set expected = new HashSet(Arrays.asList(new String[]{"string", "char", "byte", "short", "int", "long", "float", "double", "boolean"}));
        assertEquals(expected, actual);
        assertEquals(stringVal, context.lookup("env/string"));
        assertEquals(charVal, context.lookup("env/char"));
        assertEquals(byteVal, context.lookup("env/byte"));
        assertEquals(shortVal, context.lookup("env/short"));
        assertEquals(intVal, context.lookup("env/int"));
        assertEquals(longVal, context.lookup("env/long"));
        assertEquals(floatVal, context.lookup("env/float"));
        assertEquals(doubleVal, context.lookup("env/double"));
        assertEquals(booleanVal, context.lookup("env/boolean"));
    }

    public void testResourceEnv() throws Exception {
        proxy = new ArrayList();
        builder.addResourceEnvRef("resourceenvref", List.class, new RefAdapter() {
            public XmlObject getXmlObject() {
                return null;
            }

            public void setXmlObject(XmlObject xmlObject) {
            }

            public String getRefName() {
                return "resourceenvref";
            }

            public void setRefName(String name) {
            }

            public String getServerName() {
                return null;
            }

            public void setServerName(String serverName) {
            }

            public String getKernelName() {
                return "test.kernel";
            }

            public void setKernelName(String kernelName) {
            }

            public String getTargetName() {
                return "testAdminObject";
            }

            public void setTargetName(String targetName) {
            }

            public String getExternalUri() {
                return null;
            }

            public void setExternalUri(String externalURI) {
            }

        });
        ReadOnlyContext roc = builder.getContext();
        Kernel kernel = new Kernel("test.kernel", "test.domain");
        kernel.boot();
        try {
            assertEquals(kernel, Kernel.getKernel("test.kernel"));
            ObjectName proxyFactoryName = ObjectName.getInstance(JMXReferenceFactory.BASE_ADMIN_OBJECT_NAME + "testAdminObject");
            GBeanMBean gbean = new GBeanMBean(getGbeanInfo());
            gbean.setAttribute("Content", proxy);
            kernel.loadGBean(proxyFactoryName, gbean);
            kernel.startGBean(proxyFactoryName);
            Object o = roc.lookup("env/resourceenvref");
            assertEquals(proxy, o);
        } finally {
            kernel.shutdown();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        ReferenceFactory referenceFactory = new JMXReferenceFactory();
        builder = new ComponentContextBuilder(referenceFactory);
    }

    public static class TestProxyFactory {

        private Object proxy;

        public TestProxyFactory(Object proxy) {
            this.proxy = proxy;
        }

        public Object getProxy() {
            return proxy;
        }

    }

    public GBeanInfo getGbeanInfo() {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TestProxyFactory.class);
        infoFactory.addAttribute("Content", Object.class, true);
        infoFactory.addOperation("getProxy");
        infoFactory.setConstructor(new String[] {"Content"});
        return infoFactory.getBeanInfo();
    }
}
