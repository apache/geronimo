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

package org.apache.geronimo.system.configuration;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.plugin.model.AttributeType;
import org.apache.geronimo.system.plugin.model.GbeanType;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class GBeanOverrideTest extends TestCase {

    private GbeanType gbeanType;
    private String attributeName;

    @Override
    protected void setUp() throws Exception {
        gbeanType = new GbeanType();
        gbeanType.setName("name");
        
        attributeName = "attName";
    }
    
    public void testPropertyEditorIsCarriedByWriteXml() throws Exception {
        AttributeType attributeType = new AttributeType();
        gbeanType.getAttributeOrReference().add(attributeType);
        attributeType.setName(attributeName);
        attributeType.getContent().add("value");
        attributeType.setPropertyEditor("myPropertyEditor");
        
        GBeanOverride override = new GBeanOverride(gbeanType, new JexlExpressionParser());
        GbeanType copiedGBeanType = override.writeXml();
        assertEquals(1, copiedGBeanType.getAttributeOrReference().size());
        AttributeType copiedAttributeType = (AttributeType) copiedGBeanType.getAttributeOrReference().get(0);
        assertEquals(attributeType.getPropertyEditor(), copiedAttributeType.getPropertyEditor());
    }
    
    public void testPropertyEditorIsUsedToGetTextValue() throws Exception {
        GBeanOverride override = new GBeanOverride(gbeanType, new JexlExpressionParser());
        override.setAttribute(attributeName, new Bean(), Bean.class.getName(), getClass().getClassLoader());
        
        assertEquals("bean", override.getAttribute(attributeName));
        
        GbeanType copiedGBeanType = override.writeXml();
        assertEquals(1, copiedGBeanType.getAttributeOrReference().size());
        AttributeType attributeType = (AttributeType) copiedGBeanType.getAttributeOrReference().get(0);
        assertEquals("bean", attributeType.getContent().get(0));
    }
    
    public void testPropertyEditorIsDefinedWhenAttributeIsNotAPrimitiveAndItsTypeDoesNotEqualValueType() throws Exception {
        GBeanOverride override = new GBeanOverride(gbeanType, new JexlExpressionParser());
        override.setAttribute(attributeName, new Bean(), Service.class.getName(), getClass().getClassLoader());
        
        GbeanType copiedGBeanType = override.writeXml();
        assertEquals(1, copiedGBeanType.getAttributeOrReference().size());
        AttributeType attributeType = (AttributeType) copiedGBeanType.getAttributeOrReference().get(0);
        assertEquals(BeanEditor.class.getName(), attributeType.getPropertyEditor());
    }
    
    public void testPropertyEditorIsNotDefinedWhenAttributeTypeEqualsValueType() throws Exception {
        GBeanOverride override = new GBeanOverride(gbeanType, new JexlExpressionParser());
        override.setAttribute(attributeName, new Bean(), Bean.class.getName(), getClass().getClassLoader());
        
        GbeanType copiedGBeanType = override.writeXml();
        assertEquals(1, copiedGBeanType.getAttributeOrReference().size());
        AttributeType attributeType = (AttributeType) copiedGBeanType.getAttributeOrReference().get(0);
        assertNull(attributeType.getPropertyEditor());
    }
    
    public void testPropertyEditorIsNotDefinedForPrimitives() throws Exception {
        GBeanOverride override = new GBeanOverride(gbeanType, new JexlExpressionParser());
        override.setAttribute(attributeName, new Integer(1), int.class.getName(), getClass().getClassLoader());
        
        GbeanType copiedGBeanType = override.writeXml();
        assertEquals(1, copiedGBeanType.getAttributeOrReference().size());
        AttributeType attributeType = (AttributeType) copiedGBeanType.getAttributeOrReference().get(0);
        assertNull(attributeType.getPropertyEditor());
    }
    
    public void testPropertyEditorIsNotDefinedForCollectionSubClasses() throws Exception {
        GBeanOverride override = new GBeanOverride(gbeanType, new JexlExpressionParser());
        override.setAttribute(attributeName, Collections.singleton("test"), Collection.class.getName(), getClass().getClassLoader());
        
        GbeanType copiedGBeanType = override.writeXml();
        assertEquals(1, copiedGBeanType.getAttributeOrReference().size());
        AttributeType attributeType = (AttributeType) copiedGBeanType.getAttributeOrReference().get(0);
        assertNull(attributeType.getPropertyEditor());
    }
    
    public interface Service {
    }
    
    public static class Bean implements Service {

    }
    
    public static class BeanEditor extends PropertyEditorSupport {
        
        @Override
        public String getAsText() {
            return "bean";
        }
        
        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            assertEquals("bean", text);
        }
        
    }
    
}
