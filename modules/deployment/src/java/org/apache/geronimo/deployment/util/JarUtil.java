/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.deployment.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * @version $Revision$ $Date$
 */
public class JarUtil {
    public static final File DUMMY_JAR_FILE;
    static {
        try {
            DUMMY_JAR_FILE = File.createTempFile("fake", null);
            new JarOutputStream(new FileOutputStream(DUMMY_JAR_FILE), new Manifest()).close();
            DUMMY_JAR_FILE.deleteOnExit();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static URL createJarURL(JarFile jarFile, String path) throws DeploymentException {
        try {
            if (jarFile instanceof UnpackedJarFile) {
                File baseDir = ((UnpackedJarFile) jarFile).getBaseDir();
                return new File(baseDir, path).toURL();
            } else {
                String urlString = "jar:" + new File(jarFile.getName()).toURL() + "!/" + path;
                return new URL(urlString);
            }
        } catch (MalformedURLException e) {
            throw new DeploymentException("Can not create URL", e);
        }
    }

    public static JarFile createJarFile(File jarFile) throws IOException {
        if (jarFile.isDirectory()) {
            return new UnpackedJarFile(jarFile);
        } else {
            return new JarFile(jarFile);
        }
    }
}
