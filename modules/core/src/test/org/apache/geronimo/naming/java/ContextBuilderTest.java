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

import java.net.URL;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.model.geronimo.appclient.ApplicationClient;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbLocalRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.naming.jmx.TestObject;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;

/**
 *
 *
 * @version $Revision: 1.11 $ $Date: 2003/12/30 08:28:57 $
 */
public class ContextBuilderTest extends TestCase {
    protected static final String objectName1 = "geronimo.test:name=test1";
    protected static final String objectName2 = "geronimo.test:name=test2";
    protected static final String objectName3 = "geronimo.test:name=test3";

    protected ApplicationClient client;
    protected Session session;
    protected Context compCtx;
    protected JMXKernel kernel;
    protected ReferenceFactory referenceFactory;
    protected TestObject testObject1 = new TestObject();
    protected TestObject testObject2 = new TestObject();
    protected TestObject testObject3 = new TestObject();

    protected void setUp() throws Exception {
        kernel = new JMXKernel("geronimo.test");
        kernel.getMBeanServer().registerMBean(testObject1, ObjectName.getInstance(objectName1));
        kernel.getMBeanServer().registerMBean(testObject2, ObjectName.getInstance(objectName2));
        kernel.getMBeanServer().registerMBean(testObject3, ObjectName.getInstance(objectName3));

        referenceFactory = new JMXReferenceFactory(kernel.getMBeanServerId());
        client = new ApplicationClient();
        session = new Session();
        EnvEntry stringEntry = new EnvEntry();
        stringEntry.setEnvEntryName("string");
        stringEntry.setEnvEntryType("java.lang.String");
        stringEntry.setEnvEntryValue("Hello World");
        EnvEntry intEntry = new EnvEntry();
        intEntry.setEnvEntryName("int");
        intEntry.setEnvEntryType("java.lang.Integer");
        intEntry.setEnvEntryValue("12345");

        EjbRef ejbRef = new EjbRef();
        ejbRef.setEJBRefName("here/there/EJB1");
        ejbRef.setEJBRefType("Session");
        ejbRef.setJndiName(objectName1);

        EjbRef ejbLinkRef = new EjbRef();
        ejbLinkRef.setEJBRefName("here/LinkEjb");
        ejbLinkRef.setEJBRefType("Session");
        ejbLinkRef.setEJBLink(objectName3);

        EjbLocalRef ejbLocalRef = new EjbLocalRef();
        ejbLocalRef.setEJBRefName("local/here/LocalEJB2");
        ejbLocalRef.setEJBRefType("Entity");
        ejbLocalRef.setJndiName(objectName2);

        EjbLocalRef ejbLocalLinkRef = new EjbLocalRef();
        ejbLocalLinkRef.setEJBRefName("local/here/LinkLocalEjb");
        ejbLocalLinkRef.setEJBRefType("Entity");
        ejbLocalLinkRef.setEJBLink(objectName3);

        ResourceRef urlRef = new ResourceRef();
        urlRef.setResRefName("url/testURL");
        urlRef.setResType(URL.class.getName());
        urlRef.setJndiName("http://localhost/path");

        ResourceRef cfRef = new ResourceRef();
        cfRef.setResRefName("DefaultCF");
        cfRef.setJndiName(objectName1);

        client.setEnvEntry(new EnvEntry[] { stringEntry, intEntry });
        session.setEnvEntry(client.getEnvEntry());
        client.setEJBRef(new EjbRef[] {ejbRef, ejbLinkRef});
        session.setEJBRef(client.getEJBRef());
        session.setEJBLocalRef(new EjbLocalRef[] {ejbLocalRef, ejbLocalLinkRef});

        client.setResourceRef(new ResourceRef[] { urlRef, cfRef });
        session.setResourceRef(client.getResourceRef());
    }

    public void testEnvEntries() throws Exception {
        compCtx = new ComponentContextBuilder(referenceFactory, null).buildContext(client);
        assertEquals("Hello World", compCtx.lookup("env/string"));
        assertEquals(new Integer(12345), compCtx.lookup("env/int"));
        assertEquals(new URL("http://localhost/path"), compCtx.lookup("env/url/testURL"));
    }

    public void testUserTransaction() throws Exception {
        compCtx = new ComponentContextBuilder(referenceFactory, null).buildContext(client);
        try {
            compCtx.lookup("UserTransaction");
            fail("Expected NameNotFoundException");
        } catch (NameNotFoundException e) {
            // OK
        }

        UserTransaction userTransaction = new UserTransactionImpl();
        compCtx = new ComponentContextBuilder(referenceFactory, userTransaction).buildContext(client);
        assertEquals(userTransaction, compCtx.lookup("UserTransaction"));
    }

    public void XtestClientEJBRefs() throws Exception {
        ReadOnlyContext compContext = new ComponentContextBuilder(referenceFactory, null).buildContext(client);
        RootContext.setComponentContext(compContext);
        InitialContext initialContext = new InitialContext();
        assertEquals("Expected object from testObject1", testObject1.getEJBHome(),
                initialContext.lookup("java:comp/env/here/there/EJB1"));
        assertEquals("Expected object from testObject3", testObject3.getEJBHome(),
                initialContext.lookup("java:comp/env/here/LinkEjb"));
        assertEquals("Expected object from testObject1", testObject1.getConnectionFactory(),
                initialContext.lookup("java:comp/env/DefaultCF"));
    }

    public void XtestLocalEJBRefs() throws Exception {
        ReadOnlyContext compContext = new ComponentContextBuilder(referenceFactory, null).buildContext(session);
        RootContext.setComponentContext(compContext);
        InitialContext initialContext = new InitialContext();
        assertEquals("Expected object from testObject1", testObject1.getEJBHome(),
                initialContext.lookup("java:comp/env/here/there/EJB1"));

        assertEquals("Expected object from testObject3", testObject3.getEJBHome(),
                initialContext.lookup("java:comp/env/here/LinkEjb"));

        assertEquals("Expected object from testObject1", testObject2.getEJBLocalHome(),
                initialContext.lookup("java:comp/env/local/here/LocalEJB2"));

        assertEquals("Expected object from testObject3", testObject3.getEJBLocalHome(),
                initialContext.lookup("java:comp/env/local/here/LinkLocalEjb"));

        assertEquals("Expected object from testObject1", testObject1.getConnectionFactory(),
                initialContext.lookup("java:comp/env/DefaultCF"));
    }

}
