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
package org.apache.geronimo.j2ee.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

public class NamingBuilderCollectionTest extends TestCase {
    
    public void testNamingBuilderSorting() throws Exception {
                
        List<NamingBuilder> callList = new ArrayList<NamingBuilder>();        
        TestNamingBuilder p1 = new TestNamingBuilder(callList, 5);
        TestNamingBuilder p2 = new TestNamingBuilder(callList, 10);
        TestNamingBuilder p3 = new TestNamingBuilder(callList, 15);
        
        // create un-sorted list and pass it to NamingBuilderCollection
        List<NamingBuilder> builders = new ArrayList<NamingBuilder>();
        builders.add(p3);
        builders.add(p1);
        builders.add(p2);
                
        QName plan = new QName("http://foo", "bar");
        NamingBuilderCollection col = new NamingBuilderCollection(builders);
        List expectedCallList;
        
        // test buildEnvironment
        col.buildEnvironment(null, null, null);
        
        assertEquals(builders.size(), callList.size() / 2);        
        expectedCallList = Arrays.asList("buildEnvironment", p1, "buildEnvironment", p2, "buildEnvironment", p3);
        assertEquals(expectedCallList, callList);
        
        callList.clear();
        
        // test buildNaming
        col.buildNaming(null, null, null, null);
        
        assertEquals(builders.size(), callList.size() / 2);        
        expectedCallList = Arrays.asList("buildNaming", p1, "buildNaming", p2, "buildNaming", p3);
        assertEquals(expectedCallList, callList);
        
        callList.clear();
        
        // test initContext
        col.initContext(null, null, null);
        
        assertEquals(builders.size(), callList.size() / 2);        
        expectedCallList = Arrays.asList("initContext", p1, "initContext", p2, "initContext", p3);
        assertEquals(expectedCallList, callList);
    }
    
    private static class TestNamingBuilder implements NamingBuilder {

        private int priority;
        private List callList;
        
        public TestNamingBuilder(List callList, int priority) {
            this.priority = priority;
            this.callList = callList;
        }
        
        public void buildEnvironment(JndiConsumer specDD, XmlObject plan, Environment environment)
                throws DeploymentException {
            this.callList.add("buildEnvironment");
            this.callList.add(this);
        }

        public void buildNaming(JndiConsumer specDD,
                                XmlObject plan,
                                Module module,
                                Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
            this.callList.add("buildNaming");
            this.callList.add(this);
        }

        public void initContext(JndiConsumer specDD, XmlObject plan, Module module)
                throws DeploymentException {
            this.callList.add("initContext");
            this.callList.add(this);
        }

        public int getPriority() {
            return this.priority;
        }
        
        public QNameSet getPlanQNameSet() {
            return QNameSet.EMPTY;
        }

        public QNameSet getSpecQNameSet() {
            return QNameSet.EMPTY;
        }

        public QName getBaseQName() {
            return null;
        }
        
    }
    
}
