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

package org.apache.geronimo.connector.deployment;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.DeploymentHelper;
import org.apache.geronimo.kernel.deployment.scanner.URLType;

/**
 * Connector deployment helper. It allows to compute various information of
 * a URL to be deployed.
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/15 07:37:37 $
 */
public class ConnectorDeploymentHelper extends DeploymentHelper {

    public ConnectorDeploymentHelper(URL url, URLType urlType) throws DeploymentException {
        super(url, urlType, "Connector", ".rar", "ra.xml", "geronimo-ra.xml");
    }

    protected void findUnpackedArchives(List archives) throws DeploymentException {
        File rootDeploy = new File(url.getFile());
        File[] jarFiles = rootDeploy.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        });
        for (int i = 0; i < jarFiles.length; i++) {
            try {
                archives.add(jarFiles[i].toURL());
            } catch (MalformedURLException e) {
                throw new DeploymentException("Should never occurs", e);
            }
        }
        // TODO handle the .so and .dll entries.
    }

    protected void findPackedArchives(List archives) throws DeploymentException {
        String rootJar = "jar:" + url.toExternalForm();
        try {
            JarFile jFile = new JarFile(url.getFile());
            Enumeration entries = jFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jEntry = (JarEntry) entries.nextElement();
                if (jEntry.isDirectory()) {
                    continue;
                }
                if (jEntry.getName().endsWith(".jar")) {
                    archives.add(
                            new URL(rootJar + "!/" + jEntry.getName()));
                }
                // TODO handle the .so and .dll entries.
            }
        } catch (IOException e) {
            throw new DeploymentException("Should never occurs", e);
        }
    }


}
