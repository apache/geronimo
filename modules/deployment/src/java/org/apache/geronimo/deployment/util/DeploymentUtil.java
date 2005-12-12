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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public final class DeploymentUtil {
    private DeploymentUtil() {
    }

    public static final File DUMMY_JAR_FILE;
    static {
        try {
            DUMMY_JAR_FILE = DeploymentUtil.createTempFile();
            new JarOutputStream(new FileOutputStream(DeploymentUtil.DUMMY_JAR_FILE), new Manifest()).close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // be careful to clean up the temp directory
    public static File createTempDir() throws IOException {
        File tempDir = File.createTempFile("geronimo-deploymentUtil", ".tmpdir");
        tempDir.delete();
        tempDir.mkdirs();
        return tempDir;
    }

    // be careful to clean up the temp file... we tell the vm to delete this on exit
    // but VMs can't be trusted to acutally delete the file
    public static File createTempFile() throws IOException {
        File tempFile = File.createTempFile("geronimo-deploymentUtil", ".tmpdir");
        tempFile.deleteOnExit();
        return tempFile;
    }

    public static void copyFile(File source, File destination) throws IOException {
        File destinationDir = destination.getParentFile();
        if (false == destinationDir.exists() && false == destinationDir.mkdirs()) {
            throw new java.io.IOException("Cannot create directory : " + destinationDir);
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

    private static void writeAll(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.flush();
    }
    public static File toTempFile(JarFile jarFile, String path) throws IOException {
        return toTempFile(createJarURL(jarFile, path));
    }

    public static File toTempFile(URL url) throws IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = url.openStream();

            File tempFile = createTempFile();
            out = new FileOutputStream(tempFile);

            writeAll(in, out);
            return tempFile;
        } finally {
            close(out);
            close(in);
        }
    }

    public static String readAll(URL url) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(url.openStream());

            char[] buffer = new char[4000];
            StringBuffer out = new StringBuffer();
            for(int count = reader.read(buffer); count >= 0; count = reader.read(buffer)) {
                out.append(buffer, 0, count);
            }
            return out.toString();
        } finally {
            close(reader);
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
            String urlString = "jar:" + new File(jarFile.getName()).toURL() + "!/" + path;
            return toTempFile(new URL(urlString));
        }
    }

    public static URL createJarURL(JarFile jarFile, String path) throws MalformedURLException {
        if (jarFile instanceof UnpackedJarFile) {
            File baseDir = ((UnpackedJarFile) jarFile).getBaseDir();
            return new File(baseDir, path).toURL();
        } else {
            String urlString = "jar:" + new File(jarFile.getName()).toURL() + "!/" + path;
            return new URL(urlString);
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
            copyFile(new File(inputJar.getName()), outputFile);
        } else if (inputJar instanceof NestedJarFile && ((NestedJarFile)inputJar).isPacked()) {
            NestedJarFile nestedJarFile = (NestedJarFile)inputJar;
            JarFile baseJar = nestedJarFile.getBaseJar();
            String basePath = nestedJarFile.getBasePath();
            if (baseJar instanceof UnpackedJarFile) {
                // our target jar is just a file in upacked jar (a plain old directory)... now
                // we just need to find where it is and copy it to the outptu
                copyFile(((UnpackedJarFile)baseJar).getFile(basePath), outputFile);
            } else {
                // out target is just a plain old jar file directly accessabel from the file system
                copyFile(new File(baseJar.getName()), outputFile);
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
                        close(in);
                    }
                }
            } finally {
                close(out);
            }
        }
    }

    public static void jarDirectory(File sourceDirecotry, File destinationFile) throws IOException {
        JarFile inputJar = new UnpackedJarFile(sourceDirecotry);
        try {
            copyToPackedJar(inputJar, destinationFile);
        } finally {
            close(inputJar);
        }
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
                        file.delete();
                    }
                }
            }
        }
        return root.delete();
    }

    public static Collection listRecursiveFiles(File file) {
        LinkedList list = new LinkedList();
        listRecursiveFiles(file, list);
        return Collections.unmodifiableCollection(list);
    }

    public static void listRecursiveFiles(File file, Collection collection) {
        File[] files = file.listFiles();
        if ( null == files ) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                listRecursiveFiles(files[i], collection);
            } else {
                collection.add(files[i]);
            }
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

    public static void close(InputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(OutputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(Reader thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch(Exception ignored) {
            }
        }
    }

    public static void close(Writer thing) {
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
