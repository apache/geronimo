/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.plugin;

import java.io.File;

import org.apache.geronimo.testsupport.TestSupport;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class ArchiverGBeanTest extends TestSupport {

    private static int count = 0;
    private ServerInfo serverInfo;
    private File serverBase;

    protected void setUp() throws Exception {
        super.setUp();
        //set up test server location

        for (int i = 0; i < 100; i++) {
            serverBase = new File(new File(new File(new File(getBaseDir(), "target"), "test-resources"), "ArchiverGBeanTest"), "server" + ++count);
            if (serverBase.mkdirs()) {
                break;
            }
            if (i == 100) {
                throw new RuntimeException("Could not create server base: " + serverBase);
            }
        }
        serverInfo = new BasicServerInfo(getBaseDir().getAbsolutePath(), false);
    }
    
    public void testArchiverTgz() throws Exception {
        ArchiverGBean archiver = new ArchiverGBean(serverInfo);
        File dest = archiver.archive("src/test/resources/archivertest", serverBase.getAbsolutePath(), new Artifact("foo","bar", "1.0", "tar.gz"));
        assertTrue(dest.exists());
        assertEquals("bar-1.0-bin.tar.gz", dest.getName());
    }
    public void testArchiverZip() throws Exception {
        ArchiverGBean archiver = new ArchiverGBean(serverInfo);
        File dest = archiver.archive("src/test/resources/archivertest", serverBase.getAbsolutePath(), new Artifact("foo","bar", "1.0", "zip"));
        assertTrue(dest.exists());
        assertEquals("bar-1.0-bin.zip", dest.getName());
    }
}
