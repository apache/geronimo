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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

/**
 * This is just a unit test for recovery, depending on proper behavior of the log(s) it uses.
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/08 20:16:03 $
 *
 * */
public class RecoveryTest extends TestCase {

    XidFactory xidFactory = new XidFactoryImpl();
    private final String RM1 = "rm1";
    private final String RM2 = "rm2";

    public void test2ResNoProblems() throws Exception {
        MockLog mockLog = new MockLog();
        Xid[] xids = getXidArray(3);
        MockXAResource xares1 = new MockXAResource(RM1, xids);
        MockXAResource xares2 = new MockXAResource(RM2, xids);
        List xaResources = Arrays.asList(new XAResource[] {xares1, xares2});
        prepareLog(mockLog, xids, new String[] {RM1, RM2});
        Recovery recovery = new Recovery(xaResources, mockLog, xidFactory);
        recovery.recover();
        assertTrue(!recovery.hasRecoveryErrors());
        assertTrue(recovery.getExternalXids().isEmpty());
        assertTrue(recovery.localRecoveryComplete());
        assertEquals(3, xares1.committed.size());
        assertEquals(3, xares2.committed.size());
    }

    public void test2ResOnlineAfterRecoveryStart() throws Exception {
        MockLog mockLog = new MockLog();
        Xid[] xids = getXidArray(3);
        MockXAResource xares1 = new MockXAResource(RM1, xids);
        MockXAResource xares2 = new MockXAResource(RM2, xids);
        List xaResources = Collections.EMPTY_LIST;
        prepareLog(mockLog, xids, new String[] {RM1, RM2});
        Recovery recovery = new Recovery(xaResources, mockLog, xidFactory);
        recovery.recover();
        assertTrue(!recovery.hasRecoveryErrors());
        assertTrue(recovery.getExternalXids().isEmpty());
        assertTrue(!recovery.localRecoveryComplete());
        recovery.recoverResourceManager(xares1);
        assertTrue(!recovery.localRecoveryComplete());
        assertEquals(3, xares1.committed.size());
        recovery.recoverResourceManager(xares2);
        assertTrue(recovery.localRecoveryComplete());
        assertEquals(3, xares2.committed.size());

    }

    private void prepareLog(TransactionLog txLog, Xid[] xids, String[] names) throws LogException {
        for (int i = 0; i < xids.length; i++) {
            Xid xid = xids[i];
            txLog.prepare(xid, names);
        }
    }


    private Xid[] getXidArray(int i) {
        Xid[] xids = new Xid[i];
        for (int j = 0; j < xids.length; j++) {
            xids[j] = xidFactory.createXid();
        }
        return xids;
    }

    private static class MockXAResource implements NamedXAResource {

        private final String name;
        private final Xid[] xids;
        private final List committed = new ArrayList();
        private final List rolledBack = new ArrayList();

        public MockXAResource(String name, Xid[] xids) {
            this.name = name;
            this.xids = xids;
        }
        public String getName() {
            return name;
        }

        public void commit(Xid xid, boolean onePhase) throws XAException {
            committed.add(xid);
        }

        public void end(Xid xid, int flags) throws XAException {
        }

        public void forget(Xid xid) throws XAException {
        }

        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        public boolean isSameRM(XAResource xaResource) throws XAException {
            return false;
        }

        public int prepare(Xid xid) throws XAException {
            return 0;
        }

        public Xid[] recover(int flag) throws XAException {
            return xids;
        }

        public void rollback(Xid xid) throws XAException {
            rolledBack.add(xid);
        }

        public boolean setTransactionTimeout(int seconds) throws XAException {
            return false;
        }

        public void start(Xid xid, int flags) throws XAException {
        }

        public List getCommitted() {
            return committed;
        }

        public List getRolledBack() {
            return rolledBack;
        }

    }
}
