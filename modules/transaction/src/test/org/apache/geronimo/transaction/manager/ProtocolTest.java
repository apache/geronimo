/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
        tm = new TransactionManagerImpl(1000, 
                new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes()), null, null);
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
