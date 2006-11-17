/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.web.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Rev$ $Date$
 */
public class SchemaConversionTest extends XmlBeansTestSupport {
    private static final Log log = LogFactory.getLog(SchemaConversionTest.class);

    private ClassLoader classLoader = this.getClass().getClassLoader();

    public void testWeb23To24Transform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/web-23.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/web-24.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWeb23To24OtherTransform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/web-1-23.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/web-1-24.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
//        log.debug(xmlObject.toString());
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWeb22To24Transform1() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_2dtd/web-1-22.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_2dtd/web-1-24.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWebRejectBad24() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_4schema/web-1-24.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        try {
            AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
            fail("doc src/test-data/j2ee_1_4schema/web-1-24.xml is invalid, should not have validated");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testParseWeb24() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_4schema/web-2-24.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = AbstractWebModuleBuilder.convertToServletSchema(xmlObject);
        assertNotNull(xmlObject);
    }

}
