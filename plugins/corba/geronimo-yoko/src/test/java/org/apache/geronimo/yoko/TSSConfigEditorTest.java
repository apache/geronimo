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
package org.apache.geronimo.yoko;

import junit.framework.TestCase;
import org.apache.geronimo.corba.CORBABean;
import org.apache.geronimo.corba.deployment.security.config.tss.TSSConfigEditor;
import org.apache.geronimo.corba.security.config.ConfigAdapter;
import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.omg.CORBA.SystemException;
import org.osgi.framework.Bundle;


/**
 * @version $Revision$ $Date$
 */
public class TSSConfigEditorTest extends TestCase {

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

    private static final String TEST_XML4 = "            <tss:tss xmlns:tss=\"http://openejb.apache.org/xml/ns/corba-tss-config-2.1\" xmlns:sec=\"http://geronimo.apache.org/xml/ns/security-1.2\">\n" +
            "                <tss:SSL port=\"6685\" hostname=\"localhost\">\n" +
            "                    <tss:supports>Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</tss:supports>\n" +
            "                    <tss:requires>Integrity Confidentiality EstablishTrustInClient</tss:requires>\n" +
            "                </tss:SSL>\n" +
            "                <tss:compoundSecMechTypeList>\n" +
            "                    <tss:compoundSecMech>\n" +
            "                        <tss:GSSUP targetName=\"geronimo-properties-realm\"/>\n" +
            "                        <tss:sasMech>\n" +
            "                            <tss:identityTokenTypes><tss:ITTAnonymous/><tss:ITTPrincipalNameGSSUP principal-class=\"org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal\" domain=\"foo\"/><tss:ITTDistinguishedName domain=\"foo\"/><tss:ITTX509CertChain domain=\"foo\"/></tss:identityTokenTypes>\n" +
            "                        </tss:sasMech>\n" +
            "                    </tss:compoundSecMech>\n" +
            "                </tss:compoundSecMechTypeList>\n" +
            "            </tss:tss>";

    public void testCORBABean() throws Exception {
        Bundle bundle = new MockBundleContext(getClass().getClassLoader(), "", null, null).getBundle();
        Naming naming = new Jsr77Naming();
        AbstractName testName = naming.createRootName(new Artifact("test", "stuff", "", "ear"), "gbean", NameFactory.CORBA_SERVICE);
        ConfigAdapter configAdapter = new org.apache.geronimo.yoko.ORBConfigAdapter(bundle);
        CORBABean corbaBean = new CORBABean(testName, configAdapter, "localhost", 8050, getClass().getClassLoader(), null, null);
        XmlObject xmlObject = getXmlObject(TEST_XML4);
        TSSConfigEditor editor = new TSSConfigEditor();
        Object o = editor.getValue(xmlObject, null, null, bundle);
        TSSConfig tss = (TSSConfig) o;

        corbaBean.setTssConfig(tss);

        try {
            corbaBean.doStart();
        } catch (SystemException se) {
            se.printStackTrace();
            fail(se.getCause().getMessage());
        } finally {
            try {
                corbaBean.doStop();
            } catch (Throwable e) {

            }
        }
    }
}

