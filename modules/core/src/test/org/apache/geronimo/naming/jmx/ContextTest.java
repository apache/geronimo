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

package org.apache.geronimo.naming.jmx;

import java.util.HashMap;
import java.util.Hashtable;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBean;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.test.util.ServerUtil;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2003/11/19 02:08:42 $
 *
 * */
public class ContextTest extends TestCase {

    private final static String on1 = "geronimo.test:name=test,role=first";
    private final static String mn1 = "getEJBHome";

    private MBeanServer server;
    private String agentId;
    private ObjectName objectName;
    private TestObject mbean;

    protected void setUp() throws Exception {
        server = ServerUtil.newLocalServer();

        agentId = JMXKernel.getMBeanServerId(server);
        objectName = new ObjectName(on1);
        mbean = registerTestObject(server, objectName);
    }

    public static TestObject registerTestObject(MBeanServer server, ObjectName objectName) throws Exception {
        GeronimoMBeanInfo info = new GeronimoMBeanInfo();
        TestObject to = new TestObject();
        info.setTargetClass(TestObject.class.getName());
        info.setTarget(to);
        info.addOperationsDeclaredIn(TestObject.class);
        GeronimoMBean gmb = new GeronimoMBean();
        gmb.setMBeanInfo(info);
        server.registerMBean(gmb, objectName);
        return to;
    }

    protected void tearDown() throws Exception {
    }

    public void testLookupString() throws Exception {
        JMXContext context = new JMXContext(new Hashtable());
        Object result = context.lookup(JMXContext.encode(agentId, on1, mn1));
        assertTrue("Expected the ", result == mbean.getEJBHome());
    }

    public void testLookupName() throws Exception {
        JMXContext context = new JMXContext(new Hashtable());
        Object result = context.lookup(new CompositeName(JMXContext.encode(agentId, on1, mn1)));
        assertTrue("Expected the supplied object back", result == mbean.getEJBHome());
    }

    public void testWrongObjectName() throws Exception {
        JMXContext context = new JMXContext(new Hashtable());
        try {
            context.lookup(JMXContext.encode(agentId, on1 + 'x', mn1));
            fail();
        } catch (NamingException ne) {
            //expected
        }
    }

    public void testWrongMethodName() throws Exception {
        JMXContext context = new JMXContext(new Hashtable());
        try {
            context.lookup(JMXContext.encode(agentId, on1, mn1 + 'x'));
             fail();
         } catch (NamingException ne) {
             //expected
         }
    }

    public void XtestJmxURLContextFactory() throws Exception {
        jmxURLContextFactory contextFactory = new jmxURLContextFactory();
        Context context = (Context)contextFactory.getObjectInstance(null, null, null, new Hashtable());
        Object result = context.lookup(JMXContext.encode(agentId, on1, mn1));
        assertTrue("Expected the supplied object back", result == mbean.getEJBHome());
    }


    public void XtestLinkRefToJMX() throws Exception {
        InitialContext initialContext = new InitialContext();

        HashMap compBinding = new HashMap();

        HashMap envBinding = new HashMap();
        envBinding.put("link", new LinkRef(JMXContext.encode(agentId, on1, mn1)));
        compBinding.put("env", new ReadOnlyContext(envBinding));
        RootContext.setComponentContext(new ReadOnlyContext(compBinding));

        Context compContext = (Context) initialContext.lookup("java:comp");
        Context envContext = (Context) initialContext.lookup("java:comp/env");

        Object result = initialContext.lookup("java:comp/env/link");
        assertTrue("Expected the supplied object back", result == mbean.getEJBHome());
        result = compContext.lookup("env/link");
        assertTrue("Expected the supplied object back", result == mbean.getEJBHome());
        result = envContext.lookup("link");
        assertTrue("Expected the supplied object back", result == mbean.getEJBHome());
    }


}
