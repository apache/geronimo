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

package org.apache.geronimo.timer.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;

import javax.sql.DataSource;

import com.thoughtworks.xstream.XStream;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.timer.PersistenceException;
import org.apache.geronimo.timer.Playback;
import org.apache.geronimo.timer.WorkInfo;
import org.apache.geronimo.timer.WorkerPersistence;

/**
 * TODO use an insert returning or stored procedure to insert.
 *
 * @version $Rev$ $Date$
 *
 * */
public class JDBCWorkerPersistence implements WorkerPersistence, GBeanLifecycle {

    private static final String createSequenceSQL = "create sequence timertasks_seq";
    private static final String createTableSQL = "create table timertasks (id long primary key, serverid varchar(256) not null, timerkey varchar(256) not null, userid varchar(4096), userinfo varchar(4096), firsttime long not null, period long, atfixedrate boolean not null)";
    private static final String sequenceSQL = "select timertasks_seq.nextval";
    private static final String insertSQL = "insert into timertasks (id, serverid, timerkey, userid, userinfo, firsttime, period, atfixedrate) values (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String deleteSQL = "delete from timertasks where id=?";
    private static final String selectSQL = "select id, userid, userinfo, firsttime, period, atfixedrate from timertasks where serverid = ? and timerkey=?";
    private static final String fixedRateUpdateSQL = "update timertasks set firsttime = firsttime + period where id = ?";
    private static final String intervalUpdateSQL = "update timertasks set firsttime = ? where id = ?";
    private static final String selectByKeySQL = "select id from timertasks where serverid = ? and timerkey = ? and (userid = ? or ? is null)";

    private final String serverUniqueId;
    private final ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper;
    private DataSource dataSource;

    public JDBCWorkerPersistence(Kernel kernel, ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper) {
        assert managedConnectionFactoryWrapper != null;
        //TODO construct a unique name.
        this.serverUniqueId = kernel.getKernelName();
        this.managedConnectionFactoryWrapper = managedConnectionFactoryWrapper;
    }

    protected JDBCWorkerPersistence(String serverUniqueId, DataSource datasource) {
        this.serverUniqueId = serverUniqueId;
        this.managedConnectionFactoryWrapper = null;
        this.dataSource = datasource;
    }

    public ManagedConnectionFactoryWrapper getManagedConnectionFactoryWrapper() {
        return managedConnectionFactoryWrapper;
    }


