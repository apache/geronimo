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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * @version $Revision$ $Date$
 */
public class JarUtil {
    public static final File DUMMY_JAR_FILE;
    static {
        try {
            DUMMY_JAR_FILE = FileUtil.createTempFile();
            new JarOutputStream(new FileOutputStream(DUMMY_JAR_FILE), new Manifest()).close();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static URL createJarURL(JarFile jarFile, String path) throws DeploymentException {
        try {
            if (jarFile instanceof UnpackedJarFile) {
                File baseDir = ((UnpackedJarFile) jarFile).getBaseDir();
                return new File(baseDir, path).toURL();
            } else {
                String urlString = "jar:" + new File(jarFile.getName()).toURL() + "!/" + path;
                return new URL(urlString);
            }
        } catch (MalformedURLException e) {
            throw new DeploymentException("Can not create URL", e);
        }
    }

    public static JarFile createJarFile(File jarFile) throws IOException {
        if (jarFile.isDirectory()) {
            return new UnpackedJarFile(jarFile);
        } else {
            return new JarFile(jarFile);
        }
    }

    public static File extractToPackedJar(JarFile inputJar) throws IOException {
        if (inputJar.getClass() == JarFile.class) {
            // this is a plain old jar... nothign special
            return new File(inputJar.getName());
        } else if (inputJar instanceof NestedJarFile && ((NestedJarFile)inputJar).isPacked()) {
            NestedJarFile nestedJarFile = (NestedJarFile)inputJar;
            JarFile baseJar = nestedJarFile.getBaseJar();
            String basePath = nestedJarFile.getBasePath();
            if (baseJar instanceof UnpackedJarFile) {
                // our target jar is just a file in upacked jar (a plain old directory)... now
                // we just need to find where it is
                return ((UnpackedJarFile)baseJar).getFile(basePath);
            } else {
                // out target is just a plain old jar file directly accessabel from the file system
                return new File(baseJar.getName());
            }
        } else {
            // copy out the module contents to a standalone jar file (entry by entry)
            File jarFile = FileUtil.createTempFile();

            JarOutputStream out = new JarOutputStream(new FileOutputStream(jarFile));
            try {
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
                        IOUtil.close(in);
                    }
                }
                return jarFile;
            } finally {
                IOUtil.close(out);
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
