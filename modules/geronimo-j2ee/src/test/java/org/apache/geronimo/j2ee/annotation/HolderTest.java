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
package org.apache.geronimo.j2ee.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class HolderTest extends TestCase {

    public void testCopy() throws Exception {
        Holder source = new Holder();
        source.addInjection("a1", new Injection("a1", "1", "2"));
        source.addInjection("a1", new Injection("a1", "3", "4"));
        
        source.addInjection("b1", new Injection("b1", "5", "6"));

        HashMap<String, LifecycleMethod> postConstruct = new HashMap<String, LifecycleMethod>();
        postConstruct.put("class1", new LifecycleMethod("class1", "method1"));
        postConstruct.put("class2", new LifecycleMethod("class2", "method2"));
        
        source.addPostConstructs(postConstruct);
        
        HashMap<String, LifecycleMethod> preDestroy = new HashMap<String, LifecycleMethod>();
        preDestroy.put("class3", new LifecycleMethod("class3", "method1"));
        preDestroy.put("class4", new LifecycleMethod("class4", "method2"));
        
        source.addPreDestroys(preDestroy);
        
        Holder copy = new Holder(source);
        
        compareInjection(source.getInjectionMap(), copy.getInjectionMap());  
        compareLifecycleMethod(source.getPostConstruct(), copy.getPostConstruct());
        compareLifecycleMethod(source.getPreDestroy(), copy.getPreDestroy());
    }
    
    private void compareInjection(Map<String, List<Injection>> expected, Map<String, List<Injection>> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(expected != actual);
        assertEquals(expected.size(), actual.size());
        
        for (Map.Entry<String, List<Injection>> entry : expected.entrySet()) {
            String className = entry.getKey();
            List<Injection> expectedInjections = entry.getValue();
            
            List<Injection> actualInjections = actual.get(className);
            compare(expectedInjections, actualInjections);
        }                            
    }
    
    private void compare(List<Injection> expected, List<Injection> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(expected != actual);
        assertEquals(expected.size(), actual.size());
        
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }
    
    private void compareLifecycleMethod(Map<String, LifecycleMethod> expected, Map<String, LifecycleMethod> actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(expected != actual);
        assertEquals(expected.size(), actual.size());
        
        for (Map.Entry<String, LifecycleMethod> entry : expected.entrySet()) {
            String className = entry.getKey();
            LifecycleMethod expectedMethod = entry.getValue();
            
            LifecycleMethod actualMethod = actual.get(className);
            assertEquals(expectedMethod, actualMethod);
        }
    }
    
}
