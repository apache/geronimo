/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.transaction.manager;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/29 00:32:40 $
 */
public class TestTransactionManager extends TestCase {
    TransactionManager tm;
    MockResourceManager rm1, rm2, rm3;

    public void testNoTransaction() throws Exception {
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());
    }

    public void testNoResource() throws Exception {
        Transaction tx;
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tx = tm.getTransaction();
        assertNotNull(tx);
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());

        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
    }

    public void testTxOp() throws Exception {
        Transaction tx;
        tm.begin();
        tx = tm.getTransaction();
        tx.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tx.getStatus());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());

        tm.begin();
        assertFalse(tx.equals(tm.getTransaction()));
        tm.rollback();
    }

    public void testSuspend() throws Exception {
        Transaction tx;
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tx = tm.getTransaction();
        assertNotNull(tx);
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());

        tx = tm.suspend();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());

        tm.resume(tx);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(tx, tm.getTransaction());

        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNull(tm.getTransaction());
    }

    public void testOneResource() throws Exception {
        Transaction tx;
        MockResource res1 = rm1.getResource();
        tm.begin();
        tx = tm.getTransaction();
        assertNull(res1.getXid());
        assertTrue(tx.enlistResource(res1));
        assertNotNull(res1.getXid());
        assertTrue(tx.delistResource(res1, XAResource.TMFAIL));
        assertNull(res1.getXid());
        tm.rollback();

        tm.begin();
        tx = tm.getTransaction();
        assertTrue(tx.enlistResource(res1));
        tm.rollback();
        assertNull(res1.getXid());
    }

    protected void setUp() throws Exception {
        tm = new TransactionManagerImpl();
        rm1 = new MockResourceManager(true);
        rm2 = new MockResourceManager(true);
        rm3 = new MockResourceManager(false);
    }
}
