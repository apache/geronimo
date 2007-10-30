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

package org.apache.geronimo.connector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class DatabaseInitializationGBean {


    public DatabaseInitializationGBean(String testSQL, String path, ConnectionFactorySource cfSource, ClassLoader classLoader) throws Exception {

        DataSource ds = (DataSource) cfSource.$getResource();
        Connection c = ds.getConnection();
        try {
            Statement s = c.createStatement();
            try {
                try {
                    s.execute(testSQL);
                    //script does not need to be run
                    return;
                } catch (SQLException e) {
                    //script needs to be run
                }
                URL sourceURL = classLoader.getResource(path);
                InputStream ins = sourceURL.openStream();
                BufferedReader r = new BufferedReader(new InputStreamReader(ins));
                try {
                    String line;
                    StringBuffer buf = new StringBuffer();
                    while ((line = r.readLine()) != null) {
                        line = line.trim();
                        if (!line.startsWith("--") && line.length() > 0) {
                            buf.append(line).append(" ");
                            if (line.endsWith(";")) {
                                int size = buf.length();
                                buf.delete(size - 2, size - 1);
                                String sql = buf.toString();
                                s.execute(sql);
                                buf = new StringBuffer();
                            }
                        }
                    }
                } finally {
                    r.close();
                }
            } finally {
                s.close();
            }
        } finally {
            c.close();
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(DatabaseInitializationGBean.class, "GBean");
        infoBuilder.addAttribute("testSQL", String.class, true);
        infoBuilder.addAttribute("path", String.class, true);
        infoBuilder.addReference("DataSource", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[]{"testSQL", "path", "DataSource", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
