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
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
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
