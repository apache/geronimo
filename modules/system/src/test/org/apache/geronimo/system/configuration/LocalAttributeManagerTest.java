/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.configuration;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;

import javax.management.ObjectName;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @version $Rev$ $Date$
 */
public class LocalAttributeManagerTest extends TestCase {
    private static final String basedir = System.getProperties().getProperty("basedir", ".");

    private LocalAttributeManager localAttributeManager;
    private URI configurationName;
    private ObjectName gbeanName;
    private GAttributeInfo attributeInfo;
    private GReferenceInfo referenceInfo;

    public void testConfigurationShouldLoad() throws Exception {
        // should load by default
        Set originalDatas = new HashSet();
        GBeanData gbeanData = new GBeanData(gbeanName, GBEAN_INFO);
        originalDatas.add(gbeanData);

        Set newDatas;
        newDatas = new HashSet(localAttributeManager.setAttributes(configurationName, originalDatas, getClass().getClassLoader()));
        assertEquals(1, newDatas.size());
        assertEquals(originalDatas, newDatas);

        // declare an attribute value so this configuration will exist in the store
        String attributeValue = "attribute value";
        localAttributeManager.addConfiguration(configurationName.toString());
        localAttributeManager.setValue(configurationName.toString(), gbeanName, attributeInfo, attributeValue);

        // should still load
        newDatas = new HashSet(localAttributeManager.setAttributes(configurationName, originalDatas, getClass().getClassLoader()));
        assertEquals(1, newDatas.size());
        assertEquals(originalDatas, newDatas);

    }

    public void testGBeanShouldLoad() throws Exception {
        ObjectName gbeanName2 = ObjectName.getInstance(":name=gbean2");

        // should load by default
        Set originalDatas = new HashSet();
        GBeanData gbeanData = new GBeanData(gbeanName, GBEAN_INFO);
        GBeanData gbeanData2 = new GBeanData(gbeanName2, GBEAN_INFO);
        originalDatas.add(gbeanData);
        originalDatas.add(gbeanData2);

        Set newDatas;
        newDatas = new HashSet(localAttributeManager.setAttributes(configurationName, originalDatas, getClass().getClassLoader()));
        assertEquals(2, newDatas.size());
        assertEquals(originalDatas, newDatas);

        // declare an attribute value so this configuration will exist in the store
        String attributeValue = "attribute value";
        localAttributeManager.addConfiguration(configurationName.toString());
        localAttributeManager.setValue(configurationName.toString(), gbeanName, attributeInfo, attributeValue);

        // should still load
        newDatas = new HashSet(localAttributeManager.setAttributes(configurationName, originalDatas, getClass().getClassLoader()));
        assertEquals(2, newDatas.size());
        assertEquals(originalDatas, newDatas);

        // set the gbean to not load
        localAttributeManager.setShouldLoad(configurationName.toString(), gbeanName, false);

        // should not load
        newDatas = new HashSet(localAttributeManager.setAttributes(configurationName, originalDatas, getClass().getClassLoader()));
        assertEquals(1, newDatas.size());
        GBeanData newGBeanData = (GBeanData) newDatas.iterator().next();
        assertSame(gbeanData2, newGBeanData);
        assertEquals(attributeValue, gbeanData.getAttribute(attributeInfo.getName()));
    }

    public void testSetAtrribute() throws Exception {
        String attributeValue = "attribute value";
        localAttributeManager.setValue(configurationName.toString(), gbeanName, attributeInfo, attributeValue);
        Collection gbeanDatas = new ArrayList();
        GBeanData gbeanData = new GBeanData(gbeanName, GBEAN_INFO);
        gbeanDatas.add(gbeanData);
        gbeanDatas = localAttributeManager.setAttributes(configurationName, gbeanDatas, getClass().getClassLoader());
        assertEquals(attributeValue, gbeanData.getAttribute(attributeInfo.getName()));
    }

    public void testSetReference() throws Exception {
        ObjectName referencePattern = new ObjectName(":name=referencePattern,*");
        localAttributeManager.setReferencePattern(configurationName.toString(), gbeanName, referenceInfo, referencePattern);
        Collection gbeanDatas = new ArrayList();
        GBeanData gbeanData = new GBeanData(gbeanName, GBEAN_INFO);
        gbeanDatas.add(gbeanData);
        gbeanDatas = localAttributeManager.setAttributes(configurationName, gbeanDatas, getClass().getClassLoader());
        assertEquals(Collections.singleton(referencePattern), gbeanData.getReferencePatterns(referenceInfo.getName()));
    }

    public void testSetReferences() throws Exception {
        ObjectName referencePattern1 = new ObjectName(":name=referencePattern1,*");
        ObjectName referencePattern2 = new ObjectName(":name=referencePattern2,*");
        Set referencePatterns = new LinkedHashSet(Arrays.asList(new ObjectName[] {referencePattern1, referencePattern2}));
        localAttributeManager.setReferencePatterns(configurationName.toString(), gbeanName, referenceInfo, referencePatterns);
        Collection gbeanDatas = new ArrayList();
        GBeanData gbeanData = new GBeanData(gbeanName, GBEAN_INFO);
        gbeanDatas.add(gbeanData);
        gbeanDatas = localAttributeManager.setAttributes(configurationName, gbeanDatas, getClass().getClassLoader());
        assertEquals(referencePatterns, gbeanData.getReferencePatterns(referenceInfo.getName()));
    }

    public void testAddGBean() throws Exception {
        String attributeValue = "attribute value";
        ObjectName referencePattern = new ObjectName(":name=referencePattern,*");

        GBeanData gbeanData = new GBeanData(gbeanName, GBEAN_INFO);
        gbeanData.setAttribute(attributeInfo.getName(), attributeValue);
        gbeanData.setReferencePattern(referenceInfo.getName(), referencePattern);
        localAttributeManager.addConfiguration(configurationName.toString());
        localAttributeManager.addGBean(configurationName.toString(), gbeanData);


        Collection gbeanDatas = new ArrayList();
        gbeanDatas = localAttributeManager.setAttributes(configurationName, gbeanDatas, getClass().getClassLoader());
        assertEquals(1, gbeanDatas.size());
        GBeanData newGBeanData = (GBeanData) gbeanDatas.iterator().next();

        assertNotSame(gbeanData, newGBeanData);
        assertSame(gbeanData.getGBeanInfo(), newGBeanData.getGBeanInfo());
        assertSame(gbeanData.getName(), newGBeanData.getName());
        assertEquals(Collections.singleton(referencePattern), newGBeanData.getReferencePatterns(referenceInfo.getName()));
        assertEquals(attributeValue, newGBeanData.getAttribute(attributeInfo.getName()));
    }

    protected void setUp() throws Exception {
        super.setUp();
        localAttributeManager = new LocalAttributeManager("target/test-config.xml", false, new BasicServerInfo(basedir));
        configurationName = URI.create("configuration/name");
        gbeanName = ObjectName.getInstance(":name=gbean");
        attributeInfo = GBEAN_INFO.getAttribute("attribute");
        referenceInfo = GBEAN_INFO.getReference("reference");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        localAttributeManager = null;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LocalAttributeManagerTest.class);
        infoFactory.addReference("reference", String.class);
        infoFactory.addAttribute("attribute", String.class, true);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public String getAttribute() {
        throw new UnsupportedOperationException("Fake method for gbean info");
    }

    public void setAttribute(String attribute) {
        throw new UnsupportedOperationException("Fake method for gbean info");
    }

    public void setReference(String reference) {
        throw new UnsupportedOperationException("Fake method for gbean info");
    }
}
