/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;

public class PlansTest extends AbstractTestCase {
    private ObjectName configBuilderName;
    private Kernel kernel;

    /**
     * @param testName
     */
    public PlansTest(String testName) {
        super(testName);
    }

    public void testSetUpSystemWithPlans() throws Exception {
        //TODO This test should bring up the Axis module using a plan rather than setting 
        //up them with the code. When we find out how to do it we should remove this test case 
        //do all the test cases with plans
        
        //kernel.getConfigurationManager().load(new File("modules/axis/test-resources/plans/plan1.xml").toURI());
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
}
