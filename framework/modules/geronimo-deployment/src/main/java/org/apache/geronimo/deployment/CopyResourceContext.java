/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;

class CopyResourceContext implements ResourceContext {
    private final DeploymentContext deploymentContext;
    private final byte[] buffer = new byte[4096];
    private File baseDir;

    public CopyResourceContext(DeploymentContext deploymentContext, File baseDir) throws DeploymentException {
        this.deploymentContext = deploymentContext;
        if (baseDir.isFile()) {
            deploymentContext.addToClassPath("");
        }
        this.baseDir = baseDir;
    }

    /**
     * Copy a packed jar file into the deployment context and place it into the
     * path specified in the target path.  The newly added packed jar is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the packed jar file should be placed
     * @param jarFile    the jar file to copy
     * @throws IOException if there's a problem copying the jar file
     */
    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        if (targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        JarUtils.copyToPackedJar(jarFile, targetFile);

        if (!targetFile.isFile()) throw new IllegalStateException("target file should be a file: " + targetFile);
        deploymentContext.addToClassPath(targetPath.toString());
    }

    /**
     * Copy a ZIP file entry into the deployment context and place it into the
     * path specified in the target path.  The newly added entry is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the ZIP file entry should be placed
     * @param zipFile    the ZIP file
     * @param zipEntry   the ZIP file entry
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
//        if (!targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, zipFile, zipEntry);

//        if (!targetFile.isDirectory()) throw new IllegalStateException("target file should be a directory: " + targetFile);
        deploymentContext.addToClassPath(targetPath.toString());
    }

    /**
     * Copy a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the file should be placed
     * @param source     the URL of file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, URL source) throws IOException {
        if (targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);

        if (!targetFile.isFile()) throw new IllegalStateException("target file should be a file: " + targetFile);
        deploymentContext.addToClassPath(targetPath.toString());
    }

    /**
     * Copy a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the file should be placed
     * @param source     the file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, File source) throws IOException {
        if (targetPath.getPath().endsWith("/")) throw new IllegalStateException("target path must not end with a '/' character: " + targetPath);

        File targetFile = getTargetFile(targetPath);
        addFile(targetFile, source);

        if (!targetFile.isFile()) throw new IllegalStateException("target file should be a file: " + targetFile);
        deploymentContext.addToClassPath(targetPath.toString());
    }

    public void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        addFile(getTargetFile(targetPath), zipFile, zipEntry);
    }

    public void addFile(URI targetPath, URL source) throws IOException {
        addFile(getTargetFile(targetPath), source);
    }

    public void addFile(URI targetPath, File source) throws IOException {
        addFile(getTargetFile(targetPath), source);
    }

    public void addFile(URI targetPath, String source) throws IOException {
        addFile(getTargetFile(targetPath), new ByteArrayInputStream(source.getBytes()));
    }

    public void addFile(URI targetPath, byte[] contents) throws IOException {
        File file = getTargetFile(targetPath);
        if (!file.getParentFile().mkdirs()) {
            throw new IOException("Could not create parent directory for entry: " + targetPath + " at " + file.getParentFile());
        }
        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(contents);
        } finally {
            IOUtils.close(out);
        }
    }

    public File getTargetFile(URI targetPath) {
        if (targetPath == null) throw new NullPointerException("targetPath is null");
        if (targetPath.isAbsolute()) throw new IllegalArgumentException("targetPath is absolute");
        if (targetPath.isOpaque()) throw new IllegalArgumentException("targetPath is opaque");
        return new File(baseDir, targetPath.toString());
    }

    public URL getTargetURL(URI targetPath) {
        File file = getTargetFile(targetPath);
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            // should not happen            
            throw new RuntimeException("Malformed URL", e);
        }
    }
    
    public void flush() throws IOException {
    }

    private void addFile(File targetFile, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        if (zipEntry.isDirectory()) {
            targetFile.mkdirs();
        } else {
            InputStream is = zipFile.getInputStream(zipEntry);
            try {
                addFile(targetFile, is);
            } finally {
                IOUtils.close(is);
            }
        }
    }

    private void addFile(File targetFile, URL source) throws IOException {
        InputStream in = null;
        try {
            in = source.openStream();
            addFile(targetFile, in);
        } finally {
            IOUtils.close(in);
        }
    }

    private void addFile(File targetFile, File source) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(source);
            addFile(targetFile, in);
        } finally {
            IOUtils.close(in);
        }
    }

    private void addFile(File targetFile, InputStream source) throws IOException {
        targetFile.getParentFile().mkdirs();
        OutputStream out = null;
        try {
            out = new FileOutputStream(targetFile);
            int count;
            while ((count = source.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } finally {
            IOUtils.close(out);
        }
    }
}