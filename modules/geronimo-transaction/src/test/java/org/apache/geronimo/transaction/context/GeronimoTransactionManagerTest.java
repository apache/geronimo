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

package org.apache.geronimo.transaction.context;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.util.concurrent.CountDownLatch;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.ImportedTransactionActiveException;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 */
public class GeronimoTransactionManagerTest extends TestCase {

    private GeronimoTransactionManager geronimoTransactionManager;
    private XidFactory xidFactory = new XidFactoryImpl("geronimo.test.tm".getBytes());

    protected void setUp() throws Exception {
        super.setUp();
        geronimoTransactionManager = new GeronimoTransactionManager();
    }

    protected void tearDown() throws Exception {
        geronimoTransactionManager = null;
        super.tearDown();
    }

    public void testImportedTxLifecycle() throws Exception {
        Xid xid = xidFactory.createXid();
        geronimoTransactionManager.begin(xid, 1000);
        geronimoTransactionManager.end(xid);
        geronimoTransactionManager.begin(xid, 1000);
        geronimoTransactionManager.end(xid);
        int readOnly = geronimoTransactionManager.prepare(xid);
        assertEquals(XAResource.XA_RDONLY, readOnly);
//        geronimoTransactionManager.commit(xid, false);
    }

    public void testNoConcurrentWorkSameXid() throws Exception {
        final Xid xid = xidFactory.createXid();

        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch cleanupSignal = new CountDownLatch(1);
        final CountDownLatch endSignal = new CountDownLatch(1);

        new Thread() {
            public void run() {
                try {
                    try {
                        try {
                            geronimoTransactionManager.begin(xid, 1000);
                        } finally {
                            startSignal.countDown();
                        }
                        cleanupSignal.await();
                        geronimoTransactionManager.end(xid);
                        geronimoTransactionManager.rollback(xid);
                    } finally {
                        endSignal.countDown();
                    }
                } catch (Exception e) {
                    throw (AssertionFailedError) new AssertionFailedError().initCause(e);
                }
            }
        }.start();

        // wait for thread to begin the tx
        startSignal.await();
        try {
            geronimoTransactionManager.begin(xid, 1000);
            fail("should not be able begin same xid twice");
        } catch (ImportedTransactionActiveException e) {
            //expected
        } finally {
            // tell thread to start cleanup (e.g., end and rollback the tx)
            cleanupSignal.countDown();

            // wait for our thread to finish cleanup
            endSignal.await();
        }
    }

    public void testOnlyOneImportedTxAtATime() throws Exception {
        Xid xid1 = xidFactory.createXid();
        Xid xid2 = xidFactory.createXid();
        geronimoTransactionManager.begin(xid1, 1000);
        try {
            geronimoTransactionManager.begin(xid2, 1000);
            fail("should not be able to begin a 2nd tx without ending the first");
        } catch (IllegalStateException e) {
            //expected
        } finally {
            geronimoTransactionManager.end(xid1);
            geronimoTransactionManager.rollback(xid1);
        }
    }
}
