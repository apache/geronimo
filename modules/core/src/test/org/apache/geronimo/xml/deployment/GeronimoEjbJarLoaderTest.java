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
import org.apache.geronimo.deployment.model.geronimo.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.geronimo.j2ee.EjbRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;

/**
 * Tests basic Geronimo EJB JAR DD loading (not very comprehensive)
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/29 19:32:23 $
 */
public class GeronimoEjbJarLoaderTest extends TestCase {
    private File docDir;
    private GeronimoEjbJarLoader loader;

    public void testSimpleLoad() throws Exception {
        File f = new File(docDir, "simple-geronimo-ejb-jar.xml");
        Document xmlDoc = LoaderUtil.parseXML(new FileReader(f));
        GeronimoEjbJarDocument doc = loader.load(xmlDoc);
        EjbJar jar = doc.getEjbJar();
        EjbJarLoaderTest.checkEjbJar(jar, "OverrideExample");
        EnterpriseBeans beans = jar.getGeronimoEnterpriseBeans();
        Session[] session = beans.getGeronimoSession();
        assertEquals(2, session.length);
        assertEquals("Stateless", session[0].getSessionType());
        assertEquals("Stateful", session[1].getSessionType());
        checkStateless(session[0]);
        checkStateful(session[1]);
    }

    private void checkStateless(Session session) {
        assertEquals("StatelessTest", session.getEJBName());
        assertEquals("StatelessTestJNDI", session.getJndiName());

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

    private void checkStateful(Session session) {
        assertEquals("StatefulTest", session.getEJBName());
        assertEquals("StatefulTestJNDI", session.getJndiName());

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

    protected void setUp() throws Exception {
        docDir = new File("src/test-data/xml/deployment");
        loader = new GeronimoEjbJarLoader();
    }
}
