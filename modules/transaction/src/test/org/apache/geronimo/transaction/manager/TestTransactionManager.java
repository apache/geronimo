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

package org.apache.geronimo.transaction.manager;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
 */
public class TestTransactionManager extends TestCase {
    TransactionManager tm;
    MockResourceManager rm1, rm2, rm3;

    public void testNoTransaction() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());
    }

    public void testNoResource() throws Exception {
        Transaction tx;
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tx = tm.getTransaction();
        assertNotNull(tx);
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());

        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
    }

    public void testTxOp() throws Exception {
        Transaction tx;
        tm.begin();
        tx = tm.getTransaction();
        tx.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());

        tm.begin();
        assertFalse(tx.equals(tm.getTransaction()));
        tm.rollback();
    }

    public void testSuspend() throws Exception {
        Transaction tx;
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tx = tm.getTransaction();
        assertNotNull(tx);
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());

        tx = tm.suspend();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());

        tm.resume(tx);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(tx, tm.getTransaction());

        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());
    }

    public void testOneResource() throws Exception {
        Transaction tx;
        MockResource res1 = rm1.getResource();
        tm.begin();
        tx = tm.getTransaction();
        assertNull(res1.getXid());
        assertTrue(tx.enlistResource(res1));
        assertNotNull(res1.getXid());
        assertTrue(tx.delistResource(res1, XAResource.TMFAIL));
        assertNull(res1.getXid());
        tm.rollback();

        tm.begin();
        tx = tm.getTransaction();
        assertTrue(tx.enlistResource(res1));
        tm.rollback();
        assertNull(res1.getXid());
    }

    protected void setUp() throws Exception {
        tm = new TransactionManagerImpl();
        rm1 = new MockResourceManager(true);
        rm2 = new MockResourceManager(true);
        rm3 = new MockResourceManager(false);
    }
}
