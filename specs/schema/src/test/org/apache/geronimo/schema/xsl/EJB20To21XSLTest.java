/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.schema.xsl;

import java.io.File;
import java.io.FileReader;

import javax.xml.transform.stream.StreamSource;

import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Revision: 1.3 $ $Date: 2004/06/16 05:59:27 $
 *
 * */
public class EJB20To21XSLTest extends XMLTestCase {
    public void testNothing() {}
    public void test20To21Conversion() throws Exception {
        File srcXml = new File("src/test-data/j2ee_1_3dtd/ejb-jar.xml");
        File xsl2To21 = new File("src/xsl/ejb_2_0_to_2_1.xsl");
        File expectedOutputXml = new File("src/test-data/j2ee_1_3dtd/ejb-jar-21.xml");

        //validate the source doc against the ejb 2.0 dtd
        //validation doesn't really seem to work very well
//        XMLUnit.getTestDocumentBuilderFactory().setValidating(true);
//        Document testDocument = XMLUnit.buildTestDocument(new InputSource(new FileReader(srcXml)));
//        String systemId = "http://java.sun.com/dtd/ejb-jar_2_0.dtd";
//        String dTDUrl = new File("src/j2ee_1_3dtd/ejb-jar_2_0.dtd").toURL().toExternalForm();
//        Validator validator = new Validator(testDocument, systemId, dTDUrl);
////        Validator validator = new Validator(new FileReader(srcXml));
//        assertTrue("test document should be valid" + validator.toString(), validator.isValid());

        Transform transform = new Transform(new StreamSource(srcXml), new StreamSource(xsl2To21));
        //Without writing out to a string and re-parsing, the diff doesn't realize that all sub-elements
        //are in the same namespace as the top level element.  I don't know where the problem lies.
        String transformString = transform.getResultString();
        Document actual = XMLUnit.buildTestDocument(transformString);
        System.out.println(transform.getResultString());
        Document expected = XMLUnit.buildDocument(XMLUnit.getControlParser(), new FileReader(expectedOutputXml));
        Diff diff = new Diff(expected, actual);
        assertTrue("Expected good transform: " + diff, diff.similar());

        //check that expected output is consistent with xmlbeans
        EjbJarDocument ejbJarDocExpected = EjbJarDocument.Factory.parse(expectedOutputXml);
//both validations fail.. asking on xmbeans list about why.
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
//        assertTrue(ejbJarDocExpected.validate());

        //check that result is consistent with xmlbeans
//        Document transformDoc = transform.getResultDocument();
        EjbJarDocument ejbJarDoc = EjbJarDocument.Factory.parse(transformString);
//        assertTrue(ejbJarDoc.validate());
    }
}
