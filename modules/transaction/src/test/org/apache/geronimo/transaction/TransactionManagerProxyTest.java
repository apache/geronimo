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

import java.util.ArrayList;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.transaction.manager.MockLog;
import org.apache.geronimo.transaction.manager.MockResource;
import org.apache.geronimo.transaction.manager.MockResourceManager;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.RecoveryImpl;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class TransactionManagerProxyTest extends TestCase {

    MockResourceManager rm1 = new MockResourceManager(true);
    MockResource r1_1 = rm1.getResource("rm1_1");
    MockResource r1_2 = rm1.getResource("rm1_2");
    MockResourceManager rm2 = new MockResourceManager(true);
    MockResource r2_1 = rm2.getResource("rm2_1");
    MockResource r2_2 = rm2.getResource("rm2_2");

    TransactionLog transactionLog = new MockLog();

    XidFactory xidFactory = new XidFactoryImpl("tm1".getBytes());
    TransactionManagerImpl transactionManager = new TransactionManagerImpl(transactionLog, xidFactory);
    Recovery recovery = new RecoveryImpl(transactionLog, xidFactory);
    ReferenceCollection resourceManagers = new TestReferenceCollection();
    TransactionManagerProxy tm = new TransactionManagerProxy(transactionManager, transactionManager, recovery, resourceManagers);

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

    public void testNoResourcesRollback() throws Exception {
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

    //BE VERY CAREFUL!! the ResourceManager only "recovers" the LAST resource it creates.
    //This test depends on using the resource that will be recovered by the resource manager.
    public void testSimpleRecovery() throws Exception {
        Xid xid = xidFactory.createXid();
        tm.begin(xid, 1000);
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_2);
        tx.enlistResource(r2_2);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.delistResource(r2_2, XAResource.TMSUCCESS);
        tm.prepare(xid);
        //recover
        resourceManagers.add(rm1);
        tm.doStart();
        assertTrue(r1_2.isCommitted());
        assertTrue(!r2_2.isCommitted());
        resourceManagers.add(rm2);
        assertTrue(r2_2.isCommitted());
        assertTrue(recovery.localRecoveryComplete());
    }

    public void testImportedXidRecovery() throws Exception {
        XidFactory xidFactory2 = new XidFactoryImpl("tm2".getBytes());
        Xid xid = xidFactory2.createXid();
        tm.begin(xid, 1000);
        Transaction tx = tm.getTransaction();
        tx.enlistResource(r1_2);
        tx.enlistResource(r2_2);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.delistResource(r2_2, XAResource.TMSUCCESS);
        tm.prepare(xid);
        //recover
        resourceManagers.add(rm1);
        tm.doStart();
        assertTrue(!r1_2.isCommitted());
        assertTrue(!r2_2.isCommitted());
        resourceManagers.add(rm2);
        assertTrue(!r2_2.isCommitted());
        //there are no transactions started here, so local recovery is complete
        assertTrue(recovery.localRecoveryComplete());
        Xid[] recovered = tm.recover(XAResource.TMSTARTRSCAN);
        assertEquals(1, recovered.length);
        assertEquals(xid, recovered[0]);
    }

    public void testResourceManagerContract() throws Exception {
        resourceManagers.add(rm1);
        tm.doStart();
        assertTrue(rm1.areAllResourcesReturned());
    }


    private static class TestReferenceCollection extends ArrayList implements ReferenceCollection {

        ReferenceCollectionListener referenceCollectionListener;

        public void addReferenceCollectionListener(ReferenceCollectionListener listener) {
            this.referenceCollectionListener = listener;
        }

        public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
            this.referenceCollectionListener = null;
        }

        public boolean add(Object o) {
            boolean result = super.add(o);
            if (referenceCollectionListener != null) {
                referenceCollectionListener.memberAdded(new ReferenceCollectionEvent(null, o));
            }
            return result;
        }

        public boolean remove(Object o) {
            boolean result = super.remove(o);
            if (referenceCollectionListener != null) {
                referenceCollectionListener.memberRemoved(new ReferenceCollectionEvent(null, o));
            }
            return result;
        }

    }


}
