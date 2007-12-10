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
public class ListModulesCommandArgsTest extends TestCase {

    public void testArgs() throws Exception {
        ListModulesCommandArgs args = new ListModulesCommandArgsImpl(new String[] {"--all", "arg1", "arg2"});
        String[] theArgs = args.getArgs();
        assertEquals(2 , theArgs.length);
        assertEquals("arg1" , theArgs[0]);
        assertEquals("arg2" , theArgs[1]);
    }

    public void testAll() throws Exception {
        ListModulesCommandArgs args = new ListModulesCommandArgsImpl(new String[] {"--all"});
        assertTrue(args.isAll());

        args = new ListModulesCommandArgsImpl(new String[] {"-a"});
        assertTrue(args.isAll());
    }

    public void testStarted() throws Exception {
        ListModulesCommandArgs args = new ListModulesCommandArgsImpl(new String[] {"--started"});
        assertTrue(args.isStarted());
        
        args = new ListModulesCommandArgsImpl(new String[] {"-s"});
        assertTrue(args.isStarted());
    }
    
    public void testStopped() throws Exception {
        ListModulesCommandArgs args = new ListModulesCommandArgsImpl(new String[] {"--stopped"});
        assertTrue(args.isStopped());
        
        args = new ListModulesCommandArgsImpl(new String[] {"-t"});
        assertTrue(args.isStopped());
    }
    
    public void testStatePermutationsFail() throws Exception {
        try {
            new ListModulesCommandArgsImpl(new String[] {"-a", "-s"});
            fail();
        } catch (CLParserException e) {
        }
        
        try {
            new ListModulesCommandArgsImpl(new String[] {"-a", "-t"});
            fail();
        } catch (CLParserException e) {
        }
        
        try {
            new ListModulesCommandArgsImpl(new String[] {"-s", "-t"});
            fail();
        } catch (CLParserException e) {
        }
    }
    
}
