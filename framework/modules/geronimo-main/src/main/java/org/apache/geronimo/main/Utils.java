/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.main;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URI;

public class Utils {

    public static final String SERVER_NAME_SYS_PROP = "org.apache.geronimo.server.name";
    public static final String SERVER_DIR_SYS_PROP = "org.apache.geronimo.server.dir";
    public static final String HOME_DIR_SYS_PROP = "org.apache.geronimo.home.dir";
    
    public static File getGeronimoHome() throws IOException {
        File rc = null;

        // Use the system property if specified.
        String path = System.getProperty(HOME_DIR_SYS_PROP);
        if (path != null) {
            rc = validateDirectoryExists(path, "Invalid " + HOME_DIR_SYS_PROP + " system property");
        }

        // Try to figure it out using the jar file this class was loaded from.
        if (rc == null) {
            // guess the home from the location of the jar
            URL url = Main.class.getClassLoader().getResource(Main.class.getName().replace(".", "/") + ".class");
            if (url != null) {
                try {
                    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                    url = jarConnection.getJarFileURL();
                    rc = new File(new URI(url.toString())).getCanonicalFile().getParentFile().getParentFile();
                } catch (Exception ignored) {
                }
            }
        }

        if (rc == null) {
            throw new IOException("The Geronimo install directory could not be determined.  Please set the " + HOME_DIR_SYS_PROP + " system property");
        }

        return rc;
    }

    public static File getGeronimoBase(File base) {
        File baseServerDir;
        
        // first check if the base server directory has been provided via
        // system property override.
        String baseServerDirPath = System.getProperty(SERVER_DIR_SYS_PROP);
        if (baseServerDirPath == null) {
            // then check if a server name has been provided
            String serverName = System.getProperty(SERVER_NAME_SYS_PROP);
            if (serverName == null) {
                // default base server directory.
                baseServerDir = base;
            } else {
                baseServerDir = new File(base, serverName);
            }
        } else {
            baseServerDir = new File(baseServerDirPath);
            if (!baseServerDir.isAbsolute()) {
                baseServerDir = new File(base, baseServerDirPath);
            }
        }

        validateDirectoryExists(baseServerDir, "The Geronimo server directory could not be determined");
        
        return baseServerDir;
    }
    
    public static File getTempDirectory(File base) {
        String tmpDirPath = System.getProperty("java.io.tmpdir", "temp");
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.isAbsolute()) {
            tmpDir = new File(base, tmpDirPath);
        }
        
        validateDirectoryExists(tmpDir, "The temporary directory could not be determined");
        
        return tmpDir;
    }
    
    public static File getLog4jConfigurationFile(File base, String defaultFile) {
        File log4jFile = null;
        
        String log4jFilePath = System.getProperty("org.apache.geronimo.log4jservice.configuration", defaultFile);       
        if (log4jFilePath != null) {                   
            log4jFile = new File(log4jFilePath);
            if (!log4jFile.isAbsolute()) {
                log4jFile = new File(base, log4jFilePath);
            }
        }
        
        return log4jFile;
    }
    
    public static File validateDirectoryExists(String path, String errPrefix) {
        return validateDirectoryExists(new File(path), errPrefix);
    }
    
    public static File validateDirectoryExists(File path, String errPrefix) {
        File rc;
        try {
            rc = path.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(errPrefix + " '" + path + "' : " + e.getMessage());
        }
        if (!rc.exists()) {
            throw new IllegalArgumentException(errPrefix + " '" + path + "' : does not exist");
        }
        if (!rc.isDirectory()) {
            throw new IllegalArgumentException(errPrefix + " '" + path + "' : is not a directory");
        }
        return rc;
    }
  
}
