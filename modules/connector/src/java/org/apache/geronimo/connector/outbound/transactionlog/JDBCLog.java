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

package org.apache.geronimo.connector.outbound.transactionlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.transaction.manager.LogException;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidImpl;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/06 03:58:23 $
 *
 * */
public class JDBCLog implements TransactionLog, GBean {
    private final static String INSERT_XID = "INSERT INTO TXLOG (SYSTEMID, FORMATID, GLOBALID, BRANCHID) VALUES (?, ?, ?, ?)";
    private final static String DELETE_XID = "DELETE FROM TXLOG WHERE SYSTEMID = ? AND FORMATID = ? AND GLOBALID = ? BRANCHID = ?";
    private final static String RECOVER = "SELECT FORMATID, GLOBALID, BRANCHID FROM TXLOG WHERE SYSTEMID = ?";

    private DataSource dataSource;
    private final String systemId;
    private final ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper;

    public JDBCLog(String systemId, ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper) {
        this.systemId = systemId;
        this.managedConnectionFactoryWrapper = managedConnectionFactoryWrapper;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        dataSource = (DataSource)managedConnectionFactoryWrapper.getProxy();
    }

    public void doStop() throws WaitingException, Exception {
        dataSource = null;
    }

    public void doFail() {
    }
    public void begin(Xid xid) throws LogException {
    }

    public void prepare(Xid xid) throws LogException {
        xidOperation(xid, INSERT_XID);
    }

    private void xidOperation(Xid xid, String sql) throws LogException {
        try {
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement ps = connection.prepareStatement(sql);
                try {
                    ps.setString(0, systemId);
                    ps.setInt(1, xid.getFormatId());
                    ps.setBytes(2, xid.getGlobalTransactionId());
                    ps.setBytes(3, xid.getBranchQualifier());
                    ps.execute();
                } finally {
                    ps.close();
                }
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new LogException("Failure during prepare or commit", e);
        }
    }

    public void commit(Xid xid) throws LogException {
        xidOperation(xid, DELETE_XID);
    }

    public void rollback(Xid xid) throws LogException {
            throw new LogException("JDBCLog does not support rollback of prepared transactions.  Use it only on servers that do not import transactions");
    }

    public List recover() throws LogException {
        try {
            Connection connection = dataSource.getConnection();
            try {
                List xids = new ArrayList();
                PreparedStatement ps = connection.prepareStatement(RECOVER);
                ps.setString(0, systemId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int formatId = rs.getInt(0);
                    byte[] globalId = rs.getBytes(1);
                    byte[] branchId = rs.getBytes(2);
                    Xid xid = new XidImpl(formatId, globalId, branchId);
                    xids.add(xid);
                }
                return xids;
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new LogException("Recover failure", e);
        }

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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(JDBCLog.class.getName());

        infoFactory.addAttribute("SystemId", true);

        infoFactory.addInterface(TransactionLog.class);

        infoFactory.addReference("ManagedConnectionFactoryWrapper", ManagedConnectionFactoryWrapper.class);

        infoFactory.setConstructor(
                new String[]{"SystemId", "DataSource"},
                new Class[]{String.class, DataSource.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
