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
package org.apache.geronimo.naming.deployment;

import java.net.URL;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.naming.java.ProxyFactory;
import org.apache.geronimo.xbeans.geronimo.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.GerMessageDestinationRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceEnvRefType;
import org.apache.geronimo.xbeans.geronimo.GerResourceRefType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.geronimo.xbeans.j2ee.EnvEntryType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceEnvRefType;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;

/**
 * THIS IS A COPY OF org.apache.geronimo.naming.java.ContextBuilderTest.
 * Copied because maven doesn't share test classes.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/13 23:41:47 $
 */
public class ContextBuilderTest extends TestCase {

    protected EjbRefType[] ejbRefs;
    private GerEjbRefType[] gerEjbRefs;
    protected EjbLocalRefType[] ejbLocalRefs;
    private GerEjbLocalRefType[] gerEjbLocalRefs;
    protected EnvEntryType[] envEntries;
    private MessageDestinationRefType[] messageDestinationRefs;
    private GerMessageDestinationRefType[] gerMessageDestinationRefs;
    private ResourceEnvRefType[] resEnvRefs;
    private GerResourceEnvRefType[] gerResEnvRefs;
    protected ResourceRefType[] resRefs;
    private GerResourceRefType[] gerResRefs;
    protected Context compCtx;
    protected JMXKernel kernel;
    protected ProxyFactory proxyFactory;

    private static final String EJB1_NAME = "here/there/EJB1";
    private static final String EJB2_NAME = "here/LinkEjb";
    private static final String LOCAL_EJB1_NAME = "local/here/LocalEJB2";
    private static final String LOCAL_EJB2_NAME = "local/here/LinkLocalEjb";
    private static final String RESOURCE_REF1_NAME = "url/testURL";
    private static final String RESOURCE_REF2_NAME = "DefaultCF";

    protected void setUpContext(UserTransaction userTransaction) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException, MalformedObjectNameException, DeploymentException {

        proxyFactory = new ProxyFactory() {
            public Object getProxy(Class homeInterface, Class remoteInterface, Object targetId) throws NamingException {
                return targetId;
            }

            public Object getProxy(Class interfaced, Object targetId) throws NamingException {
                return targetId;
            }

        };

        //Ejb Refs
        EjbRefType ejbRef = EjbRefType.Factory.newInstance();
        ejbRef.addNewEjbRefName().setStringValue(EJB1_NAME);
        ejbRef.addNewEjbRefType().setStringValue("Session");
        ejbRef.addNewHome().setStringValue(Object.class.getName());
        ejbRef.addNewRemote().setStringValue(Object.class.getName());
        GerEjbRefType gerEjbRef = GerEjbRefType.Factory.newInstance();
        gerEjbRef.addNewEjbRefName().setStringValue(EJB1_NAME);
        gerEjbRef.setUri(EJB1_NAME);

        EjbRefType ejbLinkRef = EjbRefType.Factory.newInstance();
        ejbLinkRef.addNewEjbRefName().setStringValue(EJB2_NAME);
        ejbLinkRef.addNewEjbRefType().setStringValue("Session");
        ejbLinkRef.addNewHome().setStringValue(Object.class.getName());
        ejbLinkRef.addNewRemote().setStringValue(Object.class.getName());
        ejbLinkRef.addNewEjbLink().setStringValue(EJB2_NAME);
        GerEjbRefType gerEjbLinkRef = GerEjbRefType.Factory.newInstance();
        gerEjbLinkRef.addNewEjbRefName().setStringValue(EJB2_NAME);
        ejbRefs = new EjbRefType[]{ejbRef, ejbLinkRef};
        gerEjbRefs = new GerEjbRefType[] {gerEjbRef, gerEjbLinkRef};


        //ejb local refs
        EjbLocalRefType ejbLocalRef = EjbLocalRefType.Factory.newInstance();
        ejbLocalRef.addNewEjbRefName().setStringValue(LOCAL_EJB1_NAME);
        ejbLocalRef.addNewEjbRefType().setStringValue("Entity");
        ejbLocalRef.addNewLocalHome().setStringValue(Object.class.getName());
        ejbLocalRef.addNewLocal().setStringValue(Object.class.getName());
        GerEjbLocalRefType gerEjbLocalRef = GerEjbLocalRefType.Factory.newInstance();
        gerEjbLocalRef.addNewEjbRefName().setStringValue(LOCAL_EJB1_NAME);
        gerEjbLocalRef.setUri(LOCAL_EJB1_NAME);

        EjbLocalRefType ejbLocalLinkRef = EjbLocalRefType.Factory.newInstance();
        ejbLocalLinkRef.addNewEjbRefName().setStringValue(LOCAL_EJB2_NAME);
        ejbLocalLinkRef.addNewEjbRefType().setStringValue("Entity");
        ejbLocalLinkRef.addNewLocalHome().setStringValue(Object.class.getName());
        ejbLocalLinkRef.addNewLocal().setStringValue(Object.class.getName());
        ejbLocalLinkRef.addNewEjbLink().setStringValue(LOCAL_EJB2_NAME);
        GerEjbLocalRefType gerEjbLocalLinkRef = GerEjbLocalRefType.Factory.newInstance();
        gerEjbLocalLinkRef.addNewEjbRefName().setStringValue(LOCAL_EJB2_NAME);
        ejbLocalRefs = new EjbLocalRefType[]{ejbLocalRef, ejbLocalLinkRef};
        gerEjbLocalRefs = new GerEjbLocalRefType[] {gerEjbLocalRef, gerEjbLocalLinkRef};


        //env entries
        EnvEntryType stringEntry = EnvEntryType.Factory.newInstance();
        stringEntry.addNewEnvEntryName().setStringValue("hello/world");
        stringEntry.addNewEnvEntryType().setStringValue("java.lang.String");
        stringEntry.addNewEnvEntryValue().setStringValue("Hello World");
        EnvEntryType intEntry = EnvEntryType.Factory.newInstance();
        intEntry.addNewEnvEntryName().setStringValue("int");
        intEntry.addNewEnvEntryType().setStringValue("java.lang.Integer");
        intEntry.addNewEnvEntryValue().setStringValue("12345");
        envEntries = new EnvEntryType[]{stringEntry, intEntry};

        //message destinations
        messageDestinationRefs = new MessageDestinationRefType[] {};
        gerMessageDestinationRefs = new GerMessageDestinationRefType[] {};

        //resource env refs (admin objects)
        resEnvRefs = new ResourceEnvRefType[] {};
        gerResEnvRefs = new GerResourceEnvRefType[] {};

        //resource refs
        ResourceRefType urlRef = ResourceRefType.Factory.newInstance();
        urlRef.addNewResRefName().setStringValue(RESOURCE_REF1_NAME);
        urlRef.addNewResType().setStringValue(URL.class.getName());
        GerResourceRefType gerUrlRef = GerResourceRefType.Factory.newInstance();
        gerUrlRef.addNewResRefName().setStringValue(RESOURCE_REF1_NAME);
        gerUrlRef.setUri("http://localhost/path");

        ResourceRefType cfRef = ResourceRefType.Factory.newInstance();
        cfRef.addNewResRefName().setStringValue(RESOURCE_REF2_NAME);
        cfRef.addNewResType().setStringValue("javax.sql.DataSource");
        GerResourceRefType gerCfRef = GerResourceRefType.Factory.newInstance();
        gerCfRef.addNewResRefName().setStringValue(RESOURCE_REF2_NAME);
        gerCfRef.setUri(RESOURCE_REF2_NAME);
        resRefs = new ResourceRefType[]{urlRef, cfRef};
        gerResRefs = new GerResourceRefType[] {gerUrlRef, gerCfRef};

        //put it all together
        compCtx = new ComponentContextBuilder(proxyFactory, this.getClass().getClassLoader())
                .buildContext(ejbRefs, gerEjbRefs,
                        ejbLocalRefs, gerEjbLocalRefs,
                        envEntries,
                        messageDestinationRefs, gerMessageDestinationRefs,
                        resEnvRefs, gerResEnvRefs,
                        resRefs, gerResRefs,
                        userTransaction);
    }

