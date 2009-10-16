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
package org.apache.geronimo.security.jacc.mappingprovider;

import junit.framework.TestCase;
import org.apache.geronimo.security.jacc.mappingprovider.PolicyConfigurationGeneric;
import org.apache.geronimo.security.jacc.mappingprovider.GeronimoPolicyConfigurationFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoPolicyConfigurationFactoryTest extends TestCase {

    private static final String CONTEXT_ID = "testContextID";
    private static GeronimoPolicyConfigurationFactory policyConfigurationFactory = new GeronimoPolicyConfigurationFactory();


    /**
     * 3.1.1.1 specifies that policyConfigurationFactory.getPolicyConfiguration always returns an open PolicyConfiguration
     * @throws Exception
     */
    public void testPolicyConfigurationOpen() throws Exception {
        testPolicyConfigurationOpen(false);
        testPolicyConfigurationOpen(true);
        testPolicyConfigurationOpen(false);
    }

    private void testPolicyConfigurationOpen(boolean remove) throws Exception {
        PolicyConfigurationGeneric policyConfiguration = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, remove);
        assertEquals(PolicyConfigurationGeneric.OPEN, policyConfiguration.getState());
        policyConfiguration.commit();
        assertEquals(PolicyConfigurationGeneric.IN_SERVICE, policyConfiguration.getState());
        policyConfiguration = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, remove);
        assertEquals(PolicyConfigurationGeneric.OPEN, policyConfiguration.getState());
        policyConfiguration.delete();
        assertEquals(PolicyConfigurationGeneric.DELETED, policyConfiguration.getState());
        policyConfiguration = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, remove);
        assertEquals(PolicyConfigurationGeneric.OPEN, policyConfiguration.getState());
        policyConfiguration = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, remove);
        assertEquals(PolicyConfigurationGeneric.OPEN, policyConfiguration.getState());
    }

    /**
     * spec p. 71:
     * For a given value of policy context identifier, this method must always return the same instance of PolicyConfiguration and there must be at
     *  most one actual instance of a PolicyConfiguration with a given policy context identifier (during a process context).
     *   
     * @throws Exception
     */
    public void testSamePolicyConfigurationInstance() throws Exception {
        PolicyConfigurationGeneric policyConfiguration1 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, false);
        PolicyConfigurationGeneric policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, false);
        assertSame(policyConfiguration1, policyConfiguration2);
        policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, false);
        assertSame(policyConfiguration1, policyConfiguration2);
        policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, true);
        assertSame(policyConfiguration1, policyConfiguration2);
        policyConfiguration2.commit();
        policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, false);
        assertSame(policyConfiguration1, policyConfiguration2);
        policyConfiguration2.commit();
        policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, true);
        assertSame(policyConfiguration1, policyConfiguration2);

        policyConfiguration2.delete();
        policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, false);
        assertSame(policyConfiguration1, policyConfiguration2);
        policyConfiguration2.delete();
        policyConfiguration2 = (PolicyConfigurationGeneric) policyConfigurationFactory.getPolicyConfiguration(CONTEXT_ID, true);
        assertSame(policyConfiguration1, policyConfiguration2);

    }
}
