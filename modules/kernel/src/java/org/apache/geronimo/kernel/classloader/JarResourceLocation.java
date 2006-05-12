/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.kernel.classloader;

import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;

/**
 * @version $Rev$ $Date$
 */
public class JarResourceLocation extends AbstractUrlResourceLocation  {
    private final JarFile jarFile;

    public JarResourceLocation(URL codeSource, JarFile jarFile) {
        super(codeSource);
        this.jarFile = jarFile;
    }

    public ResourceHandle getResourceHandle(String resourceName) {
        JarEntry jarEntry = jarFile.getJarEntry(resourceName);
        if (jarEntry != null) {
            try {
                URL url = new URL(getCodeSource(), resourceName);
                return new JarResourceHandle(jarFile, jarEntry, getCodeSource());
            } catch (MalformedURLException e) {
            }
        }
        return null;
    }

    public Manifest getManifest() throws IOException {
        return jarFile.getManifest();
    }

    public void close() {
        IoUtil.close(jarFile);
    }
}
