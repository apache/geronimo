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
import java.util.Map;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.naming.jmx.TestObject;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/12 20:38:19 $
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

    public static TestObject registerTestObject(MBeanServer server, ObjectName objectName) throws Exception {
        GBeanInfo gbeanInfo = TestObject.getGBeanInfo();
        GBeanMBean gbean = new GBeanMBean(gbeanInfo);
        server.registerMBean(gbean, objectName);
        return (TestObject) gbean.getTarget();
    }
}
