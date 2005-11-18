/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.web.deployment;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

/**
 * @version $Rev$ $Date$
 */
public class GenericToSpecificPlanConverterTest extends TestCase {
    private ClassLoader classLoader = this.getClass().getClassLoader();

    public void testConvertPlan1() throws Exception {
        testConvertPlan("plans/tomcat-pre.xml");
    }
    public void testConvertPlan2() throws Exception {
        testConvertPlan("plans/tomcat-pre2.xml");
    }
    public void testConvertPlan3() throws Exception {
        testConvertPlan("plans/tomcat-pre3.xml");
    }

    public void testConvertPlan(String prePlanName) throws Exception {
        URL srcXml = classLoader.getResource(prePlanName);
        URL expectedOutputXml = classLoader.getResource("plans/tomcat-post.xml");
        XmlObject rawPlan = XmlObject.Factory.parse(srcXml);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        XmlObject webPlan = new GenericToSpecificPlanConverter("http://geronimo.apache.org/xml/ns/web/tomcat/config-1.0",
                "http://geronimo.apache.org/xml/ns/j2ee/web/tomcat-1.0", "tomcat").convertToSpecificPlan(rawPlan);

//        System.out.println(webPlan.toString());
//        System.out.println(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(webPlan, expected, problems);
        assertTrue("Differences: " + problems, ok);
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
            QName actualChars = test.getName();
            QName expectedChars = expected.getName();
            if (!actualChars.equals(expectedChars)) {
                problems.add("Different elements at elementCount: " + elementCount + ", test: " + actualChars + ", expected: " + expectedChars);
                similar = false;
            }
            test.toNextToken();
            expected.toNextToken();
        }
        if (toNextStartToken(expected)) {
            problems.add("test shorter that expected at element: " + elementCount);
            similar = false;
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
