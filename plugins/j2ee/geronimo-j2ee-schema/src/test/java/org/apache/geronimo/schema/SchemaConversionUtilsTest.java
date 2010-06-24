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

package org.apache.geronimo.schema;

import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Rev$ $Date$
 */
public class SchemaConversionUtilsTest extends XmlBeansTestSupport {
    private ClassLoader classLoader = this.getClass().getClassLoader();

//comment on validity of j2ee 1.4 schemas: validation doesn't work...
//        From: "Radu Preotiuc-Pietro" <radup@bea.com>
//        Date: Tue Jun 15, 2004  3:37:50 PM US/Pacific
//        To: <xmlbeans-user@xml.apache.org>
//        Subject: RE: Problem with validate -- xsb schema file missing/wrong name
//        Reply-To: xmlbeans-user@xml.apache.org
//
//        Unfortunately, there is an issue in XmlBeans v1 having to do with duplicate id constraints definitions.
//        XmlBeans v2 does not have this issue.
//        Also, these ejb Schemas are techically incorrect because they violate the id constraint uniqueness rule (at least when processed together, you could try and compile each one separately)
//        So, there are a couple of options:
//        1. you hand-edit the schemas to rename those problematic id constraints
//        2. you upgrade to v2
//        Well, there is a third alternative, which is a fix integrated in XmlBeans v1, may or may not be feasible
//
//        Radu

    //I've taken option (1) and fixed the schemas

    //The schemas have been fixed by sun, we can use the official schemas.

    public void testOrderDescriptionGroup() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/DescriptionGroupTestSource.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/DescriptionGroupTestExpected.xml");
        XmlObject srcObject = XmlObject.Factory.parse(srcXml);
        XmlCursor srcCursor = srcObject.newCursor();
        XmlCursor moveable = srcObject.newCursor();
        try {
            srcCursor.toFirstChild();
            srcCursor.toFirstChild();
            assertTrue(srcCursor.getName().toString(), "filter".equals(srcCursor.getName().getLocalPart()));
            do {
                srcCursor.push();
                srcCursor.toFirstChild();
                SchemaConversionUtils.convertToDescriptionGroup(SchemaConversionUtils.J2EE_NAMESPACE, srcCursor, moveable);
                srcCursor.pop();
            } while (srcCursor.toNextSibling());
        } finally {
            srcCursor.dispose();
        }
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(srcObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }

    public void testOrderJNDIEnvironmentRefsGroup() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/JNDIEnvironmentRefsGroupTestSource.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/JNDIEnvironmentRefsGroupTestExpected.xml");
        XmlObject srcObject = XmlObject.Factory.parse(srcXml);
        XmlCursor srcCursor = srcObject.newCursor();
        XmlCursor moveable = srcObject.newCursor();
        try {
            srcCursor.toFirstChild();
            srcCursor.toFirstChild();
            assertTrue(srcCursor.getName().toString(), "web-app".equals(srcCursor.getName().getLocalPart()));
            do {
                srcCursor.push();
                srcCursor.toFirstChild();
                srcCursor.toNextSibling();
                srcCursor.toNextSibling();
                moveable.toCursor(srcCursor);
                SchemaConversionUtils.convertToJNDIEnvironmentRefsGroup(SchemaConversionUtils.J2EE_NAMESPACE, srcCursor, moveable);
                srcCursor.pop();
            } while (srcCursor.toNextSibling());
        } finally {
            srcCursor.dispose();
        }
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(srcObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
    }

    public void testGeronimoNamingNamespaceChange() throws Exception {
        URL srcXml = classLoader.getResource("geronimo/ejb-naming-pre.xml");
        URL expectedOutputXml = classLoader.getResource("geronimo/ejb-naming-post.xml");
        XmlObject xmlObject = XmlBeansUtil.parse(srcXml, this.getClass().getClassLoader());
        XmlCursor cursor = xmlObject.newCursor();
        try {
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            log.debug(expected.toString());
            
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok);
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok2);
        } finally {
            cursor.dispose();
        }
    }
    
    public void testSecurityElementConverter() throws Exception {
        URL srcXml = classLoader.getResource("geronimo/security-pre.xml");
        URL expectedOutputXml = classLoader.getResource("geronimo/security-post.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        ElementConverter elementConverter = new SecurityElementConverter();
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = cursor.newCursor();
        try {
            elementConverter.convertElement(cursor, end);
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok);
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok2);
        } finally {
            cursor.dispose();
            end.dispose();
        }

    }

    public void testGBeanElementConverter() throws Exception {
        URL srcXml = classLoader.getResource("geronimo/gbean-pre.xml");
        URL expectedOutputXml = classLoader.getResource("geronimo/gbean-post.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        ElementConverter elementConverter = new GBeanElementConverter();
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = cursor.newCursor();
        try {
            elementConverter.convertElement(cursor, end);
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok);
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok2);
        } finally {
            cursor.dispose();
            end.dispose();
        }

    }

    public void testQNameConverter1() throws Exception {
        URL srcXml = classLoader.getResource("geronimo/qname1-pre.xml");
        URL expectedOutputXml = classLoader.getResource("geronimo/qname1-post.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        ElementConverter elementConverter = new QNameConverter("import", "http://geronimo.apache.org/xml/ns/deployment-1.0", "parent");
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = cursor.newCursor();
        try {
            elementConverter.convertElement(cursor, end);
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok);
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok2);
        } finally {
            cursor.dispose();
            end.dispose();
        }

    }
    public void testQNameConverter2() throws Exception {
        URL srcXml = classLoader.getResource("geronimo/qname2-pre.xml");
        URL expectedOutputXml = classLoader.getResource("geronimo/qname2-post.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        ElementConverter elementConverter = new QNameConverter("import", "http://geronimo.apache.org/xml/ns/deployment-1.0", "parent");
        XmlCursor cursor = xmlObject.newCursor();
        XmlCursor end = cursor.newCursor();
        try {
            elementConverter.convertElement(cursor, end);
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok);
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok2);
        } finally {
            cursor.dispose();
            end.dispose();
        }

    }
    public void testWebMessageDestination1() throws Exception {
        URL srcXml = classLoader.getResource("geronimo/web-md-pre.xml");
        URL expectedOutputXml = classLoader.getResource("geronimo/web-md-post.xml");
        XmlObject xmlObject = XmlBeansUtil.parse(srcXml, this.getClass().getClassLoader());
        XmlCursor cursor = xmlObject.newCursor();
        try {
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok);
            SchemaConversionUtils.convertToGeronimoSubSchemas(cursor);
            boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
            assertTrue("Differences: " + problems, ok2);
        } finally {
            cursor.dispose();
        }

    }

}
