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
public class ServerInfo {
    private final String baseDirectory;
    private final File base;
    private final URI baseURI;

    public ServerInfo() {
        baseDirectory = null;
        base = null;
        baseURI = null;
    }

    public ServerInfo(String baseDirectory) throws Exception {
        this.baseDirectory = baseDirectory;

        // force load of server constants
        ServerConstants.getVersion();

        // Before we try the persistent value, we always check the
        // system properties first.  This lets an admin override this
        // on the command line.
        baseDirectory = System.getProperty("geronimo.base.dir", baseDirectory);
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
        System.setProperty("geronimo.base.dir", base.getAbsolutePath());
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

    /**
     * Resolves an abstract pathname to a File.
     *
     * @param filename a <code>String</code> containing a pathname,
     * which will be resolved by {@link #resolvePath(String
            * filename)}.
     * @return a <code>File</code> value
     */
    public File resolve(final String filename) {
        File file = new File(filename);
        if (file.isAbsolute()) {
            return file;
        }
        return new File(base, filename);
    }

    public URI resolve(final URI uri) {
        return baseURI.resolve(uri);
    }

    public String getBaseDirectory() {
        return baseDirectory;
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ServerInfo.class);

        infoFactory.addAttribute("baseDirectory", String.class, true);
        infoFactory.addAttribute("version", String.class, false);
        infoFactory.addAttribute("buildDate", String.class, false);
        infoFactory.addAttribute("buildTime", String.class, false);
        infoFactory.addAttribute("copyright", String.class, false);

        infoFactory.addOperation("resolvePath", new Class[]{String.class});
        infoFactory.addOperation("resolve", new Class[]{String.class});
        infoFactory.addOperation("resolve", new Class[]{URI.class});

        infoFactory.setConstructor(new String[]{"baseDirectory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
