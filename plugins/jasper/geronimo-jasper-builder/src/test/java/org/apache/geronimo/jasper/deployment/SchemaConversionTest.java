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

package org.apache.geronimo.jasper.deployment;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * Schema conversion tests for various JSP TLD files. The following DTD and XSD schema versions are
 * tested:
 *
 * <ol>
 *      <li>1.1 DTD
 *      <li>1.2 DTD
 *      <li>2.0 XSD
 *      <li>2.1 XSD
 * </ol>
 *
 * <p><strong>Note(s):</strong>
 * <ul>
 *      <li>Those tags from the 1.1 and 1.2 DTD that are no longer valid (e.g., jsp-version) are
 *      removed
 *      <li>Valid  tags from the 1.1 and 1.2 DTD are converted (e.g., tlibversion to
 *      tlib-version)
 *      <li>The <taglib> root and the <tag> root elements are reordered as necessary (i.e.,
 *      description, display-name)
 *      <li>The <rtexprvalue> tag is inserted in the <attribute> tag if necessary since it was
 *      not required to preceed <type> in 2.0 schema. Default value of false is used.
 * </ul>
 */
public class SchemaConversionTest extends XmlBeansTestSupport {
    private ClassLoader classLoader = this.getClass().getClassLoader();
    private XmlOptions options = new XmlOptions();


    /**
     * Tests for empty TLD files
    */
    public void testTLD11Empty() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/taglib-empty-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-empty-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD12Empty() throws Exception {
        URL srcXML = classLoader.getResource("1_2_dtd/taglib-empty-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-empty-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD20Empty() throws Exception {
        URL srcXML = classLoader.getResource("2_0_xsd/taglib-empty-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-empty-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD21Empty() throws Exception {
        URL srcXML = classLoader.getResource("2_1_xsd/taglib-empty-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-empty-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }


    /**
     * Tests for removal of obsolete TLD tags
    */
    public void testTLD11Obsolete() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/taglib-obsolete-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-obsolete-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD12Obsolete() throws Exception {
        URL srcXML = classLoader.getResource("1_2_dtd/taglib-obsolete-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-obsolete-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }


    /**
     * Tests for reordering TLD tags
    */
    public void testTLD11Reorder() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/taglib-reorder-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-reorder-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD12Reorder_1() throws Exception {
        URL srcXML = classLoader.getResource("1_2_dtd/taglib-reorder-src-1.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-reorder-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD12Reorder_2() throws Exception {
        URL srcXML = classLoader.getResource("1_2_dtd/taglib-reorder-src-2.tld");
        URL expectedXML = classLoader.getResource("1_2_dtd/taglib-reorder-expected-2.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD12Reorder_3() throws Exception {
        URL srcXML = classLoader.getResource("1_2_dtd/taglib-reorder-src-3.tld");
        URL expectedXML = classLoader.getResource("2_0_xsd/taglib-reorder-expected-3.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD20Reorder_1() throws Exception {
        URL srcXML = classLoader.getResource("2_0_xsd/taglib-reorder-src-1.tld");
        URL expectedXML = classLoader.getResource("2_0_xsd/taglib-reorder-expected-1.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD20Reorder_2() throws Exception {
        URL srcXML = classLoader.getResource("2_0_xsd/taglib-reorder-src-2.tld");
        URL expectedXML = classLoader.getResource("2_0_xsd/taglib-reorder-expected-2.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    
    /**
     * Tests for missing TLD tags
    */
    public void testTLD11Missing() throws Exception {
        URL srcXML = classLoader.getResource("1_1_dtd/taglib-missing-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-missing-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }
    public void testTLD12Missing() throws Exception {
        URL srcXML = classLoader.getResource("1_2_dtd/taglib-missing-src.tld");
        URL expectedXML = classLoader.getResource("1_1_dtd/taglib-missing-expected.tld");
        parseAndCompare(srcXML, expectedXML);
    }

    /**
     * Common logic
     */
    private void parseAndCompare(URL srcXML, URL expectedXML) throws Exception {
        //TODO move this to openejb-jee.
//        XmlObject xmlObject = XmlObject.Factory.parse(srcXML, options);
//        xmlObject = JspModuleBuilderExtension.convertToTaglibSchema(xmlObject);
//        XmlObject expected = XmlObject.Factory.parse(expectedXML);
//        List problems = new ArrayList();
//        boolean ok = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok);
//        xmlObject = JspModuleBuilderExtension.convertToTaglibSchema(xmlObject);
//        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
//        assertTrue("Differences: " + problems, ok2);
    }
}
