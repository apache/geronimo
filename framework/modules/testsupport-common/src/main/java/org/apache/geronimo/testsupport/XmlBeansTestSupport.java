/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.geronimo.testsupport;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;

/**
 * @version $Rev$ $Date$
 */
public class XmlBeansTestSupport extends TestSupport {

    /**
     * Constructor for tests that specify a specific test name.
     *
     * @see #TestSupport()  This is the prefered constructor for sub-classes to use.
     */
    protected XmlBeansTestSupport(final String name) {
        super(name);
    }

    /**
     * Default constructor.
     */
    protected XmlBeansTestSupport() {
        super();
    }

    public boolean compareXmlObjects(XmlObject xmlObject, XmlObject expectedObject, List problems) {
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

    public boolean toNextStartToken(XmlCursor cursor) {
        while (!cursor.isStart()) {
            if (!cursor.hasNextToken()) {
                return false;
            }
            cursor.toNextToken();
        }
        return true;
    }



}
