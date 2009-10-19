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

package org.apache.geronimo.connector.wrapper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.resource.ResourceException;
import javax.sql.DataSource;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.ResourceSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class DatabaseInitializationGBean {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializationGBean.class);

    public DatabaseInitializationGBean(String testSQL, String path, ResourceSource<ResourceException> cfSource, ClassLoader classLoader) throws Exception {

        DataSource ds = (DataSource) cfSource.$getResource();
        Connection c = ds.getConnection();
        try {
            Statement s = c.createStatement();
            try {
                boolean pass = true;
                // SQL statement in testSQL can be used to determine if the sql script in path attribute should be executed.
                // This attribute can be left blank or skipped altogether.
                if (testSQL != null && !testSQL.trim().equals("")) {
                    ResultSet rs = null;
                    try {
                        rs = s.executeQuery(testSQL);
                        // passes sql test when there are one or more elements
                        pass = !rs.next();
                    } catch (SQLException e) {
                        log.info("Exception running test query, executing script: " + e.getMessage());
                    }
                    if (rs != null) {
                        rs.close();
                    }
                }

                if (pass) {
                    //script needs to be run
                    log.debug("Executing script: " + path);
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
                    }
                    catch (Exception ex) {
                        log.error(ex.getMessage());
                    }
                    finally {
                        r.close();
                    }
                } else {
                    //script need not be run
                    log.debug("Script did not run");
                }
            }
            catch (SQLException e) {
                log.error(e.getMessage());
            }
            finally {
                s.close();
            }
        }
        finally {
            c.close();
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(DatabaseInitializationGBean.class, "GBean");
        infoBuilder.addAttribute("testSQL", String.class, false);
        infoBuilder.addAttribute("path", String.class, true);
        infoBuilder.addReference("DataSource", ResourceSource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[]{"testSQL", "path", "DataSource", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
