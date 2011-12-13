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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public final class JarUtils {

    private static final Logger logger = LoggerFactory.getLogger(JarUtils.class);

    private JarUtils() {
    }

    public static final File DUMMY_JAR_FILE;
    public static final String TEMP_FILE_NAME;

    private static final boolean jarUrlRewrite;
    static {
        //Why not always set this with true ? Online deployer also lock the jar files
        jarUrlRewrite = Boolean.valueOf(System.getProperty("org.apache.geronimo.kernel.util.JarUtils.jarUrlRewrite", "true"));
        try {
            DUMMY_JAR_FILE = FileUtils.createTempFile(false);
            TEMP_FILE_NAME = DUMMY_JAR_FILE.getCanonicalPath();
            new JarOutputStream(new FileOutputStream(JarUtils.DUMMY_JAR_FILE), new Manifest()).close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static void assertTempFile() throws IOException {
    	if(DUMMY_JAR_FILE.exists()) {
    		return;
    	} else {
    		new JarOutputStream(new FileOutputStream(new File(JarUtils.TEMP_FILE_NAME)), new Manifest()).close();
    	}
    }

    public static File toTempFile(JarFile jarFile, String path) throws IOException {
        return toTempFile(createJarURL(jarFile, path));
    }

    public static File toTempFile(URL url) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        JarFile jarFile = null;
        try {
            if (url.getProtocol().equalsIgnoreCase("jar")) {
                // url.openStream() locks the jar file and does not release the lock even after the stream is closed.
                // This problem is avoided by using JarFile APIs.
                String baseFileURIString = url.getFile().substring(0, url.getFile().indexOf("!/"));
                File file = new File(new URI(baseFileURIString));
                String path = url.getFile().substring(url.getFile().indexOf("!/") + 2);
                jarFile = new JarFile(file);
                JarEntry jarEntry = jarFile.getJarEntry(path);
                if (jarEntry != null) {
                    in = jarFile.getInputStream(jarEntry);
                } else {
                    throw new FileNotFoundException("JarEntry " + path + " not found in " + file);
                }
            } else {
                in = url.openStream();
            }
            int index = url.getPath().lastIndexOf(".");
            String extension = null;
            if (index > 0) {
                extension = url.getPath().substring(index);
            }
            File tempFile = FileUtils.createTempFile(extension);
            out = new FileOutputStream(tempFile);
            IOUtils.copy(in, out);
            return tempFile;
        } catch (URISyntaxException e) {
            throw new IOException("Could not interpret url " + url, e);
        } finally {
            IOUtils.close(out);
            IOUtils.close(in);
            close(jarFile);
        }
    }

    public static String readAll(URL url) throws IOException {
        Reader reader = null;
        JarFile jarFile = null;
        try {
            if (url.getProtocol().equalsIgnoreCase("jar")) {
                // url.openStream() locks the jar file and does not release the lock even after the stream is closed.
                // This problem is avoided by using JarFile APIs.
                File file = new File(url.getFile().substring(5, url.getFile().indexOf("!/")));
                String path = url.getFile().substring(url.getFile().indexOf("!/") + 2);
                jarFile = new JarFile(file);
                JarEntry jarEntry = jarFile.getJarEntry(path);
                if (jarEntry != null) {
                    reader = new InputStreamReader(jarFile.getInputStream(jarEntry));
                } else {
                    throw new FileNotFoundException("JarEntry " + path + " not found in " + file);
                }
            } else {
                reader = new InputStreamReader(url.openStream());
            }
            char[] buffer = new char[4000];
            StringBuilder out = new StringBuilder();
            for (int count = reader.read(buffer); count >= 0; count = reader.read(buffer)) {
                out.append(buffer, 0, count);
            }
            return out.toString();
        } finally {
            IOUtils.close(reader);
            close(jarFile);
        }
    }

    public static File toFile(JarFile jarFile) throws IOException {
        if (jarFile instanceof UnpackedJarFile) {
            return ((UnpackedJarFile) jarFile).getBaseDir();
        } else {
            throw new IOException("jarFile is not a directory");
        }
    }

    // be careful with this method as it can leave a temp lying around
    public static File toFile(JarFile jarFile, String path) throws IOException {
        if (jarFile instanceof UnpackedJarFile) {
            File baseDir = ((UnpackedJarFile) jarFile).getBaseDir();
            File file = new File(baseDir, path);
            if (!file.isFile()) {
                throw new IOException("No such file: " + file.getAbsolutePath());
            }
            return file;
        } else {
            String urlString = "jar:" + new File(jarFile.getName()).toURI().toURL() + "!/" + path;
            return toTempFile(new URL(urlString));
        }
    }

    public static URL createJarURL(JarFile jarFile, String path) throws MalformedURLException {
        if (jarFile instanceof NestedJarFile) {
            NestedJarFile nestedJar = (NestedJarFile) jarFile;
            if (nestedJar.isUnpacked()) {
                JarFile baseJar = nestedJar.getBaseJar();
                String basePath = nestedJar.getBasePath();
                if (baseJar instanceof UnpackedJarFile) {
                    File baseDir = ((UnpackedJarFile) baseJar).getBaseDir();
                    baseDir = new File(baseDir, basePath);
                    return new File(baseDir, path).toURI().toURL();
                }
            }
        }
        if (jarFile instanceof UnpackedJarFile) {
            File baseDir = ((UnpackedJarFile) jarFile).getBaseDir();
            return new File(baseDir, path).toURI().toURL();
        } else {
            String urlString = "jar:" + new File(jarFile.getName()).toURI().toURL() + "!/" + path;
            if (jarUrlRewrite) {
                // To prevent the lockout of archive, instead of returning a jar url, write the content to a
                // temp file and return the url of that file.
                File tempFile = null;
                try {
                    tempFile = toTempFile(new URL(urlString));
                } catch (IOException e) {
                    // The JarEntry does not exist!
                    // Return url of a file that does not exist.
                    try {
                        tempFile = FileUtils.createTempFile();
                        tempFile.delete();
                    } catch (IOException ignored) {
                    }
                }
                return tempFile.toURI().toURL();
            } else {
                return new URL(urlString);
            }
        }
    }

    public static JarFile createJarFile(File jarFile) throws IOException {
        if (jarFile.isDirectory()) {
            return new UnpackedJarFile(jarFile);
        } else {
            return new JarFile(jarFile);
        }
    }

    public static void copyToPackedJar(JarFile inputJar, File outputFile) throws IOException {
        if (inputJar.getClass() == JarFile.class) {
            // this is a plain old jar... nothign special
            FileUtils.copyFile(new File(inputJar.getName()), outputFile);
        } else if (inputJar instanceof NestedJarFile && ((NestedJarFile) inputJar).isPacked()) {
            NestedJarFile nestedJarFile = (NestedJarFile) inputJar;
            JarFile baseJar = nestedJarFile.getBaseJar();
            String basePath = nestedJarFile.getBasePath();
            if (baseJar instanceof UnpackedJarFile) {
                // our target jar is just a file in upacked jar (a plain old directory)... now
                // we just need to find where it is and copy it to the outptu
                FileUtils.copyFile(((UnpackedJarFile) baseJar).getFile(basePath), outputFile);
            } else {
                // out target is just a plain old jar file directly accessabel from the file system
                FileUtils.copyFile(new File(baseJar.getName()), outputFile);
            }
        } else {
            // copy out the module contents to a standalone jar file (entry by entry)
            JarOutputStream out = null;
            try {
                out = new JarOutputStream(new FileOutputStream(outputFile));
                byte[] buffer = new byte[4096];
                Enumeration entries = inputJar.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) entries.nextElement();
                    InputStream in = inputJar.getInputStream(entry);
                    try {
                        out.putNextEntry(new ZipEntry(entry.getName()));
                        try {
                            int count;
                            while ((count = in.read(buffer)) > 0) {
                                out.write(buffer, 0, count);
                            }
                        } finally {
                            out.closeEntry();
                        }
                    } finally {
                        IOUtils.close(in);
                    }
                }
            } finally {
                IOUtils.close(out);
            }
        }
    }

    public static void jarDirectory(File sourceDirectory, File destinationFile) throws IOException {
        JarOutputStream out = null;
        try {
            out = new JarOutputStream(new FileOutputStream(destinationFile));
            jarDirectory(sourceDirectory, "", destinationFile, out);
        } finally {
            IOUtils.close(out);
        }
    }

    private static void jarDirectory(File baseDirectory,
                                     String baseName,
                                     File destinationFile,
                                     JarOutputStream out) throws IOException {
        File[] files = baseDirectory.listFiles();
        if (null == files) {
            return;
        }
        byte[] buffer = new byte[4096];
        for (File file : files) {
            // make sure not to include the file we're creating
            if (file.equals(destinationFile)) {
                continue;
            }
            String name = baseName + file.getName();
            if (file.isDirectory()) {
                out.putNextEntry(new ZipEntry(name + "/"));
                out.closeEntry();
                jarDirectory(file, name + "/", destinationFile, out);
            } else if (file.isFile()) {
                out.putNextEntry(new ZipEntry(name));
                InputStream in = new FileInputStream(file);
                try {
                    int count;
                    while ((count = in.read(buffer)) > 0) {
                        out.write(buffer, 0, count);
                    }
                } finally {
                    IOUtils.close(in);
                    out.closeEntry();
                }
            }
        }
    }

    private static void createDirectory(File dir) throws IOException {
        if (dir != null && !dir.exists()) {
            boolean success = dir.mkdirs();
            if (!success) {
                throw new IOException("Cannot create directory " + dir.getAbsolutePath());
            }
        }
    }

    public static void unzipToDirectory(ZipFile zipFile, File destDir) throws IOException {
        Enumeration entries = zipFile.entries();
        try {
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    File dir = new File(destDir, entry.getName());
                    createDirectory(dir);
                } else {
                    File file = new File(destDir, entry.getName());
                    createDirectory(file.getParentFile());
                    OutputStream out = null;
                    InputStream in = null;
                    try {
                        out = new BufferedOutputStream(new FileOutputStream(file));
                        in = zipFile.getInputStream(entry);
                        IOUtils.copy(in, out);
                    } finally {
                        IOUtils.close(in);
                        IOUtils.close(out);
                    }
                }
            }
        } finally {
            zipFile.close();
        }
    }

    public static void close(JarFile thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Determine whether a file is a JAR File.
     *
     * Note: Jar file is a zip file with an *optional* META-INF directory.
     * Therefore, there is no reliable way to check if a file is a Jar file.
     * So this functions returns the same as calling isZipFile(File).
     */
    public static boolean isJarFile(File file) throws IOException {
        return isZipFile(file);
    }

    /**
     * Determine whether a file is a ZIP File.
     */
    public static boolean isZipFile(File file) throws IOException {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read file " + file.getAbsolutePath());
        }
        if (file.length() < 4) {
            return false;
        }
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            return in.readInt() == 0x504b0304;
        } finally {
            IOUtils.close(in);
        }
    }
}
