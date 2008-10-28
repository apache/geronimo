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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Contains information about the server and functions for resolving
 * pathnames.
 *
 * @version $Rev$ $Date$
 */
public class BasicServerInfo implements ServerInfo {
    public static final String SERVER_NAME_SYS_PROP = "org.apache.geronimo.server.name";
    public static final String SERVER_DIR_SYS_PROP = "org.apache.geronimo.server.dir";
    public static final String HOME_DIR_SYS_PROP = "org.apache.geronimo.home.dir";
    
    private final String baseDirectory;
    private final File base;
    private final File baseServer;
    private final URI baseURI;
    private final URI baseServerURI;

    public BasicServerInfo() {
        baseDirectory = null;
        base = null;
        baseServer = null;
        baseURI = null;
        baseServerURI = null;
    }

    public BasicServerInfo(String defaultBaseDirectory) throws Exception {
        this(defaultBaseDirectory, true);
    }

    public BasicServerInfo(String defaultBaseDirectory, boolean useSystemProperties) throws Exception {
        // Before we try the persistent value, we always check the
        // system properties first.  This lets an admin override this
        // on the command line.
        this.baseDirectory = useSystemProperties? System.getProperty(HOME_DIR_SYS_PROP, defaultBaseDirectory): defaultBaseDirectory;

        // force load of server constants
        ServerConstants.getVersion();

        if (baseDirectory == null || baseDirectory.length() == 0) {
            base = DirectoryUtils.getGeronimoInstallDirectory();
            if (base == null) {
                throw new IllegalArgumentException("Could not determine geronimo installation directory");
            }
        } else {
            base = new File(baseDirectory);
        }

        if (!base.isDirectory()) {
            throw new IllegalArgumentException("Base directory is not a directory: " + baseDirectory);
        }

        baseURI = base.toURI();
        baseServer = deriveBaseServer(useSystemProperties);
        baseServerURI = baseServer.toURI();
        if (useSystemProperties) {
            System.setProperty(HOME_DIR_SYS_PROP, base.getAbsolutePath());
            System.setProperty(SERVER_DIR_SYS_PROP, baseServer.getAbsolutePath());
        }
        String tmpDir = resolveServerPath(System.getProperty("java.io.tmpdir"));       
        System.setProperty("java.io.tmpdir", tmpDir);
    }

    public BasicServerInfo(String baseDirectory, String serverName) {
        this.baseDirectory = baseDirectory;
        this.base = new File(baseDirectory);
        this.baseURI = base.toURI();
        this.baseServerURI = baseURI.resolve(serverName);
        this.baseServer = new File(baseServerURI);
    }

    /**
     * Resolves an abstract pathname to an absolute one.
     *
     * @param filename a pathname that can either be
     * fully-qualified (i.e. starts with a "/") or
     * relative (i.e. starts with any character but "/").  If it's
     * fully-qualified it will be resolved to an absolute pathname
     * using system-dependent rules (@link java.io.File). If it's relative
     * it will be resolved relative to the base directory.
     * @return an absolute pathname
     * @see java.io.File#File(String pathname)
     * @see java.io.File#getAbsolutePath()
     */
    public String resolvePath(final String filename) {
        return resolve(filename).getAbsolutePath();
    }

    public String resolveServerPath(String filename) {
        return resolveServer(filename).getAbsolutePath();
    }
    
    /**
     * Resolves an abstract pathname to a File.
     *
     * @param filename a <code>String</code> containing a pathname,
     * which will be resolved by {@link #resolvePath(String
            * filename)}.
     * @return a <code>File</code> value
     */
    public File resolve(final String filename) {
        return resolveWithBase(base, filename);
    }

    public File resolveServer(String filename) {
        return resolveWithBase(baseServer, filename);
    }

    public URI resolve(final URI uri) {
        return baseURI.resolve(uri);
    }

    public URI resolveServer(URI uri) {
        return baseServerURI.resolve(uri);
    }
    
    public String getBaseDirectory() {
        return baseDirectory;
    }

    public String getCurrentBaseDirectory() {
        return base.getAbsolutePath();
    }

    public String getVersion() {
        return ServerConstants.getVersion();
    }

    public String getBuildDate() {
        return ServerConstants.getBuildDate();
    }

    public String getBuildTime() {
        return ServerConstants.getBuildTime();
    }

    public String getCopyright() {
        return ServerConstants.getCopyright();
    }

    private File resolveWithBase(File baseDir, String filename) {
        File file = new File(filename);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(baseDir, filename);
    }

    private File deriveBaseServer(boolean useSystemProperties) {
        File baseServerDir;
        
        // first check if the base server directory has been provided via
        // system property override.
        String baseServerDirPath = System.getProperty(SERVER_DIR_SYS_PROP);
        if (!useSystemProperties || null == baseServerDirPath) {
            // then check if a server name has been provided
            String serverName = System.getProperty(SERVER_NAME_SYS_PROP);
            if (!useSystemProperties || null == serverName) {
                // default base server directory.
                baseServerDir = base;
            } else {
                baseServerDir = new File(base, serverName);
            }
        } else {
            baseServerDir = new File(baseServerDirPath);
            if (!baseServerDir.isAbsolute()) {
                baseServerDir = new File(base, baseServerDirPath);
            }
        }

        if (!baseServerDir.isDirectory()) {
            throw new IllegalArgumentException("Server directory is not a directory: " + baseServerDir);
        }
        
        return baseServerDir;
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(BasicServerInfo.class);

        infoFactory.addAttribute("baseDirectory", String.class, true);

        infoFactory.addInterface(ServerInfo.class);

        infoFactory.setConstructor(new String[]{"baseDirectory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
