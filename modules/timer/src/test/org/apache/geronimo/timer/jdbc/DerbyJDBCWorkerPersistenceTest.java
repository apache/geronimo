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
package org.apache.geronimo.timer.jdbc;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.derby.jdbc.EmbeddedDataSource;

/**
 * @version $Rev$ $Date$
 */
public class DerbyJDBCWorkerPersistenceTest extends JDBCWorkerPersistenceTestAbstract {
    private static final String SYSTEM_HOME = "derby.system.home";
    private static final String SHUTDOWN_ALL = "jdbc:derby:;shutdown=true";

    private File systemDir;


    private void connect() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:derby:testdb;create=true");
        c.close();
    }

    protected void setUp() throws Exception {
        useSequence = false;
        try {
            systemDir = File.createTempFile("derbyTest", ".tmp");
            systemDir.delete();
            systemDir.mkdirs();
        } catch (Exception e) {
            delete(systemDir);
            throw e;
        }
        String derbyDir = "var/dbderby";
        File derby = new File(systemDir, derbyDir);
        System.setProperty(SYSTEM_HOME, derby.getAbsolutePath());

        // set the magic system property that causes derby to use explicity
        // file sync instead of relying on vm support for file open rws
        System.setProperty("derby.storage.fileSyncTransactionLog", "true");

        // load the Embedded driver to initialize the home
        new org.apache.derby.jdbc.EmbeddedDriver();

        EmbeddedDataSource datasource = new EmbeddedDataSource();
        datasource.setDatabaseName("SystemDatabase");
        datasource.setCreateDatabase("create");
        try {
            Connection c = datasource.getConnection();
            c.close();
        } catch (SQLException e) {
            while (e.getNextException() != null) {
                e.printStackTrace();
                e = e.getNextException();
            }
            throw e;
        }
        this.datasource = datasource;
//        assertTrue(new File(systemDir, derbyDir + "/derby.log").exists());
        super.setUp();
    }

    protected void tearDown() throws Exception {
//        ((EmbeddedDataSource)datasource).setShutdownDatabase("shutdown");
        try {
            DriverManager.getConnection(SHUTDOWN_ALL, null, null);
        } catch (SQLException e) {
            //ignore.. expected??
        }
//        Connection c = datasource.getConnection();
//        c.close();
        delete(systemDir);
        super.tearDown();
//        Thread.sleep(5000);
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
