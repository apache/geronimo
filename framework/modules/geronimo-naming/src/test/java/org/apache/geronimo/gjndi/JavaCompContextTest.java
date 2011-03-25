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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.geronimo.naming.java.RootContext;
import org.apache.xbean.naming.context.ContextAccess;
import org.apache.xbean.naming.context.ImmutableContext;
import org.apache.xbean.naming.context.WritableContext;
import org.apache.xbean.naming.global.GlobalContextManager;

/**
 * @version $Rev$ $Date$
 */
public class JavaCompContextTest extends AbstractContextTest {
    protected Context readOnlyContext;
    protected Properties syntax;
    protected Map envBinding;
    protected Context initialContext;
    protected Context compContext;
    protected Context envContext;

    public void testInitialContext() throws NamingException {
        assertEquals("Hello", initialContext.lookup("java:comp/env/hello"));
        assertEquals("Hello", initialContext.lookup(new CompositeName("java:comp/env/hello")));
    }

    public void testLookup() throws NamingException {
        assertEquals("Hello", envContext.lookup("hello"));
        assertEquals("Hello", compContext.lookup("env/hello"));
        try {
            envContext.lookup("foo");
            fail();
        } catch (NamingException e) {
            // OK
        }
        assertEquals("Hello", envContext.lookup(new CompositeName("hello")));
        assertEquals("Hello", compContext.lookup(new CompositeName("env/hello")));
        //assertEquals("Hello", envContext.lookup(new CompoundName("hello", syntax)));
        //assertEquals("Hello", compContext.lookup(new CompoundName("env/hello", syntax)));

        assertEquals(envContext, envContext.lookup(""));
    }

    public void testSubContext() throws NamingException {
        assertEquals("long name", initialContext.lookup("java:comp/env/here/there/anywhere"));
        Context intermediate = (Context)initialContext.lookup("java:comp/env/here/there");
        assertNotNull(intermediate);
        assertEquals("long name", intermediate.lookup("anywhere"));
    }

   /* public void testLookupLink() throws NamingException {
        assertEquals("Hello", envContext.lookup("link"));
    }*/

    public void testComposeName() throws NamingException {
        assertEquals("org/research/user/jane", envContext.composeName("user/jane", "org/research"));
        assertEquals("research/user/jane", envContext.composeName("user/jane", "research"));
        assertEquals(new CompositeName("org/research/user/jane"), envContext.composeName(new CompositeName("user/jane"), new CompositeName("org/research")));
        assertEquals(new CompositeName("research/user/jane"), envContext.composeName(new CompositeName("user/jane"), new CompositeName("research")));
    }

    public void testList() throws NamingException {
        NamingEnumeration ne;
        Map expected;
        Map result;

        expected = new HashMap();
        for (Iterator i = envBinding.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            expected.put(entry.getKey(), entry.getValue().getClass().getName());
        }
        ne = envContext.list("");
        result = new HashMap();
        while (ne.hasMore()) {
            NameClassPair pair = (NameClassPair) ne.next();
            result.put(pair.getName(), pair.getClassName());
        }
        assertEquals(expected, result);

        try {
            ne.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
        try {
            ne.nextElement();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    public void testListBindings() throws NamingException {
        NamingEnumeration ne;
        ne = envContext.listBindings("");
        int count = 0;
        while (ne.hasMore()) {
            count ++;
            Binding pair = (Binding) ne.next();
            assertTrue(envBinding.containsKey(pair.getName()));
            if (! (envBinding.get(pair.getName()) instanceof Context)) {
                assertEquals(pair.getObject(), envBinding.get(pair.getName()));
            }
        }
        assertEquals(envBinding.size(), count);

        try {
            ne.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
        try {
            ne.nextElement();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    public void testSpeed() throws NamingException {
        Context comp = (Context) initialContext.lookup("java:comp");

        long start = System.currentTimeMillis();
        for (int i=0; i < 1000000; i++) {
            // initialContext.lookup("java:comp/hello"); // this is sloooow due to scheme resolution
            // envContext.lookup("hello");
            comp.lookup("env/hello");
        }

        long end = System.currentTimeMillis();
        System.out.println("lookup(String) milliseconds: " + (end - start));
    }

    protected void setUp() throws Exception {
        super.setUp();
        //System.setProperty("java.naming.factory.initial", GlobalContextManager.class.getName());
        //System.setProperty("java.naming.factory.url.pkgs", "org.apache.geronimo.naming");

        LinkRef link = new LinkRef("java:comp/env/hello");

        Map bindings = new HashMap();
        bindings.put("comp/env/hello", "Hello");
        bindings.put("comp/env/world", "Hello World");
        bindings.put("comp/env/here/there/anywhere", "long name");
        bindings.put("comp/env/link", link);

        readOnlyContext = new WritableContext("", bindings, ContextAccess.UNMODIFIABLE);

        envBinding = new HashMap();
        envBinding.put("hello", "Hello");
        envBinding.put("world", "Hello World");
        envBinding.put("here", readOnlyContext.lookup("comp/env/here"));
        envBinding.put("link", link);

        RootContext.setComponentContext(readOnlyContext);

        Context javaCompContext = new JavaCompContextGBean();
        Context globalContext = new ImmutableContext(Collections.<String, Object>singletonMap(javaCompContext.getNameInNamespace(), javaCompContext));
        GlobalContextManager.setGlobalContext(globalContext);

        initialContext = new InitialContext();
        compContext = (Context) initialContext.lookup("java:comp");
        envContext = (Context) initialContext.lookup("java:comp/env");

        syntax = new Properties();
    }
}
