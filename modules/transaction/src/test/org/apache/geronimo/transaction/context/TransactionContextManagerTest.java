package org.apache.geronimo.transaction.context;

import javax.transaction.xa.Xid;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.TransactionManagerProxy;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;
import org.apache.geronimo.transaction.manager.XidFactory;

/**
 */
public class TransactionContextManagerTest extends TestCase {

    private TransactionContextManager transactionContextManager;
    private XidFactory xidFactory = new XidFactoryImpl("geronimo.test.tm".getBytes());

    protected void setUp() throws Exception {
        TransactionManagerProxy tm = new GeronimoTransactionManager(null, null);
        transactionContextManager = new TransactionContextManager(tm, tm, tm);
    }

    protected void tearDown() throws Exception {
        transactionContextManager = null;
    }

    public void testImportedTxLifecycle() throws Exception {
        Xid xid = xidFactory.createXid();
        transactionContextManager.begin(xid, 0);
        transactionContextManager.end(xid);
        transactionContextManager.begin(xid, 0);
        transactionContextManager.end(xid);
        transactionContextManager.prepare(xid);
        transactionContextManager.commit(xid, false);
    }
}
