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

package org.apache.geronimo.web25.deployment;

import org.apache.geronimo.testsupport.XmlBeansTestSupport;

/**
 * @version $Rev$ $Date$
 */
public class SchemaConversionTest extends XmlBeansTestSupport {

    private ClassLoader classLoader = this.getClass().getClassLoader();

    //TODO move these tests to openejb-jee
    public void testDummy() {

    }

//    public void testWeb25To30Transform() throws Exception {
//        URL srcXml = classLoader.getResource("javaee_5schema/web-2-25.xml");
//        URL expectedOutputXml = classLoader.getResource("javaee_6schema/web-2-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml, options);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok2);
//    }
//
//    public void testWeb24To30Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_4schema/web-2-24.xml");
//        URL expectedOutputXml = classLoader.getResource("javaee_6schema/web-2-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml, options);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok2);
//    }
//
//    public void testWeb23To30Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/web-23.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/web-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok2);
//    }
//
//    public void testWeb23To30OtherTransform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/web-1-23.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/web-1-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
////        log.debug(xmlObject.toString());
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok2);
//    }
//
//    public void testWeb22To30Transform1() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_2dtd/web-1-22.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_2dtd/web-1-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
////        log.debug(xmlObject.toString());
////        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        xmlObject = SchemaConversionUtils.convertToServletSchema(xmlObject);
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok2);
//    }
//
//    public void testWebRejectBad30() throws Exception {
//        URL srcXml = classLoader.getResource("javaee_6schema/web-1-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        try {
//            SchemaConversionUtils.convertToServletSchema(xmlObject);
//            fail("doc src/test-data/javaee_5schema/web-1-30.xml is invalid, should not have validated");
//        } catch (XmlException e) {
//            //expected
//        }
//    }
//
//    public void testParseWeb30() throws Exception {
//        URL srcXml = classLoader.getResource("javaee_6schema/web-2-30.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        SchemaConversionUtils.convertToServletSchema(xmlObject);
//    }

}
