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
package org.apache.geronimo.jetty.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.xbeans.geronimo.deployment.jetty.JettyWebAppType;

/**
 *
 *
 * @version $Revision: 1.10 $ $Date: 2004/02/08 20:19:21 $
 */
public class JettyModule extends AbstractModule {
    private final File moduleDirectory;
    private final ZipInputStream zipArchive;
    private final boolean closeStream;
    private URI classes;
    private URI lib;

    public JettyModule(URI configID, InputStream moduleArchive, JettyWebAppType webApp) throws DeploymentException {
        super(configID);
        moduleDirectory = null;
        this.zipArchive = new ZipInputStream(moduleArchive);
        closeStream = false;
        contextPath = webApp.getContextRoot().getStringValue();
        if (contextPath == null) {
            throw new DeploymentException("No context root specified");
        }
        contextPriorityClassLoader=webApp.getContextPriorityClassloader();
    }

    public void init() throws DeploymentException {
        super.init();
        assert contextPath.startsWith("/") : "Did not ensure contextPath is absolute";
        uri = URI.create(contextPath.substring(1) + "/");
        classes = uri.resolve("WEB-INF/classes/");
        lib = uri.resolve("WEB-INF/lib/");
    }

    public void generateClassPath(ConfigurationCallback callback) throws DeploymentException {
        if (zipArchive != null) {
            // unpack archive into Configuration
            try {
                ZipEntry entry;
                boolean addedClasses= false;
                while ((entry= zipArchive.getNextEntry()) != null) {
                    String name= entry.getName();
                    if (name.endsWith("/")) {
                        continue;
                    }
                    callback.addFile(uri.resolve(name), zipArchive);

                    // If we do not give the context priority over classloading, then we add the standard locations to our classpath.
                    if (!contextPriorityClassLoader) {
                        if (!addedClasses && name.startsWith("WEB-INF/classes/")) {
                            callback.addToClasspath(classes);
                        }
                        else if (name.startsWith("WEB-INF/lib/")) {
                            if (name.indexOf('/', 12) == -1 && (name.endsWith(".jar") || name.endsWith(".zip"))) {
                                callback.addToClasspath(uri.resolve(name));
                            }
                        }
                    }
                }
            }
            catch (IOException e) {
                throw new DeploymentException("Unable to unpack WAR content", e);
            }
        }
        else {
            // copy directory into Configuration
            try {
                copyDir(callback, uri, moduleDirectory);
            }
            catch (IOException e) {
                throw new DeploymentException("Unable to copy archive directory", e);
            }
        }
    }

    private void copyDir(ConfigurationCallback callback, URI path, File dir) throws IOException {
        assert dir.isDirectory();
        File[] files = dir.listFiles();
        boolean libDir = lib.equals(path);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                URI subURI = path.resolve(file.getName() + "/");
                copyDir(callback, subURI, file);
                if (classes.equals(subURI)) {
                    callback.addToClasspath(classes);
                }
            } else {
                FileInputStream is = new FileInputStream(file);
                try {
                    URI subURI = path.resolve(file.getName());
                    callback.addFile(subURI, is);
                    if (libDir && (file.getName().endsWith(".jar") || file.getName().endsWith(".jar"))) {
                        callback.addToClasspath(subURI);
                    }
                } finally {
                    is.close();
                }
            }
        }
    }

    public void complete() {
        super.complete();
        if (zipArchive != null && closeStream) {
            try {
                zipArchive.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
