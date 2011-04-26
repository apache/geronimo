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

package org.apache.geronimo.kernel;

import java.io.File;

import junit.framework.TestCase;
import org.apache.geronimo.kernel.basic.BasicKernel;
import org.apache.geronimo.kernel.osgi.MockBundleContext;

/**
 * @version $Rev$ $Date$
 */
public class BootstrapTest extends TestCase {
    private MockBundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), null, null, null);
    private File kernelState;

    public void testCreate() throws Exception {
        Kernel kernel = new BasicKernel();
    }

    protected void setUp() throws Exception {
        super.setUp();
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        kernelState = new File(tmpDir, "kernel.ser");
    }

    protected void tearDown() throws Exception {
        kernelState.delete();
        super.tearDown();
    }
}
