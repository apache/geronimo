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

package org.apache.geronimo.naming.deployment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.ConnectorModule;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.enc.EnterpriseNamingContext;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class EnvironmentEntryBuilderTest extends TestCase {
    private Map<EARContext.Key, Object> componentContext = new HashMap<EARContext.Key, Object>();
    private NamingBuilder environmentEntryBuilder = new EnvironmentEntryBuilder(new String[]{AbstractNamingBuilder.JEE_NAMESPACE});

    private static final String TEST = "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\">" +
            "<env-entry>" +
            "<env-entry-name>string</env-entry-name>" +
            "<env-entry-type>java.lang.String</env-entry-type>" +
            "<env-entry-value>Hello World</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>char</env-entry-name>" +
            "<env-entry-type>java.lang.Character</env-entry-type>" +
            "<env-entry-value>H</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>byte</env-entry-name>" +
            "<env-entry-type>java.lang.Byte</env-entry-type>" +
            "<env-entry-value>12</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>short</env-entry-name>" +
            "<env-entry-type>java.lang.Short</env-entry-type>" +
            "<env-entry-value>12345</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>int</env-entry-name>" +
            "<env-entry-type>java.lang.Integer</env-entry-type>" +
            "<env-entry-value>12345678</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>long</env-entry-name>" +
            "<env-entry-type>java.lang.Long</env-entry-type>" +
            "<env-entry-value>1234567890123456</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>float</env-entry-name>" +
            "<env-entry-type>java.lang.Float</env-entry-type>" +
            "<env-entry-value>123.456</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>double</env-entry-name>" +
            "<env-entry-type>java.lang.Double</env-entry-type>" +
            "<env-entry-value>12345.6789</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>boolean</env-entry-name>" +
            "<env-entry-type>java.lang.Boolean</env-entry-type>" +
            "<env-entry-value>TRUE</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>class</env-entry-name>" +
            "<env-entry-type>java.lang.Class</env-entry-type>" +
            "<env-entry-value>java.net.URI</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>enum</env-entry-name>" +
            "<env-entry-type>java.util.concurrent.TimeUnit</env-entry-type>" +
            "<env-entry-value>NANOSECONDS</env-entry-value>" +
            "</env-entry>" +
            "</web-app>";

    private static final String TEST_PLAN = "<tmp xmlns=\"http://geronimo.apache.org/xml/ns/naming-1.2\">" +
            "<env-entry>" +
            "<env-entry-name>string</env-entry-name>" +
            "<env-entry-value>Goodbye World</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>char</env-entry-name>" +
            "<env-entry-value>K</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>byte</env-entry-name>" +
            "<env-entry-value>21</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>short</env-entry-name>" +
            "<env-entry-value>4321</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>int</env-entry-name>" +
            "<env-entry-value>87654321</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>long</env-entry-name>" +
            "<env-entry-value>6543210987654321</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>float</env-entry-name>" +
            "<env-entry-value>654.321</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>double</env-entry-name>" +
            "<env-entry-value>9876.54321</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>boolean</env-entry-name>" +
            "<env-entry-value>FALSE</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>class</env-entry-name>" +
            "<env-entry-type>java.lang.Class</env-entry-type>" +
            "<env-entry-value>java.net.URL</env-entry-value>" +
            "</env-entry>" +

            "<env-entry>" +
            "<env-entry-name>enum</env-entry-name>" +
            "<env-entry-type>java.util.concurrent.TimeUnit</env-entry-type>" +
            "<env-entry-value>SECONDS</env-entry-value>" +
            "</env-entry>" +
            "</tmp>";

    private Module module;

    protected void setUp() throws Exception {
        Artifact artifact = new Artifact("foo", "bar", "1.0", "car");
        Map<String, Artifact> locations = new HashMap<String, Artifact>();
        locations.put(null, artifact);
        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", null, locations);
        Artifact id = new Artifact("test", "test", "", "car");
        module  = new ConnectorModule(false, new AbstractName(id, Collections.singletonMap("name", "test")), null, null, null, "foo", null, null, null, null, null);
        ConfigurationManager configurationManager = new MockConfigurationManager();
        EARContext earContext = new EARContext(new File("foo"),
            null,
            new Environment(artifact),
            ConfigurationModuleType.EAR,
            new Jsr77Naming(),
            configurationManager,
            bundleContext,
            null,
            null,
            null,
            null,
            null);
        earContext.initializeConfiguration();
        module.setEarContext(earContext);
        module.setRootEarContext(earContext);
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

        WebApp webApp = load(TEST, WebApp.class);
        environmentEntryBuilder.buildNaming(webApp, null, module, componentContext);
        Context context = EnterpriseNamingContext.livenReferences(module.getJndiScope(JndiScope.comp), null, null, getClass().getClassLoader(), null, "comp/");
        Set actual = new HashSet();
        for (NamingEnumeration e = context.listBindings("comp/env"); e.hasMore();) {
            NameClassPair pair = (NameClassPair) e.next();
            actual.add(pair.getName());
        }
        Set expected = new HashSet(Arrays.asList(new String[]{"string", "char", "byte", "short", "int", "long", "float", "double", "boolean", "class", "enum"}));
        assertEquals(expected, actual);
        assertEquals(stringVal, context.lookup("comp/env/string"));
        assertEquals(charVal, context.lookup("comp/env/char"));
        assertEquals(byteVal, context.lookup("comp/env/byte"));
        assertEquals(shortVal, context.lookup("comp/env/short"));
        assertEquals(intVal, context.lookup("comp/env/int"));
        assertEquals(longVal, context.lookup("comp/env/long"));
        assertEquals(floatVal, context.lookup("comp/env/float"));
        assertEquals(doubleVal, context.lookup("comp/env/double"));
        assertEquals(booleanVal, context.lookup("comp/env/boolean"));
        assertEquals(URI.class, context.lookup("comp/env/class"));
        assertEquals(TimeUnit.NANOSECONDS, context.lookup("comp/env/enum"));
    }

    public void testEnvEntriesOverride() throws Exception {

        String stringVal = "Goodbye World";
        Character charVal = new Character('K');
        Byte byteVal = new Byte((byte) 21);
        Short shortVal = new Short((short) 4321);
        Integer intVal = new Integer(87654321);
        Long longVal = new Long(6543210987654321L);
        Float floatVal = new Float(654.321);
        Double doubleVal = new Double(9876.54321);
        Boolean booleanVal = Boolean.FALSE;

        WebApp webApp = load(TEST, WebApp.class);
        XmlObject plan = XmlObject.Factory.parse(TEST_PLAN);
        XmlCursor cursor = plan.newCursor();
        try {
            cursor.toFirstChild();
            plan = cursor.getObject();
        } finally {
            cursor.dispose();
        }
        environmentEntryBuilder.buildNaming(webApp, plan, module, componentContext);
        Context context = EnterpriseNamingContext.livenReferences(module.getJndiScope(JndiScope.comp), null, null, getClass().getClassLoader(), null, "comp/");
        Set actual = new HashSet();
        for (NamingEnumeration e = context.listBindings("comp/env"); e.hasMore();) {
            NameClassPair pair = (NameClassPair) e.next();
            actual.add(pair.getName());
        }
        Set expected = new HashSet(Arrays.asList(new String[]{"string", "char", "byte", "short", "int", "long", "float", "double", "boolean", "class", "enum"}));
        assertEquals(expected, actual);
        assertEquals(stringVal, context.lookup("comp/env/string"));
        assertEquals(charVal, context.lookup("comp/env/char"));
        assertEquals(byteVal, context.lookup("comp/env/byte"));
        assertEquals(shortVal, context.lookup("comp/env/short"));
        assertEquals(intVal, context.lookup("comp/env/int"));
        assertEquals(longVal, context.lookup("comp/env/long"));
        assertEquals(floatVal, context.lookup("comp/env/float"));
        assertEquals(doubleVal, context.lookup("comp/env/double"));
        assertEquals(booleanVal, context.lookup("comp/env/boolean"));
        assertEquals(URL.class, context.lookup("comp/env/class"));
        assertEquals(TimeUnit.SECONDS, context.lookup("comp/env/enum"));
    }

    public void testEmptyEnvironment() throws NamingException {
        Context context = EnterpriseNamingContext.livenReferences(module.getJndiScope(JndiScope.comp), null, null, null, null, "comp/");
        Context env = (Context) context.lookup("comp/env");
        assertNotNull(env);
    }

    private <T> T load(String text, Class<T> clazz) throws Exception {
        InputStream in = new ByteArrayInputStream(text.getBytes());
        try {
            return (T) JaxbJavaee.unmarshalJavaee(clazz, in);
        } finally {
            in.close();
        }
    }


}
