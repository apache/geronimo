/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.naming.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.NoSuchElementException;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.LinkRef;

import org.apache.geronimo.common.StopWatch;

import junit.framework.TestCase;

/**
* Unit tests for basic ops on an {@link InitialContext}.
 * 
 * @version $Revision: 1.4 $ $Date: 2003/09/03 17:21:16 $
 */
public class BasicContextTest extends TestCase {
    private Properties syntax;
    private Map compBinding;
    private Map envBinding;
    private Context initialContext;
    private Context compContext;
    private Context envContext;

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
        assertEquals("Hello", envContext.lookup(new CompoundName("hello", syntax)));
        assertEquals("Hello", compContext.lookup(new CompoundName("env/hello", syntax)));

        assertEquals(envContext, envContext.lookup(""));
    }
    
    public void testSchemeLookup() throws NamingException {
        envContext.lookup("dns:apache.org");
        assertEquals("Hello", envContext.lookup("java:comp/env/hello"));
        assertEquals("Hello", compContext.lookup("java:comp/env/hello"));
    }

    public void testLookupLink() throws NamingException {
        assertEquals("Hello", envContext.lookup("link"));
    }

    public void testComposeName() throws NamingException {
        assertEquals("org/research/user/jane", envContext.composeName("user/jane", "org/research"));
        assertEquals("research/user/jane", envContext.composeName("user/jane", "research"));
        assertEquals(new CompositeName("org/research/user/jane"), envContext.composeName(new CompositeName("user/jane"), new CompositeName("org/research")));
        assertEquals(new CompositeName("research/user/jane"), envContext.composeName(new CompositeName("user/jane"), new CompositeName("research")));
    }

    public void testList() throws NamingException {
        NamingEnumeration enum;
        Map expected;
        Map result;

        expected = new HashMap();
        for (Iterator i = envBinding.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            expected.put(entry.getKey(), entry.getValue().getClass().getName());
        }
        enum = envContext.list("");
        result = new HashMap();
        while (enum.hasMore()) {
            NameClassPair pair = (NameClassPair) enum.next();
            result.put(pair.getName(), pair.getClassName());
        }
        assertEquals(expected, result);

        try {
            enum.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
        try {
            enum.nextElement();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    public void testListBindings() throws NamingException {
        NamingEnumeration enum;
        Map result;
        enum = envContext.listBindings("");
        result = new HashMap();
        while (enum.hasMore()) {
            Binding pair = (Binding) enum.next();
            result.put(pair.getName(), pair.getObject());
        }
        assertEquals(envBinding, result);

        try {
            enum.next();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
        try {
            enum.nextElement();
            fail();
        } catch (NoSuchElementException e) {
            // ok
        }
    }

    public void testSpeed() throws NamingException {
        StopWatch watch = new StopWatch();
        Context comp = (Context) initialContext.lookup("java:comp");
        
        watch.start();
        for (int i=0; i < 1000000; i++) {
            // initialContext.lookup("java:comp/hello"); // this is sloooow due to scheme resolution
            // envContext.lookup("hello");
            comp.lookup("env/hello");
        }
        
        System.out.println("lookup(String): " + watch.toDuration());
    }

    protected void setUp() throws Exception {
        super.setUp();
        initialContext = new InitialContext();

        compBinding = new HashMap();

        envBinding = new HashMap();
        envBinding.put("hello", "Hello");
        envBinding.put("world", "Hello World");
        envBinding.put("link", new LinkRef("java:comp/env/hello"));
        envContext = new ReadOnlyContext(envBinding);

        compBinding.put("env", envContext);
        compContext = new ReadOnlyContext(compBinding);

        syntax = new Properties();
        RootContext.setComponentContext(compContext);
    }
}
