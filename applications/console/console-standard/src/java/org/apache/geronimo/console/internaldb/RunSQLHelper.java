/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.internaldb;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class RunSQLHelper {

    public static final String SQL_SUCCESS_MSG = "SQL command/s successful";

    public static final String SQL_EMPTY_MSG = "SQL Command/s can't be empty";

    private static final String DB_CREATED_MSG = "Database created";

    private static final String DB_DELETED_MSG = "Database deleted";

    private static final String DERBY_BACKUP_FOLDER = "derby.backup";

    private static final String PARENT_FOLDER = "..";

    private static final String BAK_EXTENSION = ".bak";

    private static final String BAK_PREFIX = "BAK_";

    public String createDB(String dbName) {
        String result = DB_CREATED_MSG + ": " + dbName;

        Connection conn = null;
        try {
            conn = DerbyConnectionUtil.getDerbyConnection(dbName,
                    DerbyConnectionUtil.CREATE_DB_PROP);
        } catch (Throwable e) {
            if (e instanceof SQLException) {
                result = getSQLError((SQLException) e);
            } else {
                result = e.getMessage();
            }
        } finally {
            // close DB connection
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                result = "Problem closing DB connection";
            }
        }

        return result;
    }

    public String backupDB(String derbyHome, String dbName) {
        return "";
    }

    public String restoreDB(String derbyHome, String dbName) {
        return "";
    }

    public String deleteDB(String derbyHome, String dbName) {
        String result = DB_DELETED_MSG + ": " + dbName;

        // shutdown database before deleting it
        if (!shutdownDB(dbName)) {
            result = "Database not deleted: " + dbName
                    + " Couldn't shutdown db: " + dbName;
            return result;
        }

        try {
            // create backup folder if not created
            File derbyBackupFolder = new File(derbyHome + File.separatorChar
                    + PARENT_FOLDER + File.separatorChar + DERBY_BACKUP_FOLDER);
            if (!derbyBackupFolder.exists()) {
                if (!derbyBackupFolder.mkdirs()) {
                    result = "Database not deleted: " + dbName
                            + " Derby backup folder not created: "
                            + derbyBackupFolder;
                    return result;
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
                            result = "Database not deleted: " + dbName
                                    + " DB folder not renamed";
                            return result;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String runSQL(String dbName, String sql) {
        String result = SQL_SUCCESS_MSG;

        if ((sql == null) || (sql.trim().length() == 0)) {
            result = SQL_EMPTY_MSG;
            return result;
        }

        Connection conn = null;
        Statement s = null;
        try {

            conn = DerbyConnectionUtil.getDerbyConnection(dbName);
            conn.setAutoCommit(false);

            s = conn.createStatement();
            String[] sqlCmds = sql.split(";");
            for (int i = 0; i < sqlCmds.length; i++) {
                if (sqlCmds[i].trim().length() > 0) {
                    // debug printout (remove later)
                    System.out.println("SQL" + i + ": <" + sqlCmds[i].trim()
                            + ">");
                    s.execute(sqlCmds[i]);
                }
            }
            conn.commit();
        } catch (Throwable e) {
            if (e instanceof SQLException) {
                result = getSQLError((SQLException) e);
            } else {
                result = e.getMessage();
            }
        } finally {
            // close DB connection
            try {
                if (s != null) {
                    s.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                if (SQL_SUCCESS_MSG.equals(result)) {
                    result = "Problem closing DB connection: " + e.getMessage();
                }
            }
        }

        return result;
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

    private String getSQLError(SQLException e) {
        StringBuffer errorMsg = new StringBuffer();
        while (e != null) {
            //errorMsg.append(e.toString());
            errorMsg.append(e.getMessage());
            errorMsg.append(" * ");
            e = e.getNextException();
        }

        return errorMsg.toString();
    }

    public static void main(String[] args) {
        new RunSQLHelper().runSQL("derbyDB4",
                "create table derbyTbl1(num int, addr varchar(40));"
                        + "create table derbyTbl2(num int, addr varchar(40));"
                        + "create table derbyTbl3(num int, addr varchar(40));"
                        + "insert into derb");
    }
}
