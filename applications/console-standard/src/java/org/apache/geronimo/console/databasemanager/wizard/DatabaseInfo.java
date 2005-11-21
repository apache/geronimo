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
    /**
     * todo: EVIL!!!  Should be replaced with something, somehow!
     */
    private final static String TRANQL_RAR_NAME = "tranql/rars/tranql-connector-1.0.rar";
    private final static String DERBY_EMBEDDED_RAR_NAME = "tranql/rars/tranql-connector-derby-embed-xa-1.0.rar";
    private final static String DERBY_NETWORK_RAR_NAME = "tranql/rars/tranql-connector-derby-client-xa-1.0.rar";

    //todo: Load this from a config file or something
    public static final DatabaseInfo[] ALL_DATABASES = new DatabaseInfo[]{
        new DatabaseInfo("DaffodilDB Embedded","jdbc:daffodilDB_embedded:<Database>","in.co.daffodil.db.jdbc.DaffodilDBDriver"),
        new DatabaseInfo("DaffodilDB Server","jdbc:daffodilDB://<Host>:<Port>/<Database>","in.co.daffodil.db.rmi.RmiDaffodilDBDriver", 3456),
        new DatabaseInfo("DB2","jdbc:db2://<Host>:<Port>/<Database>","com.ibm.db2.jcc.DB2Driver", 50000),
        new DatabaseInfo("DB2 (DataDirect)","jdbc:datadirect:db2://<Host>:<Port>;DatabaseName=<Database>","com.ddtek.jdbc.db2.DB2Driver", 50000),
        new DatabaseInfo("Derby embedded","jdbc:derby:<Database>","org.apache.derby.jdbc.EmbeddedDriver"),
        new DatabaseInfo("Derby network","jdbc:derby://<Host>:<Port>/<Database>","org.apache.derby.jdbc.ClientDriver", 1527),
        new DatabaseInfo("Derby embedded XA", DERBY_EMBEDDED_RAR_NAME),
        new DatabaseInfo("Derby network XA", DERBY_NETWORK_RAR_NAME),
        new DatabaseInfo("FrontBase","jdbc:FrontBase://<Host>:<Port>/<Database>","com.frontbase.jdbc.FBJDriver"),
        new DatabaseInfo("HSQLDB embedded","jdbc:hsqldb:<Database>","org.hsqldb.jdbcDriver"),
        new DatabaseInfo("HSQLDB server","jdbc:hsqldb:hsql://<Host>:<Port>/<Database>","org.hsqldb.jdbcDriver"),
        new DatabaseInfo("Informix","jdbc:informix-sqli://<Host>:<Port>/<Database>:informixserver=<dbservername>","com.informix.jdbc.IfxDriver"),
        new DatabaseInfo("Informix (DataDirect)","jdbc:datadirect:informix://<Host>:<Port>;informixServer=<dbservername>;DatabaseName=<Database>","com.ddtek.jdbc.informix.InformixDriver"),
        new DatabaseInfo("InterSystems Cache","jdbc:Cache://<Host>:<Port>/<namespace>","com.intersys.jdbc.CacheDriver"),
        new DatabaseInfo("JDataStore","jdbc:borland:dslocal:<file>","com.borland.datastore.jdbc.DataStoreDriver"),
        new DatabaseInfo("JDBC/ODBC Bridge","jdbc:odbc:<datasource>","sun.jdbc.odbc.JdbcOdbcDriver"),
        new DatabaseInfo("McKoi embedded","jdbc:mckoi:local://<ConfigPath>/<Schema>/","com.mckoi.JDBCDriver"),
        new DatabaseInfo("McKoi server","jdbc:mckoi://<Host>:<Port>/<Schema>/","com.mckoi.JDBCDriver"),
        new DatabaseInfo("Mimer","jdbc:mimer://<Host>:<Port>/<Database>","com.mimer.jdbc.Driver"),
        new DatabaseInfo("MySQL","jdbc:mysql://<Host>:<Port>/<Database>","com.mysql.jdbc.Driver", 3306),
        new DatabaseInfo("Oracle Thin","jdbc:oracle:thin:@<Host>:<Port>:<SID>","oracle.jdbc.OracleDriver", 1521),
        new DatabaseInfo("Oracle OCI","jdbc:oracle:oci:@<Host>:<Port>:<SID>","oracle.jdbc.OracleDriver", 1521),
        new DatabaseInfo("Oracle (DataDirect)","jdbc:datadirect:oracle://<Host>:<Port>;ServiceName=<SID>","com.ddtek.jdbc.oracle.OracleDriver", 1521),
        new DatabaseInfo("Pervasive","jdbc:pervasive://<Host>:<Port>/<Database>","com.pervasive.jdbc.v2.Driver"),
        new DatabaseInfo("Pointbase server","jdbc:pointbase:server://<Host>:<Port>/<Database>","com.pointbase.jdbc.jdbcUniversalDriver"),
        new DatabaseInfo("PostgreSQL","jdbc:postgresql://<Host>:<Port>/<Database>","org.postgresql.Driver", 5432),
        new DatabaseInfo("Progress","jdbc:jdbcProgress:T:<Host>:<Port>:<Database>","com.progress.sql.jdbc.JdbcProgressDriver"),
        new DatabaseInfo("MaxDB","jdbc:sapdb://<Host>:<Port>/<Database>","com.sap.dbtech.jdbc.DriverSapDB"),
        new DatabaseInfo("SQL Server","jdbc:microsoft:sqlserver://<Host>:<Port>;DatabaseName=<Database>","com.microsoft.jdbc.sqlserver.SQLServerDriver", 1433),
        new DatabaseInfo("SQL Server (jTDS)","jdbc:jtds:sqlserver://<Host>:<Port>;DatabaseName=<Database>","net.sourceforge.jtds.jdbc.Driver", 1433),
        new DatabaseInfo("SQL Server (DataDirect)","jdbc:datadirect:sqlserver://<Host>:<Port>;DatabaseName=<Database>","com.ddtek.jdbc.sqlserver.SQLServerDriver", 1433),
        new DatabaseInfo("Sybase ASE","jdbc:sybase:Tds:<Host>:<Port>/<Database>","com.sybase.jdbc3.jdbc.SybDriver", 2048),
        new DatabaseInfo("Sybase ASA","jdbc:sybase:Tds:<Host>:<Port>/<Database>","com.sybase.jdbc3.jdbc.SybDriver", 2638),
        new DatabaseInfo("Sybase (DataDirect)","jdbc:datadirect:sybase://<Host>:<Port>;DatabaseName=<Database>","com.ddtek.jdbc.sybase.SybaseDriver"),
        new DatabaseInfo("Other","",""),
    };
    private final static Pattern PARAM_PATTERN = Pattern.compile("<.+?>");

    private String name;
    private String url;
    private String driverClass;
    private int defaultPort;
    private boolean xa;
    private String rarPath;

    public DatabaseInfo(String name, String url, String driverClass) {
        this.name = name;
        this.url = url;
        this.driverClass = driverClass;
        xa = false;
        rarPath = getDefaultRARPath();
    }

    public DatabaseInfo(String name, String url, String driverClass, int defaultPort) {
        this.name = name;
        this.url = url;
        this.driverClass = driverClass;
        this.defaultPort = defaultPort;
        xa = false;
        rarPath = getDefaultRARPath();
    }

    public DatabaseInfo(String name, String rarPath) {
        this.name = name;
        xa = true;
        this.rarPath = rarPath;
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

    public boolean isXA() {
        return xa;
    }

    public String getRarPath() {
        return rarPath;
    }

    public String[] getUrlParameters() {
        Matcher m = PARAM_PATTERN.matcher(url);
        List list = new ArrayList();
        while(m.find()) {
            list.add(url.substring(m.start()+1, m.end()-1));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static String getDefaultRARPath() {
        return TRANQL_RAR_NAME;
    }
}
