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

package org.apache.geronimo.jmxdebug.web.velocity;

import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeConstants;

import javax.servlet.ServletContext;

/**
 * Simple wrapper for the servlet log.
 *
 * @version $Rev$ $Date$
 */
public class ServletLogger {
    private ServletContext servletContext = null;

    private static final String PREFIX = " Velocity ";

    public ServletLogger(ServletContext sc) {
        servletContext = sc;
    }

    /**
     * init()
     */
    public void init(RuntimeServices rs)
            throws Exception {
    }

    /**
     * Send a log message from Velocity.
     */
    public void logVelocityMessage(int level, String message) {
        
        switch (level) {
            case LogSystem.WARN_ID:
                servletContext.log(PREFIX + RuntimeConstants.WARN_PREFIX + message);
                break;
            case LogSystem.INFO_ID:
                servletContext.log(PREFIX + RuntimeConstants.INFO_PREFIX + message);
                break;
            case LogSystem.DEBUG_ID:
                servletContext.log(PREFIX + RuntimeConstants.DEBUG_PREFIX + message);
                break;
            case LogSystem.ERROR_ID:
                servletContext.log(PREFIX + RuntimeConstants.ERROR_PREFIX + message);
                break;
            default:
                servletContext.log(PREFIX + " : " + message);
                break;
        }
    }
}
