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
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import junit.framework.TestCase;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/17 06:55:11 $
 *
 * */
public class EJB20To21TransformTest extends TestCase {

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

    public void testXMLBeansTransform() throws Exception {
        File srcXml = new File("src/test-data/j2ee_1_3dtd/ejb-jar.xml");
        File expectedOutputXml = new File("src/test-data/j2ee_1_3dtd/ejb-jar-21.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = SchemaConversionUtils.convertToEJBSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        List problems = new ArrayList();
        assertTrue("Differences: " + problems, compareXmlObjects(xmlObject, expected, problems));
        //make sure trying to convert twice has no bad effects
        XmlCursor cursor2 = xmlObject.newCursor();
        try {
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            assertFalse(SchemaConversionUtils.convertToSchema(cursor2, schemaLocationURL, version));
        } finally {
            cursor2.dispose();
        }
        assertTrue("Differences after reconverting to schema: " + problems, compareXmlObjects(xmlObject, expected, problems));
    }


    private boolean compareXmlObjects(XmlObject xmlObject, XmlObject expectedObject, List problems) {
        XmlCursor test = xmlObject.newCursor();
        XmlCursor expected = expectedObject.newCursor();
        boolean similar = true;
        int elementCount = 0;
        while (toNextStartToken(test)) {
            elementCount++;
            if (!toNextStartToken(expected)) {
                problems.add("test longer than expected at element: " + elementCount);
                return false;
            }
            if (!test.getChars().equals(expected.getChars())) {
                problems.add("Different elements at elementCount: " + elementCount + ", test: " + test.getChars() + ", expected: " + expected.getChars());
                similar = false;
            }
            test.toNextToken();
            expected.toNextToken();
        }
        return similar;
    }

    private boolean toNextStartToken(XmlCursor cursor) {
        while (!cursor.isStart()) {
            if (!cursor.hasNextToken()) {
                return false;
            }
            cursor.toNextToken();
        }
        return true;
    }

}
