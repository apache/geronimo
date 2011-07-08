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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

public class InternalDBHelper {
    private static final Logger log = LoggerFactory.getLogger(InternalDBHelper.class);

    private static final int RDBMS_DERBY = 1;

    private static final int RDBMS_MSSQL = 2;

    private static final String JNDI_DERBY = "java:comp/env/SystemDatasource";

    private static final Map derbyDBInfo = new Hashtable();

    /**
     * Returns the database metadata as a map.
     */
    public Map getDBInfo() {
        derbyDBInfo.clear();
        Connection conn = null;
        try {
            conn = DerbyConnectionUtil.getSystemDBConnection();
            DatabaseMetaData dbMD = (DatabaseMetaData) conn.getMetaData();

            // DB
            derbyDBInfo.put("URL", removeNull(dbMD.getURL()));
            derbyDBInfo.put("Username", removeNull(dbMD.getUserName()));
            derbyDBInfo.put("Read Only", removeNull(String.valueOf(dbMD
                    .isReadOnly())));
            derbyDBInfo.put("DB Product Name", removeNull(dbMD
                    .getDatabaseProductName()));
            derbyDBInfo.put("DB Product Version", removeNull(dbMD
                    .getDatabaseProductVersion()));
            derbyDBInfo.put("DB Major Version", removeNull(String.valueOf(dbMD
                    .getDatabaseMajorVersion())));
            derbyDBInfo.put("DB Minor Version", removeNull(String.valueOf(dbMD
                    .getDatabaseMinorVersion())));

            // Driver
            derbyDBInfo.put("Driver Name", removeNull(dbMD.getDriverName()));
            derbyDBInfo.put("Driver Version", removeNull(dbMD
                    .getDriverVersion()));
            derbyDBInfo.put("Driver Major Version", removeNull(String
                    .valueOf(dbMD.getDriverMajorVersion())));
            derbyDBInfo.put("Driver Minor Version", removeNull(String
                    .valueOf(dbMD.getDriverMinorVersion())));

            // JDBC
            derbyDBInfo.put("JDBC Major Version", removeNull(String
                    .valueOf(dbMD.getJDBCMajorVersion())));
            derbyDBInfo.put("JDBC Minor Version", removeNull(String
                    .valueOf(dbMD.getJDBCMinorVersion())));

            // Functions
            derbyDBInfo.put("Numeric Functions", removeNull(dbMD
                    .getNumericFunctions()));
            derbyDBInfo.put("String Functions", removeNull(dbMD
                    .getStringFunctions()));
            derbyDBInfo.put("System Functions", removeNull(dbMD
                    .getSystemFunctions()));
            derbyDBInfo.put("Time Date Functions", removeNull(dbMD
                    .getTimeDateFunctions()));

            // Etc
            derbyDBInfo.put("Supported SQL Keywords", removeNull(dbMD
                    .getSQLKeywords().replace(',', ' ')));
            derbyDBInfo.put("Supported Types", removeNull(getColumnData(dbMD
                    .getTypeInfo(), "TYPE_NAME")));
            derbyDBInfo.put("Table Types", removeNull(getColumnData(dbMD
                    .getTableTypes(), "TABLE_TYPE")));
            derbyDBInfo.put("Schemas", removeNull(getColumnData(dbMD
                    .getSchemas(), "TABLE_SCHEM")));
            String tx = null;

            switch (dbMD.getDefaultTransactionIsolation()) {
            case Connection.TRANSACTION_NONE:
                tx = "not supported";
                break;
            case Connection.TRANSACTION_READ_COMMITTED:
                tx = "dirty reads are prevented; non-repeatable reads and phantom reads can occur";
                break;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                tx = "dirty reads, non-repeatable reads and phantom reads can occur";
                break;
            case Connection.TRANSACTION_REPEATABLE_READ:
                tx = "dirty reads and non-repeatable reads are prevented; phantom reads can occur";
                break;
            case Connection.TRANSACTION_SERIALIZABLE:
                tx = "dirty reads, non-repeatable reads and phantom reads are prevented";
                break;
            default:
                tx = "";
                break;
            }

            derbyDBInfo.put("Default Transaction Isolation", removeNull(tx));
            String holdability = null;

            switch (dbMD.getResultSetHoldability()) {
            case ResultSet.HOLD_CURSORS_OVER_COMMIT:
                holdability = "hold cursors over commit";
                break;
            case ResultSet.CLOSE_CURSORS_AT_COMMIT:
                holdability = "close cursors at commit";
                break;
            default:
                holdability = "";
                break;
            }
            derbyDBInfo.put("Result Set Holdability", removeNull(holdability));
            String sqlStateType = null;

            switch (dbMD.getSQLStateType()) {
            case DatabaseMetaData.sqlStateXOpen:
                sqlStateType = "X/Open SQL CLI";
                break;
            case DatabaseMetaData.sqlStateSQL99:
                sqlStateType = "SQL99";
                break;
            default:
                sqlStateType = "";
                break;
            }
            derbyDBInfo.put("SQL State Type", removeNull(sqlStateType));
        } catch (SQLException e) {
            printSQLError((SQLException) e);
        } finally {
            // close DB connection
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                // problem closing DB connection
            }
        }

        return derbyDBInfo;
    }

    private String removeNull(String s) {
        return ((s == null) ? "" : s);
    }

    /**
     * Get a specific column data as a string separated by ','.
     */
    private String getColumnData(ResultSet rs, String colName) {
        StringBuilder result = new StringBuilder();
        try {
            ResultSetMetaData rsmd = rs.getMetaData();

            // 1) Get column number
            int selectedCol = -1;
            int numberOfColumns = rsmd.getColumnCount();
            for (int i = 1; i <= numberOfColumns; i++) {
                String columnName = rsmd.getColumnName(i);
                if (columnName.equals(colName)) {
                    selectedCol = i;
                    break;
                }
            }

            // 2) Get data
            boolean firstData = true;
            while (rs.next()) {
                for (int i = 1; i <= numberOfColumns; i++) {
                    if (i == selectedCol) {
                        if (firstData) {
                            firstData = false;
                        } else {
                            result.append(',');
                        }
                        String columnValue = rs.getString(i);
                        result.append(columnValue);
                    }
                }
            }
        } catch (SQLException e) {
            printSQLError((SQLException) e);
        }

        return result.toString();
    }

    /**
     * Print the SQL exception including chained exceptions
     * if there is one.
     *
     * @param e
     */
    private void printSQLError(SQLException e) {
        while (e != null) {
            log.error(e.toString(), e);
            e = e.getNextException();
        }
    }

}