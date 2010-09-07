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
package org.apache.geronimo.corba.security.config.tss;

import junit.framework.TestCase;
import org.apache.geronimo.corba.deployment.security.config.tss.TSSConfigEditor;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.apache.geronimo.common.DeploymentException;


/**
 * @version $Revision: 452600 $ $Date: 2006-10-03 12:29:42 -0700 (Tue, 03 Oct 2006) $
 */
public class TSSConfigEditorTest extends TestCase {
    private static final String TEST_XML1 = "<foo:tss xmlns:foo=\"http://openejb.apache.org/xml/ns/corba-tss-config-2.1\">\n" +
//                                            "                <foo:description>this is a foo</foo:description>" +
                                            "                <foo:SSL port=\"443\" hostname=\"corba.apache.org\">\n" +
                                            "                    <foo:supports>Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</foo:supports>\n" +
                                            "                    <foo:requires>Integrity</foo:requires>\n" +
                                            "                </foo:SSL>\n" +
                                            "                <foo:compoundSecMechTypeList>\n" +
                                            "                    <foo:compoundSecMech>\n" +
                                            "                    </foo:compoundSecMech>\n" +
                                            "                </foo:compoundSecMechTypeList>\n" +
                                            "            </foo:tss>";
    private static final String TEST_XML2 = "<foo:tss inherit=\"true\" xmlns:foo=\"http://openejb.apache.org/xml/ns/corba-tss-config-2.1\"/>";
    private static final String TEST_XML3 = "<tss xmlns=\"http://openejb.apache.org/xml/ns/corba-tss-config-2.1\">\n" +
                                            "                <SSL port=\"443\">\n" +
                                            "                    <supports>BAD_ENUM Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</supports>\n" +
                                            "                    <requires>Integrity</requires>\n" +
                                            "                </SSL>\n" +
                                            "                <compoundSecMechTypeList>\n" +
                                            "                    <compoundSecMech>\n" +
                                            "                    </compoundSecMech>\n" +
                                            "                </compoundSecMechTypeList>\n" +
                                            "            </tss>";


    private XmlObject getXmlObject(String xmlString) throws XmlException {
        XmlObject xmlObject = XmlObject.Factory.parse(xmlString);
        XmlCursor xmlCursor = xmlObject.newCursor();
        try {
            xmlCursor.toFirstChild();
            return xmlCursor.getObject();
        } finally {
            xmlCursor.dispose();
        }
    }

    public void testSimple1() throws Exception {
        XmlObject xmlObject = getXmlObject(TEST_XML1);
        TSSConfigEditor editor = new TSSConfigEditor();
        Object o = editor.getValue(xmlObject, null, null, null);
        TSSConfig tss = (TSSConfig) o;
        assertFalse(tss.isInherit());
        assertNotNull(tss.getTransport_mech());
    }

    public void testSimple2() throws Exception {
        XmlObject xmlObject = getXmlObject(TEST_XML2);
        TSSConfigEditor editor = new TSSConfigEditor();
        TSSConfig tss = (TSSConfig) editor.getValue(xmlObject, null, null, null);
        assertTrue(tss.isInherit());
        assertNotNull(tss.getTransport_mech());
        assertTrue(tss.getTransport_mech() instanceof TSSNULLTransportConfig);
    }

    public void testSimple3() throws Exception {
        try {
            XmlObject xmlObject = getXmlObject(TEST_XML3);
            TSSConfigEditor editor = new TSSConfigEditor();
            TSSConfig tss = (TSSConfig) editor.getValue(xmlObject, null, null, null);
            fail("Should fail");
        } catch (DeploymentException e) {
        }

    }
}
