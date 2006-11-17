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

package org.apache.geronimo.derby;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class DerbySystemGBeanTest extends TestCase {
    private File systemDir;

    public void testCreateSystemUsingServerInfo() throws Exception {
        ServerInfo serverInfo = new BasicServerInfo(systemDir.toString());
        String derbyDir = "var/dbderby";
        DerbySystemGBean gbean = new DerbySystemGBean(serverInfo, derbyDir);
        try {
            gbean.doStart();
            new org.apache.derby.jdbc.EmbeddedDriver();
            connect();
            gbean.doStop();
            assertTrue(new File(systemDir, derbyDir+"/derby.log").exists());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void connect() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:derby:testdb;create=true");
        c.close();
    }

    protected void setUp() throws Exception {
        try {
            super.setUp();
            systemDir = File.createTempFile("derbyTest", ".tmp");
            systemDir.delete();
            systemDir.mkdir();

            Properties props = System.getProperties();
            props.remove("derby.system.home");
        } catch (Exception e) {
            delete(systemDir);
            throw e;
        }
    }

    protected void tearDown() throws Exception {
        delete(systemDir);
        super.tearDown();
    }

    private void delete(File file) throws IOException {
        if (file == null) {
            return;
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }
        }
        file.delete();
    }
}
