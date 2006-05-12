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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.geronimo.kernel.classloader.util.ClassLoaderUtil;

/**
 * @version $Rev$ $Date$
 */
public class JarFileResourceFinder implements ResourceFinder {
    private final Object lock = new Object();

    private final LinkedHashSet urls = new LinkedHashSet();
    private final LinkedHashMap classPath = new LinkedHashMap();
    private final LinkedHashSet watchedFiles = new LinkedHashSet();

    private boolean destroyed = false;

    public JarFileResourceFinder() {
    }

    public JarFileResourceFinder(URL[] urls) {
        addUrls(urls);
    }

    public void destroy() {
        synchronized (lock) {
            if (destroyed) {
                return;
            }
            destroyed = true;
            urls.clear();
            for (Iterator iterator = classPath.values().iterator(); iterator.hasNext();) {
                JarFile jarFile = (JarFile) iterator.next();
                ClassLoaderUtil.close(jarFile);
            }
            classPath.clear();
        }
    }

    public void addUrl(URL url) {
        addUrls(Collections.singletonList(url));
    }

    public URL[] getUrls() {
        synchronized (lock) {
            return (URL[]) urls.toArray(new URL[urls.size()]);
        }
    }

    /**
     * Adds an array of urls to the end of this class loader.
     * @param urls the URLs to add
     */
    protected void addUrls(URL[] urls) {
        addUrls(Arrays.asList(urls));
    }

    /**
     * Adds a list of urls to the end of this class loader.
     * @param urls the URLs to add
     */
    protected void addUrls(List urls) {
        synchronized (lock) {
            boolean shouldRebuild = this.urls.addAll(urls);
            if (shouldRebuild) {
                rebuildClassPath();
            }
        }
    }

    private LinkedHashMap getClassPath() {
        assert Thread.holdsLock(lock): "This method can only be called while holding the lock";

        for (Iterator iterator = watchedFiles.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            if (file.canRead()) {
                rebuildClassPath();
                break;
            }
        }

        return classPath;
    }

    private void rebuildClassPath() {
        assert Thread.holdsLock(lock): "This method can only be called while holding the lock";

        Map existingJarFiles = new LinkedHashMap(classPath);
        classPath.clear();

        LinkedList locationStack = new LinkedList(urls);
        try {
            while (!locationStack.isEmpty()) {
                URL url = (URL) locationStack.removeFirst();

                if (!"file".equals(url.getProtocol())) {
                    // download the jar
                    throw new Error("Only local file jars are supported " + url);
                }

                if (classPath.containsKey(url)) {
                    continue;
                }

                File file = new File(url.getPath());
                if (!file.canRead()) {
                    // file most likely doesn't exist yet... watch to see if it appears later
                    watchedFiles.add(file);
                    continue;
                }

                // open the jar file
                JarFile jarFile = (JarFile) existingJarFiles.remove(url);
                if (jarFile == null) {
                    try {
                        jarFile = ClassLoaderUtil.createJarFile(file);
                    } catch (IOException ignored) {
                        // can't seem to open the file... this is most likely a bad jar file
                        // so don't keep a watch out for it because that would require lots of checking
                        // Dain: We may want to review this decision later
                        continue;
                    }
                }

                // add the jar to our class path
                classPath.put(url, jarFile);

                // push the manifest classpath on the stack (make sure to maintain the order)
                Manifest manifest = null;
                try {
                    manifest = jarFile.getManifest();
                } catch (IOException ignored) {
                }

                if (manifest != null) {
                    Attributes mainAttributes = manifest.getMainAttributes();
                    String manifestClassPath = mainAttributes.getValue(Attributes.Name.CLASS_PATH);
                    if (manifestClassPath != null) {
                        LinkedList classPathUrls = new LinkedList();
                        for (StringTokenizer tokenizer = new StringTokenizer(manifestClassPath, " "); tokenizer.hasMoreTokens();) {
                            String entry = tokenizer.nextToken();
                            File parentDir = file.getParentFile();
                            File entryFile = new File(parentDir, entry);

                            try {
                                classPathUrls.addLast(entryFile.toURL());
                            } catch (MalformedURLException ignored) {
                                // most likely a poorly named entry
                            }
                        }
                        locationStack.addAll(0, classPathUrls);
                    }
                }
            }
        } catch (Error e) {
            destroy();
            throw e;
        }

        for (Iterator iterator = existingJarFiles.values().iterator(); iterator.hasNext();) {
            JarFile jarFile = (JarFile) iterator.next();
            ClassLoaderUtil.close(jarFile);
        }
    }

    public ResourceHandle getResource(String resourceName) {
        synchronized (lock) {
            if (destroyed) {
                return null;
            }
            for (Iterator iterator = getClassPath().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                JarFile jarFile = (JarFile) entry.getValue();
                JarEntry jarEntry = jarFile.getJarEntry(resourceName);
                if (jarEntry != null && !jarEntry.isDirectory()) {
                    URL codeSource = (URL) entry.getKey();
                    try {
                        URL url = JarFileUrlStreamHandler.createUrl(jarFile, jarEntry);
                        return new JarResourceHandle(jarFile, jarEntry, url, codeSource);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public URL findResource(String resourceName) {
        synchronized (lock) {
            if (destroyed) {
                return null;
            }
            for (Iterator iterator = getClassPath().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                JarFile jarFile = (JarFile) entry.getValue();
                JarEntry jarEntry = jarFile.getJarEntry(resourceName);
                if (jarEntry != null) {
                    try {
                        return JarFileUrlStreamHandler.createUrl(jarFile, jarEntry);
                    } catch (MalformedURLException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public Enumeration findResources(String resourceName) {
        synchronized (lock) {
            return new ResourceEnumeration(new ArrayList(getClassPath().values()), resourceName);
        }
    }

    private static class ResourceEnumeration implements Enumeration {
        private Iterator iterator;
        private final String resourceName;
        private Object next;

        public ResourceEnumeration(Collection jarFiles, String resourceName) {
            this.iterator = jarFiles.iterator();
            this.resourceName = resourceName;
        }

        public boolean hasMoreElements() {
            fetchNext();
            return (next != null);
        }

        public Object nextElement() {
            fetchNext();

            // save next into a local variable and clear the next field
            Object next = this.next;
            this.next = null;

            // if we didn't have a next throw an exception
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next;
        }

        private void fetchNext() {
            if (iterator == null) {
                return;
            }
            if (next != null) {
                return;
            }

            try {
                while (iterator.hasNext()) {
                    JarFile jarFile = (JarFile) iterator.next();
                    JarEntry jarEntry = jarFile.getJarEntry(resourceName);
                    if (jarEntry != null) {
                        try {
                            next = JarFileUrlStreamHandler.createUrl(jarFile, jarEntry);
                            return;
                        } catch (MalformedURLException e) {
                        }
                    }
                }
                // no more elements
                // clear the iterator so it can be GCed
                iterator = null;
            } catch (IllegalStateException e) {
                // Jar file was closed... this means the resource finder was destroyed
                // clear the iterator so it can be GCed
                iterator = null;
                throw e;
            }
        }
    }
}
