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

import org.apache.geronimo.deployment.model.connector.Connector;
import org.apache.geronimo.deployment.model.connector.ConnectorDocument;
import org.apache.geronimo.deployment.model.connector.InboundResourceAdapter;
import org.apache.geronimo.deployment.model.connector.OutboundResourceAdapter;
import org.apache.geronimo.deployment.model.connector.ResourceAdapter;
import org.apache.geronimo.deployment.model.connector.ConfigProperty;
import org.w3c.dom.Document;

/**
 * ConnectorLoaderTest
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/13 22:22:31 $
 */
public class ConnectorLoaderTest extends TestCase {
	private File docDir_1_5;
	private File docDir_1_0;

	public void testSimpleLoad_1_5() throws Exception {
		File f = new File(docDir_1_5, "ra.xml");
		Document xmlDoc = LoaderUtil.parseXML(new FileReader(f));
		ConnectorDocument doc = ConnectorLoader.load(xmlDoc);
		Connector connector = doc.getConnector();
		checkResourceAdapter_1_5(connector.getResourceAdapter());
	}
	
	protected static void checkResourceAdapter_1_5(ResourceAdapter resourceAdapter) throws Exception {
	    assertTrue("Expected a ResourceAdapter object", resourceAdapter != null);
	    assertTrue("Expected a ResourceAdapter class", resourceAdapter.getResourceAdapterClass() != null);
	    assertEquals("Expected 1 ConfigProperty", 1, resourceAdapter.getConfigProperty().length);
	    checkOutboundResourceAdapter_1_5(resourceAdapter.getOutboundResourceAdapter());
	    checkInboundResourceAdapter(resourceAdapter.getInboundResourceAdapter());
	    assertEquals("Expected 1 adminobject", 1, resourceAdapter.getAdminObject().length);
	    assertEquals("Expected admin object to have one config property", 1, resourceAdapter.getAdminObject()[0].getConfigProperty().length);
	}
	
	protected static void checkOutboundResourceAdapter_1_5(OutboundResourceAdapter outboundResourceAdapter) throws Exception {
	    assertTrue("Expected an OutboundResourceAdapter object", outboundResourceAdapter != null);
		assertEquals("Expected 2 ConnectionDefinition objects", 2, outboundResourceAdapter.getConnectionDefinition().length);
        ConfigProperty[] configProperty = outboundResourceAdapter.getConnectionDefinition()[0].getConfigProperty();
        assertEquals("Examining first ConnectionDefinition ConfigProperty count", 4,
		        configProperty.length);
        assertEquals("ConnectionDefinition 1, ConfigProperty 1", "originalvalue1", configProperty[0].getConfigPropertyValue());
        assertEquals("ConnectionDefinition 1, ConfigProperty 2", "originalvalue2", configProperty[1].getConfigPropertyValue());
        assertEquals("ConnectionDefinition 1, ConfigProperty 3", null, configProperty[2].getConfigPropertyValue());
        assertEquals("ConnectionDefinition 1, ConfigProperty 4", null, configProperty[3].getConfigPropertyValue());

	}
	
	protected static void checkInboundResourceAdapter(InboundResourceAdapter inboundResourceAdapter) throws Exception {
		assertTrue("Expected an InboundResourceAdapter object", inboundResourceAdapter != null);
		assertEquals("Expected 1 MessageListener object", 1, inboundResourceAdapter.getMessageAdapter().getMessageListener().length);
		assertEquals("Expected 1 required config property", 1,
		inboundResourceAdapter.getMessageAdapter().getMessageListener()[0].getActivationSpec().getRequiredConfigProperty().length);

	}

	public void testSimpleLoad_1_0() throws Exception {
		File f = new File(docDir_1_0, "ra.xml");
		Document xmlDoc = LoaderUtil.parseXML(new FileReader(f));
		ConnectorDocument doc = ConnectorLoader.load(xmlDoc);
		Connector connector = doc.getConnector();
		checkResourceAdapter_1_0(connector.getResourceAdapter());
	}
	
	protected static void checkResourceAdapter_1_0(ResourceAdapter resourceAdapter) throws Exception {
		assertTrue("Expected a ResourceAdapter object", resourceAdapter != null);
		assertTrue("Expected no ResourceAdapter class", resourceAdapter.getResourceAdapterClass() == null);
		assertEquals("Expected 0 ConfigProperty", 0, resourceAdapter.getConfigProperty().length);
		checkOutboundResourceAdapter_1_0(resourceAdapter.getOutboundResourceAdapter());
		assertTrue("Expected no inboundResourceAdapter", null == resourceAdapter.getInboundResourceAdapter());
		assertEquals("Expected 0 adminobject", 0, resourceAdapter.getAdminObject().length);
	}

	protected static void checkOutboundResourceAdapter_1_0(OutboundResourceAdapter outboundResourceAdapter) throws Exception {
		assertTrue("Expected an OutboundResourceAdapter object", outboundResourceAdapter != null);
		assertEquals("Expected 1 ConnectionDefinition objects", 1, outboundResourceAdapter.getConnectionDefinition().length);
		assertEquals("Expected first ConnectionDefinition to have 1 ConfigProperty", 1, 
				outboundResourceAdapter.getConnectionDefinition()[0].getConfigProperty().length);

	}
	
	protected void setUp() throws Exception {
		docDir_1_5 = new File("src/test-data/xml/deployment/connector_1_5");
		docDir_1_0 = new File("src/test-data/xml/deployment/connector_1_0");
	}

}
