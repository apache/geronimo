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
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GOperationInfo;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/12 18:12:52 $
 */
public class ServerInfo {
    private String baseDirectory;
    private final File base;
    private final URI baseURI;

    public ServerInfo() {
        base = null;
        baseURI = null;
    }

    public ServerInfo(String baseDirectory) {
        // force load of server constants
        ServerConstants.getVersion();

        // Before we try the persistent value, we always check the
        // system properties first.  This lets an admin override this
        // on the command line.
        String systemProperty = System.getProperty("geronimo.base.dir");
        if (systemProperty != null && systemProperty.length() > 0) {
            this.baseDirectory = systemProperty;
        } else {
            // next try the persistent value
            if (baseDirectory != null && baseDirectory.length() > 0) {
                this.baseDirectory = baseDirectory;
            } else {
                // last chance - guess where the base directory shoul be
                throw new IllegalArgumentException("Could not find base directory. Please use the -Dgeronimo.base.dir=<your-directory> command line option.");
            }
        }

        // now that we have the base directory, check that it is a valid directory
        base = new File(this.baseDirectory);
        if (base.exists()) {
            if (!base.isDirectory()) {
                throw new IllegalArgumentException("Base directory is not a directory: " + this.baseDirectory);
            }
        } else {
            if (!base.mkdirs()) {
                throw new IllegalArgumentException("Could not create base directory: " + this.baseDirectory);
            }
        }
        baseURI = base.toURI();
    }

    public String resolvePath(final String filename) {
        File dir;
        File file;
        try {
            // uri supplied from the user
            URI logURI = new URI(filename);

            // if the log uri is relative, resolve it based on the baseDirectory
            logURI = new URI(baseDirectory.trim() + "/.").resolve(logURI);

            // the base directory may be relative to the server start directory
            logURI = new File(".").toURI().resolve(logURI);

            file = new File(logURI);
            dir = file.getParentFile();
        } catch (URISyntaxException e) {
            throw (IllegalArgumentException) new IllegalArgumentException().initCause(e);
        }

        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                throw new IllegalArgumentException("Failed to create directory structure: " + dir);
            }
        }
        return file.getAbsolutePath();
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

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServerInfo.class.getName());
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"BaseDirectory"},
                new Class[]{String.class}));
        infoFactory.addAttribute(new GAttributeInfo("BaseDirectory", true));
        infoFactory.addAttribute(new GAttributeInfo("Version"));
        infoFactory.addAttribute(new GAttributeInfo("BuildDate"));
        infoFactory.addAttribute(new GAttributeInfo("BuildTime"));
        infoFactory.addAttribute(new GAttributeInfo("Copyright"));
        infoFactory.addOperation(new GOperationInfo("resolvePath", new String[]{"java.lang.String"}));
        infoFactory.addOperation(new GOperationInfo("resolve", new Class[]{URI.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
