/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.system.serverinfo;

import java.util.Properties;

/**
 * Information about this build of the server.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:14 $
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
        try {
            versionInfo.load(ServerConstants.class.getClassLoader().getResourceAsStream("org/apache/geronimo/system/serverinfo/geronimo-version.properties"));
        } catch (java.io.IOException e) {
            throw new ExceptionInInitializerError(new Exception("Could not load geronim-version.properties", e));
        }
        VERSION = versionInfo.getProperty("version");
        if (VERSION == null || VERSION.length() == 0) {
            throw new ExceptionInInitializerError("geronimo-version.properties does not contain a 'version' property");
        }

        BUILD_DATE = versionInfo.getProperty("build.date");
        if (BUILD_DATE == null || BUILD_DATE.length() == 0) {
            throw new ExceptionInInitializerError("geronimo-version.properties does not contain a 'build.date' property");
        }

        BUILD_TIME = versionInfo.getProperty("build.time");
        if (BUILD_TIME == null || BUILD_TIME.length() == 0) {
            throw new ExceptionInInitializerError("geronimo-version.properties does not contain a 'build.time' property");
        }

        COPYRIGHT = versionInfo.getProperty("copyright");
        if (COPYRIGHT == null || COPYRIGHT.length() == 0) {
            throw new ExceptionInInitializerError("geronimo-version.properties does not contain a 'copyright' property");
        }
    }
}
