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
import java.io.IOException;
import java.io.InputStream;
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
public class NestedJarFile extends JarFile {
    private final JarFile baseJar;
    private final String basePath;
    private boolean manifestLoaded = false;
    private Manifest manifest;

    public NestedJarFile(JarFile jarFile, String path) throws IOException {
        super(JarUtil.DUMMY_JAR_FILE);

        // verify that the jar actually contains that path
        JarEntry targetEntry = jarFile.getJarEntry(path + "/");
        if (targetEntry == null) {
            targetEntry = jarFile.getJarEntry(path);
            if (targetEntry == null) {
                throw new IOException("Jar entry does not exist: jarFile=" + jarFile.getName() + ", path=" + path);
            }
        }

        if (targetEntry.isDirectory()) {
            baseJar = jarFile;
            if (!path.endsWith("/")) {
                path += "/";
            }
            basePath = path;
        } else {
            if (targetEntry instanceof UnpackedJarEntry) {
                // for unpacked jars we don't need to copy the jar file
                // out to a temp directory, since it is already available
                // as a raw file
                File targetFile = ((UnpackedJarEntry) targetEntry).getFile();
                baseJar = new JarFile(targetFile);
                basePath = "";
            } else {
                File tempFile = FileUtil.toTempFile(jarFile.getInputStream(targetEntry), true);
                baseJar = new JarFile(tempFile);
                basePath = "";
            }
        }
    }

    public boolean isUnpacked() {
        return basePath.length() > 0;
    }

    public boolean isPacked() {
        return basePath.length() == 0;
    }

    public JarFile getBaseJar() {
        return baseJar;
    }

    public String getBasePath() {
        return basePath;
    }

    public Manifest getManifest() throws IOException {
        if (manifestLoaded) {
            JarEntry manifestEntry = getBaseEntry("META-INF/MANIFEST.MF");

            if (manifestEntry != null && !manifestEntry.isDirectory()) {
                InputStream in = null;
                try {
                    in = baseJar.getInputStream(manifestEntry);
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

    public NestedJarEntry getNestedJarEntry(String name) {
        JarEntry baseEntry = getBaseEntry(name);
        if (baseEntry == null) {
            return null;
        }
        return new NestedJarEntry(name, baseEntry, getManifestSafe());
    }

    public JarEntry getJarEntry(String name) {
        return getNestedJarEntry(name);
    }

    public ZipEntry getEntry(String name) {
        return getNestedJarEntry(name);
    }

    public Enumeration entries() {
        Collection baseEntries = Collections.list(baseJar.entries());
        Collection entries = new LinkedList();
        for (Iterator iterator = baseEntries.iterator(); iterator.hasNext();) {
            JarEntry baseEntry = (JarEntry) iterator.next();
            String path = baseEntry.getName();
            if (path.startsWith(basePath)) {
                entries.add(new NestedJarEntry(path.substring(basePath.length()), baseEntry, getManifestSafe()));
            }
        }
        return Collections.enumeration(entries);
    }

    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        JarEntry baseEntry;
        if (zipEntry instanceof NestedJarEntry) {
            baseEntry = ((NestedJarEntry)zipEntry).getBaseEntry();
        } else {
            baseEntry = getBaseEntry(zipEntry.getName());
        }

        if (baseEntry == null) {
            throw new IOException("Entry not found: name=" + baseEntry.getName());
        } else if (baseEntry.isDirectory()) {
            return new JarUtil.EmptyInputStream();
        }
        return baseJar.getInputStream(baseEntry);
    }

    public String getName() {
        return baseJar.getName();
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

    private JarEntry getBaseEntry(String name) {
        return baseJar.getJarEntry(basePath + name);
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
