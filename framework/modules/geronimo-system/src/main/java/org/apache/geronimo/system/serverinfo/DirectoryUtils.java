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
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public final class DirectoryUtils {
    private static final Logger log = LoggerFactory.getLogger(DirectoryUtils.class);
    private static final File geronimoInstallDirectory;

    static {
        // guess from the location of the jar
        URL url = DirectoryUtils.class.getClassLoader().getResource("META-INF/startup-jar");

        File directory = null;
        if (url != null) {
            try {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                url = jarConnection.getJarFileURL();

                URI baseURI = new URI(url.toString()).resolve("..");
                directory = new File(baseURI);
            } catch (Exception ignored) {
                log.error("Error while determining the installation directory of Apache Geronimo", ignored);
            }
        } else {
            log.error("Cound not determine the installation directory of Apache Geronimo, because the startup jar could not be found in the current class loader.");
        }
        geronimoInstallDirectory = directory;
    }

    public static File getGeronimoInstallDirectory() {
        return geronimoInstallDirectory;
    }
}
