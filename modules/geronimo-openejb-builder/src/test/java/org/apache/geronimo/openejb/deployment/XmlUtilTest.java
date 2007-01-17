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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb.deployment;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import org.apache.geronimo.openejb.xbeans.ejbjar.OpenejbGeronimoEjbJarType;
import org.apache.geronimo.xbeans.javaee.EjbJarType;
import org.apache.openejb.jee.EjbJar;

/**
 * @version $Rev$ $Date$
 */
public class XmlUtilTest extends TestCase {
    public void testLoadGeronimOpenejbJar() throws Exception {
        URL resource = getClass().getClassLoader().getResource("plans/geronimo-openejb.xml");
        File plan = new File(resource.toURI());
        OpenejbGeronimoEjbJarType openejbGeronimoEjbJarType = XmlUtil.loadGeronimOpenejbJar(plan, null, true, null, null);
        assertNotNull(openejbGeronimoEjbJarType);
    }

    public void testConvertToXmlbeans() throws Exception {
        URL specDDUrl = getClass().getClassLoader().getResource("jee_5schema/ejb-jar.xml");
        assertNotNull(specDDUrl);

        // load the ejb-jar.xml
        String ejbJarXml = XmlUtil.loadEjbJarXml(specDDUrl, null);
        assertNotNull(ejbJarXml);

        EjbJar ejbJar = XmlUtil.unmarshal(EjbJar.class, ejbJarXml);
        EjbJarType ejbJarType = XmlUtil.convertToXmlbeans(ejbJar);
        assertNotNull(ejbJarType);
    }
}
