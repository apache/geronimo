/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.mavenplugins.geronimo;

import org.apache.commons.logging.Log;

import java.io.Serializable;

//
// TODO: Move this to MojoSupport
//

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class MavenPluginLog
    implements Log, Serializable
{
    private static org.apache.maven.plugin.logging.Log log;

    public static void setLog(final org.apache.maven.plugin.logging.Log log) {
        assert log != null;
        
        MavenPluginLog.log = log;
    }

    public static org.apache.maven.plugin.logging.Log getLog() {
        if (log == null) {
            throw new RuntimeException("Maven plugin log delegate as not been initialized");
        }

        return log;
    }

    private String name;

    public MavenPluginLog(final String name) {
        assert name != null;
        
        this.name = name;
    }

    public boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return getLog().isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return getLog().isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return getLog().isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return getLog().isDebugEnabled();
    }

    public boolean isWarnEnabled() {
        return getLog().isWarnEnabled();
    }

    private String createMessage(final Object object) {
        if (isDebugEnabled()) {
            return "(" + name + ") " + object;
        }
        else {
            return String.valueOf(object);
        }
    }
    
    public void trace(Object object) {
        debug(object);
    }

    public void trace(Object object, Throwable throwable) {
        debug(object, throwable);
    }

    public void debug(Object object) {
        getLog().debug(createMessage(object));
    }

    public void debug(Object object, Throwable throwable) {
        getLog().debug(createMessage(object), throwable);
    }

    public void info(Object object) {
        getLog().info(createMessage(object));
    }

    public void info(Object object, Throwable throwable) {
        getLog().info(createMessage(object), throwable);
    }

    public void warn(Object object) {
        getLog().warn(createMessage(object));
    }

    public void warn(Object object, Throwable throwable) {
        getLog().warn(createMessage(object), throwable);
    }

    public void error(Object object) {
        getLog().error(createMessage(object));
    }

    public void error(Object object, Throwable throwable) {
        getLog().error(createMessage(object), throwable);
    }

    public void fatal(Object object) {
        error(object);
    }

    public void fatal(Object object, Throwable throwable) {
        error(object, throwable);
    }
}