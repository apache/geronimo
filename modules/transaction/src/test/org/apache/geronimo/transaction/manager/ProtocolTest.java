package org.apache.geronimo.transaction.manager;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

/**
 */
public class ProtocolTest extends TestCase {

    private TransactionManagerImpl tm;
    private MockResourceManager mrm1, mrm2;
    private MockResource mr11, mr12, mr21, mr22;

    protected void setUp() throws Exception {
        tm = new TransactionManagerImpl(1000, null, null);
        mrm1 = new MockResourceManager(true);
        mrm2 = new MockResourceManager(true);
        mr11 = new MockResource(mrm1, "mr11");
        mr12 = new MockResource(mrm1, "mr12");
        mr21 = new MockResource(mrm2, "mr21");
        mr22 = new MockResource(mrm2, "mr22");
    }

    public void testOnePhaseCommit() throws Exception {
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(mr11);
        tx.delistResource(mr11, XAResource.TMSUSPEND);
        tm.commit();
    }

    public void testOnePhaseCommiTwoResources() throws Exception {
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(mr11);
        tx.delistResource(mr11, XAResource.TMSUSPEND);
        tx.enlistResource(mr12);
        tx.delistResource(mr12, XAResource.TMSUSPEND);
        tm.commit();
    }
    public void testTwoPhaseCommit() throws Exception {
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(mr11);
        tx.delistResource(mr11, XAResource.TMSUSPEND);
        tx.enlistResource(mr21);
        tx.delistResource(mr21, XAResource.TMSUSPEND);
        tm.commit();
    }
    public void testTwoPhaseCommit4Resources() throws Exception {
        tm.begin();
        Transaction tx = tm.getTransaction();
        tx.enlistResource(mr11);
        tx.delistResource(mr11, XAResource.TMSUSPEND);
        tx.enlistResource(mr12);
        tx.delistResource(mr12, XAResource.TMSUSPEND);
        tx.enlistResource(mr21);
        tx.delistResource(mr21, XAResource.TMSUSPEND);
        tx.enlistResource(mr22);
        tx.delistResource(mr22, XAResource.TMSUSPEND);
        tm.commit();
    }

}
