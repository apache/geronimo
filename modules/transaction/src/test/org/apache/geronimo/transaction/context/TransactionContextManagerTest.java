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

import junit.framework.TestCase;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.ImportedTransactionActiveException;
import org.apache.geronimo.transaction.TransactionManagerProxy;
import org.apache.geronimo.transaction.manager.XidFactory;
import org.apache.geronimo.transaction.manager.XidFactoryImpl;

/**
 *
 *
 * @version $Rev: 54013 $ $Date: 2004-10-07 13:49:55 -0700 (Thu, 07 Oct 2004) $
 *
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

    public void testNoConcurrentWorkSameXid() throws Exception {
        Xid xid = xidFactory.createXid();
        transactionContextManager.begin(xid, 0);
        try {
            transactionContextManager.begin(xid, 0);
            fail("should not be able begin same xid twice");
        } catch (ImportedTransactionActiveException e) {
            //expected
        }
    }

    public void testOnlyOneImportedTxAtATime() throws Exception {
        Xid xid1 = xidFactory.createXid();
        Xid xid2 = xidFactory.createXid();
        transactionContextManager.begin(xid1, 0);
        try {
            transactionContextManager.begin(xid2, 0);
            fail("should not be able to begin a 2nd tx without ending the first");
        } catch (IllegalStateException e) {
            //expected
        }
    }
}
