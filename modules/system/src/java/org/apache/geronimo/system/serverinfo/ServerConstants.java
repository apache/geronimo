/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.system.serverinfo;

import java.util.Properties;

/**
 * Information about this build of the server.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/12 18:12:52 $
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
