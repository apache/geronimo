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

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;

/**
 * @version $Revision: 1.4 $ $Date: 2004/02/20 17:25:12 $
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

    private static final GBeanInfo GBEAN_INFO;

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
