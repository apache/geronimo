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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import junit.framework.TestCase;

/**
 * This is just a unit test for recovery, depending on proper behavior of the log(s) it uses.
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/11 19:20:55 $
 *
 * */
public class RecoveryTest extends TestCase {

    XidFactory xidFactory = new XidFactoryImpl();
    private final String RM1 = "rm1";
    private final String RM2 = "rm2";
    private final String RM3 = "rm3";

    public void test2ResOnlineAfterRecoveryStart() throws Exception {
        MockLog mockLog = new MockLog();
        Xid[] xids = getXidArray(3);
        MockXAResource xares1 = new MockXAResource(RM1, xids);
        MockXAResource xares2 = new MockXAResource(RM2, xids);
        MockTransactionInfo[] txInfos = makeTxInfos(xids);
        addBranch(txInfos, xares1);
        addBranch(txInfos, xares2);
        prepareLog(mockLog, txInfos);
        Recovery recovery = new RecoveryImpl(mockLog, xidFactory);
        recovery.recoverLog();
        assertTrue(!recovery.hasRecoveryErrors());
        assertTrue(recovery.getExternalXids().isEmpty());
        assertTrue(!recovery.localRecoveryComplete());
        recovery.recoverResourceManager(xares1);
        assertTrue(!recovery.localRecoveryComplete());
        assertEquals(3, xares1.committed.size());
        recovery.recoverResourceManager(xares2);
        assertEquals(3, xares2.committed.size());
        assertTrue(recovery.localRecoveryComplete());

    }

    private void addBranch(MockTransactionInfo[] txInfos, MockXAResource xaRes) {
        for (int i = 0; i < txInfos.length; i++) {
            MockTransactionInfo txInfo = txInfos[i];
            txInfo.branches.add(new MockTransactionBranchInfo(xaRes.getName()));
        }
    }

    private MockTransactionInfo[] makeTxInfos(Xid[] xids) {
        MockTransactionInfo[] txInfos = new MockTransactionInfo[xids.length];
        for (int i = 0; i < xids.length; i++) {
            Xid xid = xids[i];
            txInfos[i] = new MockTransactionInfo(xid, new ArrayList());
        }
        return txInfos;
    }

    public void test3ResOnlineAfterRecoveryStart() throws Exception {
        MockLog mockLog = new MockLog();
        Xid[] xids12 = getXidArray(3);
        List xids12List = Arrays.asList(xids12);
        Xid[] xids13 = getXidArray(3);
        List xids13List = Arrays.asList(xids13);
        Xid[] xids23 = getXidArray(3);
        List xids23List = Arrays.asList(xids23);
        ArrayList tmp = new ArrayList();
        tmp.addAll(xids12List);
        tmp.addAll(xids13List);
        Xid[] xids1 = (Xid[]) tmp.toArray(new Xid[6]);
        tmp.clear();
        tmp.addAll(xids12List);
        tmp.addAll(xids23List);
        Xid[] xids2 = (Xid[]) tmp.toArray(new Xid[6]);
        tmp.clear();
        tmp.addAll(xids13List);
        tmp.addAll(xids23List);
        Xid[] xids3 = (Xid[]) tmp.toArray(new Xid[6]);

        MockXAResource xares1 = new MockXAResource(RM1, xids1);
        MockXAResource xares2 = new MockXAResource(RM2, xids2);
        MockXAResource xares3 = new MockXAResource(RM3, xids3);
        MockTransactionInfo[] txInfos12 = makeTxInfos(xids12);
        addBranch(txInfos12, xares1);
        addBranch(txInfos12, xares2);
        prepareLog(mockLog, txInfos12);
        MockTransactionInfo[] txInfos13 = makeTxInfos(xids13);
        addBranch(txInfos13, xares1);
        addBranch(txInfos13, xares3);
        prepareLog(mockLog, txInfos13);
        MockTransactionInfo[] txInfos23 = makeTxInfos(xids23);
        addBranch(txInfos23, xares2);
        addBranch(txInfos23, xares3);
        prepareLog(mockLog, txInfos23);
        Recovery recovery = new RecoveryImpl(mockLog, xidFactory);
        recovery.recoverLog();
        assertTrue(!recovery.hasRecoveryErrors());
        assertTrue(recovery.getExternalXids().isEmpty());
        assertEquals(9, recovery.localUnrecoveredCount());
        recovery.recoverResourceManager(xares1);
        assertEquals(9, recovery.localUnrecoveredCount());
        assertEquals(6, xares1.committed.size());
        recovery.recoverResourceManager(xares2);
        assertEquals(6, recovery.localUnrecoveredCount());
        assertEquals(6, xares2.committed.size());
        recovery.recoverResourceManager(xares3);
        assertEquals(0, recovery.localUnrecoveredCount());
        assertEquals(6, xares3.committed.size());

    }

    private void prepareLog(TransactionLog txLog, MockTransactionInfo[] txInfos) throws LogException {
        for (int i = 0; i < txInfos.length; i++) {
            MockTransactionInfo txInfo = txInfos[i];
            txLog.prepare(txInfo.globalXid, txInfo.branches);
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

    private static class MockTransactionInfo {
        private Xid globalXid;
        private List branches;

        public MockTransactionInfo(Xid globalXid, List branches) {
            this.globalXid = globalXid;
            this.branches = branches;
        }
    }

    private static class MockTransactionBranchInfo implements TransactionBranchInfo {
        private String name;

        public MockTransactionBranchInfo(String name) {
            this.name = name;
        }

        public String getResourceName() {
            return name;
        }

        public Xid getBranchXid() {
            return null;
        }
    }
}
