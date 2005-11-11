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
package org.apache.geronimo.console.databasemanager.wizard;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

/**
 * Information about common database drivers.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DatabaseInfo {
    public static final DatabaseInfo[] ALL_DATABASES = new DatabaseInfo[]{
        new DatabaseInfo("DaffodilDB Embedded","jdbc:daffodilDB_embedded:<database>","in.co.daffodil.db.jdbc.DaffodilDBDriver"),
        new DatabaseInfo("DaffodilDB Server","jdbc:daffodilDB://<host>:<port>/<database>","in.co.daffodil.db.rmi.RmiDaffodilDBDriver", 3456),
        new DatabaseInfo("DB2","jdbc:db2://<host>:<port>/<database>","com.ibm.db2.jcc.DB2Driver", 50000),
        new DatabaseInfo("DB2 (DataDirect)","jdbc:datadirect:db2://<host>:<port>;DatabaseName=<database>","com.ddtek.jdbc.db2.DB2Driver", 50000),
        new DatabaseInfo("FrontBase","jdbc:FrontBase://<host>:<port>/<database>","com.frontbase.jdbc.FBJDriver"),
        new DatabaseInfo("HSQLDB embedded","jdbc:hsqldb:<database>","org.hsqldb.jdbcDriver"),
        new DatabaseInfo("HSQLDB server","jdbc:hsqldb:hsql://<host>:<port>/<database>","org.hsqldb.jdbcDriver"),
        new DatabaseInfo("Informix","jdbc:informix-sqli://<host>:<port>/<database>:informixserver=<dbservername>","com.informix.jdbc.IfxDriver"),
        new DatabaseInfo("Informix (DataDirect)","jdbc:datadirect:informix://<host>:<port>;informixServer=<dbservername>;DatabaseName=<database>","com.ddtek.jdbc.informix.InformixDriver"),
        new DatabaseInfo("InterSystems Cache","jdbc:Cache://<host>:<port>/<namespace>","com.intersys.jdbc.CacheDriver"),
        new DatabaseInfo("JDataStore","jdbc:borland:dslocal:<file>","com.borland.datastore.jdbc.DataStoreDriver"),
        new DatabaseInfo("JDBC/ODBC Bridge","jdbc:odbc:<datasource>","sun.jdbc.odbc.JdbcOdbcDriver"),
        new DatabaseInfo("McKoi","jdbc:mckoi://<host>/","com.mckoi.JDBCDriver"),
        new DatabaseInfo("Mimer","jdbc:mimer://<host>:<port>/<database>","com.mimer.jdbc.Driver"),
        new DatabaseInfo("MySQL","jdbc:mysql://<host>:<port>/<database>","com.mysql.jdbc.Driver", 3306),
        new DatabaseInfo("Oracle Thin","jdbc:oracle:thin:@<host>:<port>:<sid>","oracle.jdbc.OracleDriver", 1521),
        new DatabaseInfo("Oracle OCI","jdbc:oracle:oci:@<host>:<port>:<sid>","oracle.jdbc.OracleDriver", 1521),
        new DatabaseInfo("Oracle (DataDirect)","jdbc:datadirect:oracle://<host>:<port>;ServiceName=<sid>","com.ddtek.jdbc.oracle.OracleDriver", 1521),
        new DatabaseInfo("Pervasive","jdbc:pervasive://<host>:<port>/<database>","com.pervasive.jdbc.v2.Driver"),
        new DatabaseInfo("Pointbase server","jdbc:pointbase:server://<host>:<port>/<database>","com.pointbase.jdbc.jdbcUniversalDriver"),
        new DatabaseInfo("PostgreSQL","jdbc:postgresql://<host>:<port>/<database>","org.postgresql.Driver", 5432),
        new DatabaseInfo("Progress","jdbc:jdbcProgress:T:<host>:<port>:<database>","com.progress.sql.jdbc.JdbcProgressDriver"),
        new DatabaseInfo("MaxDB","jdbc:sapdb://<host>:<port>/<database>","com.sap.dbtech.jdbc.DriverSapDB"),
        new DatabaseInfo("SQL Server","jdbc:microsoft:sqlserver://<host>:<port>;DatabaseName=<database>","com.microsoft.jdbc.sqlserver.SQLServerDriver", 1433),
        new DatabaseInfo("SQL Server (jTDS)","jdbc:jtds:sqlserver://<host>:<port>;DatabaseName=<database>","net.sourceforge.jtds.jdbc.Driver", 1433),
        new DatabaseInfo("SQL Server (DataDirect)","jdbc:datadirect:sqlserver://<host>:<port>;DatabaseName=<database>","com.ddtek.jdbc.sqlserver.SQLServerDriver", 1433),
        new DatabaseInfo("Sybase ASE","jdbc:sybase:Tds:<host>:<port>/<database>","com.sybase.jdbc3.jdbc.SybDriver", 2048),
        new DatabaseInfo("Sybase ASA","jdbc:sybase:Tds:<host>:<port>/<database>","com.sybase.jdbc3.jdbc.SybDriver", 2638),
        new DatabaseInfo("Sybase (DataDirect)","jdbc:datadirect:sybase://<host>:<port>;DatabaseName=<database>","com.ddtek.jdbc.sybase.SybaseDriver"),
    };
    private final static Pattern PARAM_PATTERN = Pattern.compile("<.+?>");

    private String name;
    private String url;
    private String driverClass;
    private int defaultPort;

    public DatabaseInfo(String name, String url, String driverClass) {
        this.name = name;
        this.url = url;
        this.driverClass = driverClass;
    }

    public DatabaseInfo(String name, String url, String driverClass, int defaultPort) {
        this.name = name;
        this.url = url;
        this.driverClass = driverClass;
        this.defaultPort = defaultPort;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public String[] getUrlParameters() {
        Matcher m = PARAM_PATTERN.matcher(url);
        List list = new ArrayList();
        while(m.find()) {
            list.add(url.substring(m.start()+1, m.end()-1));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
}
