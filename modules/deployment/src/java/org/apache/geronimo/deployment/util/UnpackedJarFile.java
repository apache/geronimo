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
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.net.URI;

/**
 * @version $Revision$ $Date$
 */
public class UnpackedJarFile extends JarFile {
    private static final File dummyJarFile;
    static {
        try {
            dummyJarFile = File.createTempFile("fake", null);
            new JarOutputStream(new FileOutputStream(dummyJarFile), new Manifest()).close();
            dummyJarFile.deleteOnExit();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final File baseDir;
    private boolean manifestLoaded = false;
    private Manifest manifest;

    public UnpackedJarFile(String name) throws IOException {
        this(new File(name));
    }

    public UnpackedJarFile(File baseDir) throws IOException {
        super(dummyJarFile);
        this.baseDir = baseDir;
        if (!baseDir.isDirectory()) {
            throw new IOException("File must be a directory: file=" + baseDir.getAbsolutePath());
        }
    }

    public Manifest getManifest() throws IOException {
        if (manifestLoaded) {
            File manifestFile = getFile("META-INF/MANIFEST.MF");

            if (manifestFile.isFile()) {
                FileInputStream in = null;
                try {
                    in = new FileInputStream(manifestFile);
                    manifest = new Manifest(in);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
            manifestLoaded = true;
        }
        return manifest;
    }

    public JarEntry getJarEntry(String name) {
        File file = getFile(name);
        if (file == null) {
            return null;
        }
        return new UnpackedJarEntry(name, file, getManifestSafe());
    }

    public ZipEntry getEntry(String name) {
        return getJarEntry(name);
    }

    public Enumeration entries() {
        Collection files = FileUtil.listRecursiveFiles(baseDir);

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
            file = getFile(zipEntry.getName());
        } else {
            file = ((UnpackedJarEntry)zipEntry).getFile();
        }

        if (file == null) {
            throw new IOException("Entry not found: name=" + file.getAbsolutePath());
        } else if (file.isDirectory()) {
            throw new IOException("Entry is a directory: name=" + file.getAbsolutePath());
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
    }

    protected void finalize() throws IOException {
    }

    private File getFile(String name) {
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
