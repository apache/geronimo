/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.geronimo.openejb.deployment;

import org.apache.geronimo.testsupport.XmlBeansTestSupport;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class SchemaConversionTest extends XmlBeansTestSupport {

    // Dain: I don't believe we need this test anymore since openejb is doing the converstions.
    public void testNothing() {
    }

    //TODO consider moving tests to openejb-jee.

//    public void XtestEJB11ToEJB21Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_2dtd/ejb-1-11.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_2dtd/ejb-1-21.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
////        log.debug(xmlObject.toString());
////        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
//            String version = "2.1";
//            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
//    }

//    public void XtestEJB20ToEJB21Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/ejb-jar.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/ejb-jar-21.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
////        log.debug(xmlObject.toString());
////        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
//            String version = "2.1";
//            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
//    }

//    public void xtestMDB20ToEJB21TransformBugGERONIMO_1649() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-20-GERONIMO-1649.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-21-GERONIMO-1649.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
////        log.debug(xmlObject.toString());
////        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
//            String version = "2.1";
//            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
//    }

//    public void XtestMDB20To21Transform() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-20.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-21.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        XmlBeansUtil.validateDD(expected);
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
////        log.debug(xmlObject.toString());
////        log.debug(expected.toString());
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences: " + problems, ok);
//        //make sure trying to convert twice has no bad effects
//        XmlCursor cursor2 = xmlObject.newCursor();
//        try {
//            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
//            String version = "2.1";
//            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
//        } finally {
//            cursor2.dispose();
//        }
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
//        //do the whole transform twice...
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
//        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
//    }


//    public void XtestEJB21To21DoesNothing() throws Exception {
//        URL srcXml = classLoader.getResource("j2ee_1_4schema/ejb-jar.xml");
//        URL expectedOutputXml = classLoader.getResource("j2ee_1_4schema/ejb-jar.xml");
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
//        xmlObject = XmlUtil.convertToEJBSchema(xmlObject);
//        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        Assert.assertTrue("Differences: " + problems, ok);
//    }

}
