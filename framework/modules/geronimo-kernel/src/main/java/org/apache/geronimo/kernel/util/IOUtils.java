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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class IOUtils {
    private static final Logger log = LoggerFactory.getLogger(IOUtils.class);

    public static void recursiveCopy(File srcDir, File destDir) throws IOException {
        if (srcDir == null) throw new NullPointerException("sourceDir is null");
        if (destDir == null) throw new NullPointerException("destDir is null");
        if (!srcDir.isDirectory() || !srcDir.canRead()) {
            throw new IllegalArgumentException("Source directory must be a readable directory " + srcDir);
        }
        if (destDir.exists()) {
            throw new IllegalArgumentException("Destination directory already exists " + destDir);
        }
        if (srcDir.equals(destDir)) {
            throw new IllegalArgumentException("Source and destination directory are the same " + srcDir);
        }

        destDir.mkdirs();
        if (!destDir.exists()) {
            throw new IOException("Could not create destination directory " + destDir);
        }


        File[] srcFiles = srcDir.listFiles();
        if (srcFiles != null) {
            for (int i = 0; i < srcFiles.length; i++) {
                File srcFile = srcFiles[i];
                File destFile = new File(destDir, srcFile.getName());
                if (srcFile.isDirectory()) {
                    recursiveCopy(srcFile, destFile);
                } else {
                    copyFile(srcFile, destFile);
                }
            }
        }
    }

    public static void copyFile(File source, File destination) throws IOException {
        File destinationDir = destination.getParentFile();
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            throw new IOException("Cannot create directory : " + destinationDir);
        }

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            writeAll(in, out);
        } finally {
            close(in);
            close(out);
        }
    }

    public static void writeAll(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }

    private static void listFiles(File directory) {
        if (!log.isDebugEnabled() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        log.debug(directory.getPath() + " has " + files.length + " files:");
        for (File file : files) {
            log.debug(file.getPath());
        }
    }

    private static boolean deleteFile(File file) {
        boolean fileDeleted = file.delete();
        if (fileDeleted) {
            return true;
        }

        // special retry code to handle occasional Windows JDK and Unix NFS timing failures
        int retryLimit = 5;
        int retries;
        int interruptions = 0;
        for (retries = 1; !fileDeleted && retries <= retryLimit; retries++) {
            if (log.isDebugEnabled()) {
                listFiles(file);
            }
            System.runFinalization();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                interruptions++;
            }
            System.gc();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                interruptions++;
            }
            fileDeleted = file.delete();
        }
        if (fileDeleted) {
            if (log.isDebugEnabled()) {
                log.debug(file.getPath() + " deleted after " + retries
                        + " retries, with " + interruptions + " interruptions.");
            }
        } else {
            log.warn(file.getPath() + " not deleted after " + retryLimit
                    + " retries, with " + interruptions + " interruptions.");
        }
        return fileDeleted;
    }

    public static boolean recursiveDelete(File root) {
        if (root == null) {
            return true;
        }

        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        recursiveDelete(file);
                    } else {
                        deleteFile(file);
                    }
                }
            }
        }
        return deleteFile(root);
    }

    public static void close(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static Map<String, File> find(File root, String pattern) {
        Map<String, File> matches = new HashMap<String, File>();
        find(root, pattern, matches);
        return matches;
    }
    
    public static void find(File root, String pattern, Map<String, File> matches) {   
        if (!SelectorUtils.hasWildcards(pattern)) {
            File match = new File(root, pattern);
            if (match.exists() && match.canRead()) {
                matches.put(pattern, match);
            }
        } else {
            Map<String, File> files = IOUtils.listAllFileNames(root);
            for (Map.Entry<String, File> entry : files.entrySet()) {
                String fileName = entry.getKey();
                if (SelectorUtils.matchPath(pattern, fileName)) {
                    matches.put(fileName, entry.getValue());
                }
            }
        }
    }
    
    public static Set<URL> search(File root, String pattern) throws MalformedURLException {
        if (root.isDirectory()) {
            if (pattern == null || pattern.length() == 0) {
                return Collections.singleton(new URL("file:" + root.toURI().normalize().getPath()));
            }
            if (!SelectorUtils.hasWildcards(pattern)) {
                File match = new File(root, pattern);
                if (match.exists() && match.canRead()) {
                    return Collections.singleton(new URL("file:" + match.toURI().normalize().getPath()));
                } else {
                    return Collections.emptySet();
                }
            } else {
                Set<URL> matches = new LinkedHashSet<URL>();
                Map<String, File> files = listAllFileNames(root);
                for (Map.Entry<String, File> entry : files.entrySet()) {
                    String fileName = entry.getKey();
                    if (SelectorUtils.matchPath(pattern, fileName)) {
                        File file = entry.getValue();
                        matches.add(new URL("file:" + file.toURI().normalize().getPath()));
                    }
                }
                return matches;
            }
        } else {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(root);
                URL baseURL = new URL("jar:" + root.toURI().toURL().toString() + "!/");
                if (pattern == null || pattern.length() == 0) {
                    return Collections.singleton(baseURL);
                }
                if (!SelectorUtils.hasWildcards(pattern)) {
                    ZipEntry entry = jarFile.getEntry(pattern);
                    if (entry != null) {
                        URL match = new URL(baseURL, entry.getName());
                        return Collections.singleton(match);
                    } else {
                        return Collections.emptySet();
                    }
                } else {
                    Set<URL> matches = new LinkedHashSet<URL>();
                    Enumeration entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();
                        String fileName = entry.getName();
                        if (SelectorUtils.matchPath(pattern, fileName)) {
                            URL url = new URL(baseURL, fileName);
                            matches.add(url);
                        }
                    }
                    return matches;
                }
            } catch (MalformedURLException e) {
                throw e;
            } catch (IOException e) {
                return Collections.emptySet();
            } finally {
                close(jarFile);
            }
        }
    }

    public static Map<String, File> listAllFileNames(File base) {
        return listAllFileNames(base, "");
    }

    private static Map<String, File> listAllFileNames(File base, String prefix) {
        if (!base.canRead() || !base.isDirectory()) {
            throw new IllegalArgumentException(base.getAbsolutePath());
        }
        Map<String, File> map = new LinkedHashMap<String, File>();
        File[] hits = base.listFiles();
        for (File hit : hits) {
            if (hit.canRead()) {
                if (hit.isDirectory()) {
                    map.putAll(listAllFileNames(hit, prefix.equals("") ? hit.getName() : prefix + "/" + hit.getName()));
                } else {
                    map.put(prefix.equals("") ? hit.getName() : prefix + "/" + hit.getName(), hit);
                }
            }
        }
        map.put(prefix, base);
        return map;
    }
    
    public static byte[] getBytes(InputStream inputStream) throws IOException {
        try {
            byte[] buffer = new byte[4096];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            for (int count = inputStream.read(buffer); count >= 0; count = inputStream.read(buffer)) {
                out.write(buffer, 0, count);
            }
            byte[] bytes = out.toByteArray();
            return bytes;
        } finally {
            close(inputStream);
        }
    }

    public static void flush(OutputStream thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch(Exception ignored) {
            }
        }
    }

    public static void flush(Writer thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(JarFile thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static final class EmptyInputStream extends InputStream {
        public int read() {
            return -1;
        }

        public int read(byte b[])  {
            return -1;
        }

        public int read(byte b[], int off, int len) {
            return -1;
        }

        public long skip(long n) {
            return 0;
        }

        public int available() {
            return 0;
        }

        public void close() {
        }

        public synchronized void mark(int readlimit) {
        }

        public synchronized void reset() {
        }

        public boolean markSupported() {
            return false;
        }
    }
}
