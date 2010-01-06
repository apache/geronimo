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
    private JarFile baseJar;
    private String basePath;
    private boolean isClosed = false;
    private boolean manifestLoaded = false;
    private Manifest manifest;
    private File tempFile;

    public NestedJarFile(JarFile jarFile, String path) throws IOException {
        super(JarUtils.DUMMY_JAR_FILE);

        // verify that the jar actually contains that path
        JarEntry targetEntry = jarFile.getJarEntry(path + "/");
        if (targetEntry == null) {
            targetEntry = jarFile.getJarEntry(path);
            if (targetEntry == null) {
                throw new IOException("Jar entry does not exist: jarFile=" + jarFile.getName() + ", path=" + path);
            }
        }

        if (targetEntry.isDirectory()) {
        	if(targetEntry instanceof UnpackedJarEntry) {
        		//unpacked nested module inside unpacked ear
        		File targetFile = ((UnpackedJarEntry) targetEntry).getFile();
        		baseJar = new UnpackedJarFile(targetFile);
                basePath = "";
        	} else {
        		baseJar = jarFile;
        		if (!path.endsWith("/")) {
                    path += "/";
                }
                basePath = path;
        	}
        } else {
            if (targetEntry instanceof UnpackedJarEntry) {
                // for unpacked jars we don't need to copy the jar file
                // out to a temp directory, since it is already available
                // as a raw file
                File targetFile = ((UnpackedJarEntry) targetEntry).getFile();
                baseJar = new JarFile(targetFile);
                basePath = "";
            } else {
                tempFile = JarUtils.toFile(jarFile, targetEntry.getName());
                baseJar = new JarFile(tempFile);
                basePath = "";
            }
        }
    }

    public boolean isUnpacked() {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        return ( basePath.length() > 0 ) ||
               ( ( baseJar != null ) && ( baseJar instanceof UnpackedJarFile ) );
    }

    public boolean isPacked() {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        return ( basePath.length() == 0 ) &&
               ( ( baseJar == null ) || !( baseJar instanceof UnpackedJarFile ) );
    }

    public JarFile getBaseJar() {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }
        return baseJar;
    }

    public String getBasePath() {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }
        return basePath;
    }

    public Manifest getManifest() throws IOException {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        if (!manifestLoaded) {
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
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        JarEntry baseEntry = getBaseEntry(name);
        if (baseEntry == null) {
            return null;
        }
        return new NestedJarEntry(name, baseEntry, getManifestSafe());
    }

    public JarEntry getJarEntry(String name) {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        return getNestedJarEntry(name);
    }

    public ZipEntry getEntry(String name) {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        return getNestedJarEntry(name);
    }

    public Enumeration entries() {
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

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
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }

        JarEntry baseEntry;
        if (zipEntry instanceof NestedJarEntry) {
            baseEntry = ((NestedJarEntry)zipEntry).getBaseEntry();
        } else {
            baseEntry = getBaseEntry(zipEntry.getName());
        }

        if (baseEntry == null) {
            throw new IOException("Entry not found: name=" + zipEntry.getName());
        } else if (baseEntry.isDirectory()) {
            return new IOUtils.EmptyInputStream();
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
        if (isClosed) {
            throw new IllegalStateException("NestedJarFile is closed");
        }
        return -1;
    }

    public void close() throws IOException {
        if (isClosed) {
            return;
        }

        try {
            try {
                super.close();
            } catch(IOException ignored) {
            }
            if (baseJar != null && basePath.length() == 0) {
                // baseJar is created by us.  We should be closing it too.
                baseJar.close();
            }
        } finally {
            isClosed = true;
            baseJar = null;
            basePath = null;
            manifestLoaded = false;
            manifest = null;
            if (tempFile != null) {
                tempFile.delete();
                tempFile = null;
            }
        }
    }

    protected void finalize() throws IOException {
        close();
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
