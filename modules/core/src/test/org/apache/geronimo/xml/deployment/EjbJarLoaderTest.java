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
import org.apache.geronimo.deployment.model.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.j2ee.Description;
import org.apache.geronimo.deployment.model.j2ee.DisplayName;
import org.apache.geronimo.deployment.model.ejb.EjbJar;
import org.apache.geronimo.deployment.model.ejb.EjbJarDocument;
import org.apache.geronimo.deployment.model.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.ejb.Session;
import org.w3c.dom.Document;

/**
 * Tests basic EJB JAR DD loading (not very comprehensive)
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/07 03:49:43 $
 */
public class EjbJarLoaderTest extends TestCase {
    private File docDir;

    public void testSimpleLoad() throws Exception {
        File f = new File(docDir, "simple-ejb-jar.xml");
        Document xmlDoc = LoaderUtil.parseXML(new FileReader(f));
        EjbJarDocument doc = EjbJarLoader.load(xmlDoc);
        EjbJar jar = doc.getEjbJar();
        checkEjbJar(jar, "example");
    }

    static void checkEjbJar(EjbJar jar, String expectedEnvValue) {
        assertEquals("2.1", jar.getVersion());
        checkDescription("This is a test EJB JAR DD for JSR-88 purposes",jar.getDescription());
        checkDisplayName("Test EJB JAR",jar.getDisplayName());
        EnterpriseBeans beans = jar.getEnterpriseBeans();
        Session[] session = beans.getSession();
        assertEquals(2, session.length);
        assertEquals("Stateless", session[0].getSessionType());
        assertEquals("Stateful", session[1].getSessionType());
        checkStateless(session[0], expectedEnvValue);
        checkStateful(session[1]);
    }

    private static void checkStateless(Session session, String expectedEnvValue) {
        checkDescription("This is a sample stateless session bean", session.getDescription());
        checkDisplayName("Stateless Session Bean", session.getDisplayName());
        assertEquals("StatelessTest", session.getEJBName());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.StatelessHome", session.getHome());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.Stateless", session.getRemote());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.StatelessBean", session.getEJBClass());
        assertEquals("Container", session.getTransactionType());

        EnvEntry[] envs = session.getEnvEntry();
        assertEquals(1, envs.length);
        EnvEntry envEntry = envs[0];
        assertEquals("Variable 1", envEntry.getEnvEntryName());
        assertEquals("java.lang.String", envEntry.getEnvEntryType());
        assertEquals(expectedEnvValue, envEntry.getEnvEntryValue());

        ResourceEnvRef[] resEnvRefs = session.getResourceEnvRef();
        assertEquals(1, resEnvRefs.length);
        ResourceEnvRef resEnvRef = resEnvRefs[0];
        assertEquals("jdbc/StatelessDatabase", resEnvRef.getResourceEnvRefName());
        assertEquals("javax.sql.DataSource", resEnvRef.getResourceEnvRefType());
    }

    private static void checkStateful(Session session) {
        checkDescription("This is a sample stateful session bean", session.getDescription());
        checkDisplayName("Stateful Session Bean", session.getDisplayName());
        assertEquals("StatefulTest", session.getEJBName());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.StatefulHome", session.getHome());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.Stateful", session.getRemote());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.StatefulBean", session.getEJBClass());
        assertEquals("Container", session.getTransactionType());

        EJBRef[] ejbRefs = session.getEJBRef();
        assertEquals(1, ejbRefs.length);
        EJBRef ejbRef = ejbRefs[0];
        assertEquals("ejb/MyStateless", ejbRef.getEJBRefName());
        assertEquals("Session", ejbRef.getEJBRefType());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.StatelessHome", ejbRef.getHome());
        assertEquals("org.apache.geronimo.enterprise.deploy.test.Stateless", ejbRef.getRemote());
        assertEquals("Stateless", ejbRef.getEJBLink());

        ResourceEnvRef[] resEnvRefs = session.getResourceEnvRef();
        assertEquals(1, resEnvRefs.length);
        ResourceEnvRef resEnvRef = resEnvRefs[0];
        assertEquals("jdbc/StatefulDatabase", resEnvRef.getResourceEnvRefName());
        assertEquals("javax.sql.DataSource", resEnvRef.getResourceEnvRefType());
    }

    private static void checkDescription(String s, Description[] description) {
        assertEquals(1, description.length);
        assertEquals(s, description[0].getContent());
    }

    private static void checkDisplayName(String s, DisplayName[] name) {
        assertEquals(1, name.length);
        assertEquals(s, name[0].getContent());
    }

    protected void setUp() throws Exception {
        docDir = new File("src/test-data/xml/deployment");
    }
}
