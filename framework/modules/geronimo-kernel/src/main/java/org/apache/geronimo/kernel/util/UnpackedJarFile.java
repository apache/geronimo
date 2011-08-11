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
package org.apache.geronimo.kernel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public class UnpackedJarFile extends JarFile {
    private final File baseDir;
    private boolean manifestLoaded = false;
    private Manifest manifest;

    public UnpackedJarFile(File baseDir) throws IOException {
        super(JarUtils.DUMMY_JAR_FILE);
        this.baseDir = baseDir;
        if (!baseDir.isDirectory()) {
            throw new IOException("File must be a directory: file=" + baseDir.getAbsolutePath());
        }
    }

    public File getBaseDir() {
        return baseDir;
    }

    public Manifest getManifest() throws IOException {
        if (!manifestLoaded) {
            File manifestFile = getFile("META-INF/MANIFEST.MF");

            if (manifestFile != null && manifestFile.isFile()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(manifestFile);
                    manifest = new Manifest(in);
                } finally {
                    IOUtils.close(in);
                }
            }
            manifestLoaded = true;
        }
        return manifest;
    }

    public UnpackedJarEntry getUnpackedJarEntry(String name) {
        File file = getFile(name);
        if (file == null) {
            return null;
        }
        return new UnpackedJarEntry(name, file, getManifestSafe());
    }

    public JarEntry getJarEntry(String name) {
        return getUnpackedJarEntry(name);
    }

    public ZipEntry getEntry(String name) {
        return getUnpackedJarEntry(name);
    }

    public Enumeration entries() {
        Collection files = FileUtils.listRecursiveFiles(baseDir);

        Manifest manifest = getManifestSafe();
        LinkedList entries = new LinkedList();
        URI baseURI = baseDir.getAbsoluteFile().toURI();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File entryFile = ((File) iterator.next()).getAbsoluteFile();
            URI entryURI = entryFile.toURI();
            URI relativeURI = baseURI.relativize(entryURI);
            entries.add(new UnpackedJarEntry(relativeURI.getPath(), entryFile, manifest));
        }
        return Collections.enumeration(entries);
    }

    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        File file;
        if (zipEntry instanceof UnpackedJarEntry) {
            file = ((UnpackedJarEntry)zipEntry).getFile();
        } else {
            file = getFile(zipEntry.getName());
        }

        if (file == null) {
            throw new IOException("Entry not found: name=" + zipEntry.getName());
        } else if (file.isDirectory()) {
            return new IOUtils.EmptyInputStream();
        }
        return new FileInputStream(file);
    }

    public String getName() {
        return baseDir.getAbsolutePath();
    }

    /**
     * Always returns -1.
     * @return -1
     */
    public int size() {
        return -1;
    }

    public void close() throws IOException {
        try {
            super.close();
        } catch(IOException ignored) {
        }
    }

    protected void finalize() throws IOException {
    }

    public File getFile(String name) {
        File file = new File(baseDir, name);
        if (!file.exists()) {
            return null;
        }
        return file;
    }

    private Manifest getManifestSafe() {
        Manifest manifest = null;
        try {
            manifest = getManifest();
        } catch (IOException e) {
            // ignore
        }
        return manifest;
    }
}
