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
package org.apache.geronimo.converter.bea;

import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.geronimo.converter.DOMUtils;
import org.apache.geronimo.converter.DatabaseConversionStatus;
import org.apache.geronimo.converter.JDBCPool;
import org.apache.geronimo.converter.XADatabasePool;
import org.apache.geronimo.converter.AbstractDatabasePool;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Converts database pools from WebLogic 8.1 to Geronimo
 *
 * @version $Rev$ $Date$
 */
public class WebLogic81DatabaseConverter extends DOMUtils {
    public static DatabaseConversionStatus convert(String libDir, String domainDir) throws IOException {
        Weblogic81Utils utils = new Weblogic81Utils(libDir, domainDir);
        String config = utils.getConfigXML();
        return convert(new StringReader(config));
    }

    public static DatabaseConversionStatus convert(Reader configXml) throws IOException {
        List status = new ArrayList();
        List noTx = new ArrayList();
        List local = new ArrayList();
        List xa = new ArrayList();

        DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
        factory.setValidating(false);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(configXml));
            configXml.close();
            parseDocument(doc, status, local, xa);
        } catch (ParserConfigurationException e) {
            throw (IOException)new IOException().initCause(e);
        } catch (SAXException e) {
            throw (IOException)new IOException().initCause(e);
        }

        DatabaseConversionStatus result = new DatabaseConversionStatus();
        result.setMessages((String[]) status.toArray(new String[status.size()]));
        result.setNoTXPools((JDBCPool[]) noTx.toArray(new JDBCPool[noTx.size()]));
        result.setJdbcPools((JDBCPool[]) local.toArray(new JDBCPool[noTx.size()]));
        result.setXaPools((XADatabasePool[]) xa.toArray(new XADatabasePool[xa.size()]));
        return result;
    }

    private static void parseDocument(Document doc, List status, List local, List xa) {
        Element domain = doc.getDocumentElement();
        if(!domain.getNodeName().equalsIgnoreCase("Domain")) {
            status.add("ERROR: Unrecognized file beginning with "+domain.getNodeName()+" element.  Expected a WebLogic config.xml file.");
            return;
        }
        NodeList list = domain.getChildNodes();
        Map pools = new HashMap();
        for(int i=0; i<list.getLength(); i++) {
            Node node = list.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                String name = node.getNodeName();
                if(name.equalsIgnoreCase("JDBCConnectionPool")) {
                    ConnectionPool pool = getConnectionPool((Element)node, status);
                    pools.put(pool.getName(), pool);
                } else if(name.equalsIgnoreCase("JDBCDataSource")) {
                    DataSource ds = getDataSource((Element)node, false);
                    ConnectionPool pool = (ConnectionPool) pools.get(ds.getPoolName());
                    if(pool != null) {
                        pool.getDataSources().add(ds);
                    } else {
                        status.add("ERROR: Can't find pool for data source '"+ds.getName()+"' ("+ds.getPoolName()+")");
                    }
                } else if(name.equalsIgnoreCase("JDBCTxDataSource")) {
                    DataSource ds = getDataSource((Element)node, true);
                    ConnectionPool pool = (ConnectionPool) pools.get(ds.getPoolName());
                    if(pool != null) {
                        pool.getDataSources().add(ds);
                    } else {
                        status.add("ERROR: Can't find pool for data source '"+ds.getName()+"' ("+ds.getPoolName()+")");
                    }
                } else {
                    status.add("Skipping element '"+name+"'");
                }
            }
        }
        if(pools.size() > 0) {
            for (Iterator it = pools.values().iterator(); it.hasNext();) {
                ConnectionPool pool = (ConnectionPool) it.next();
                if(pool.getPassword() != null && pool.getPassword().startsWith("{")) {
                    status.add("NOTE: When importing from WebLogic, typically database passwords cannot be recovered, and will need to be re-entered.");
                    break;
                }
            }
        }
        processPools((ConnectionPool[]) pools.values().toArray(new ConnectionPool[0]),
                status, local, xa);
    }

    private static void processPools(ConnectionPool[] pools, List status, List local, List xa) {
        for (int i = 0; i < pools.length; i++) {
            ConnectionPool pool = pools[i];
            boolean isXA;
            if(pool.hasEmulate()) {
                isXA = false;
            } else if(pool.hasNonTX()) {
                isXA = false;
            } else if(pool.hasXADriverName()) {
                isXA = true;
            } else {
                isXA = false;
                status.add("Can't tell whether pool '"+pool.getName()+"' is an XA driver or not; will create local transaction pools in Geronimo.");
            }
            if(pool.getDataSources().size() == 0) {
                status.add("Pool '"+pool.getName()+"' has no associated data sources.  Creating a default pool for it.");
                if(isXA) {
                    xa.add(createXAPool(pool, pool.getName(), null));
                } else {
                    local.add(createJDBCPool(pool, pool.getName(), null));
                }
            } else {
                for (int j = 0; j < pool.getDataSources().size(); j++) {
                    DataSource ds = (DataSource) pool.getDataSources().get(j);
                    if(isXA) {
                        xa.add(createXAPool(pool, ds.getName(), ds.getJndiName()));
                    } else {
                        local.add(createJDBCPool(pool, ds.getName(), ds.getJndiName()));
                    }
                }
            }
        }
    }

    private static void populatePool(ConnectionPool pool, AbstractDatabasePool target) {
        if(pool.getReserveTimeoutSecs() != null) {
            target.setBlockingTimeoutMillis(new Integer(pool.getReserveTimeoutSecs().intValue()*1000));
        }
        if(pool.getIdleTimeoutSecs() != null) {
            target.setIdleTimeoutMillis(new Integer(pool.getIdleTimeoutSecs().intValue()*1000));
        }
        target.setMaxSize(pool.getMax());
        target.setMinSize(pool.getMin());
        target.setNewConnectionSQL(pool.getInitSQL());
        target.setStatementCacheSize(pool.getCacheSize());
        target.setTestConnectionSQL(pool.getTestTable() == null ? null : "SELECT * FROM "+pool.getTestTable()+" WHERE 0=1");
        if(pool.getDriverName().toLowerCase().indexOf("oracle") > -1) target.setVendor(JDBCPool.VENDOR_ORACLE);
        if(pool.getDriverName().toLowerCase().indexOf("mysql") > -1) target.setVendor(JDBCPool.VENDOR_MYSQL);
        if(pool.getDriverName().toLowerCase().indexOf("sybase") > -1) target.setVendor(JDBCPool.VENDOR_SYBASE);
        if(pool.getDriverName().toLowerCase().indexOf("informix") > -1) target.setVendor(JDBCPool.VENDOR_INFORMIX);
    }

    private static JDBCPool createJDBCPool(ConnectionPool pool, String name, String jndiName) {
        JDBCPool result = new JDBCPool();
        result.setName(name);
        result.setJndiName(jndiName);
        populatePool(pool, result);
        result.setConnectionProperties(pool.getProperties());
        result.setDriverClass(pool.getDriverName());
        result.setJdbcURL(pool.getUrl());
        // Don't bother putting encrypted passwords into the pool
        if(pool.getPassword() != null && !pool.getPassword().startsWith("{")) {
            result.setPassword(pool.getPassword());
        }
        result.setUsername(pool.getUsername());
        return result;
    }

    private static XADatabasePool createXAPool(ConnectionPool pool, String name, String jndiName) {
        XADatabasePool result = new XADatabasePool();
        result.setName(name);
        result.setJndiName(jndiName);
        populatePool(pool, result);
        result.setXaDataSourceClass(pool.getDriverName());
        result.setProperties(pool.getProperties());
        return result;
    }

    private static DataSource getDataSource(Element root, boolean tx) {
        DataSource ds = new DataSource();
        ds.setDeclaredAsTX(tx);
        ds.setEmulate(getBoolean(root.getAttribute("EnableTwoPhaseCommit"), false));
        ds.setName(root.getAttribute("Name"));
        ds.setJndiName(root.getAttribute("JNDIName"));
        ds.setPoolName(root.getAttribute("PoolName"));
        return ds;
    }

    private static boolean getBoolean(String value, boolean defaultResult) {
        if(value == null) {
            return defaultResult;
        }
        return new Boolean(value).booleanValue();
    }

    private static ConnectionPool getConnectionPool(Element root, List status) {
        ConnectionPool pool = new ConnectionPool();
        pool.setName(root.getAttribute("Name"));
        pool.setDriverName(root.getAttribute("DriverName"));
        pool.setUrl(root.getAttribute("URL"));
        pool.setMin(getInteger(root.getAttribute("InitialCapacity")));
        pool.setMax(getInteger(root.getAttribute("MaxCapacity")));
        readProperties(pool.getProperties(), root.getAttribute("Properties"), status);
        pool.setUsername(pool.getProperties().getProperty("user"));
        pool.getProperties().remove("user");
        if(root.hasAttribute("Password")) {
            pool.setPassword(root.getAttribute("Password"));
        } else if(root.hasAttribute("PasswordEncrypted")) {
            pool.setPassword(root.getAttribute("PasswordEncrypted"));
        }
        pool.setReserveTimeoutSecs(getInteger(root.getAttribute("ConnectionReserveTimeoutSeconds")));
        pool.setIdleTimeoutSecs(getInteger(root.getAttribute("InactiveConnectionTimeoutSeconds")));
        pool.setCacheSize(getInteger(root.getAttribute("StatementCacheSize")));
        pool.setInitSQL(root.getAttribute("InitSQL"));
        pool.setTestTable(root.getAttribute("TestTableName"));
        return pool;
    }

    private static void readProperties(Properties props, String value, List status) {
        if(value == null) {
            return;
        }
        value = value.trim();
        if(value.equals("")) {
            return;
        }
        int last = -1;
        int pos = value.indexOf(';');
        while(pos > -1) {
            String s = value.substring(last+1, pos);
            int eq = s.indexOf('=');
            if(eq > -1) {
                props.setProperty(s.substring(0, eq), s.substring(eq+1));
            } else {
                status.add("WARN: Unable to read property '"+s+"'");
            }
            last = pos;
            pos = value.indexOf(';', pos+1);
        }
        String s = value.substring(last+1);
        int eq = s.indexOf('=');
        if(eq > -1) {
            props.setProperty(s.substring(0, eq), s.substring(eq+1));
        } else {
            status.add("WARN: Unable to read property '"+s+"'");
        }
    }

    private static Integer getInteger(String value) {
        if(value == null) {
            return null;
        }
        value = value.trim();
        if(value.equals("")) {
            return null;
        }
        return new Integer(value);
    }

    public static class DataSource {
        private String name;
        private String poolName;
        private String jndiName;
        private boolean emulate;
        private boolean declaredAsTX;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPoolName() {
            return poolName;
        }

        public void setPoolName(String poolName) {
            this.poolName = poolName;
        }

        public String getJndiName() {
            return jndiName;
        }

        public void setJndiName(String jndiName) {
            this.jndiName = jndiName;
        }

        public boolean isEmulate() {
            return emulate;
        }

        public void setEmulate(boolean emulate) {
            this.emulate = emulate;
        }

        public boolean isDeclaredAsTX() {
            return declaredAsTX;
        }

        public void setDeclaredAsTX(boolean declaredAsTX) {
            this.declaredAsTX = declaredAsTX;
        }
    }

    public static class ConnectionPool {
        private String name;
        private String driverName;
        private Integer min, max;
        private String url;
        private String username;
        private String password;
        private Integer reserveTimeoutSecs;
        private Integer idleTimeoutSecs;
        private Integer cacheSize;
        private String initSQL;
        private String testTable;
        private Properties properties = new Properties();
        private List dataSources = new ArrayList();

        public boolean hasEmulate() {
            for (int i = 0; i < dataSources.size(); i++) {
                DataSource ds = (DataSource) dataSources.get(i);
                if(ds.isEmulate()) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasNonTX() {
            for (int i = 0; i < dataSources.size(); i++) {
                DataSource ds = (DataSource) dataSources.get(i);
                if(!ds.isDeclaredAsTX()) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasXADriverName() {
            return driverName.toUpperCase().indexOf("XA") > -1;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDriverName() {
            return driverName;
        }

        public void setDriverName(String driverName) {
            this.driverName = driverName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getInitSQL() {
            return initSQL;
        }

        public void setInitSQL(String initSQL) {
            this.initSQL = initSQL;
        }

        public String getTestTable() {
            return testTable;
        }

        public void setTestTable(String testTable) {
            this.testTable = testTable;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }

        public List getDataSources() {
            return dataSources;
        }

        public void setDataSources(List dataSources) {
            this.dataSources = dataSources;
        }

        public Integer getMin() {
            return min;
        }

        public void setMin(Integer min) {
            this.min = min;
        }

        public Integer getMax() {
            return max;
        }

        public void setMax(Integer max) {
            this.max = max;
        }

        public Integer getReserveTimeoutSecs() {
            return reserveTimeoutSecs;
        }

        public void setReserveTimeoutSecs(Integer reserveTimeoutSecs) {
            this.reserveTimeoutSecs = reserveTimeoutSecs;
        }

        public Integer getIdleTimeoutSecs() {
            return idleTimeoutSecs;
        }

        public void setIdleTimeoutSecs(Integer idleTimeoutSecs) {
            this.idleTimeoutSecs = idleTimeoutSecs;
        }

        public Integer getCacheSize() {
            return cacheSize;
        }

        public void setCacheSize(Integer cacheSize) {
            this.cacheSize = cacheSize;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
