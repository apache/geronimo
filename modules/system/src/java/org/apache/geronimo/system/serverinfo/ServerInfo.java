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
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;

/**
 * @version $Revision: 1.7 $ $Date: 2004/05/26 03:22:21 $
 */
public class ServerInfo {
    private final File base;
    private final URI baseURI;

    public ServerInfo() {
        base = null;
        baseURI = null;
    }

    public ServerInfo(String baseDirectory) throws Exception {
        // force load of server constants
        ServerConstants.getVersion();

        // Before we try the persistent value, we always check the
        // system properties first.  This lets an admin override this
        // on the command line.
        baseDirectory = System.getProperty("geronimo.base.dir", baseDirectory);
        if (baseDirectory == null || baseDirectory.length() == 0) {
            // guess from the location of the jar
            URL url = getClass().getClassLoader().getResource("META-INF/startup-jar");
            if (url == null) {
                throw new IllegalArgumentException("Unable to determine location of startup jar");
            }
            try {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                url = jarConnection.getJarFileURL();
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to extract base URL from location");
            }
            baseURI = new URI(url.toString()).resolve("..");
            base = new File(baseURI);
        } else {
            base = new File(baseDirectory);
            baseURI = base.toURI();
        }
        if (!base.isDirectory()) {
            throw new IllegalArgumentException("Base directory is not a directory: " + baseDirectory);
        }
    }

    public String resolvePath(final String filename) {
        File file = new File(base, filename);
        return file.getAbsolutePath();
    }

    public URI resolve(final URI uri) {
        return baseURI.resolve(uri);
    }

    public String getBaseDirectory() {
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

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServerInfo.class.getName());
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"BaseDirectory"},
                new Class[]{String.class}));
        infoFactory.addAttribute("BaseDirectory", true);
        infoFactory.addAttribute("Version", false);
        infoFactory.addAttribute("BuildDate", false);
        infoFactory.addAttribute("BuildTime", false);
        infoFactory.addAttribute("Copyright", false);
        infoFactory.addOperation("resolvePath", new Class[]{String.class});
        infoFactory.addOperation("resolve", new Class[]{URI.class});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
