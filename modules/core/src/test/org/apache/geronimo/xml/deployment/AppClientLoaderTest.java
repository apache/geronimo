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

import org.apache.geronimo.deployment.model.appclient.ApplicationClient;
import org.apache.geronimo.deployment.model.j2ee.EJBRef;
import org.apache.geronimo.deployment.model.j2ee.EnvEntry;
import org.apache.geronimo.deployment.model.j2ee.MessageDestination;
import org.apache.geronimo.deployment.model.j2ee.MessageDestinationRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceEnvRef;
import org.apache.geronimo.deployment.model.j2ee.ResourceRef;
import org.apache.geronimo.deployment.model.j2ee.ServiceRef;
import org.w3c.dom.Document;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/22 08:47:26 $
 */
public class AppClientLoaderTest extends AbstractLoaderUtilTest {
    private File docDir;

    public void testSimpleLoad() throws Exception {
        File f = new File(docDir, "simple-app-client.xml");
        Document doc = parser.parse(f);
        ApplicationClient client = AppClientLoader.load(doc);

        EnvEntry[] envEntries = client.getEnvEntry();
        assertEquals(1, envEntries.length);
        EnvEntry envEntry = envEntries[0];
        assertEquals("hello", envEntry.getEnvEntryName());
        assertEquals("java.lang.String", envEntry.getEnvEntryType());
        assertEquals("Hello World", envEntry.getEnvEntryValue());

        EJBRef[] ejbRefs = client.getEJBRef();
        assertEquals(1, ejbRefs.length);
        EJBRef ejbRef = ejbRefs[0];
        assertEquals("ejb/MyEJB", ejbRef.getEJBRefName());
        assertEquals("Session", ejbRef.getEJBRefType());
        assertEquals("my.EJB.Home", ejbRef.getHome());
        assertEquals("my.EJB.Remote", ejbRef.getRemote());
        assertNull(ejbRef.getEJBLink());

        ServiceRef[] serviceRefs = client.getServiceRef();
        assertEquals(1, serviceRefs.length);
        ServiceRef serviceRef = serviceRefs[0];
        assertEquals("service/MyService", serviceRef.getServiceRefName());
        assertEquals("javax.xml.rpc.Service", serviceRef.getServiceInterface());

        ResourceRef[] resourceRefs = client.getResourceRef();
        assertEquals(1, resourceRefs.length);
        ResourceRef resourceRef = resourceRefs[0];
        assertEquals("jdbc/MyDS", resourceRef.getResRefName());
        assertEquals("javax.sql.DataSource", resourceRef.getResType());
        assertEquals("Container", resourceRef.getResAuth());
        assertEquals("Shareable", resourceRef.getResSharingScope());

        ResourceEnvRef[] resEnvRefs = client.getResourceEnvRef();
        assertEquals(1, resEnvRefs.length);
        ResourceEnvRef resEnvRef = resEnvRefs[0];
        assertEquals("jms/MyOldQueue", resEnvRef.getResourceEnvRefName());
        assertEquals("javax.jms.Queue", resEnvRef.getResourceEnvRefType());

        MessageDestinationRef[] msgDestRefs = client.getMessageDestinationRef();
        assertEquals(1, msgDestRefs.length);
        MessageDestinationRef msgDestRef = msgDestRefs[0];
        assertEquals("jms/MyQueue", msgDestRef.getMessageDestinationRefName());
        assertEquals("javax.jms.Queue", msgDestRef.getMessageDestinationType());

        assertEquals(client.getCallbackHandler(), "my.callback.handler");

        MessageDestination[] msgDests = client.getMessageDestination();
        assertEquals(1, msgDests.length);
        MessageDestination msgDest = msgDests[0];
        assertEquals("jms/MyOwnQueue", msgDest.getMessageDestinationName());
    }

    protected void setUp() throws Exception {
        super.setUp();
        docDir = new File("src/test-data/xml/deployment");
    }
}
