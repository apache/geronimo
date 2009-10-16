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
package org.apache.geronimo.cli.deployer;

import org.apache.geronimo.cli.CLParserException;

import junit.framework.TestCase;

/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class DistributeCommandArgsTest extends TestCase {

    public void testInPlace() throws Exception {
        DistributeCommandArgs args = new DistributeCommandArgsImpl(new String[] {"--inPlace", "plan.xml"});
        assertTrue(args.isInPlace());

        args = new DistributeCommandArgsImpl(new String[] {"-i", "plan.xml"});
        assertTrue(args.isInPlace());
    }

    public void testTargets() throws Exception {
        DistributeCommandArgs args = new DistributeCommandArgsImpl(new String[] {"--targets", "t1;t2", "plan.xml"});
        String[] targets = args.getTargets();
        assertEquals(2, targets.length);
        assertEquals("t1", targets[0]);
        assertEquals("t2", targets[1]);
        
        args = new DistributeCommandArgsImpl(new String[] {"-t", "t1;t2", "plan.xml"});
        targets = args.getTargets();
        assertEquals(2, targets.length);
        assertEquals("t1", targets[0]);
        assertEquals("t2", targets[1]);
    }

    public void testNoPlanOrModuleFails() throws Exception {
        try {
            new DistributeCommandArgsImpl(new String[] {"--targets", "t1;t2"});
            fail();
        } catch (CLParserException e) {
        }
    }

    public void testTooManyArgsFails() throws Exception {
        try {
            new DistributeCommandArgsImpl(new String[] {"plan.xml", "module.jar", "extra"});
            fail();
        } catch (CLParserException e) {
        }
    }
    
}
