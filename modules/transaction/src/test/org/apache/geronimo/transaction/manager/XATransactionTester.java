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

import java.sql.Connection;
import java.sql.Statement;
import java.io.IOException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 18:54:16 $
 */
public class XATransactionTester {
    private TransactionLog log;
    private TransactionManager manager;
    private XADataSource ds;
    private Xid xid;

    public static void main(String[] args) throws Exception {
        new XATransactionTester().run(args);
    }

    public void run(String[] args) throws Exception {
        ds = getDataSource(args);
        XAConnection xaConn = ds.getXAConnection("test", "test");
        XAResource xaRes = xaConn.getXAResource();
        log = new DummyLog();
        manager = new TransactionManagerImpl(log);
        Connection c = xaConn.getConnection();
        Statement s = c.createStatement();

        manager.begin();
        manager.getTransaction().enlistResource(xaRes);
        s.execute("UPDATE XA_TEST SET X=X+1");
        manager.getTransaction().delistResource(xaRes, XAResource.TMSUCCESS);
        manager.commit();

/*
        manager.begin();
        manager.getTransaction().enlistResource(xaRes);
        xid = new XidImpl(xid, 1);
        System.out.println("xid = " + xid);
        s.execute("UPDATE XA_TEST SET X=X+1");

        xaRes.end(xid, XAResource.TMSUCCESS);
        xaRes.prepare(xid);
        c.close();
*/

/*
        Xid[] prepared = xaRes.recover(XAResource.TMNOFLAGS);
        for (int i = 0; i < prepared.length; i++) {
            Xid xid = prepared[i];
            StringBuffer s = new StringBuffer();
            s.append(Integer.toHexString(xid.getFormatId())).append('.');
            byte[] globalId = xid.getGlobalTransactionId();
            for (int j = 0; j < globalId.length; j++) {
                s.append(Integer.toHexString(globalId[j]));
            }

            System.out.println("recovery = " + s);
            xaRes.forget(xid);
        }
*/

    }

    /*
     * @todo get something that loads this from a file
     */
    private XADataSource getDataSource(String[] args) throws Exception {
//        oracle.jdbc.xa.client.OracleXADataSource ds = new oracle.jdbc.xa.client.OracleXADataSource();
//        ds.setURL("jdbc:oracle:thin:@localhost:1521:ABU");
//        return ds;
        return null;
    }

    private class DummyLog implements TransactionLog {

        public void begin(Xid xid) throws IOException {
            XATransactionTester.this.xid = xid;
        }

        public void prepare(Xid xid) throws IOException {
        }

        public void commit(Xid xid) throws IOException {
        }

        public void rollback(Xid xid) throws IOException {
        }
    }
}
