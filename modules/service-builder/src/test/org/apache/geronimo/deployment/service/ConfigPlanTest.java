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

package org.apache.geronimo.deployment.service;

import java.net.URL;

import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.DependencyType;

import junit.framework.TestCase;

/**
 * 
 * 
 * @version $Rev$ $Date$
 */
public class ConfigPlanTest extends TestCase {
    private URL plan1;

    public void testParser() throws Exception {
        ConfigurationDocument doc = ConfigurationDocument.Factory.parse(plan1);
        ConfigurationType configuration = doc.getConfiguration();
        assertEquals("test/plan1", configuration.getConfigId());

        DependencyType[] dependencies = configuration.getDependencyArray();
        assertEquals(1, dependencies.length);
        assertEquals("geronimo", dependencies[0].getGroupId());
        assertEquals("geronimo-kernel", dependencies[0].getArtifactId());
        assertEquals("DEV", dependencies[0].getVersion());

        GbeanType[] gbeans = configuration.getGbeanArray();
        assertEquals(1, gbeans.length);
        assertEquals("geronimo.test:name=MyMockGMBean", gbeans[0].getName());
        AttributeType[] attrs = gbeans[0].getAttributeArray();
        assertEquals(2, attrs.length);
        assertEquals("Value", attrs[0].getName());
        assertEquals("1234", attrs[0].getStringValue());
        assertEquals("IntValue", attrs[1].getName());
        assertEquals("1234", attrs[1].getStringValue());

        ReferenceType[] refs = gbeans[0].getReferenceArray();
        assertEquals(1, refs.length);
        assertEquals("MockEndpoint", refs[0].getName());
        assertEquals("geronimo.test:name=MyMockGMBean", refs[0].getStringValue());
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        plan1 = cl.getResource("services/plan1.xml");
    }
}
