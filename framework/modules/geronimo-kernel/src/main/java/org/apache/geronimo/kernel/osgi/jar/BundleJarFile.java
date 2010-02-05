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
package org.apache.geronimo.kernel.osgi.jar;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.kernel.util.JarUtils;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class BundleJarFile extends JarFile {
    
    private final Bundle bundle;
    private boolean manifestLoaded = false;
    private Manifest manifest;
    
    public BundleJarFile(Bundle bundle) throws IOException {
        super(JarUtils.DUMMY_JAR_FILE);
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public Manifest getManifest() throws IOException {        
        if (!manifestLoaded) {
            URL manifestURL = bundle.getEntry("META-INF/MANIFEST.MF");
            if (manifestURL != null) {
                InputStream in = null;
                try {
                    in = manifestURL.openStream();
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

    public BundleJarEntry getBundleJarEntry(String name) {
        URL url = bundle.getEntry(name);
        if (url == null) {
            return null;
        }
        return new BundleJarEntry(name, url, getManifestSafe());
    }

    public JarEntry getJarEntry(String name) {
        return getBundleJarEntry(name);
    }

    public ZipEntry getEntry(String name) {
        return getBundleJarEntry(name);
    }

    public Enumeration entries() {
        Manifest manifest = getManifestSafe();
        Enumeration e = bundle.findEntries("/", "*", true);
        LinkedList entries = new LinkedList();
        while (e.hasMoreElements()) {
            URL entryURL = (URL) e.nextElement();
            entries.add(new BundleJarEntry(entryURL.getPath(), entryURL, manifest));
        }
        return Collections.enumeration(entries);
    }

    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        BundleJarEntry entry;
        if (zipEntry instanceof BundleJarEntry) {
            entry = (BundleJarEntry) zipEntry;
        } else {
            entry = getBundleJarEntry(zipEntry.getName());
        }

        if (entry == null) {
            throw new IOException("Entry not found: name=" + zipEntry.getName());
        } else if (entry.isDirectory()) {
            return new IOUtils.EmptyInputStream();
        } else {
            return entry.getEntryURL().openStream();
        }
    }

    public String getName() {
        return bundle.getSymbolicName();
    }

    public int size() {
        return -1;
    }

    public void close() throws IOException {
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
