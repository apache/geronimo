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

package org.apache.geronimo.connector.deployment;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.jar.JarOutputStream;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/08 20:21:57 $
 *
 * */
public class Connector_1_5Test extends TestCase implements ConfigurationCallback {
    private URL j2eeDD;
    private URL geronimoDD;
    private URI configID = URI.create("geronimo/connector15/test");
    private Map gbeans = new HashMap();
    XmlOptions xmlOptions;
    private List errors;

    public void testLoadJ2eeDeploymentDescriptor() throws Exception {
        InputStream j2eeInputStream = j2eeDD.openStream();
        ConnectorDocument connectorDocument = ConnectorDocument.Factory.parse(j2eeInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }
    }

    public void testLoadGeronimoDeploymentDescriptor() throws Exception {
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument connectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        assertNotNull(connectorDocument.getConnector().getResourceadapter());
        if (!connectorDocument.validate(xmlOptions)) {
            fail(errors.toString());
        }
    }

    public void testCreateConnector_1_5Module() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JarOutputStream jarOutputStream = new JarOutputStream(baos);
        ZipEntry entry = new ZipEntry("META-INF/ra.xml");
        jarOutputStream.putNextEntry(entry);

        InputStream j2eeInputStream = j2eeDD.openStream();
        byte[] buffer = new byte[1024];
        for (int length; (length = j2eeInputStream.read(buffer)) > 0; ) {
            jarOutputStream.write(buffer, 0, length);
        }
        jarOutputStream.flush();
        jarOutputStream.closeEntry();
        jarOutputStream.close();

        InputStream moduleArchive = new ByteArrayInputStream(baos.toByteArray());
        InputStream geronimoInputStream = geronimoDD.openStream();
        GerConnectorDocument connectorDocument = GerConnectorDocument.Factory.parse(geronimoInputStream);
        RARConfigurationFactory rarConfigurationFactory = new RARConfigurationFactory(ObjectName.getInstance("geronimo.test:role=ConnectionTracker"));
        DeploymentModule connector_1_5Module = rarConfigurationFactory.createModule(moduleArchive, connectorDocument, configID, true);
        connector_1_5Module.init();
        connector_1_5Module.generateClassPath(this);
        connector_1_5Module.defineGBeans(this, this.getClass().getClassLoader());
    }

    protected void setUp() throws Exception {
        File docDir = new File("src/test-data/connector_1_5");
        j2eeDD = new File(docDir, "ra.xml").toURL();
        geronimoDD = new File(docDir, "geronimo-ra.xml").toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }

    public void addFile(URI path, InputStream source) throws IOException {
    }

    public void addToClasspath(URI uri) {
    }

    public void addGBean(ObjectName name, GBeanMBean gbean) {
        gbeans.put(name, gbean);
    }
}
