package org.apache.geronimo.transaction.manager;

import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.log.UnrecoverableLog;

/**
 */
public class ProtocolTest extends TestCase {

    XidFactory xidFactory = new XidFactoryImpl("test".getBytes());
    TransactionManagerImpl tm;
    MockResourceManager mrm1, mrm2;
    MockResource mr11, mr12, mr21, mr22;

    protected void setUp() throws Exception {
        tm = new TransactionManagerImpl(10, new UnrecoverableLog(), xidFactory);
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
