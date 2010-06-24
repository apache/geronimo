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
package org.apache.geronimo.client.builder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Rev$ $Date$
 */
public class SchemaConversionTest extends XmlBeansTestSupport {
    private ClassLoader classLoader = this.getClass().getClassLoader();


    public void testDummy() {

    }

    //TODO move tests to openejb-jee
//    public void testApplicationClient13ToApplicationClient6Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/application-client-13.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/application-client-6.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = AppClientModuleBuilder.convertToApplicationClientSchema(xmlObject);
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/application_6.xsd";
//            String version = "6";
//            assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = AppClientModuleBuilder.convertToApplicationClientSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences after reconverting to application client schema: " + problems, ok3);
//    }

//    public void testApplicationClient14ToApplicationClient6Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/application-client-14.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/application-client-6.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = AppClientModuleBuilder.convertToApplicationClientSchema(xmlObject);
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/application_6.xsd";
//            String version = "6";
//            assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = AppClientModuleBuilder.convertToApplicationClientSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences after reconverting to application client schema: " + problems, ok3);
//    }

//    public void testApplicationClient15ToApplicationClient6Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/application-client-5.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/application-client-6.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = AppClientModuleBuilder.convertToApplicationClientSchema(xmlObject);
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/javaee/application_6.xsd";
//            String version = "6";
//            assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.JAVAEE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = AppClientModuleBuilder.convertToApplicationClientSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences after reconverting to application client schema: " + problems, ok3);
//    }
}
