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

package org.apache.geronimo.upgrade;


import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Rev$ $Date$
 */
public class Upgrade1_0To1_1Test extends TestCase {

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    public void test1() throws Exception {
        test("appclient_ejb_1");
    }

    public void test2() throws Exception {
        test("appclient_dep_1");
    }

    public void test3() throws Exception {
        test("transport_1");
    }

    public void test4() throws Exception {
        test("transport_2");
    }

    public void test5() throws Exception {
        test("assembly_1");
    }

    public void test6() throws Exception {
        try {
            test("ejb_pkgen_1");
            fail();
        } catch (XmlException e) {

        }
    }

    public void test7() throws Exception {
        test("servlet_1");
    }

    public void test8() throws Exception {
        test("gbean_1");
    }

    private void test(String testName) throws Exception {
        InputStream srcXml = classLoader.getResourceAsStream(testName + ".xml");
        try {
            Writer targetXml = new StringWriter();
            new Upgrade1_0To1_1().upgrade(srcXml, targetXml);
        
            String targetString = targetXml.toString();
            XmlObject targetXmlObject = XmlObject.Factory.parse(targetString);
            URL expectedOutputXml = classLoader.getResource(testName + "_result.xml");
            XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
            List problems = new ArrayList();
            boolean ok = compareXmlObjects(targetXmlObject, expected, problems);
            if (!ok) {
                System.out.println(targetString);
            }
            assertTrue("Differences: " + problems, ok);
        } finally {
            if (srcXml != null)
            {
                try {
                    srcXml.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
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
            QName actualQName = test.getName();
            QName expectedQName = expected.getName();
            if (!actualQName.equals(expectedQName)) {
                problems.add("Different elements at elementCount: " + elementCount + ", test: " + actualQName + ", expected: " + expectedQName);
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