    public void save(WorkInfo workInfo) throws PersistenceException {
        try {
            Connection c = dataSource.getConnection();
            try {
                long id;
                PreparedStatement seqStatement = c.prepareStatement(sequenceSQL);
                try {
                    ResultSet seqRS = seqStatement.executeQuery();
                    try {
                        seqRS.next();
                        id = seqRS.getLong(1);
                    } finally {
                        seqRS.close();
                    }
                } finally {
                    seqStatement.close();
                }
                workInfo.setId(id);
                PreparedStatement insertStatement = c.prepareStatement(insertSQL);
                try {
                    String serializedUserId = serialize(workInfo.getUserId());
                     String serializedUserKey = serialize(workInfo.getUserInfo());
                    insertStatement.setLong(1, id);
                    insertStatement.setString(2, serverUniqueId);
                    insertStatement.setString(3, workInfo.getKey());
                    insertStatement.setString(4, serializedUserId);
                    insertStatement.setString(5, serializedUserKey);
                      insertStatement.setLong(6, workInfo.getTime().getTime());
                    if (workInfo.getPeriod() == null) {
                        insertStatement.setNull(7, Types.NUMERIC);
                    } else {
                        insertStatement.setLong(7, workInfo.getPeriod().longValue());
                    }
                    insertStatement.setBoolean(8, workInfo.getAtFixedRate());
                    int result = insertStatement.executeUpdate();
                    if (result != 1) {
                        throw new PersistenceException("Could not insert!");
                    }
                } finally {
                    insertStatement.close();
                }
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void cancel(long id) throws PersistenceException {
        try {
            Connection c = dataSource.getConnection();
            try {
                PreparedStatement deleteStatement = c.prepareStatement(deleteSQL);
                try {
                    deleteStatement.setLong(1, id);
                    deleteStatement.execute();
                } finally {
                    deleteStatement.close();
                }
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }

    }

    public void playback(String key, Playback playback) throws PersistenceException {
        try {
            Connection c = dataSource.getConnection();
            try {
                PreparedStatement selectStatement = c.prepareStatement(selectSQL);
                selectStatement.setString(1, serverUniqueId);
                selectStatement.setString(2, key);
                try {
                    ResultSet taskRS = selectStatement.executeQuery();
                    try {
                        while (taskRS.next()) {
                            long id = taskRS.getLong(1);
                            String serizalizedUserId = taskRS.getString(2);
                            Object userId = deserialize(serizalizedUserId);
                            String serializedUserInfo = taskRS.getString(3);
                            Object userInfo = deserialize(serializedUserInfo);
                            long timeMillis = taskRS.getLong(4);
                            Date time = new Date(timeMillis);
                            Long period = null;
                            period = new Long(taskRS.getLong(5));
                            if (!taskRS.wasNull()) {
                                period = null;
                            }
                            boolean atFixedRate = taskRS.getBoolean(6);
                            //TODO make sure the reference to this is ok, meaning we can't use a handle to this WorkerPersistence.
                            WorkInfo workInfo = new WorkInfo(key, userId, userInfo, time, period, atFixedRate);
                            workInfo.setId(id);
                            playback.schedule(workInfo);
                        }
                    } finally {
                        taskRS.close();
                    }
                } finally {
                    selectStatement.close();
                }
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void fixedRateWorkPerformed(long id) throws PersistenceException {
        try {
            Connection c = dataSource.getConnection();
            try {
                PreparedStatement updateStatement = c.prepareStatement(fixedRateUpdateSQL);
                try {
                    updateStatement.setLong(1, id);
                    updateStatement.execute();
                } finally {
                    updateStatement.close();
                }
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public void intervalWorkPerformed(long id, long period) throws PersistenceException {
        long next = System.currentTimeMillis() + period;
        try {
            Connection c = dataSource.getConnection();
            try {
                PreparedStatement updateStatement = c.prepareStatement(intervalUpdateSQL);
                try {
                    updateStatement.setLong(1, next);
                    updateStatement.setLong(2, id);
                    updateStatement.execute();
                } finally {
                    updateStatement.close();
                }
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    public Collection getIdsByKey(String key, Object userId) throws PersistenceException {
        Collection ids = new ArrayList();
        try {
            Connection c = dataSource.getConnection();
            try {
                PreparedStatement selectStatement = c.prepareStatement(selectByKeySQL);
                selectStatement.setString(1, serverUniqueId);
                selectStatement.setString(2, key);
                if (userId == null) {
                    selectStatement.setNull(3, Types.VARCHAR);
                    selectStatement.setNull(4, Types.VARCHAR);
                } else {
                    String userIdString = serialize(userId);
                    selectStatement.setString(3, userIdString);
                    selectStatement.setString(4, userIdString);
                }
                try {
                    ResultSet taskRS = selectStatement.executeQuery();
                    try {
                        while (taskRS.next()) {
                            long id = taskRS.getLong(1);
                            ids.add(new Long(id));
                        }
                    } finally {
                        taskRS.close();
                    }
                } finally {
                    selectStatement.close();
                }
            } finally {
                c.close();
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
        return ids;
    }

    private String serialize(Object task) {
        XStream xStream = new XStream();
        return xStream.toXML(task);
    }

    private Object deserialize(String serializedRunnable) {
        XStream xStream = new XStream();
        return xStream.fromXML(serializedRunnable);
    }

    public void doStart() throws WaitingException, Exception {
        if (managedConnectionFactoryWrapper != null) {
            dataSource = (DataSource) managedConnectionFactoryWrapper.$getResource();
        }
        if (createSequenceSQL != null && !createSequenceSQL.equals("")) {
            execSQL(createSequenceSQL);
        }
        if (createTableSQL != null && !createTableSQL.equals("")) {
            execSQL(createTableSQL);
        }
    }

    public void doStop() throws WaitingException, Exception {
        dataSource = null;
    }

    public void doFail() {
        dataSource = null;
    }

    private void execSQL(String sql) throws SQLException {
        Connection c = dataSource.getConnection();
        try {
            PreparedStatement updateStatement = c.prepareStatement(sql);
            try {
                updateStatement.execute();
            } catch (SQLException e) {
                //ignore... table already exists.
            } finally {
                updateStatement.close();
            }
        } finally {
            c.close();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(JDBCWorkerPersistence.class);
//        infoFactory.addAttribute("sequenceSQL", String.class, true);
//        infoFactory.addAttribute("insertSQL", String.class, true);
//        infoFactory.addAttribute("deleteSQL", String.class, true);
//        infoFactory.addAttribute("fixedRateUpdateSQL", String.class, true);
//        infoFactory.addAttribute("selectSQL", String.class, true);

        infoFactory.addAttribute("createSequenceSQL", String.class, true);
        infoFactory.addAttribute("createTableSQL", String.class, true);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addReference("managedConnectionFactoryWrapper", ManagedConnectionFactoryWrapper.class);

//        infoFactory.setConstructor(new String[]{"kernel", "managedConnectionFactoryWrapper", "sequenceSQL", "insertSQL", "deleteSQL", "fixedRateUpdateSQL", "selectSQL"});
        infoFactory.setConstructor(new String[]{"kernel", "managedConnectionFactoryWrapper"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
