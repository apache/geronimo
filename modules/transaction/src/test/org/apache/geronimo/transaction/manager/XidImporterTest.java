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

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
import javax.transaction.Transaction;
import javax.transaction.Status;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class XidImporterTest extends TestCase{

    MockResourceManager rm1 = new MockResourceManager(true);
    MockResource r1_1 = new MockResource(rm1, "rm1");
    MockResource r1_2 = new MockResource(rm1, "rm1");
    MockResourceManager rm2 = new MockResourceManager(true);
    MockResource r2_1 = new MockResource(rm2, "rm2");
    MockResource r2_2 = new MockResource(rm2, "rm2");

    XidImporter tm = new TransactionManagerImpl();
    XidFactory xidFactory = new XidFactoryImpl();

    public void testImportXid() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        assertNotNull(tx);
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());
    }

    public void testNoResourcesCommitOnePhase() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        tm.commit(tx, true);
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
    }

    public void testNoResourcesCommitTwoPhase() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        assertEquals(XAResource.XA_RDONLY, tm.prepare(tx));
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
    }

    public void testNoResourcesMarkRollback() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        tx.setRollbackOnly();
        try {
            tm.prepare(tx);
            fail("should throw rollback exception in an XAException");
        } catch (XAException e) {
        }
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
    }

    public void testNoResourcesRollback() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        tm.rollback(tx);
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
    }

    public void testOneResourceOnePhaseCommit() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tm.commit(tx, true);
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
    }

    public void testOneResourceTwoPhaseCommit() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        tx.enlistResource(r1_1);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        assertEquals(XAResource.XA_OK, tm.prepare(tx));
        assertTrue(!r1_1.isCommitted());
        assertTrue(r1_1.isPrepared());
        assertTrue(!r1_1.isRolledback());
        tm.commit(tx, false);
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
        assertTrue(r1_1.isCommitted());
        assertTrue(r1_1.isPrepared());
        assertTrue(!r1_1.isRolledback());
    }

    public void testFourResourceTwoPhaseCommit() throws Exception {
        Xid externalXid = xidFactory.createXid();
        Transaction tx = tm.importXid(externalXid);
        tx.enlistResource(r1_1);
        tx.enlistResource(r1_2);
        tx.enlistResource(r2_1);
        tx.enlistResource(r2_2);
        tx.delistResource(r1_1, XAResource.TMSUCCESS);
        tx.delistResource(r1_2, XAResource.TMSUCCESS);
        tx.delistResource(r2_1, XAResource.TMSUCCESS);
        tx.delistResource(r2_2, XAResource.TMSUCCESS);
        assertEquals(XAResource.XA_OK, tm.prepare(tx));
        assertTrue(!r1_1.isCommitted() & !r1_2.isCommitted());
        assertTrue(r1_1.isPrepared() ^ r1_2.isPrepared());
        assertTrue(!r1_1.isRolledback() & !r1_2.isRolledback());
        assertTrue(!r2_1.isCommitted() & !r2_2.isCommitted());
        assertTrue(r2_1.isPrepared() ^ r2_2.isPrepared());
        assertTrue(!r2_1.isRolledback() & !r2_2.isRolledback());
        tm.commit(tx, false);
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
        assertTrue((r1_1.isCommitted() & r1_1.isPrepared()) ^ (r1_2.isCommitted() & r1_2.isPrepared()));
        assertTrue(!r1_1.isRolledback() & !r1_2.isRolledback());
        assertTrue((r2_1.isCommitted() & r2_1.isPrepared()) ^ (r2_2.isCommitted() & r2_2.isPrepared()));
        assertTrue(!r2_1.isRolledback() & !r2_2.isRolledback());
    }

}
