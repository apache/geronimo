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
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;

/**
 * Unit tests for {@link GeronimoBootstrapContext}
 * @version $Rev$ $Date$
 */
public class BootstrapContextTest extends TestCase {
    ThreadPool pool;
    protected void setUp() throws Exception {
        super.setUp();
        pool = new ThreadPool(1, "Connector Test", 30000, ThreadPool.class.getClassLoader(), "foo:test=bar");
    }

    /**
     * Tests get and set work manager
     */
    public void testGetSetWorkManager() throws Exception {
        GeronimoTransactionManager transactionManager = new GeronimoTransactionManager();
        GeronimoWorkManager manager = new GeronimoWorkManager(pool, pool, pool, transactionManager);
        GeronimoBootstrapContext context = new GeronimoBootstrapContext(manager, transactionManager);
        WorkManager wm = context.getWorkManager();

        assertSame("Make sure it is the same object", manager, wm);
    }

    /**
     * Tests get and set XATerminator
     */
    public void testGetSetXATerminator() throws Exception {
        GeronimoTransactionManager transactionManager = new GeronimoTransactionManager();
        GeronimoWorkManager manager = new GeronimoWorkManager(pool, pool, pool, transactionManager);
        GeronimoBootstrapContext context = new GeronimoBootstrapContext(manager, transactionManager);
        XATerminator xat = context.getXATerminator();

        assertSame("Make sure it is the same object", transactionManager, xat);
    }

    /**
     * Tests getTimer
     */
    public void testGetTimer() throws Exception {
        GeronimoBootstrapContext context = new GeronimoBootstrapContext(null, null);
        Timer t = context.createTimer();
        assertNotNull("Object is not null", t);
    }

}
