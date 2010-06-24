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

package org.apache.geronimo.myfaces.deployment;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Schema conversion tests for various faces-config XML files. These are fairly straight-forward
 * tests as the only tag that must be updated is the <faces-config> tab. The following DTD and XSD
 * schema versions are tested:
 *
 * <ol>
 *      <li>1.0 DTD
 *      <li>1.1 DTD
 *      <li>1.2 XSD
 *      <li>No schema
 * </ol>
 */
public class SchemaConversionTest extends XmlBeansTestSupport {
  //TODO move tests to openejb-jee
    public void testDummy() {}
/*
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private XmlOptions options = new XmlOptions();


    */
/**
     * Tests for empty faces-config.xml files
     *//*

    public void testFacesConfig10Empty() throws Exception {
        URL srcXML = classLoader.getResource("1_0_dtd/faces-config-empty-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-empty-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig11Empty() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/faces-config-empty-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-empty-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig12Empty() throws Exception {
        URL srcXML = classLoader.getResource("1_2_xsd/faces-config-empty-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-empty-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfigNoSchemaEmpty() throws Exception {
        URL srcXML = classLoader.getResource("no_schema/faces-config-empty-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-empty-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }


    */
/**
     * Tests for simple faces-config.xml files
     *//*

    public void testFacesConfig10Simple() throws Exception {
        URL srcXML = classLoader.getResource("1_0_dtd/faces-config-simple-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-simple-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig11Simple() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/faces-config-simple-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-simple-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig12Simple() throws Exception {
        URL srcXML = classLoader.getResource("1_2_xsd/faces-config-simple-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-simple-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfigNoSchemaSimple() throws Exception {
        URL srcXML = classLoader.getResource("no_schema/faces-config-simple-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-simple-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }


    */
/**
     * Tests for somewhat moderate faces-config.xml files
     *//*

    public void testFacesConfig10Moderate() throws Exception {
        URL srcXML = classLoader.getResource("1_0_dtd/faces-config-moderate-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-moderate-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig11Moderate() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/faces-config-moderate-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-moderate-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig12Moderate() throws Exception {
        URL srcXML = classLoader.getResource("1_2_xsd/faces-config-moderate-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-moderate-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfigNoSchemaModerate() throws Exception {
        URL srcXML = classLoader.getResource("no_schema/faces-config-moderate-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-moderate-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }


    */
/**
     * Tests for slightly more complex faces-config.xml files
     *//*

    public void testFacesConfig10Complex() throws Exception {
        URL srcXML = classLoader.getResource("1_0_dtd/faces-config-complex-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-complex-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig11Complex() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/faces-config-complex-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-complex-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfig12Complex() throws Exception {
        URL srcXML = classLoader.getResource("1_2_xsd/faces-config-complex-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-complex-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testFacesConfigNoSchemaComplex() throws Exception {
        URL srcXML = classLoader.getResource("no_schema/faces-config-complex-src.xml");
        URL expectedXML = classLoader.getResource("1_0_dtd/faces-config-complex-expected.xml");
        parseAndCompare(srcXML, expectedXML);
    }


    */
/**
     * Common logic
     *//*

    private void parseAndCompare(URL srcXML, URL expectedXML) throws Exception {
        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
        xmlObject = MyFacesModuleBuilderExtension.convertToFacesConfigSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedXML);
        log.debug("[Source XML] " + '\n' + xmlObject.toString() + '\n');
        log.debug("[Expected XML]" + '\n' + expected.toString() + '\n');
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = MyFacesModuleBuilderExtension.convertToFacesConfigSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }
*/
}
