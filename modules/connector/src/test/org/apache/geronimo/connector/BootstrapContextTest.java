/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector;

import java.util.Timer;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import junit.framework.TestCase;

/**
 * Unit tests for {@link BootstrapContext}
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:12 $
 */
public class BootstrapContextTest extends TestCase {

    /**
     * Creates a new instance of BootstrapContextTest
     * @param name the name of the test
     */
    public BootstrapContextTest(String name) {
        super(name);
    }

    /**
     * Tests get and set work manager
     */
    public void testGetSetWorkManager() {
        BootstrapContext context = new BootstrapContext();
        MockWorkManager manager = new MockWorkManager("testGetSetWorkManager");
        context.setWorkManager(manager);
        WorkManager wm = context.getWorkManager();

        assertTrue("Make sure it is the same object", manager.equals(wm));
    }

    /**
     * Tests get and set XATerminator
     */
    public void testGetSetXATerminator() {
        BootstrapContext context = new BootstrapContext();
        MockXATerminator t = new MockXATerminator("testGetSetXATerminator");
        context.setXATerminator(t);
        XATerminator xat = context.getXATerminator();

        assertTrue("Make sure it is the same object", t.equals(xat));
    }

    /**
     * Tests getTimer
     */
    public void testGetTimer() throws Exception {
        BootstrapContext context = new BootstrapContext();
        Timer t = context.createTimer();
        assertNotNull("Object is not null", t);
    }

}
