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
package org.apache.geronimo.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.geronimo.deployment.DeploymentException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/16 19:03:09 $
 */
public class AppClient implements AppClientMBean {
    private static final Class[] MAIN_ARGS = {String[].class};

    private final ClassLoader clientCL;

    private Method mainMethod;

    public AppClient(URL clientURL) throws DeploymentException {
        clientCL = new URLClassLoader(new URL[]{clientURL}, Thread.currentThread().getContextClassLoader());

        String mainClassName = null;
        try {
            mainClassName = getMainClass(clientURL);
            if (mainClassName == null) {
                throw new DeploymentException("No Main-Class defined in manifest for " + clientURL);
            }
        } catch (IOException e) {
            throw new DeploymentException("Unable to get Main-Class from manifest for " + clientURL, e);
        }

        try {
            Class mainClass = clientCL.loadClass(mainClassName);
            mainMethod = mainClass.getMethod("main", MAIN_ARGS);
        } catch (ClassNotFoundException e) {
            IllegalArgumentException ex = new IllegalArgumentException("Unable to load Main-Class " + mainClassName);
            ex.initCause(e);
            throw ex;
        } catch (NoSuchMethodException e) {
            IllegalArgumentException ex = new IllegalArgumentException("Main-Class " + mainClassName + " does not have a main method");
            ex.initCause(e);
            throw ex;
        }
    }

    private String getMainClass(URL url) throws IOException {
        Manifest manifest;
        if (url.toString().endsWith("/")) {
            // unpacked
            URL manifestURL = new URL(url, "META-INF/MANIFEST.MF");
            InputStream is = manifestURL.openStream();
            manifest = new Manifest(is);
            is.close();
        } else {
            URL jarURL = new URL("jar:" + url + "!/");
            JarURLConnection jarConn = (JarURLConnection) jarURL.openConnection();
            manifest = jarConn.getManifest();
        }
        Attributes attrs = manifest.getMainAttributes();
        return (String) attrs.get(Attributes.Name.MAIN_CLASS);
    }

    public void runMain(String[] args) throws InvocationTargetException {
        try {
            mainMethod.invoke(null, new Object[]{args});
        } catch (IllegalAccessException e) {
            IllegalStateException ex = new IllegalStateException("Unable to invoke main");
            ex.initCause(e);
            throw ex;
        }
    }
}