    public void testEnvEntries() throws Exception {
        setUpContext(null);
        assertEquals("Hello World", compCtx.lookup("env/hello/world"));
        assertEquals(new Integer(12345), compCtx.lookup("env/int"));
    }

    public void testNoUserTransaction() throws Exception {
        setUpContext(null);
        try {
            compCtx.lookup("UserTransaction");
            fail("Expected NameNotFoundException");
        } catch (NameNotFoundException e) {
            // OK
        }
    }

    public void testUserTransaction() throws Exception {
        UserTransaction userTransaction = new UserTransaction() {
            public void begin() throws NotSupportedException, SystemException {
            }

            public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {
            }

            public int getStatus() throws SystemException {
                return 0;
            }

            public void rollback() throws IllegalStateException, SecurityException, SystemException {
            }

            public void setRollbackOnly() throws IllegalStateException, SystemException {
            }

            public void setTransactionTimeout(int seconds) throws SystemException {
            }

        };
        setUpContext(userTransaction);
        assertEquals(userTransaction, compCtx.lookup("UserTransaction"));
    }


    public void testEJBRefs() throws Exception {
        setUpContext(null);
        assertEquals("Expected object from testObject1", EJB1_NAME,
                compCtx.lookup("env/" + EJB1_NAME));
        assertEquals("Expected object from testObject3", EJB2_NAME,
                compCtx.lookup("env/" + EJB2_NAME));
    }

    public void testLocalEJBRefs() throws Exception {
        setUpContext(null);
        assertEquals("Expected object from testObject1", LOCAL_EJB1_NAME,
                compCtx.lookup("env/" + LOCAL_EJB1_NAME));

        assertEquals("Expected object from testObject3", LOCAL_EJB2_NAME,
                compCtx.lookup("env/" + LOCAL_EJB2_NAME));
    }

    public void testResourceRefs() throws Exception {
        setUpContext(null);
        assertEquals(new URL("http://localhost/path"), compCtx.lookup("env/" + RESOURCE_REF1_NAME));
        assertEquals("Expected object from testObject1", RESOURCE_REF2_NAME,
                compCtx.lookup("env/" + RESOURCE_REF2_NAME));
    }

}
