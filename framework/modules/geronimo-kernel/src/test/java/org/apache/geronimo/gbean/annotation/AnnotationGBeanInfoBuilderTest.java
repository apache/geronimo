/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.gbean.annotation;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBeanAnnotationException;



/**
 *
 * @version $Rev:$ $Date:$
 */
public class AnnotationGBeanInfoBuilderTest extends TestCase {

    public void testSmoke() throws Exception {
        AnnotationGBeanInfoBuilder builder = new AnnotationGBeanInfoBuilder(SmokeGBean.class);
        GBeanInfo beanInfo = builder.buildGBeanInfo();
        
        assertEquals(SmokeGBean.class.getSimpleName(), beanInfo.getName());
        assertEquals("GBean", beanInfo.getJ2eeType());
        
        assertEquals(123, beanInfo.getPriority());

        GConstructorInfo constructor = beanInfo.getConstructor();
        List<String> cstrNames = constructor.getAttributeNames();
        assertEquals(4, cstrNames.size());
        assertEquals("classLoader", cstrNames.get(0));
        assertEquals("name", cstrNames.get(1));
        assertEquals("Name", cstrNames.get(2));
        assertEquals("Collection", cstrNames.get(3));
        
        GAttributeInfo nameAttribute = beanInfo.getAttribute("name");
        assertNotNull(nameAttribute);
        assertEquals(String.class.getName(), nameAttribute.getType());

        GReferenceInfo nameReference = beanInfo.getReference("Name");
        assertNotNull(nameReference);
        assertEquals(Runnable.class.getName(), nameReference.getProxyType());
        
        GReferenceInfo collectionReference = beanInfo.getReference("Collection");
        assertNotNull(collectionReference);
        assertEquals(Collection.class.getName(), collectionReference.getProxyType());
        assertEquals(Runnable.class.getName(), collectionReference.getReferenceType());
        
        GAttributeInfo setterAttribute = beanInfo.getAttribute("setterAttribute");
        assertNotNull(setterAttribute);
        assertEquals(String.class.getName(), setterAttribute.getType());
        assertEquals(true, setterAttribute.isPersistent());
        assertEquals(true, setterAttribute.isManageable());

        GAttributeInfo setterNotManageableAttribute = beanInfo.getAttribute("setterNotManageableAttribute");
        assertNotNull(setterNotManageableAttribute);
        assertEquals(String.class.getName(), setterNotManageableAttribute.getType());
        assertEquals(true, setterNotManageableAttribute.isPersistent());
        assertEquals(false, setterNotManageableAttribute.isManageable());
        
        GReferenceInfo setterReference = beanInfo.getReference("SetterReference");
        assertNotNull(setterReference);
        assertEquals(Runnable.class.getName(), setterReference.getProxyType());

        GReferenceInfo setterCollectionReference = beanInfo.getReference("SetterCollectionReference");
        assertNotNull(setterCollectionReference);
        assertEquals(Collection.class.getName(), setterCollectionReference.getProxyType());
        assertEquals(Runnable.class.getName(), setterCollectionReference.getReferenceType());
    }
    
    public void testDefaultConstructorGBean() throws Exception {
        AnnotationGBeanInfoBuilder builder = new AnnotationGBeanInfoBuilder(DefaultConstructorGBean.class);
        GBeanInfo beanInfo = builder.buildGBeanInfo();
        
        assertTrue(beanInfo.getConstructor().getAttributeNames().isEmpty());
    }

    public void testMissingDefaultConstructorThrowsGBAE() throws Exception {
        executeThrowGBAETest(MissingDefaultConstructorGBean.class);
    }
    
    public void testMissingConstructorAnnotationThrowsGBAE() throws Exception {
        executeThrowGBAETest(MissingConstructorAnnotationGBean.class);
    }
    
    public void testPersistentOnNonSetterThrowsGBAE() throws Exception {
        executeThrowGBAETest(PersistentOnNonSetterGBean.class);
    }
    
    public void testReferenceOnNonSetterThrowsGBAE() throws Exception {
        executeThrowGBAETest(ReferenceOnNonSetterGBean.class);
    }
    
    public void testCollectionIsNotGenerifiedThrowsGBAE() throws Exception {
        executeThrowGBAETest(CollectionNotGenerifiedGBean.class);
    }

    private void executeThrowGBAETest(Class gbeanClass) {
        try {
            AnnotationGBeanInfoBuilder builder = new AnnotationGBeanInfoBuilder(gbeanClass);
            builder.buildGBeanInfo();
            fail();
        } catch (GBeanAnnotationException e) {
        }
    }

    @Priority(priority=123)
    public static class SmokeGBean {
        public SmokeGBean(@ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
            @ParamAttribute(name = "name") String name,
            @ParamReference(name = "Name") Runnable runnable,
            @ParamReference(name = "Collection") Collection<Runnable> runnables) {
        }
        
        @Persistent
        public void setSetterAttribute(String value) {
        }
        
        @Persistent(manageable=false)
        public void setSetterNotManageableAttribute(String value) {
        }
        
        @Reference
        public void setSetterReference(Runnable value) {
        }

        @Reference
        public void setSetterCollectionReference(Collection<Runnable> value) {
        }
    }

    public static class DefaultConstructorGBean {
    }

    public static class MissingDefaultConstructorGBean {
        public MissingDefaultConstructorGBean(String name) {
        }
    }
    
    public static class MissingConstructorAnnotationGBean {
        public MissingConstructorAnnotationGBean(@ParamAttribute(name = "name") String name,
            Runnable runnable) {
        }
    }
    
    public static class PersistentOnNonSetterGBean {
        @Persistent
        public void run() {
        }
    }

    public static class ReferenceOnNonSetterGBean {
        @Reference
        public void run() {
        }
    }
 
    public static class CollectionNotGenerifiedGBean {
        public CollectionNotGenerifiedGBean(@ParamReference(name = "Collection") Collection runnables) {
        }
    }
    
}
