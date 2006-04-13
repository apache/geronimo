/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.plugin.packaging;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev:$ $Date:$
 */
public class PlanProcessorTest extends TestCase {

    public void testExistingEnvironmentMerge() throws Exception {
        URL planURL = this.getClass().getClassLoader().getResource("plan.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(planURL);
        XmlCursor xmlCursor = xmlObject.newCursor();
        Artifact configId = new Artifact("groupId", "artifactId", "version", "car");
        LinkedHashSet dependencies = new LinkedHashSet();
        PlanProcessor planProcessor = new PlanProcessor();
        planProcessor.mergeEnvironment(xmlCursor, configId, dependencies);
        System.out.println(xmlObject.toString());
        URL expectedURL = this.getClass().getClassLoader().getResource("result.xml");
        XmlObject result = XmlObject.Factory.parse(expectedURL);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, result, problems);
        assertTrue("problems: " + problems, ok);

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
