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

package org.apache.geronimo.tools.xdoclet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.kernel.service.DependencyService2;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;

/**
 * GeronimoMBean description test.
 * Skipped body.
 * 
 * @geronimo.mbean
 *      name="GeronimoDocletTaskTest"
 *  
 * @version $Revision: 1.1 $ $Date: 2003/12/10 11:27:13 $
 */
public class GeronimoDocletTaskTest
    extends TestCase
    implements GeronimoMBeanTarget
{

    private JMXKernel kernel;
    private MBeanServer server;

    private static final String OPERATION_RETURN = "success";
    
    private final ObjectName sourceName =
        new ObjectName("geronimo.test:role=testSource");
            
    private GeronimoDocletTaskTest endPoint;
    private String attribute;

    
    public GeronimoDocletTaskTest() throws Exception {
    }
    
    public void setMBeanContext(GeronimoMBeanContext context) {
    }

    public boolean canStart() {
        return true;
    }

    public void doStart() {
    }

    public boolean canStop() {
        return true;
    }

    public void doStop() {
    }

    public void doFail() {
    }

    protected void setUp() throws Exception {
        kernel = new JMXKernel("geronimo");
        server = kernel.getMBeanServer();
        server.createMBean(DependencyService2.class.getName(),
            new ObjectName("geronimo.boot:role=DependencyService2"));

        MBeanMetadata metaData = new MBeanMetadata();
        metaData.setGeronimoMBeanDescriptor(
            GeronimoDocletTaskTest.class.getName().replace('.', '/') +
            "MBeanInfo.xml"); 
        metaData.setName(sourceName);
        new DeployGeronimoMBean(server, metaData).perform();

        server.invoke(sourceName, "start", null, null);
    }

    protected void tearDown() throws Exception {
        server.unregisterMBean(sourceName);
        kernel.release();
    }

    public void testAttributes() throws Exception {
        GeronimoMBeanInfo info =
            (GeronimoMBeanInfo) server.getMBeanInfo(sourceName);
        int nbExpected = 0;

        Set attributes = info.getAttributeSet();
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            GeronimoAttributeInfo attribute =
                (GeronimoAttributeInfo) iter.next();
            if ( attribute.getName().equals("Attribute") ) {
                nbExpected++;
                assertTrue(
                    "Attribute is readable",
                    attribute.isReadable()); 
                assertTrue(
                    "Attribute is writable",
                    attribute.isWritable()); 
                assertEquals(
                    "Description should be first line of javadoc comment.",
                    "Attribute description test.",
                    attribute.getDescription()); 
                assertEquals("Wrong cache limit",
                    10, attribute.getCacheTimeLimit());
            } else if ( attribute.getName().equals("Attribute2") ) {
                nbExpected++;
                assertFalse(
                    "Attribute2 is not readable",
                    attribute.isReadable()); 
                assertTrue(
                    "Attribute2 is writable",
                    attribute.isWritable()); 
                assertEquals(
                    "Description should be first line of javadoc comment.",
                    "Attribute2 description test.",
                    attribute.getDescription()); 
            }
        }
        assertEquals("2 attributes expected", 2, nbExpected);
        String value = "value";
        Attribute attribute = new Attribute("Attribute", value);
        server.setAttribute(sourceName, attribute);
        assertEquals("Wrong attribute value",
            value, server.getAttribute(sourceName, "Attribute"));        
    }

    public void testOperations() throws Exception {
        GeronimoMBeanInfo info =
            (GeronimoMBeanInfo) server.getMBeanInfo(sourceName);
        int nbExpected = 0;
        
        Set operations = info.getOperationsSet();
        for (Iterator iter = operations.iterator(); iter.hasNext();) {
            GeronimoOperationInfo operation =
                (GeronimoOperationInfo) iter.next();
            if ( operation.getName().equals("performAction") ) {
                nbExpected++;
                MBeanParameterInfo[] parameters = operation.getSignature(); 
                assertEquals(
                    "performAction has only one parameter",
                    parameters.length, 1); 
                assertEquals(
                    "Wrong parameter type",
                    parameters[0].getType(), Collection.class.getName()); 
                assertEquals(
                    "Wrong parameter description",
                    "aCollection Parameter test.",
                    parameters[0].getDescription()
                    ); 
                assertEquals(
                    "Description should be first line of javadoc comment.",
                    "Operation description test.",
                    operation.getDescription()
                    );
                assertEquals(
                    "Wrong impact",
                    MBeanOperationInfo.ACTION, operation.getImpact());
                assertEquals("Wrong cache limit",
                    10, operation.getCacheTimeLimit());
            }
        }
        assertEquals("1 operation expected", nbExpected, 1);
        String returned = (String) server.invoke(sourceName,
            "performAction", new Object[] {null},
            new String[] {Collection.class.getName()});
        assertEquals("Wrong return value", OPERATION_RETURN, returned);
    }

    public void testEndPoints() throws Exception {
        GeronimoMBeanInfo info =
            (GeronimoMBeanInfo) server.getMBeanInfo(sourceName);
        int nbExpected = 0;
        
        Set endpoints = info.getEndpointsSet();
        for (Iterator iter = endpoints.iterator(); iter.hasNext();) {
            GeronimoMBeanEndpoint endpoint =
                (GeronimoMBeanEndpoint) iter.next();
            assertEquals(
                "Description should be first line of javadoc comment.",
                "Endpoint description test.",
                endpoint.getDescription()
                ); 
            assertFalse(
                "Endpoint is required.",
                endpoint.isRequired()
                );
            Collection peers = endpoint.getPeers();
            assertEquals("One peer expected.", 1, peers.size());
            ObjectName peer = (ObjectName) peers.iterator().next();
            assertTrue("Incorrect peer name.", peer.equals(sourceName));
            nbExpected++;
        }
        assertEquals("1 endpoint expected", nbExpected, 1);
        GeronimoDocletTaskTest returned =
            (GeronimoDocletTaskTest)server.getAttribute(sourceName, "EndPoint");
        assertNotNull("Endpoint should exist", returned);        
    }

    /**
     * Endpoint description test.
     * Skipped body.
     * 
     * @geronimo.endpoint
     *      required="false"
     * @geronimo.peer
     *      pattern="geronimo.test:role=testSource"   
     */
    public void setEndPoint(GeronimoDocletTaskTest anEndPoint) {
        endPoint = anEndPoint;
    }

    /**
     * @geronimo.attribute
     */
    public GeronimoDocletTaskTest getEndPoint() {
        return endPoint; 
    }

    /**
     * Attribute description test.
     * Skipped body.
     * 
     * @geronimo.attribute
     *      cache="10"
     */
    public void setAttribute(String anAttribute) {
        attribute =  anAttribute;
    }

    public String getAttribute() {
        return attribute;
    }

    /**
     * Attribute2 description test.
     * Skipped body.
     * 
     * @geronimo.attribute
     */
    public void setAttribute2(String anAttribute) {
    }

    /**
     * Operation description test.
     * Skipped body.
     * 
     * @param aCollection Parameter test.
     * 
     * @geronimo.operation
     *      impact="ACTION"
     *      cache="10"
     */
    public String performAction(Collection aCollection) {
        return OPERATION_RETURN;
    }
    
}
