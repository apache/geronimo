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
package org.apache.geronimo.gjndi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;

import junit.framework.TestCase;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.WritableURLContextFactory;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.naming.java.javaURLContextFactory;
import org.apache.xbean.naming.context.ContextUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractContextTest extends TestCase {
    protected static MockBundleContext bundleContext = new MockBundleContext(AbstractContextTest.class.getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), null);

    protected static Context jcaContext;
    
    protected static Context javaContext;
    static {
        try {
            WritableURLContextFactory jcaContextFactory = new WritableURLContextFactory("jca");
            jcaContext = (Context) jcaContextFactory.getObjectInstance(null, null, null, null);
            Hashtable jcaProperties = new Hashtable();
            jcaProperties.put("osgi.jndi.url.scheme", "jca");
            bundleContext.registerService(ObjectFactory.class.getName(), jcaContextFactory, jcaProperties);

            javaURLContextFactory javaContextFactory = new javaURLContextFactory();
            javaContext = (Context) javaContextFactory.getObjectInstance(null, null, null, null);
            Hashtable javaProperties = new Hashtable();
            jcaProperties.put("osgi.jndi.url.scheme", "java");
            bundleContext.registerService(ObjectFactory.class.getName(), javaContextFactory, javaProperties);

        } catch (Exception e) {
        }
    }
    
    private static MockInitialContextFactoryBuilder initialContextFactoryBuilder = new MockInitialContextFactoryBuilder(bundleContext);

    static {
        try {
            NamingManager.setInitialContextFactoryBuilder(initialContextFactoryBuilder);
        } catch (NamingException e) {
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        /* Create a jca context
        try {
            WritableURLContextFactory jcaContextFactory = new WritableURLContextFactory("jca");
            jcaContext = (Context) jcaContextFactory.getObjectInstance(null, null, null, null);
            Hashtable jcaProperties = new Hashtable();
            jcaProperties.put("osgi.jndi.url.scheme", "jca");
            bundleContext.registerService(ObjectFactory.class.getName(), jcaContextFactory, jcaProperties);

            javaURLContextFactory javaContextFactory = new javaURLContextFactory();
            javaContext = (Context) javaContextFactory.getObjectInstance(null, null, null, null);
            Hashtable javaProperties = new Hashtable();
            jcaProperties.put("osgi.jndi.url.scheme", "java");
            bundleContext.registerService(ObjectFactory.class.getName(), javaContextFactory, javaProperties);

        } catch (Exception e) {
        }*/
    }

    public static void assertEq(Map expected, Context actual) throws NamingException {
        AbstractContextTest.assertEq(ContextUtil.buildMapTree(expected), actual, actual, null);
    }

    public static void assertEq(Map expected, String pathInExpected, Context actual) throws NamingException {
        ContextUtil.Node node = ContextUtil.buildMapTree(expected);
        Name parsedName = actual.getNameParser("").parse(pathInExpected);
        for (int i = 0; i < parsedName.size(); i++) {
            String part = parsedName.get(i);
            Object value = node.get(part);
            if (value == null) {
                throw new NamingException("look for " + parsedName.getPrefix(i+1) + " in node tree is null ");
            }
            node = (ContextUtil.Node) value;
        }

        AbstractContextTest.assertEq(node, actual, actual, null);
    }

    private static void assertEq(ContextUtil.Node node, Context rootContext, Context currentContext, String path) throws NamingException {
        for (Iterator iterator = node.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String expectedName = (String) entry.getKey();
            Object expectedValue = entry.getValue();

            String fullName = path == null ? expectedName : path + "/" + expectedName;

            // verify we can lookup by string name and parsed name using the root context and current context
            Object value = AbstractContextTest.assertLookup(expectedValue, currentContext, expectedName);
            Object absoluteValue = AbstractContextTest.assertLookup(expectedValue, rootContext, fullName);
            assertSame(fullName, value, absoluteValue);

            if (expectedValue instanceof ContextUtil.Node) {
                ContextUtil.Node expectedNode = (ContextUtil.Node) expectedValue;

                // verufy listing of this context returns the expected results
                AbstractContextTest.assertList(expectedNode, currentContext, expectedName);
                AbstractContextTest.assertList(expectedNode, rootContext, fullName);

                AbstractContextTest.assertEq(expectedNode, rootContext, (Context) value, fullName);
            }
        }
    }

    public static Object assertLookup(Object expectedValue, Context context, String name) throws NamingException {
        Object value = context.lookup(name);

        String contextName = context.getNameInNamespace();
        if (contextName == null || contextName.length() == 0) contextName = "<root>";

        assertNotNull("lookup of " +  name + " on " + contextName + " returned null", value);

        if (expectedValue instanceof ContextUtil.Node) {
            assertTrue("Expected lookup of " +  name + " on " + contextName + " to return a Context, but got a " + value.getClass().getName(),
                    value instanceof Context);
        } else {
            assertEquals("lookup of " + name + " on " + contextName, expectedValue, value);
        }

        Name parsedName = context.getNameParser("").parse(name);
        Object valueFromParsedName = context.lookup(parsedName);
        assertSame("lookup of " +  name + " on " + contextName + " using a parsed name", value, valueFromParsedName);

        return value;
    }

    public static void assertList(ContextUtil.Node node, Context context, String name) throws NamingException {
        String contextName = context.getNameInNamespace();
        if (contextName == null || contextName.length() == 0) contextName = "<root>";

        AbstractContextTest.assertListResults(node, context.list(name), contextName, name, false);
        AbstractContextTest.assertListResults(node, context.listBindings(name), contextName, name, true);

        Name parsedName = context.getNameParser("").parse(name);
        AbstractContextTest.assertListResults(node, context.list(parsedName), contextName, "parsed name " + name, false);
        AbstractContextTest.assertListResults(node, context.listBindings(parsedName), contextName, "parsed name " + name, true);
    }

    public static void assertListResults(ContextUtil.Node node, NamingEnumeration enumeration, String contextName, String name, boolean wasListBinding) {
        Map actualValues;
        if (wasListBinding) {
            actualValues = AbstractContextTest.toListBindingResults(enumeration);
        } else {
            actualValues = AbstractContextTest.toListResults(enumeration);
        }

        for (Iterator iterator = node.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String expectedName = (String) entry.getKey();
            Object expectedValue = entry.getValue();

            Object actualValue = actualValues.get(expectedName);

            assertNotNull("list of " + name + " on " + contextName + " did not find value for " + expectedName, actualValue);
            if (wasListBinding) {
                if (expectedValue instanceof ContextUtil.Node) {
                    assertTrue("Expected list of " + name + " on " + contextName + " result value for " + expectedName + " to return a Context, but got a " + actualValue.getClass().getName(),
                        actualValue instanceof Context);
                } else {
                    assertEquals("list of " + name + " on " + contextName + " for value for " + expectedName, expectedValue, actualValue);
                }
            } else {
                if (!(expectedValue instanceof ContextUtil.Node)) {
                    assertEquals("list of " + name + " on " + contextName + " for value for " + expectedName, expectedValue.getClass().getName(), actualValue);
                } else {
                    // can't really test this since it the value is the name of a nested node class
                }
            }
        }

        TreeSet extraNames = new TreeSet(actualValues.keySet());
        extraNames.removeAll(node.keySet());
        if (!extraNames.isEmpty()) {
            fail("list of " + name + " on " + contextName + " found extra values: " + extraNames);
        }
    }

    private static Map toListResults(NamingEnumeration enumeration) {
        Map result = new HashMap();
        while (enumeration.hasMoreElements()) {
            NameClassPair nameClassPair = (NameClassPair) enumeration.nextElement();
            String name = nameClassPair.getName();
            assertFalse(result.containsKey(name));
            result.put(name, nameClassPair.getClassName());
        }
        return result;
    }

    private static Map toListBindingResults(NamingEnumeration enumeration) {
        Map result = new HashMap();
        while (enumeration.hasMoreElements()) {
            Binding binding = (Binding) enumeration.nextElement();
            String name = binding.getName();
            assertFalse(result.containsKey(name));
            result.put(name, binding.getObject());
        }
        return result;
    }
    
    private static class MockInitialContextFactoryBuilder implements InitialContextFactoryBuilder, InitialContextFactory {

        private MockContext mockContext;

        public MockInitialContextFactoryBuilder(BundleContext bundleContext) {
            mockContext = new MockContext(bundleContext);
        }

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment)
                throws NamingException {
            return mockContext;
        }

        @Override
        public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
            return this;
        }
    }

    private static class MockContext implements Context {

       private BundleContext bundleContext;
       private String scheme;
       private Context context;

        public MockContext(BundleContext bundleContext) {
           this.bundleContext = bundleContext;
       }

        @Override
        public Object addToEnvironment(String propName, Object propVal) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void bind(Name name, Object obj) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public void bind(String name, Object obj) throws NamingException {            
            // TODO Auto-generated method stub            
            int colonIndex = name.indexOf(":");
            if(colonIndex == -1) {
                throw new NamingException(name + " not found");
            }
            String scheme = name.substring(0, colonIndex);
            setScheme(scheme);
            if (scheme.equals("java")){
                javaContext.bind(name, obj);
            }else if (scheme.equals("jca")){
                jcaContext.bind(name, obj);
            }

        }

        @Override
        public void close() throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public Name composeName(Name name, Name prefix) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String composeName(String name, String prefix) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Context createSubcontext(Name name) throws NamingException {
            // TODO Auto-generated method stub
            this.context.createSubcontext(name);
            return null;
        }

        @Override
        public Context createSubcontext(String name) throws NamingException {
            // TODO Auto-generated method stub
            int colonIndex = name.indexOf(":");
            if(colonIndex == -1) {
                throw new NamingException(name + " not found");
            }
            String scheme = name.substring(0, colonIndex);
            setScheme(scheme);
            //TODO get from osgi service
            /*ServiceReference[] sr;
            try {
                sr = bundleContext.getServiceReferences(ObjectFactory.class.getName(), "(osgi.jndi.url.scheme=" + scheme +")");
                if (sr != null) {
                        ObjectFactory objectFactory = (ObjectFactory) bundleContext.getService(sr[0]);
                        // Get context
                        context = (Context) objectFactory.getObjectInstance(null, null, null, null);
                        return context.createSubcontext(name);
                    }
            } catch (InvalidSyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }*/
            if (scheme.equals("java")){
                return javaContext.createSubcontext(name);
            }else if (scheme.equals("jca")){
                return jcaContext.createSubcontext(name);
            }
            return null;
        }

        @Override
        public void destroySubcontext(Name name) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public void destroySubcontext(String name) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public Hashtable<?, ?> getEnvironment() throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getNameInNamespace() throws NamingException {
            // TODO Auto-generated method stub
            
            return null;
        }

        @Override
        public NameParser getNameParser(Name name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NameParser getNameParser(String name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NamingEnumeration<NameClassPair> list(String name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public NamingEnumeration<Binding> listBindings(String name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object lookup(Name name) throws NamingException {
            // TODO Auto-generated method stub
            if (name instanceof CompositeName){
                CompositeName cn = (CompositeName) name;
                String scheme = cn.get(0).substring(0, cn.get(0).indexOf(":"));
                setScheme(scheme);
                if (scheme.equals("java")){
                    return javaContext.lookup(name);
                }else if (scheme.equals("jca")){
                    return jcaContext.lookup(name);
                }
            }else if (name instanceof CompoundName){
                
            }
            return null;
        }

        @Override
        public Object lookup(String name) throws NamingException {
            int colonIndex = name.indexOf(":");
            if(colonIndex == -1) {                
                    throw new NamingException(name + " not found");
            }else {
            String scheme = name.substring(0, colonIndex);
            setScheme(scheme);
            //TODO get from osgi service, MockServiceReference, MockService need to update
           /* ServiceReference[] sr;
            try {
                sr = bundleContext.getServiceReferences(ObjectFactory.class.getName(), "(osgi.jndi.url.scheme=" + scheme +")");
                if (sr != null) {
                        ObjectFactory objectFactory = (ObjectFactory) bundleContext.getService(sr[0]);
                        // Get context
                        this.context = (Context) objectFactory.getObjectInstance(null, null, null, null);
                        return context.lookup(name);
                    }
            } catch (InvalidSyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }*/
             if (scheme.equals("java")){
                return javaContext.lookup(name);
            }else if (scheme.equals("jca")){
                return jcaContext.lookup(name);
            }
            }
            
            return null;
        }

        @Override
        public Object lookupLink(Name name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object lookupLink(String name) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void rebind(Name name, Object obj) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public void rebind(String name, Object obj) throws NamingException {
            // TODO Auto-generated method stub                  
            int colonIndex = name.indexOf(":");
            if(colonIndex == -1) {
                throw new NamingException(name + " not found");
            }
            String scheme = name.substring(0, colonIndex);
            setScheme(scheme);
            if (scheme.equals("java")){
                javaContext.rebind(name, obj);
            }else if (scheme.equals("jca")){
                jcaContext.rebind(name, obj);
            }

        }

        @Override
        public Object removeFromEnvironment(String propName) throws NamingException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void rename(Name oldName, Name newName) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public void rename(String oldName, String newName) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public void unbind(Name name) throws NamingException {
            // TODO Auto-generated method stub

        }

        @Override
        public void unbind(String name) throws NamingException {
            // TODO Auto-generated method stub

        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getScheme() {
            return scheme;
        }
    }
}
