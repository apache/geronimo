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
package org.apache.geronimo.gbean;

import java.io.Serializable;

import junit.framework.TestCase;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-16 23:35:17 -0500 (Thu, 16 Nov 2006) $
 */
public class DynamicGBeanDelegateTest extends TestCase {
    
    public void testSimple() throws Exception {
        DynamicGBeanDelegate delegate = new DynamicGBeanDelegate();
        Bean bean = new Bean();
        delegate.addAll(bean);
        
        Integer value;
        
        delegate.setAttribute("foo", 1);
        value = (Integer)delegate.getAttribute("foo");
        assertEquals(1, value.intValue());
        
        delegate.setAttribute("foo", new Integer(2));
        value = (Integer)delegate.getAttribute("foo");
        assertEquals(2, value.intValue());
        
        delegate.setAttribute("foo", "3");
        value = (Integer)delegate.getAttribute("foo");
        assertEquals(3, value.intValue());
    }
   
    public void testPolymorphism() throws Exception {
        DynamicGBeanDelegate delegate = new DynamicGBeanDelegate();
        Bean bean = new Bean();
        delegate.addAll(bean);
        
        Bean expected1 = new Bean();
        delegate.setAttribute("bean", expected1);
        Bean actual1 = (Bean)delegate.getAttribute("bean");
        assertEquals(expected1, actual1);
        
        String expected2 = new String();
        delegate.setAttribute("bean", expected2);
        String actual2 = (String)delegate.getAttribute("bean");
        assertEquals(expected2, actual2);
    }
    
    public void testBoxing() throws Exception {
        DynamicGBeanDelegate delegate = new DynamicGBeanDelegate();
        Bean bean = new Bean();
        delegate.addAll(bean);
        
        Integer value;
        
        delegate.setAttribute("bar", 1);
        value = (Integer)delegate.getAttribute("bar");
        assertEquals(1, value.intValue());
        
        delegate.setAttribute("bar", new Integer(2));
        value = (Integer)delegate.getAttribute("bar");
        assertEquals(2, value.intValue());
        
        delegate.setAttribute("bar", "3");
        value = (Integer)delegate.getAttribute("bar");
        assertEquals(3, value.intValue());
    }
    
    private static class Bean implements Serializable {
        private int foo;
        private int bar;
        private Serializable bean;
        
        public void setFoo(String s) {
            setFoo(Integer.parseInt(s));
        }
        
        public void setFoo(Integer i) {
            setFoo(i.intValue());
        }
        
        public void setFoo(int i) {
            this.foo = i;
        }
        
        public int getFoo() {
            return this.foo;
        }
                
        public void setBean(Bean b) {
            this.bean = b;
        }
        
        public void setBean(Serializable s) {
            this.bean = s;
        }
        
        public Serializable getBean() {
            return this.bean;
        }
        
        public void setBar(String b) {
            setBar(Integer.parseInt(b));
        }
        
        public void setBar(int b) {
            this.bar = b;
        }
        
        public int getBar() {
            return this.bar;
        }        
    }
}

