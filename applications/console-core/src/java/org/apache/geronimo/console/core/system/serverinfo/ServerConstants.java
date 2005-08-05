/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.core.system.serverinfo;

import java.util.Properties;

public class ServerConstants {

    private static final String PROPERTIES_FILE = "org/apache/geronimo/console/core/system/serverinfo/geronimo-version.properties";

    private static final String VERSION;

    private static final String BUILD_DATE;

    private static final String BUILD_TIME;

    private static final String COPYRIGHT;

    private static final String GERONIMO_BUILD_VERSION;

    private static final String GERONIMO_SPEC_VERSION;

    private static final String PORTAL_CORE_VERSION;

    /**
     * Gets the server version
     *
     * @return version of the server
     */
    public static String getVersion() {
        return VERSION;
    }

    public static String getGeronimoBuildVersion() {
        return GERONIMO_BUILD_VERSION;
    }

    public static String getGeronimoSpecVersion() {
        return GERONIMO_SPEC_VERSION;
    }

    public static String getPortalCoreVersion() {
        return PORTAL_CORE_VERSION;
    }

    /**
     * Gets the date the server was built
     *
     * @return date of the server build
     */
    public static String getBuildDate() {
        return BUILD_DATE;
    }

    /**
     * Gets the time the server was built
     *
     * @return time of the server build
     */
    public static String getBuildTime() {
        return BUILD_TIME;
    }

    /**
     * Gets the copyright message for the server
     *
     * @return
     */
    public static String getCopyright() {
        return COPYRIGHT;
    }

    /**
     * load all of the properties from the geronimo-version.properties file,
     * which is generated during the build
     */
    static {
        Properties versionInfo = new Properties();
        try {
            versionInfo.load(ServerConstants.class.getClassLoader()
                    .getResourceAsStream(PROPERTIES_FILE));
        } catch (java.io.IOException e) {
            throw new ExceptionInInitializerError(new Exception(
                    "Could not load geronimo-version.properties", e));
        }
        VERSION = versionInfo.getProperty("version");
        if (VERSION == null || VERSION.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'version' property");
        }

        BUILD_DATE = versionInfo.getProperty("build.date");
        if (BUILD_DATE == null || BUILD_DATE.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'build.date' property");
        }

        BUILD_TIME = versionInfo.getProperty("build.time");
        if (BUILD_TIME == null || BUILD_TIME.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'build.time' property");
        }

        COPYRIGHT = versionInfo.getProperty("copyright");
        if (COPYRIGHT == null || COPYRIGHT.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'copyright' property");
        }
        GERONIMO_BUILD_VERSION = versionInfo
                .getProperty("geronimo.build.version");
        if (GERONIMO_BUILD_VERSION == null || COPYRIGHT.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'copyright' property");
        }
        GERONIMO_SPEC_VERSION = versionInfo
                .getProperty("geronimo.spec.version");
        if (GERONIMO_SPEC_VERSION == null || COPYRIGHT.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'copyright' property");
        }
        PORTAL_CORE_VERSION = versionInfo.getProperty("portal.core.version");
        if (PORTAL_CORE_VERSION == null || COPYRIGHT.length() == 0) {
            throw new ExceptionInInitializerError(
                    "geronimo-version.properties does not contain a 'copyright' property");
        }

    }
}
