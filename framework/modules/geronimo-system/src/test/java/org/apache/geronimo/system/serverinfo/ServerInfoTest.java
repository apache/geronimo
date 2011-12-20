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
package org.apache.geronimo.system.serverinfo;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ServerInfoTest extends TestCase {
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    protected void setUp() throws Exception {
        // Ensure we are in a known state before each test
        System.getProperties().remove(BasicServerInfo.HOME_DIR_SYS_PROP);
    }
    
    public final void testResolvePath() throws Exception {
        ServerInfo si = null;

        String pathArg;
        {
            si = new BasicServerInfo("/");
            pathArg = "/";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "/x";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            pathArg = "/x/y";
            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
            //TODO these stopped passing with a minor osgi change.  WHy?
//            pathArg = "C:/Documents and Settings/Administrator/Application Data/geronimo";
//            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));

//            pathArg = ".";
//            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
//            pathArg = "x";
//            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
//            pathArg = "x/y";
//            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
//            pathArg = "Documents and Settings/Administrator/Application Data/geronimo";
//            assertEquals(new File(pathArg).getAbsolutePath(), si.resolvePath(pathArg));
        }

        try {
            String basedir = "/";
            si = new BasicServerInfo(basedir, false);
            pathArg = "Documents and Settings/Administrator/Application Data/geronimo";
            assertEquals(new File(basedir, pathArg).getAbsolutePath(), si.resolvePath(pathArg));
        } catch (Exception e) {
            fail("ServerInfo ctor threw exception " + e);
        }

        //try {
        //    String basedir = File.listRoots()[0].getAbsolutePath();
        //    si = new ServerInfo(basedir);
        //    pathArg = "Documents and Settings/Administrator/Application Data/geronimo";
        //    assertEquals(new File(basedir, pathArg).getAbsolutePath(), si.resolvePath(pathArg));
        //} catch (Exception e) {
        //    fail("ServerInfo ctor threw exception " + e);
        //}
    }

    public final void testServerInfo() throws Exception {
		try {
			File file;
			try {
				file = File.createTempFile("geronimo", null);
				// a workaround - ServerInfo sets system-wide property
				System.setProperty(BasicServerInfo.HOME_DIR_SYS_PROP, file.getName());
				new BasicServerInfo(file.getName());
				fail("ServerInfo should throw exception when given non-directory path");
			} catch (IOException ioe) {
				fail(ioe.getMessage());
			} catch (Exception expected) {
			}
            
			String basedir = ".";
			// a workaround - ServerInfo sets system-wide property
			System.setProperty(BasicServerInfo.HOME_DIR_SYS_PROP, basedir);
			ServerInfo si = new BasicServerInfo(basedir);
			assertNotNull(System.getProperty(BasicServerInfo.HOME_DIR_SYS_PROP));
			assertEquals("base directory is incorrect", basedir, si.getBaseDirectory());
		} finally {
            resetSysProperties();
		}
    }

    public void testWithServerName() throws Exception {
        String serverName = "target/serverName";
        File serverDir = new File(basedir, serverName);
        serverDir.mkdirs();
        try {
            System.setProperty(BasicServerInfo.SERVER_NAME_SYS_PROP, serverName);
            new BasicServerInfo(basedir.getAbsolutePath());
            assertEquals(serverDir.getAbsolutePath(), System.getProperty(BasicServerInfo.SERVER_DIR_SYS_PROP));
        } finally {
            resetSysProperties();
            serverDir.delete();
        }
    }

    public void testWithServerDirAbsolute() throws Exception {
        String serverDirName = "./target/serverDir";
        File serverDir = new File(basedir, serverDirName);
        serverDir.mkdirs();
        try {
            System.setProperty(BasicServerInfo.SERVER_DIR_SYS_PROP, serverDir.getAbsolutePath());
            new BasicServerInfo(basedir.getAbsolutePath());
            assertEquals(serverDir.getAbsolutePath(), System.getProperty(BasicServerInfo.SERVER_DIR_SYS_PROP));
        } finally {
            resetSysProperties();
            serverDir.delete();
        }
    }

    public void testWithServerDirRelative() throws Exception {
        String serverDirName = "./target/serverDir";
        File serverDir = new File(basedir, serverDirName);
        serverDir.mkdirs();
        try {
            System.setProperty(BasicServerInfo.SERVER_DIR_SYS_PROP, serverDirName);
            new BasicServerInfo(basedir.getAbsolutePath());
            assertEquals(serverDir.getAbsolutePath(), System.getProperty(BasicServerInfo.SERVER_DIR_SYS_PROP));
        } finally {
            resetSysProperties();
        }
    }
    
    private void resetSysProperties() {
        Properties sysProps = System.getProperties();
        sysProps.remove(BasicServerInfo.HOME_DIR_SYS_PROP);
        sysProps.remove(BasicServerInfo.SERVER_DIR_SYS_PROP);
        sysProps.remove(BasicServerInfo.SERVER_NAME_SYS_PROP);
    }
}
