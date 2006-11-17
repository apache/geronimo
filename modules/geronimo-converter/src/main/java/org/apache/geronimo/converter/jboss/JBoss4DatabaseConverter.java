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
package org.apache.geronimo.converter.jboss;

import java.io.Reader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.geronimo.converter.DatabaseConversionStatus;
import org.apache.geronimo.converter.JDBCPool;
import org.apache.geronimo.converter.XADatabasePool;
import org.apache.geronimo.converter.AbstractDatabasePool;
import org.apache.geronimo.converter.DOMUtils;
import org.apache.geronimo.kernel.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Converts database pools from JBoss 4 to Geronimo
 *
 * @version $Rev$ $Date$
 */
public class JBoss4DatabaseConverter extends DOMUtils {
    public static DatabaseConversionStatus convert(Reader dsXml) throws IOException {
        List status = new ArrayList();
        List noTx = new ArrayList();
        List local = new ArrayList();
        List xa = new ArrayList();

        DocumentBuilderFactory factory = XmlUtil.newDocumentBuilderFactory();
        factory.setValidating(false);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(dsXml));
            dsXml.close();
            parseDocument(doc, status, noTx, local, xa);
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

    private static void parseDocument(Document doc, List status, List noTx, List local, List xa) {
        Element datasources = doc.getDocumentElement();
        if(!datasources.getNodeName().equalsIgnoreCase("datasources")) {
            if(datasources.getNodeName().equals("connection-factories")) {
                status.add("ERROR: Geronimo cannot parse a JBoss data source configured using conection-factories.  This typically means a custom RAR file is required.");
                return;
            } else {
                status.add("ERROR: Unrecognized file beginning with "+datasources.getNodeName()+" element.  Expected a JBoss *-ds.xml file.");
                return;
            }
        }
        NodeList list = datasources.getChildNodes();
        for(int i=0; i<list.getLength(); i++) {
            Node node = list.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                String name = node.getNodeName();
                if(name.equalsIgnoreCase("no-tx-datasource")) {
                    addJDBCDataSource((Element)node, status, noTx);
                } else if(name.equalsIgnoreCase("local-tx-datasource")) {
                    addJDBCDataSource((Element)node, status, local);
                } else if(name.equalsIgnoreCase("xa-datasource")) {
                    addXADataSource((Element)node, status, xa);
                } else if(name.equalsIgnoreCase("mbean")) {
                    status.add("Skipping MBean element");
                } else {
                    status.add("WARN: Skipped element "+name);
                }
            }
        }
    }

    private static void addDataSourceCommon(Element root, AbstractDatabasePool pool, List status) {
        pool.setJndiName(getChildText(root, "jndi-name"));
        pool.setName(pool.getJndiName());
        if(pool.getJndiName() != null && pool.getJndiName().indexOf('/') > -1) {
            status.add("NOTE: pool will use name '"+pool.getJndiName()+"' though Geronimo doesn't put it in JNDI");
        }
        String test = getChildText(root, "min-pool-size");
        if(test != null && !test.equals("")) pool.setMinSize(new Integer(test));
        test = getChildText(root, "max-pool-size");
        if(test != null && !test.equals("")) pool.setMaxSize(new Integer(test));
        test = getChildText(root, "blocking-timeout-millis");
        if(test != null && !test.equals("")) pool.setBlockingTimeoutMillis(new Integer(test));
        test = getChildText(root, "idle-timeout-minutes");
        if(test != null && !test.equals("")) pool.setIdleTimeoutMillis(new Integer(Integer.parseInt(test)*60*1000));
        pool.setNewConnectionSQL(getChildText(root, "new-connection-sql"));
        pool.setTestConnectionSQL(getChildText(root, "check-valid-connection-sql"));
        String sorter = getChildText(root, "exception-sorter-class-name");
        if(sorter != null) {
            if(sorter.indexOf("Oracle") > -1) pool.setVendor(AbstractDatabasePool.VENDOR_ORACLE);
            if(sorter.indexOf("MySQL") > -1) pool.setVendor(AbstractDatabasePool.VENDOR_MYSQL);
            if(sorter.indexOf("Sybase") > -1) pool.setVendor(AbstractDatabasePool.VENDOR_SYBASE);
            if(sorter.indexOf("Informix") > -1) pool.setVendor(AbstractDatabasePool.VENDOR_INFORMIX);
        }
        test = getChildText(root, "prepared-statement-cache-size");
        if(test != null && !test.equals("")) pool.setStatementCacheSize(new Integer(test));
    }

    private static void addJDBCDataSource(Element root, List status, List results) {
        JDBCPool pool = new JDBCPool();
        addDataSourceCommon(root, pool, status);
        pool.setJdbcURL(getChildText(root, "connection-url"));
        pool.setDriverClass(getChildText(root, "driver-class"));
        NodeList list = root.getElementsByTagName("connection-property");
        for(int i=0; i<list.getLength(); i++) {
            Element prop = (Element) list.item(i);
            pool.getConnectionProperties().setProperty(prop.getAttribute("name"), getText(prop));
        }
        pool.setUsername(getChildText(root, "user-name"));
        pool.setPassword(getChildText(root, "password"));


        if(pool.getName() != null && !pool.getName().equals("")) {
            results.add(pool);
        } else {
            status.add("WARN: Ignoring pool with no JNDI name");
        }
    }

    private static void addXADataSource(Element root, List status, List results) {
        XADatabasePool pool = new XADatabasePool();
        addDataSourceCommon(root, pool, status);
        pool.setXaDataSourceClass(getChildText(root, "xa-datasource-class"));
        NodeList list = root.getElementsByTagName("xa-datasource-property");
        for(int i=0; i<list.getLength(); i++) {
            Element prop = (Element) list.item(i);
            pool.getProperties().setProperty(prop.getAttribute("name"), getText(prop));
        }

        if(pool.getName() != null && !pool.getName().equals("")) {
            results.add(pool);
        } else {
            status.add("WARN: Ignoring pool with no JNDI name");
        }
    }

    /*
    public static void main(String[] args) {
        File dir = new File("/Users/ammulder/temp/jboss-4.0.3SP1/docs/examples/jca/");
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("-ds.xml");
            }
        });
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println("Reading "+file.getName());
            try {
                FileReader reader = new FileReader(file);
                DatabaseConversionStatus status = JBoss4DatabaseConverter.convert(reader);
                for (int j = 0; j < status.getMessages().length; j++) {
                    String message = status.getMessages()[j];
                    System.out.println("    "+message);
                }
                System.out.println("    FOUND "+status.getNoTXPools().length+" NoTX Pools");
                System.out.println("    FOUND "+status.getJdbcPools().length+" JDBC Pools");
                System.out.println("    FOUND "+status.getXaPools().length+" XA Pools");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } */
}
