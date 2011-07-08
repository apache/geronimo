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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.resource.ResourceException;
import javax.sql.DataSource;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.ResourceSource;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class DatabaseInitializationGBean {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializationGBean.class);

    public DatabaseInitializationGBean(@ParamAttribute(name="testSQL") String testSQL,
                                       @ParamAttribute(name="sql") String sql,
                                       @ParamAttribute(name="path") String path,
                                       @ParamReference(name="DataSource", namingType=NameFactory.JCA_CONNECTION_MANAGER) ResourceSource<ResourceException> cfSource,
                                       @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) throws Exception {

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
                    Reader reader = getSQLInput(sql, path, bundle);
                    BufferedReader r = new BufferedReader(reader);
                    try {
                        String line;
                        StringBuilder buf = new StringBuilder();
                        while ((line = r.readLine()) != null) {
                            line = line.trim();
                            if (!line.startsWith("--") && line.length() > 0) {
                                buf.append(line).append(" ");
                                if (line.endsWith(";")) {
                                    int size = buf.length();
                                    buf.delete(size - 2, size - 1);
                                    String sqlCmd = buf.toString();
                                    s.execute(sqlCmd);
                                    buf.setLength(0);
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

	private Reader getSQLInput(String sqlString, String path, Bundle bundle)
			throws Exception {
		if (sqlString != null) {
			return new StringReader(sqlString);
		} else if (path != null) {
			URL resource = bundle.getResource(path);
			if (resource == null) {
				resource = bundle.getEntry(path);
				if (resource != null) {
					return new InputStreamReader(resource.openStream());
				} else {
					File file = new File(path);
					if (!file.exists()) {
						throw new Exception("SQL resource file not found: "
								+ path);
					}
					return new InputStreamReader(new FileInputStream(file));
				}
			} else {
				return new InputStreamReader(resource.openStream());
			}
		} else {
			throw new Exception("SQL resource file or string not specified");
		}
	}
}
