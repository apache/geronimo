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

package org.apache.geronimo.web25.deployment;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.jar.JarFile;

import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.common.DeploymentException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class SchemaConversionTest extends XmlBeansTestSupport {

    private ClassLoader classLoader = this.getClass().getClassLoader();
    private XmlOptions options = new XmlOptions();
    private WebModuleBuilder webModuleBuilder = new WebModuleBuilder(null);

    public void testWeb24To25Transform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_4schema/web-2-24.xml");
        URL expectedOutputXml = classLoader.getResource("javaee_5schema/web-2-25.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml, options);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        log.debug(xmlObject.toString());
        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWeb23To25Transform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/web-23.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/web-25.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        log.debug(xmlObject.toString());
        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWeb23To25OtherTransform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/web-1-23.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/web-1-25.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
//        log.debug(xmlObject.toString());
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWeb22To25Transform1() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_2dtd/web-1-22.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_2dtd/web-1-25.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok);
        xmlObject = webModuleBuilder.convertToServletSchema(xmlObject);
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        assertTrue("Differences: " + problems, ok2);
    }

    public void testWebRejectBad25() throws Exception {
        URL srcXml = classLoader.getResource("javaee_5schema/web-1-25.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        try {
            webModuleBuilder.convertToServletSchema(xmlObject);
            fail("doc src/test-data/javaee_5schema/web-1-25.xml is invalid, should not have validated");
        } catch (XmlException e) {
            //expected
        }
    }

    public void testParseWeb25() throws Exception {
        URL srcXml = classLoader.getResource("javaee_5schema/web-2-25.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        webModuleBuilder.convertToServletSchema(xmlObject);
    }

    private static class WebModuleBuilder extends AbstractWebModuleBuilder {

        protected WebModuleBuilder(Kernel kernel) {
            super(kernel, null, null, null, Collections.EMPTY_SET, null);
        }

        public Module createModule(Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
            return null;
        }
        
        protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
            return null;
        }

        public void initContext(EARContext earContext, Module module, Bundle classLoader) throws DeploymentException {
        }

        public void addGBeans(EARContext earContext, Module module, Bundle classLoader, Collection repositories) throws DeploymentException {
        }

        public String getSchemaNamespace() {
            return null;
        }
        
    }


}
