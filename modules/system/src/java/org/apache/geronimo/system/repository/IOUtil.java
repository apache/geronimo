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
package org.apache.geronimo.system.repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.Reader;
import java.util.jar.JarFile;

/**
 * @version $Rev$ $Date$
 */
public class IOUtil {
    public static void recursiveCopy(File srcDir, File destDir) throws IOException {
        if (srcDir == null)  throw new NullPointerException("sourceDir is null");
        if (srcDir == null)  throw new NullPointerException("destDir is null");
        if (!srcDir.isDirectory() || ! srcDir.canRead()) {
            throw new IllegalArgumentException("Source directory must be a readable directory " + srcDir);
        }
        if (destDir.exists()) {
            throw new IllegalArgumentException("Destination directory already exists " + destDir);
        }
        if (destDir.equals(destDir)) {
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
}
