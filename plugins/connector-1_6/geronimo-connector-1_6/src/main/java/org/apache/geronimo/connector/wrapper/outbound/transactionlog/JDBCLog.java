/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector.wrapper.outbound.transactionlog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.resource.ResourceException;
import javax.sql.DataSource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.transaction.manager.LogException;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.TransactionBranchInfo;
import org.apache.geronimo.transaction.manager.TransactionBranchInfoImpl;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.XidFactory;

/**
 * "Last Resource optimization" for single servers wishing to have valid xa transactions with
 * a single 1-pc datasource.  The database is used for the log, and the database work is
 * committed when the log writes its prepare record.
 *
 * @version $Rev$ $Date$
 */
public class JDBCLog implements TransactionLog, GBeanLifecycle {
    private final static String INSERT_XID = "INSERT INTO TXLOG (SYSTEMID, FORMATID, GLOBALID, GLOBALBRANCHID, BRANCHBRANCHID, NAME) VALUES (?, ?, ?, ?, ?)";
    private final static String DELETE_XID = "DELETE FROM TXLOG WHERE SYSTEMID = ? AND FORMATID = ? AND GLOBALID = ?  AND GLOBALBRANCHID = ?";
    private final static String RECOVER = "SELECT FORMATID, GLOBALID, GLOBALBRANCHID, BRANCHBRANCHID, NAME FROM TXLOG WHERE SYSTEMID = ? ORDER BY FORMATID, GLOBALID, GLOBALBRANCHID, BRANCHBRANCHID, NAME";

    private DataSource dataSource;
    private final String systemId;
    private final ResourceSource<ResourceException> managedConnectionFactoryWrapper;

    public JDBCLog(String systemId, ResourceSource<ResourceException> managedConnectionFactoryWrapper) {
        this.systemId = systemId;
        this.managedConnectionFactoryWrapper = managedConnectionFactoryWrapper;
    }

    public JDBCLog(String systemId, DataSource dataSource) {
        this.systemId = systemId;
        this.managedConnectionFactoryWrapper = null;
        this.dataSource = dataSource;
    }

    public void doStart() throws Exception {
        dataSource = (DataSource) managedConnectionFactoryWrapper.$getResource();
    }

    public void doStop() throws Exception {
        dataSource = null;
    }

    public void doFail() {
    }

    public void begin(Xid xid) throws LogException {
    }

    public Object prepare(Xid xid, List branches) throws LogException {
        int formatId = xid.getFormatId();
        byte[] globalTransactionId = xid.getGlobalTransactionId();
        byte[] branchQualifier = xid.getBranchQualifier();
        try {
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement ps = connection.prepareStatement(INSERT_XID);
                try {
                    for (Iterator iterator = branches.iterator(); iterator.hasNext();) {
                        TransactionBranchInfo branch = (TransactionBranchInfo) iterator.next();
                        ps.setString(0, systemId);
                        ps.setInt(1, formatId);
                        ps.setBytes(2, globalTransactionId);
                        ps.setBytes(3, branchQualifier);
                        ps.setBytes(4, branch.getBranchXid().getBranchQualifier());
                        ps.setString(5, branch.getResourceName());
                        ps.execute();
                    }
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
        return null;
    }

    public void commit(Xid xid, Object logMark) throws LogException {
        try {
            Connection connection = dataSource.getConnection();
            try {
                PreparedStatement ps = connection.prepareStatement(DELETE_XID);
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

    public void rollback(Xid xid, Object logMark) throws LogException {
        throw new LogException("JDBCLog does not support rollback of prepared transactions.  Use it only on servers that do not import transactions");
    }

    public Collection recover(XidFactory xidFactory) throws LogException {
        try {
            Connection connection = dataSource.getConnection();
            try {
                Collection recovered = new ArrayList();
                PreparedStatement ps = connection.prepareStatement(RECOVER);
                try {
                    ps.setString(0, systemId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        Xid lastXid = null;
                        Xid currentXid = null;
                        Recovery.XidBranchesPair xidBranchesPair = null;
                        while (rs.next()) {
                            int formatId = rs.getInt(0);
                            byte[] globalId = rs.getBytes(1);
                            byte[] globalBranchId = rs.getBytes(2);
                            byte[] branchBranchId = rs.getBytes(3);
                            String name = rs.getString(4);
                            currentXid = xidFactory.recover(formatId, globalId, globalBranchId);
                            Xid branchXid = xidFactory.recover(formatId, globalId, branchBranchId);
                            if (!currentXid.equals(lastXid)) {
                                xidBranchesPair = new Recovery.XidBranchesPair(currentXid, null);
                                recovered.add(xidBranchesPair);
                                lastXid = currentXid;
                            }
                            xidBranchesPair.addBranch(new TransactionBranchInfoImpl(branchXid, name));
                        }
                        return recovered;
                    } finally {
                        rs.close();
                    }
                } finally {
                    ps.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            throw new LogException("Recovery failure", e);
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

}
