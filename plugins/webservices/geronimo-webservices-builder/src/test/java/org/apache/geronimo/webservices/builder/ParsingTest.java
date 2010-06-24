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
package org.apache.geronimo.webservices.builder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.testsupport.TestSupport;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * @version $Rev$ $Date$
 */
public class ParsingTest
    extends TestSupport
{
    private SchemaInfoBuilder schemaInfoBuilder;

    public void testSchema1() throws Exception {
        File schema1 = new File(BASEDIR, "src/test/resources/schema/schema1.xsd");
        log.debug("SCHEMA 1");
        Map map = parse(schema1);
        assertEquals(13, map.size());
    }

    public void testSchema2() throws Exception {
        File schema1 = new File(BASEDIR, "src/test/resources/schema/schema2.xsd");
        log.debug("SCHEMA 2");
        Map map = parse(schema1);
        assertEquals(4, map.size());
    }

    public void testSchema3() throws Exception {
        File schema1 = new File(BASEDIR, "src/test/resources/schema/schema3.xsd");
        log.debug("SCHEMA 3");
        Map map = parse(schema1);
        assertEquals(3, map.size());
    }

    private Map parse(File schema1) throws IOException, XmlException, DeploymentException, URISyntaxException {
        XmlObject xmlObject = XmlBeansUtil.parse(schema1.toURI().toURL(), getClass().getClassLoader());
        Collection errors = new ArrayList();
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setErrorListener(errors);
        XmlObject[] schemas = new XmlObject[] {xmlObject};
        SchemaTypeLoader schemaTypeLoader = XmlBeans.loadXsd(new XmlObject[] {});
        SchemaTypeSystem schemaTypeSystem;
        try {
            schemaTypeSystem = XmlBeans.compileXsd(schemas, schemaTypeLoader, xmlOptions);
            if (errors.size() > 0) {
                throw new DeploymentException("Could not compile schema type system: errors: " + errors);
            }
        } catch (XmlException e) {
            throw new DeploymentException("Could not compile schema type system", e);
        }
        schemaInfoBuilder = new SchemaInfoBuilder(null, new URI(""), schemaTypeSystem);
        Map map = schemaInfoBuilder.getSchemaTypeKeyToSchemaTypeMap();
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            log.debug(entry.getKey() + " --> " + entry.getValue());
        }
        return map;
    }

    public void testElementToTypeMapping() throws Exception {
        File schema1 = new File(BASEDIR, "src/test/resources/schema/schema4.xsd");
        log.debug("SCHEMA 4");
        Map map = parse(schema1);
        assertEquals(3, map.size());
        Map elements = schemaInfoBuilder.getElementToTypeMap();
        log.debug("ELEMENT MAP");
        log.debug("{}", elements);
        assertEquals(1, elements.size());
    }

    public void testAnyElements() throws Exception {
        File schema1 = new File(BASEDIR, "src/test/resources/schema/schema5.xsd");
        log.debug("SCHEMA 5");
        Map map = parse(schema1);
        assertEquals(8, map.size());
        Map elements = schemaInfoBuilder.getElementToTypeMap();
        log.debug("ELEMENT MAP");
        log.debug("{}", elements);
        assertEquals(4, elements.size());
    }

    public void testWebservicesJ2ee14() throws Exception {
        URL url = getClass().getClassLoader().getResource("webservices-j2ee14.xml");
        assertNotNull(WSDescriptorParser.getWebservices(url));
    }

    public void testWebservicesJee5() throws Exception {
        URL url = getClass().getClassLoader().getResource("webservices-jee5.xml");
        assertNotNull(WSDescriptorParser.getWebservices(url));
    }

    public void testWebservicesJavaEE6() throws Exception {
        URL url = getClass().getClassLoader().getResource("webservices-javaee6.xml");
        assertNotNull(WSDescriptorParser.getWebservices(url));
    }
}
