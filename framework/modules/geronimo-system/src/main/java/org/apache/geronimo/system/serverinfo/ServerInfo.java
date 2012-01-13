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
import java.net.URI;

/**
 * Contains information about the server and functions for resolving
 * pathnames.
 *
 * @version $Rev$ $Date$
 */
public interface ServerInfo {
    public String resolvePath(final String filename);

    public String resolveServerPath(final String filename);

    /**
     * Resolves a relative pathname to a File, relative to the server
     * installation directory (e.g. "foo" becomes .../geronimo/foo)
     *
     * @param filename a <code>String</code> containing a pathname,
     * which will be resolved by {@link #resolvePath(String
            * filename)}.
     * @return a <code>File</code> value
     */
    public File resolve(final String filename);

    /**
     * Resolves a relative pathname to a File, relative to the server
     * configuration directory.  Normally, this is the same as the Geronimo
     * installation directory (e.g. "foo" becomes .../geronimo/foo).
     * However, you can pass command-line arguments to the server to relocate
     * this to a server-instance-specific directory (e.g. "var/foo" might
     * become .../geronimo/instance1/var/foo if the server was started with
     * the flag indicating that the server instance dir was "instance1").
     */
    public File resolveServer(final String filename);

    public URI resolve(final URI uri);

    public URI resolveServer(final URI uri);

    /**
     * A config.xml setting for the base directory.  This is normally
     * left null, which means the ServerInfo will use the Geronimo
     * install directory.
     */
    public String getBaseDirectory();

    /**
     * The base directory that this ServerInfo is actually using.
     */
    public String getCurrentBaseDirectory();

    /**
     * command line arguments
     */
    public String[] getArgs();

    public String getVersion();

    public String getBuildDate();

    public String getBuildTime();

    public String getCopyright();
}
