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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class AbstractContextTest extends TestCase {
    protected ReadOnlyContext readOnlyContext;
    protected Properties syntax;
    protected Map envBinding;
    protected Context initialContext;
    protected Context compContext;
    protected Context envContext;

    public void testNothing() { }

    protected void setUp() throws Exception {
        System.setProperty("java.naming.factory.initial", "com.sun.jndi.rmi.registry.RegistryContextFactory");
        System.setProperty("java.naming.factory.url.pkgs", "org.apache.geronimo.naming");
        System.setProperty("java.naming.provider.url", "rmi://localhost:1099");

        initialContext = new InitialContext();

        readOnlyContext = new ReadOnlyContext();

        envBinding = new HashMap();
        readOnlyContext.internalBind("env/hello", "Hello");
        envBinding.put("hello", "Hello");
        bind("env/world", "Hello World");
        envBinding.put("world", "Hello World");
        bind("env/here/there/anywhere", "long name");
        envBinding.put("here", readOnlyContext.lookup("env/here"));
        LinkRef link = new LinkRef("java:comp/env/hello");
        bind("env/link", link);
        envBinding.put("link", link);

        RootContext.setComponentContext(readOnlyContext);

        compContext = (Context) initialContext.lookup("java:comp");
        envContext = (Context) initialContext.lookup("java:comp/env");

        syntax = new Properties();
    }

    protected void bind(String name, Object value) throws NamingException {
        readOnlyContext.internalBind(name, value);
    }

}
