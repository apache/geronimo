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
package org.apache.geronimo.monitoring.console.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBManager
{
    private Connection con         = null;
    private static boolean    initialized = false;
    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);
    
    public DBManager()
    {
        con = createConnection();
        if (!initialized)
            if (this.initializeDB())
                initialized = true;
    }

    public static Connection createConnection() {
        try {
            Context context = new InitialContext();
            DataSource ds = (DataSource) context.lookup("java:comp/env/MonitoringClientDS");
            return ds.getConnection();
        } catch (NamingException e) {
            logger.error("Fail to get connection from MonitoringClientDS", e);
            return null;
        } catch (SQLException e) {
            logger.error("Fail to get connection from MonitoringClientDS", e);
            return null;
        }
    }

    public Connection getConnection()
    {
        return con;
    }

    public boolean isInitialized()
    {
        return initialized;
    }

    private boolean checkTables(String name)
    {
        try
        {
            DatabaseMetaData metadata = con.getMetaData();
            String[] names = { "TABLE" };
            ResultSet tableNames = metadata.getTables(null, null, name, names);

            if (tableNames.next())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (SQLException e)
        {
            System.err.println("SQL state: " + e.getSQLState());
            System.err.println("SQL error: " + e.getErrorCode());
            e.printStackTrace();
        }
        return false;
    }

    private boolean initializeDB()
    {
        boolean success = false;
//        if (checkTables())
//            return true;
        try
        {
            if (!checkTables("SERVERS")) {
                PreparedStatement pStmt = con
                    .prepareStatement("CREATE TABLE servers("
                                + "server_id   INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),"
                                + "enabled     SMALLINT DEFAULT 1 NOT NULL,"
                                + "name        VARCHAR(128) DEFAULT NULL,"
                                + "ip          VARCHAR(128) NOT NULL,"
                                + "port        INTEGER NOT NULL,"
                                + "protocol    INTEGER DEFAULT 1 NOT NULL,"
                                + "username    VARCHAR(128) NOT NULL,"
                                + "password    VARCHAR(1024) NOT NULL,"
                                + "added       TIMESTAMP NOT NULL,"
                                + "modified    TIMESTAMP NOT NULL,"
                                + "last_seen   TIMESTAMP NOT NULL,"
                                + "CONSTRAINT  UNQ_IP_PORT UNIQUE(ip,port)"
                                + ")");
                pStmt.executeUpdate();
            }
//            pStmt = con
//                    .prepareStatement("CREATE TABLE graphs("
//                            + "graph_id    INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),"
//                            + "enabled     SMALLINT NOT NULL DEFAULT 1,"
//                            + "server_id   INTEGER NOT NULL DEFAULT 0,"
//                            + "name        VARCHAR(128) UNIQUE NOT NULL,"
//                            + "description LONG VARCHAR DEFAULT NULL,"
//                            + "timeframe   INTEGER NOT NULL DEFAULT 60,"
//                            + "mbean       VARCHAR(512) NOT NULL,"
//                            + "data1operation  CHAR DEFAULT NULL,"
//                            + "dataname1   VARCHAR(128) NOT NULL,"
//                            + "operation   VARCHAR(128) DEFAULT NULL,"
//                            + "data2operation  CHAR DEFAULT NULL,"
//                            + "dataname2   VARCHAR(128) DEFAULT NULL,"
//                            + "xlabel      VARCHAR(128) DEFAULT NULL,"
//                            + "ylabel      VARCHAR(128) DEFAULT NULL,"
//                            + "warninglevel1   FLOAT DEFAULT NULL,"
//                            + "warninglevel2   FLOAT DEFAULT NULL,"
//                            + "color       VARCHAR(6) NOT NULL DEFAULT '1176c2',"
//                            + "last_js     LONG VARCHAR DEFAULT NULL,"
//                            + "added       TIMESTAMP NOT NULL,"
//                            + "modified    TIMESTAMP NOT NULL,"
//                            + "archive     SMALLINT NOT NULL DEFAULT 0,"
//                            + "last_seen   TIMESTAMP NOT NULL" + ")");
//            pStmt.executeUpdate();
            if (!checkTables("VIEWS")) {
                PreparedStatement pStmt = con
                        .prepareStatement("CREATE TABLE views("
                                + "view_id     INTEGER PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1),"
                                + "enabled     SMALLINT NOT NULL DEFAULT 1,"
                                + "name        VARCHAR(128) NOT NULL,"
                                + "description LONG VARCHAR DEFAULT NULL,"
                                + "graph_count INTEGER NOT NULL DEFAULT 0,"
                                + "added       TIMESTAMP NOT NULL,"
                                + "modified    TIMESTAMP NOT NULL)");
                pStmt.executeUpdate();
            }
            if (!checkTables("views_graphs")) {
                PreparedStatement pStmt = con.prepareStatement("CREATE TABLE views_graphs("
                        + "view_id     INTEGER NOT NULL,"
                        + "graph_id     INTEGER NOT NULL)");
                pStmt.executeUpdate();
            }
            success = true;
        }
        catch (SQLException e)
        {
            System.err.println("SQL state: " + e.getSQLState());
            System.err.println("SQL error: " + e.getErrorCode());
            e.printStackTrace();
        }
        return success;
    }
}
