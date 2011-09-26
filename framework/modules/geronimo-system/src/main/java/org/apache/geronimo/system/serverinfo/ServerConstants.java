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

import java.io.InputStream;
import java.util.Properties;

import org.apache.geronimo.kernel.util.IOUtils;

/**
 * Information about this build of the server.
 *
 * @version $Rev$ $Date$
 */
public class ServerConstants {
    private static final String VERSION;
    private static final String BUILD_DATE;
    private static final String BUILD_TIME;
    private static final String COPYRIGHT;

    /**
     * Gets the server version
     * @return version of the server
     */
    public static String getVersion() {
        return VERSION;
    }

    /**
     * Gets the date the server was built
     * @return date of the server build
     */
    public static String getBuildDate() {
        return BUILD_DATE;
    }

    /**
     * Gets the time the server was built
     * @return time of the server build
     */
    public static String getBuildTime() {
        return BUILD_TIME;
    }

    /**
     * Gets the copyright message for the server
     * @return
     */
    public static String getCopyright() {
        return COPYRIGHT;
    }

    /**
     * load all of the properties from the geronimo-version.properties file, which is generated during the build
     */
    static {
        Properties versionInfo = new Properties();
        InputStream input = null;
        try {
            input = ServerConstants.class.getClassLoader().getResourceAsStream("org/apache/geronimo/system/serverinfo/geronimo-version.properties");
            if (input == null) {
                throw new Error("Missing geronimo-version.properties");
            }

            versionInfo.load(input);
        } catch (java.io.IOException e) {
            throw new Error("Could not load geronimo-version.properties", e);
        } finally {
            IOUtils.close(input);
        }

        VERSION = versionInfo.getProperty("version");
        if (VERSION == null || VERSION.length() == 0) {
            throw new Error("geronimo-version.properties does not contain a 'version' property");
        }

        BUILD_DATE = versionInfo.getProperty("build.date");
        if (BUILD_DATE == null || BUILD_DATE.length() == 0) {
            throw new Error("geronimo-version.properties does not contain a 'build.date' property");
        }

        BUILD_TIME = versionInfo.getProperty("build.time");
        if (BUILD_TIME == null || BUILD_TIME.length() == 0) {
            throw new Error("geronimo-version.properties does not contain a 'build.time' property");
        }

        COPYRIGHT = versionInfo.getProperty("copyright");
        if (COPYRIGHT == null || COPYRIGHT.length() == 0) {
            throw new Error("geronimo-version.properties does not contain a 'copyright' property");
        }
    }
}
