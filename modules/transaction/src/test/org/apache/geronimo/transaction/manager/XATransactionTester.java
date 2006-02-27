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

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 *
 *
 *
 * @version $Rev$ $Date$
 */
public class XATransactionTester {
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
        manager = new TransactionManagerImpl(10,
                new XidFactoryImpl("WHAT DO WE CALL IT?".getBytes()), 
                new DummyLog(), null);
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
//        ds.setConnectionURL("jdbc:oracle:thin:@localhost:1521:ABU");
//        return ds;
        return null;
    }

    private class DummyLog implements TransactionLog {

        public void begin(Xid xid) throws LogException {
            XATransactionTester.this.xid = xid;
        }

        public Object prepare(Xid xid, List branches) throws LogException {
            return new Object();
        }

        public void commit(Xid xid, Object logMark) throws LogException {
        }

        public void rollback(Xid xid, Object logMark) throws LogException {
        }

        public Collection recover(XidFactory xidFactory) throws LogException {
            return new ArrayList();
        }

        public String getXMLStats() {
            return null;
        }

        public int getAverageForceTime() {
            return 0;
        }

        public int getAverageBytesPerForce() {
            return 0;
        }
    }
}
