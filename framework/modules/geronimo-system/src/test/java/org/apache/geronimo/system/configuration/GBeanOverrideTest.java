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
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.ReferencePatterns;
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
    
    public void testReferences() throws Exception {
        GBeanOverride override;
        
        override = new GBeanOverride(gbeanType, new JexlExpressionParser());        
        override.setClearReference("foo");
        override.setReferencePatterns("foo", new ReferencePatterns(new AbstractName(new URI("/foo/bar/car?foo=bar"))));
        override.writeXml();
        
        assertFalse(override.isClearReference("foo"));
        assertFalse(override.getClearReferences().contains("foo"));        
        assertTrue(override.getReferencePatterns("foo") != null);
        assertTrue(override.getReferences().containsKey("foo"));
        
        override = new GBeanOverride(gbeanType, new JexlExpressionParser());   
        override.setReferencePatterns("foo", new ReferencePatterns(new AbstractName(new URI("/foo/bar/car?foo=bar"))));        
        override.setClearReference("foo");
        override.writeXml();
        
        assertTrue(override.isClearReference("foo"));
        assertTrue(override.getClearReferences().contains("foo"));        
        assertFalse(override.getReferencePatterns("foo") != null);
        assertFalse(override.getReferences().containsKey("foo"));        
    }
    
    public void testAttributes() throws Exception {
        GBeanOverride override;
        
        override = new GBeanOverride(gbeanType, new JexlExpressionParser()); 
        override.setNullAttribute("foo");
        override.setClearAttribute("foo");
        override.setAttribute("foo", "bar");
        override.writeXml();
        
        assertFalse(override.isNullAttribute("foo"));
        assertFalse(override.getNullAttributes().contains("foo"));    
        assertFalse(override.isClearAttribute("foo"));
        assertFalse(override.getClearAttributes().contains("foo"));  
        assertTrue(override.getAttribute("foo") != null);
        assertTrue(override.getAttributes().containsKey("foo"));
            
        override = new GBeanOverride(gbeanType, new JexlExpressionParser()); 
        override.setAttribute("foo", "bar");
        override.setNullAttribute("foo");
        override.setClearAttribute("foo");
        override.writeXml();
        
        assertFalse(override.isNullAttribute("foo"));
        assertFalse(override.getNullAttributes().contains("foo"));    
        assertFalse(override.getAttribute("foo") != null);
        assertFalse(override.getAttributes().containsKey("foo"));        
        assertTrue(override.isClearAttribute("foo"));
        assertTrue(override.getClearAttributes().contains("foo"));  
        
        override = new GBeanOverride(gbeanType, new JexlExpressionParser()); 
        override.setClearAttribute("foo");
        override.setAttribute("foo", "bar");
        override.setNullAttribute("foo");
        override.writeXml();
            
        assertFalse(override.getAttribute("foo") != null);
        assertFalse(override.getAttributes().containsKey("foo"));        
        assertFalse(override.isClearAttribute("foo"));
        assertFalse(override.getClearAttributes().contains("foo"));         
        assertTrue(override.isNullAttribute("foo"));
        assertTrue(override.getNullAttributes().contains("foo"));
        
        override = new GBeanOverride(gbeanType, new JexlExpressionParser()); 
        override.setAttribute("bar1", "foo");
        override.setAttribute("bar2", "foo");
        override.getAttributes().put("foo", null);
        GbeanType gbean = override.writeXml();
        assertEquals(3, gbean.getAttributeOrReference().size());
        AttributeType attribute = (AttributeType)gbean.getAttributeOrReference().get(2);
        assertEquals("foo", attribute.getName());
        assertTrue(attribute.isNull());
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
