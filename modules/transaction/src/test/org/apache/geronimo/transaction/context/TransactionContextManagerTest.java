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

package org.apache.geronimo.transaction.context;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.ImportedTransactionActiveException;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 */
public class TransactionContextManagerTest extends TestCase {

    private TransactionContextManager transactionContextManager;
    private XidFactory xidFactory = new XidFactoryImpl("geronimo.test.tm".getBytes());

    protected void setUp() throws Exception {
        TransactionManagerImpl tm = new TransactionManagerImpl(1000, 
                new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes()), null, null);
        transactionContextManager = new TransactionContextManager(tm, tm);
    }

    protected void tearDown() throws Exception {
        transactionContextManager = null;
    }

    public void testImportedTxLifecycle() throws Exception {
        Xid xid = xidFactory.createXid();
        transactionContextManager.begin(xid, 1000);
        transactionContextManager.end(xid);
        transactionContextManager.begin(xid, 1000);
        transactionContextManager.end(xid);
        int readOnly = transactionContextManager.prepare(xid);
        assertEquals(XAResource.XA_RDONLY, readOnly);
//        transactionContextManager.commit(xid, false);
    }

    public void testNoConcurrentWorkSameXid() throws Exception {
        Xid xid = xidFactory.createXid();
        transactionContextManager.begin(xid, 1000);
        try {
            transactionContextManager.begin(xid, 1000);
            fail("should not be able begin same xid twice");
        } catch (ImportedTransactionActiveException e) {
            //expected
        } finally {
            transactionContextManager.end(xid);
            transactionContextManager.rollback(xid);
        }
    }

    public void testOnlyOneImportedTxAtATime() throws Exception {
        Xid xid1 = xidFactory.createXid();
        Xid xid2 = xidFactory.createXid();
        transactionContextManager.begin(xid1, 1000);
        try {
            transactionContextManager.begin(xid2, 1000);
            fail("should not be able to begin a 2nd tx without ending the first");
        } catch (IllegalStateException e) {
            //expected
        } finally {
            transactionContextManager.end(xid1);
            transactionContextManager.rollback(xid1);
        }
    }
}
