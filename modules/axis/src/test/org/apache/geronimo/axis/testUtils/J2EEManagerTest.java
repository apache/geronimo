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
package org.apache.geronimo.axis.testUtils;

import javax.management.ObjectName;

import org.apache.geronimo.axis.AbstractTestCase;
import org.apache.geronimo.kernel.Kernel;

/**
 * <p>This test case represents the code generation with the EWS module.
 * This test case needed the $JAVA_HOME/lib.tools.jar at the classapth.</p>
 */
public class J2EEManagerTest extends AbstractTestCase {
    private ObjectName name;
    private Kernel kernel;

    public J2EEManagerTest(String testName) {
        super(testName);
    }

    public void testEcho() throws Exception {
        J2EEManager j2eem = new J2EEManager();
        j2eem.startJ2EEContainer(kernel);
        j2eem.stopJ2EEContainer(kernel);
    }

    protected void setUp() throws Exception {
        name = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
