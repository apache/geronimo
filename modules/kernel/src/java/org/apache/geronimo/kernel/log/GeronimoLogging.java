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

package org.apache.geronimo.kernel.log;

import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoLogging {
    public static final GeronimoLogging TRACE = new GeronimoLogging("TRACE");
    public static final GeronimoLogging DEBUG = new GeronimoLogging("DEBUG");
    public static final GeronimoLogging INFO = new GeronimoLogging("INFO");
    public static final GeronimoLogging WARN = new GeronimoLogging("WARN");
    public static final GeronimoLogging ERROR = new GeronimoLogging("ERROR");
    public static final GeronimoLogging FATAL = new GeronimoLogging("FATAL");

    private static boolean initialized = false;
    private static GeronimoLogging defaultLevel;

    /**
     * Initializes the logging system used by Geronimo.  This MUST be called in
     * in the main class used to start the geronimo server.  This method forces
     * commons logging to use GeronimoLogFactory, starts the initial commons-logging
     * logging system, and forces mx4j to use commons logging.
     */
    public static void initialize(GeronimoLogging level) {
        if (!initialized) {
            defaultLevel = level;

            // force commons-logging to use our log factory
            System.setProperty(LogFactory.FACTORY_PROPERTY, GeronimoLogFactory.class.getName());

            // force the log factory to initialize
            LogFactory.getLog(GeronimoLogging.class);

            // force mx4j to use commons logging
            // todo do this with reflection so mx4j is not required (this is important in JDK 1.5)
            mx4j.log.Log.redirectTo(new mx4j.log.CommonsLogger());

            initialized = true;
        }

    }

    public static GeronimoLogging getDefaultLevel() {
        return defaultLevel;
    }

    private final String level;

    private GeronimoLogging(String level) {
        this.level = level;
    }

    public String toString() {
        return level;
    }

    public boolean equals(Object object) {
        return object == this;
    }
}
