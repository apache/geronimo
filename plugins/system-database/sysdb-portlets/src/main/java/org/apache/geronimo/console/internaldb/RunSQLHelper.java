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

package org.apache.geronimo.console.internaldb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.portlet.PortletRequest;

import org.apache.geronimo.console.BasePortlet;

import org.apache.geronimo.kernel.util.InputUtils;

public class RunSQLHelper {

    private static final Logger log = LoggerFactory.getLogger(RunSQLHelper.class);

    private static final String DERBY_BACKUP_FOLDER = "derby.backup";

    private static final String PARENT_FOLDER = "..";

    private static final String BAK_EXTENSION = ".bak";

    private static final String BAK_PREFIX = "BAK_";
    
    private final BasePortlet portlet;

    public RunSQLHelper (BasePortlet portlet) {
        this.portlet = portlet;
    }
    
    public boolean createDB(String dbName, PortletRequest request) {

        // ensure there are no illegal chars in DB name
        InputUtils.validateSafeInput(dbName);

        Connection conn = null;
        try {
            conn = DerbyConnectionUtil.getDerbyConnection(dbName,
                    DerbyConnectionUtil.CREATE_DB_PROP);
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "sysdb.infoMsg01", dbName));
            return true;
        } catch (Throwable e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg01"), e.getMessage());
            return false;
        } finally {
            // close DB connection
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg02"), e.getMessage());
            }
        }        
    }

    public boolean backupDB(String derbyHome, String dbName, PortletRequest request) {
        return false;
    }

    public boolean restoreDB(String derbyHome, String dbName, PortletRequest request) {
        return false;
    }

    public boolean deleteDB(String derbyHome, String dbName, PortletRequest request) {
        // shutdown database before deleting it
        if (!shutdownDB(dbName)) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg03"));
            return false;
        }

        try {
            // create backup folder if not created
            File derbyBackupFolder = new File(derbyHome + File.separatorChar
                    + PARENT_FOLDER + File.separatorChar + DERBY_BACKUP_FOLDER);
            if (!derbyBackupFolder.exists()) {
                if (!derbyBackupFolder.mkdirs()) {
                    portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg04"));
                    return false;
                }
            }

            File oldDBFolder = new File(derbyHome + File.separatorChar + dbName);
            if (oldDBFolder.exists()) {
                // Need to add a prefix because File.createTempFile's first
                // argument must be a String at least three characters long.
                File tmpFile = File.createTempFile(BAK_PREFIX + dbName,
                        BAK_EXTENSION, derbyBackupFolder);
                File newDBFolder = new File(tmpFile.getAbsolutePath());
                /*
                 * Delete temp file and create a temp folder using the temp
                 * filename
                 */
                if (tmpFile.delete()) {
                    if (newDBFolder.mkdirs()) {
                        if (!oldDBFolder.renameTo(new File(newDBFolder,
                                oldDBFolder.getName()))) {
                            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg05"));
                            return false;
                        }
                    }
                }
            }
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "sysdb.infoMsg02", dbName));
            return true;
        } catch (Exception e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg06"), e.getMessage());
            return false;
        }
    }

    public boolean runSQL(String connName, String sql, Boolean dsConn, PortletRequest request) {
        if ((sql == null) || (sql.trim().length() == 0)) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg07"));
            return false;
        }
        
        Connection conn = null;
        Statement s = null;
        try {
            if (dsConn) {
                conn = DerbyConnectionUtil.getDataSourceConnection(connName);
            } else {
                conn = DerbyConnectionUtil.getDerbyConnection(connName);
            }
            conn.setAutoCommit(false);

            s = conn.createStatement();
            String[] sqlCmds = sql.split(";");
            for (int i = 0; i < sqlCmds.length; i++) {
                if (sqlCmds[i].trim().length() > 0) {
                    // debug printout (remove later)
                    log.debug("SQL" + i + ": <" + sqlCmds[i].trim() + ">");
                    s.execute(sqlCmds[i]);
                }
            }
            conn.commit();
            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "sysdb.infoMsg03"));
            return true;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (Exception unHandledException) {
            }
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg08"), e.getMessage());
            return false;
        } catch (Throwable e) {
            portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg08"), e.getMessage());
            return false;
        } finally {
            // close DB connection
            try {
                if (s != null) {
                    s.close();
                }
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                portlet.addErrorMessage(request, portlet.getLocalizedString(request, "sysdb.errorMsg02"), e.getMessage());
            }
        }
    }

    private boolean shutdownDB(String dbName) {
        boolean ok = true;

        boolean gotSQLExc = false;
        try {
            DerbyConnectionUtil.getDerbyConnection(dbName,
                    DerbyConnectionUtil.SHUTDOWN_DB_PROP);
        } catch (SQLException se) {
            gotSQLExc = true;
        }

        if (!gotSQLExc) {
            ok = false;
        }

        return ok;
    }

}
