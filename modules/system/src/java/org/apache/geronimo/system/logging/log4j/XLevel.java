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

package org.apache.geronimo.system.logging.log4j;

import org.apache.log4j.Level;

/**
 * Extention levels for Log4j
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:14 $
 */
public final class XLevel extends Level {
    public static final int TRACE_INT = Level.DEBUG_INT - 1;
    private static String TRACE_NAME = "TRACE";

    /**
     * The Log4j Level Object to use for trace level messages
     */
    public static final XLevel TRACE = new XLevel(TRACE_INT, TRACE_NAME, 7);

    protected XLevel(int level, String name, int syslogEquiv) {
        super(level, name, syslogEquiv);
    }

    /**
     * Convert the String argument to a level. If the conversion
     * fails then this method returns {@link #TRACE}.
     */
    public static Level toLevel(String name) {
        return toLevel(name, XLevel.TRACE);
    }

    /**
     * Convert the String argument to a level. If the conversion
     * fails, return the level specified by the second argument,
     * i.e. defaultValue.
     */
    public static Level toLevel(String name, Level defaultValue) {
        if (name == null) {
            return defaultValue;
        }
        if (name.toUpperCase().equals(TRACE_NAME)) {
            return XLevel.TRACE;
        }
        return Level.toLevel(name, defaultValue);
    }

    /**
     * Convert an integer passed as argument to a level. If the
     * conversion fails, then this method returns {@link #DEBUG}.
     */
    public static Level toLevel(int level) throws IllegalArgumentException {
        if (level == TRACE_INT) {
            return XLevel.TRACE;
        } else {
            return Level.toLevel(level);
        }
    }
}
