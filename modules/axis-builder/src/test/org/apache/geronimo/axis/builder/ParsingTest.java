/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.axis.builder;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;
import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.common.DeploymentException;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ParsingTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private SchemaInfoBuilder schemaInfoBuilder;

    public void testSchema1() throws Exception {
        File schema1 = new File(basedir, "src/test-resources/schema/schema1.xsd");
        System.out.println("SCHEMA 1");
        Map map = parse(schema1);
        assertEquals(13, map.size());
    }

    public void testSchema2() throws Exception {
        File schema1 = new File(basedir, "src/test-resources/schema/schema2.xsd");
        System.out.println("SCHEMA 2");
        Map map = parse(schema1);
        assertEquals(4, map.size());
    }

    public void testSchema3() throws Exception {
        File schema1 = new File(basedir, "src/test-resources/schema/schema3.xsd");
        System.out.println("SCHEMA 3");
        Map map = parse(schema1);
        assertEquals(3, map.size());
    }

    private Map parse(File schema1) throws IOException, XmlException, DeploymentException, URISyntaxException {
        XmlObject xmlObject = SchemaConversionUtils.parse(schema1.toURL());
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
            System.out.println(entry.getKey() + " --> " + entry.getValue());
        }
        return map;
    }

    public void testElementToTypeMapping() throws Exception {
        File schema1 = new File(basedir, "src/test-resources/schema/schema4.xsd");
        System.out.println("SCHEMA 4");
        Map map = parse(schema1);
        assertEquals(3, map.size());
        Map elements = schemaInfoBuilder.getElementToTypeMap();
        System.out.println("ELEMENT MAP");
        System.out.println(elements);
        assertEquals(1, elements.size());
    }

}
