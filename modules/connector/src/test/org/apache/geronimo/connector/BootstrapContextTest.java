/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.XAServices;

/**
 * Unit tests for {@link BootstrapContextImpl}
 * @version $Revision: 1.5 $ $Date: 2004/07/11 21:55:34 $
 */
public class BootstrapContextTest extends TestCase {

    /**
     * Tests get and set work manager
     */
    public void testGetSetWorkManager() {
        XAServices xaServices = new MockXATerminator("MockXATerminator");
        GeronimoWorkManager manager = new GeronimoWorkManager(1, xaServices);
        BootstrapContextImpl context = new BootstrapContextImpl(manager);
        WorkManager wm = context.getWorkManager();

        assertSame("Make sure it is the same object", manager, wm);
    }

    /**
     * Tests get and set XATerminator
     */
    public void testGetSetXATerminator() {
        XAServices xaServices = new MockXATerminator("MockXATerminator");
        GeronimoWorkManager manager = new GeronimoWorkManager(1, xaServices);
        BootstrapContextImpl context = new BootstrapContextImpl(manager);
        XATerminator xat = context.getXATerminator();

        assertSame("Make sure it is the same object", xaServices, xat);
    }

    /**
     * Tests getTimer
     */
    public void testGetTimer() throws Exception {
        BootstrapContextImpl context = new BootstrapContextImpl(null);
        Timer t = context.createTimer();
        assertNotNull("Object is not null", t);
    }

}
