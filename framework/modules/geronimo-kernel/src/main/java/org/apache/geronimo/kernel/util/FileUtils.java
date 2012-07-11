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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final long FILE_UTILS_INITIALIZATION_TIME_MILL = (System.currentTimeMillis()/1000) * 1000;

    public static final String DEFAULT_TEMP_PREFIX = "geronimo-fileutils";

    public static final String DEFAULT_TEMP_FILE_SUFFIX = ".tmpfile";

    public static final String DEFAULT_TEMP_DIRECTORY_SUFFIX = ".tmpdir";

    private static final ThreadLocal<List<File>> TEMPORARY_FILES = new ThreadLocal<List<File>>();
    
    public static void copyFile(File source, File destination) throws IOException {
        copyFile(source, destination, IOUtils.DEFAULT_COPY_BUFFER_SIZE);
    }

    public static void beginRecordTempFiles() {
        TEMPORARY_FILES.set(new LinkedList<File>());
    }
    
    public static List<File> endRecordTempFiles() {
        List<File> tempFiles = TEMPORARY_FILES.get();
        TEMPORARY_FILES.remove();
        return tempFiles;
    }
    
    public static void addTempFile(File tempFile) {
        List<File> tempFiles = TEMPORARY_FILES.get();
        if(tempFiles != null) {
            tempFiles.add(tempFile);
        } else if(logger.isDebugEnabled()) {
            logger.debug("Unable to record temporary file " + tempFile.getAbsolutePath() + " ,  it is required to call beginRecordTempFiles first");
        }
    }
    
    public static void copyFile(File source, File destination, int bufferSizeInBytes) throws IOException {
        if (!source.exists() || source.isDirectory()) {
            throw new IllegalArgumentException("Source does not exist or it is not a file");
        }
        File destinationDir = destination.getParentFile();
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            throw new java.io.IOException("Cannot create directory : " + destinationDir);
        }
        InputStream in = null;
        OutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(destination);
            IOUtils.copy(in, out, bufferSizeInBytes);
        } finally {
            IOUtils.close(in);
            IOUtils.close(out);
        }
    }

    // be careful to clean up the temp directory
    public static File createTempDir() throws IOException {
        File tempDir = File.createTempFile(DEFAULT_TEMP_PREFIX, DEFAULT_TEMP_DIRECTORY_SUFFIX);
        tempDir.delete();
        tempDir.mkdirs();
        deleteOnExit(tempDir);
        addTempFile(tempDir);
        return tempDir;
    }

    public static File createTempFile(boolean record) throws IOException {
        File tempFile = File.createTempFile(DEFAULT_TEMP_PREFIX, DEFAULT_TEMP_FILE_SUFFIX);
        if (record) {
            addTempFile(tempFile);
        }
        tempFile.deleteOnExit();
        return tempFile;
    }
    
    // be careful to clean up the temp file... we tell the vm to delete this on exit
    // but VMs can't be trusted to acutally delete the file
    public static File createTempFile() throws IOException {
        return createTempFile(true);
    }

    // be careful to clean up the temp file... we tell the vm to delete this on exit
    // but VMs can't be trusted to acutally delete the file
    public static File createTempFile(String extension) throws IOException {
        File tempFile = File.createTempFile(DEFAULT_TEMP_PREFIX, extension == null ? DEFAULT_TEMP_DIRECTORY_SUFFIX : extension);
        addTempFile(tempFile);
        tempFile.deleteOnExit();
        return tempFile;
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
            Map<String, File> files = listAllFileNames(root);
            for (Map.Entry<String, File> entry : files.entrySet()) {
                String fileName = entry.getKey();
                if (SelectorUtils.matchPath(pattern, fileName)) {
                    matches.put(fileName, entry.getValue());
                }
            }
        }
    }

    public static Map<String, File> listAllFileNames(File base) {
        return listAllFileNames(base, "");
    }

    public static Collection<File> listRecursiveFiles(File file) {
        Collection<File> list = new LinkedList<File>();
        listRecursiveFiles(file, list);
        return Collections.unmodifiableCollection(list);
    }

    public static void listRecursiveFiles(File file, Collection<File> collection) {
        File[] files = file.listFiles();
        if (null == files) {
            return;
        }
        for (File file1 : files) {
            collection.add(file1);
            if (file1.isDirectory()) {
                listRecursiveFiles(file1, collection);
            }
        }
    }

    public static void recursiveCopy(File srcDir, File destDir) throws IOException {
        if (srcDir == null)
            throw new NullPointerException("sourceDir is null");
        if (destDir == null)
            throw new NullPointerException("destDir is null");
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

    public static boolean recursiveDelete(File root) {
        return recursiveDelete(root, null);
    }

    public static boolean recursiveDelete(File root, Collection<String> unableToDeleteCollection) {
        if (root == null) {
            return true;
        }
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        recursiveDelete(file, unableToDeleteCollection);
                    } else {
                        if (!file.delete() && unableToDeleteCollection != null) {
                            unableToDeleteCollection.add(file.getAbsolutePath());
                        }
                    }
                    // help out the GC of file handles by nulling the references
                    files[i] = null;
                }
            }
        }
        boolean rootDeleteStatus;
        if (!(rootDeleteStatus = root.delete()) && unableToDeleteCollection != null)
            unableToDeleteCollection.add(root.getAbsolutePath());
        return rootDeleteStatus;
    }

    public static boolean recursiveDeleteWithRetries(File root) {
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
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
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
                JarUtils.close(jarFile);
            }
        }
    }

    public static String readFileAsString(File file, String encoding, String fileSeparator) {
        if (file == null || !file.exists()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
            StringBuilder stringBuilder = new StringBuilder();
            String currentLine = null;
            while ((currentLine = reader.readLine()) != null) {
                stringBuilder.append(currentLine).append(fileSeparator);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            logger.error("Fail to read file " + file.getAbsolutePath() + " as a string", e);
            return null;
        } finally {
            IOUtils.close(reader);
        }
    }

    public static String readFileAsString(File file) {
        return readFileAsString(file, "iso-8859-1", File.separator);
    }

    public static void writeStringToFile(File file, String line) {
        writeStringToFile(file, line, "iso-8859-1");
    }

    public static void writeStringToFile(File file, String line, String encoding) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
            writer.write(line);
        } catch (IOException e) {
        } finally {
            IOUtils.close(writer);
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
            if (logger.isDebugEnabled()) {
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
            if (logger.isDebugEnabled()) {
                logger.debug(file.getPath() + " deleted after " + retries + " retries, with " + interruptions + " interruptions.");
            }
        } else {
            logger.warn(file.getPath() + " not deleted after " + retryLimit + " retries, with " + interruptions + " interruptions.");
        }
        return fileDeleted;
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

    private static void listFiles(File directory) {
        if (!logger.isDebugEnabled() || !directory.isDirectory()) {
            return;
        }
        File[] files = directory.listFiles();
        logger.debug(directory.getPath() + " has " + files.length + " files:");
        for (File file : files) {
            logger.debug(file.getPath());
        }
    }

    public static String removeExtension(String name, String extension) {
        if (name.endsWith(extension)) {
            return name.substring(0, name.length() - extension.length());
        } else {
            return name;
        }
    }

    private FileUtils() {
    }

    static final List<String> delete = new ArrayList<String>();

    private static void deleteOnExit(File file) {
        delete.add(file.getAbsolutePath());
    }

    private static void delete() {
        for (String path : delete) {
            delete(new File(path));
        }
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }

        file.delete();
    }

     // Shutdown hook for recurssive delete on tmp directories

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                delete();
            }
        });
    }
}
