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
package org.apache.geronimo.xml.deployment;

import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.apache.geronimo.deployment.model.geronimo.ejb.GeronimoEjbJarDocument;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.ejb.Entity;
import org.apache.geronimo.deployment.model.geronimo.ejb.Query;
import org.apache.geronimo.deployment.model.geronimo.ejb.Binding;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.ClassSpace;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.ejb.QueryMethod;

/**
 * Tests basic Geronimo EJB JAR DD loading (not very comprehensive)
 *
 * @version $Revision: 1.6 $ $Date: 2003/11/19 00:33:59 $
 */
public class GeronimoEjbJarLoaderTest extends TestCase {
    private File docDir;
    private GeronimoEjbJarLoader loader;

    public void testSimpleLoad() throws Exception {
        File f = new File(docDir, "simple-geronimo-ejb-jar.xml");
        Document xmlDoc = LoaderUtil.parseXML(new FileReader(f));
        GeronimoEjbJarDocument doc = loader.load(xmlDoc);
        checkGeronimoJar(doc);
    }

    static void checkGeronimoJar(GeronimoEjbJarDocument doc) {
        EjbJar jar = doc.getEjbJar();
        EjbJarLoaderTest.checkEjbJar(jar, "OverrideExample");
        ClassSpace classSpace = jar.getClassSpace();
        assertEquals("geronimo.system:role=ClassSpace,name=System", classSpace.getParentClassSpace());
        assertEquals("geronimo.system:role=ClassSpace,name=Test", classSpace.getClassSpace());
        assertEquals("TestEJBModule", jar.getModuleName());
        assertEquals("DefaultDS", jar.getDatasourceName());
        EnterpriseBeans beans = jar.getGeronimoEnterpriseBeans();
        Session[] session = beans.getGeronimoSession();
        assertEquals(2, session.length);
        assertEquals("Stateless", session[0].getSessionType());
        assertEquals("Stateful", session[1].getSessionType());
        Entity[] entity = beans.getGeronimoEntity();
        assertEquals(1, entity.length);
        assertEquals("Container", entity[0].getPersistenceType());
        assertEquals("java.lang.Integer", entity[0].getPrimKeyClass());
        MessageDriven[] messageDriven = beans.getGeronimoMessageDriven();
        assertEquals(1, messageDriven.length);
        assertEquals("Container", messageDriven[0].getTransactionType());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.MessageBeanIntf", messageDriven[0].getMessagingType());
        assertEquals("javax.jms.Queue", messageDriven[0].getMessageDestinationType());
        ActivationConfig activationConfig = messageDriven[0].getGeronimoActivationConfig();
        assertEquals("TestInboundAdapter", activationConfig.getResourceAdapterName());
        assertEquals("org.apache.geronimo.connector.TestActivationSpec", activationConfig.getActivationSpecClass());
        assertEquals(1, activationConfig.getActivationConfigProperty().length);
        assertEquals("StringProp", activationConfig.getActivationConfigProperty()[0].getActivationConfigPropertyName());
        assertEquals("some value", activationConfig.getActivationConfigProperty()[0].getActivationConfigPropertyValue());


        checkStateless(session[0]);
        checkStateful(session[1]);
        checkCMPEntity(entity[0]);
    }

    static void checkStateless(Session session) {
        assertEquals("StatelessTest", session.getEJBName());

        EnvEntry[] envs = session.getEnvEntry();
        assertEquals(1, envs.length);
        EnvEntry envEntry = envs[0];
        assertEquals("Variable 1", envEntry.getEnvEntryName());
        assertEquals("OverrideExample", envEntry.getEnvEntryValue());

        ResourceEnvRef[] resEnvRefs = session.getGeronimoResourceEnvRef();
        assertEquals(1, resEnvRefs.length);
        ResourceEnvRef resEnvRef = resEnvRefs[0];
        assertEquals("jdbc/StatelessDatabase", resEnvRef.getResourceEnvRefName());
        assertEquals("java:jdbc/MyDatabase", resEnvRef.getJndiName());
    }

    static void checkStateful(Session session) {
        assertEquals("StatefulTest", session.getEJBName());

        EjbRef[] ejbRefs = session.getGeronimoEJBRef();
        assertEquals(1, ejbRefs.length);
        EjbRef ejbRef = ejbRefs[0];
        assertEquals("ejb/MyStateless", ejbRef.getEJBRefName());
        assertNull(ejbRef.getJndiName());

        ResourceEnvRef[] resEnvRefs = session.getGeronimoResourceEnvRef();
        assertEquals(1, resEnvRefs.length);
        ResourceEnvRef resEnvRef = resEnvRefs[0];
        assertEquals("jdbc/StatefulDatabase", resEnvRef.getResourceEnvRefName());
        assertTrue("resEnvRef does not have an empty JNDI name!", resEnvRef.getJndiName() == null || resEnvRef.getJndiName().equals(""));
    }

    static void checkCMPEntity(Entity cmpEntity) {
        assertEquals("CMPTest", cmpEntity.getEJBName());
        //No references...
        //test queries
        Query[] query = cmpEntity.getGeronimoQuery();
        checkQuery(query);
        Query[] update = cmpEntity.getUpdate();
        checkQuery(update);
    }

    private static void checkQuery(Query[] query) {
        assertEquals(1, query.length);
        QueryMethod queryMethod = query[0].getQueryMethod();
        assertTrue(null != queryMethod);
        assertEquals("doSomething", queryMethod.getMethodName());
        assertEquals(2, queryMethod.getMethodParam().length);
        assertEquals("java.lang.String", queryMethod.getMethodParam()[0]);
        assertEquals("OtherThingy", query[0].getAbstractSchemaName());
        assertEquals("Many", query[0].getMultiplicity());
        Binding[] inputBinding = query[0].getInputBinding();
        assertEquals(2, inputBinding.length);
        assertEquals("org.openejb.nova.persistence.binding.jdbc.StringBinding", inputBinding[0].getType());
        assertEquals(0, inputBinding[0].getParam());
        Binding[] outputBinding = query[0].getOutputBinding();
        assertEquals(1, outputBinding.length);
        assertEquals("org.openejb.nova.persistence.binding.jdbc.StringBinding", outputBinding[0].getType());
        assertEquals(0, outputBinding[0].getParam());
    }

    protected void setUp() throws Exception {
        docDir = new File("src/test-data/xml/deployment");
        loader = new GeronimoEjbJarLoader();
    }
}
