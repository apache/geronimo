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

package org.apache.geronimo.transaction;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.MockResource;
import org.apache.geronimo.transaction.manager.MockResourceManager;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 18:05:51 $
 *
 * */
public class TransactionManagerProxyTest extends TestCase {

    MockResourceManager rm1 = new MockResourceManager(true);
    MockResource r1_1 = new MockResource(rm1);
    MockResource r1_2 = new MockResource(rm1);
    MockResourceManager rm2 = new MockResourceManager(true);
    MockResource r2_1 = new MockResource(rm2);
    MockResource r2_2 = new MockResource(rm2);

    TransactionManagerProxy tm = new TransactionManagerProxy();

    public void testNoResourcesCommit() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.commit();
        assertNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        Transaction tx = tm.getTransaction();
        assertNotNull(tx);
        tx.commit();
        assertNotNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
    }

    public void testNoResourcesMarkRollbackOnly() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.getTransaction().setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
        try {
            tm.commit();
            fail("tx should roll back");
        } catch (RollbackException e) {
            //expected
        }
        assertNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        Transaction tx = tm.getTransaction();
        assertNotNull(tx);
        tx.setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tx.getStatus());
        try {
            tx.commit();
            fail("tx should roll back");
        } catch (RollbackException e) {
            //expected
        }
        assertNotNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
    }

    public void testNoResoucesRollback() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.rollback();
        assertNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        Transaction tx = tm.getTransaction();
        assertNotNull(tx);
        tx.rollback();
        assertNotNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());

        //check rollback when marked rollback only
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.getTransaction().setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
        tm.rollback();
        assertNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
    }

    public void testOneResourceCommit() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue(r1_1.isCommitted());
        assertTrue(!r1_1.isPrepared());
        assertTrue(!r1_1.isRolledback());
    }

    public void testOneResourceMarkedRollback() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.setRollbackOnly();
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        try {
            tx.commit();
            fail("tx should roll back");
        } catch (RollbackException e) {
            //expected
        }
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue(!r1_1.isCommitted());
        assertTrue(!r1_1.isPrepared());
        assertTrue(r1_1.isRolledback());
    }

    public void testOneResourceRollback() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue(!r1_1.isCommitted());
        assertTrue(!r1_1.isPrepared());
        assertTrue(r1_1.isRolledback());
    }

    public void testTwoResourceOneRMCommit() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.enlistResource(r1_2);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue(r1_1.isCommitted() ^ r1_2.isCommitted());
        assertTrue(!r1_1.isPrepared() & !r1_2.isPrepared());
        assertTrue(!r1_1.isRolledback() & !r1_2.isRolledback());
    }

    public void testTwoResourceOneRMMarkRollback() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.enlistResource(r1_2);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.setRollbackOnly();
        try {
            tx.commit();
            fail("tx should roll back");
        } catch (RollbackException e) {
            //expected
        }
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue(!r1_1.isCommitted() & !r1_2.isCommitted());
        assertTrue(!r1_1.isPrepared() & !r1_2.isPrepared());
        assertTrue(r1_1.isRolledback() ^ r1_2.isRolledback());
    }

    public void testTwoResourcesOneRMRollback() throws Exception {
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.enlistResource(r1_2);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.setRollbackOnly();
        tx.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue(!r1_1.isCommitted() & !r1_2.isCommitted());
        assertTrue(!r1_1.isPrepared() & !r1_2.isPrepared());
        assertTrue(r1_1.isRolledback() ^ r1_2.isRolledback());
    }

    public void testFourResourceTwoRMCommit() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_1);
        tx.enlistResource(r1_2);
        tx.enlistResource(r2_1);
        tx.enlistResource(r2_2);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.delistResource(r2_1, XAResource.TMSUCCESS);
        tx.delistResource(r2_2, XAResource.TMSUCCESS);
        tx.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertTrue((r1_1.isCommitted() & r1_1.isPrepared()) ^ (r1_2.isCommitted() & r1_2.isPrepared()));
        assertTrue(!r1_1.isRolledback() & !r1_2.isRolledback());
        assertTrue((r2_1.isCommitted() & r2_1.isPrepared()) ^ (r2_2.isCommitted() & r2_2.isPrepared()));
        assertTrue(!r2_1.isRolledback() & !r2_2.isRolledback());
    }


}
