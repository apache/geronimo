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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.system.properties.JvmVendor;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains information about the server and functions for resolving
 * pathnames.
 *
 * @version $Rev$ $Date$
 */

@GBean
public class BasicServerInfo implements ServerInfo {
    
    private static final Logger LOG = LoggerFactory.getLogger(BasicServerInfo.class);
    
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
        this(defaultBaseDirectory, true, null);
    }

    public BasicServerInfo(String defaultBaseDirectory, boolean useSystemProperties) throws Exception {
        this(defaultBaseDirectory, useSystemProperties, null);
    }

    public BasicServerInfo(@ParamAttribute(name = "baseDirectory")String defaultBaseDirectory,
                           @ParamAttribute(name="useSystemProperties") boolean useSystemProperties,
                           @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws Exception {
        // Before we try the persistent value, we always check the
        // system properties first.  This lets an admin override this
        // on the command line.
        this.baseDirectory = useSystemProperties? System.getProperty(HOME_DIR_SYS_PROP, defaultBaseDirectory): defaultBaseDirectory;

        // force load of server constants
        ServerConstants.getVersion();

        if (baseDirectory == null || baseDirectory.length() == 0) {
            String karafHome = System.getProperty("karaf.home");
            if (karafHome == null) {
                throw new IllegalStateException("NO karaf.home specified");
            }
            this.base = new File(karafHome);
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
        
        logEnvInfo();
    }

    public BasicServerInfo(String baseDirectory, String serverName) {
        this.baseDirectory = baseDirectory;
        this.base = new File(baseDirectory);
        this.baseURI = base.toURI();
        this.baseServerURI = baseURI.resolve(serverName);
        this.baseServer = new File(baseServerURI);
        
        logEnvInfo();
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

    @Override
    public String[] getArgs() {
        throw new RuntimeException("not implemented");
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
    
    private void logEnvInfo() {
        try {
           LOG.info("Runtime Information:");
           LOG.info("  Install directory = " + base);
           LOG.info("  Server directory  = " + baseServer);
           LOG.info("  JVM in use        = " + JvmVendor.getJvmInfo());
           LOG.info("Java Information:");
           LOG.info("  System property [java.runtime.name]     = " + System.getProperty("java.runtime.name"));
           LOG.info("  System property [java.runtime.version]  = " + System.getProperty("java.runtime.version"));
           LOG.info("  System property [os.name]               = " + System.getProperty("os.name"));
           LOG.info("  System property [os.version]            = " + System.getProperty("os.version"));
           LOG.info("  System property [sun.os.patch.level]    = " + System.getProperty("sun.os.patch.level"));
           LOG.info("  System property [os.arch]               = " + System.getProperty("os.arch"));
           LOG.info("  System property [java.class.version]    = " + System.getProperty("java.class.version"));
           LOG.info("  System property [locale]                = " + System.getProperty("user.language") + "_" + System.getProperty("user.country"));
           LOG.info("  System property [unicode.encoding]      = " + System.getProperty("sun.io.unicode.encoding"));
           LOG.info("  System property [file.encoding]         = " + System.getProperty("file.encoding"));
           LOG.info("  System property [java.vm.name]          = " + System.getProperty("java.vm.name"));
           LOG.info("  System property [java.vm.vendor]        = " + System.getProperty("java.vm.vendor"));
           LOG.info("  System property [java.vm.version]       = " + System.getProperty("java.vm.version"));
           LOG.info("  System property [java.vm.info]          = " + System.getProperty("java.vm.info"));
           LOG.info("  System property [java.home]             = " + System.getProperty("java.home"));
           LOG.info("  System property [java.classpath]        = " + System.getProperty("java.classpath"));
           LOG.info("  System property [java.library.path]     = " + System.getProperty("java.library.path"));
           LOG.info("  System property [java.endorsed.dirs]    = " + System.getProperty("java.endorsed.dirs"));
           LOG.info("  System property [java.ext.dirs]         = " + System.getProperty("java.ext.dirs"));
           LOG.info("  System property [sun.boot.class.path]   = " + System.getProperty("sun.boot.class.path"));
           LOG.info("----------------------------------------------");
        } catch (Exception e) {
           System.err.println("Exception caught during logging of Runtime Information.  Exception=" + e.toString());
        }
     }
    
}
