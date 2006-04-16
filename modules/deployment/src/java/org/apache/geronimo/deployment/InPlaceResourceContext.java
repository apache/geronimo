/**
 *
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.config.Configuration;

class InPlaceResourceContext implements ResourceContext {
    private final Configuration configuration;
    private final URI inPlaceBaseConfigurationUri;
    
    public InPlaceResourceContext(Configuration configuration, File inPlaceBaseConfigurationDir) throws DeploymentException {
        this.configuration = configuration;
        this.inPlaceBaseConfigurationUri = inPlaceBaseConfigurationDir.toURI();

        if (inPlaceBaseConfigurationDir.isFile()) {
            try {
                configuration.addToClassPath(URI.create(""));
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }
    }

    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        configuration.addToClassPath(targetPath);
    }

    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        configuration.addToClassPath(targetPath);
    }

    public void addInclude(URI targetPath, URL source) throws IOException {
        configuration.addToClassPath(targetPath);
    }

    public void addInclude(URI targetPath, File source) throws IOException {
        configuration.addToClassPath(targetPath);
    }

    public void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
    }

    public void addFile(URI targetPath, URL source) throws IOException {
    }

    public void addFile(URI targetPath, File source) throws IOException {
    }

    public void addFile(URI targetPath, String source) throws IOException {
    }
    
    public File getTargetFile(URI targetPath) {
        if (targetPath == null) throw new NullPointerException("targetPath is null");
        if (targetPath.isAbsolute()) throw new IllegalArgumentException("targetPath is absolute");
        if (targetPath.isOpaque()) throw new IllegalArgumentException("targetPath is opaque");
        return new File(inPlaceBaseConfigurationUri.resolve(targetPath));
    }
}