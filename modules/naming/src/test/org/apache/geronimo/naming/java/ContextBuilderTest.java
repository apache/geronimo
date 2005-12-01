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
import java.util.Map;
import java.util.HashMap;
import javax.management.ObjectName;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.Context;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;

/**
 * @version $Rev$ $Date$
 */
public class ContextBuilderTest extends TestCase {
    private ComponentContextBuilder builder;

    private List proxy;

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
        builder.addEnvEntry("string", String.class.getName(), stringVal, null);
        builder.addEnvEntry("char", Character.class.getName(), charVal.toString(), null);
        builder.addEnvEntry("byte", Byte.class.getName(), byteVal.toString(), null);
        builder.addEnvEntry("short", Short.class.getName(), shortVal.toString(), null);
        builder.addEnvEntry("int", Integer.class.getName(), intVal.toString(), null);
        builder.addEnvEntry("long", Long.class.getName(), longVal.toString(), null);
        builder.addEnvEntry("float", Float.class.getName(), floatVal.toString(), null);
        builder.addEnvEntry("double", Double.class.getName(), doubleVal.toString(), null);
        builder.addEnvEntry("boolean", Boolean.class.getName(), booleanVal.toString(), null);

        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(builder.getContext());
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

    public void xtestResourceEnv() throws Exception {
        proxy = new ArrayList();
//        builder.addResourceEnvRef("resourceenvref", List.class, localRef);

        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(builder.getContext());
        Kernel kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();
        try {
            assertEquals(kernel, KernelRegistry.getKernel("test.kernel"));
            ObjectName proxyFactoryName = null;//referenceFactory.createAdminObjectObjectName("testAdminObject");
            GBeanData gbean = new GBeanData(proxyFactoryName, getGbeanInfo());
            gbean.setAttribute("Content", proxy);
            kernel.loadGBean(gbean, Class.forName(gbean.getGBeanInfo().getClassName()).getClassLoader());
            kernel.startGBean(proxyFactoryName);
            Object o = context.lookup("env/resourceenvref");
            assertEquals(proxy, o);
        } finally {
            kernel.shutdown();
        }
    }

    public void testEmptyEnvironment() throws NamingException {
        Context context = EnterpriseNamingContext.createEnterpriseNamingContext(builder.getContext());
        try {
            Context env = (Context) context.lookup("env");
            assertNotNull(env);
        } catch (NamingException e) {
            fail();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
//        referenceFactory = new JMXReferenceFactory("geronimo.server", "geronimo");
        builder = new ComponentContextBuilder();
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
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ContextBuilderTest.class, TestProxyFactory.class);
        infoFactory.addAttribute("Content", Object.class, true);
        infoFactory.addOperation("getProxy");
        infoFactory.setConstructor(new String[]{"Content"});
        return infoFactory.getBeanInfo();
    }
}
