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
package org.apache.geronimo.kernel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @version $Rev$ $Date$
 */
public class IOUtil {
    public static void recursiveCopy(File srcDir, File destDir) throws IOException {
        if (srcDir == null)  throw new NullPointerException("sourceDir is null");
        if (destDir == null)  throw new NullPointerException("destDir is null");
        if (!srcDir.isDirectory() || ! srcDir.canRead()) {
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

    public static Set search(File root, String pattern) throws MalformedURLException {
        if (root.isDirectory()) {
            if (!SelectorUtils.hasWildcards(pattern)) {
                File match = new File(root, pattern);
                if (match.exists() && match.canRead()) {
                    return Collections.singleton(match.toURL());
                } else {
                    return Collections.EMPTY_SET;
                }
            } else {
                Set matches = new LinkedHashSet();
                Map files = listAllFileNames(root);
                for (Iterator iterator = files.entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String fileName = (String) entry.getKey();
                    if (SelectorUtils.matchPath(pattern, fileName)) {
                        File file = (File) entry.getValue();
                        matches.add(file.toURL());
                    }
                }
                return matches;
            }
        } else {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(root);
                URL baseURL = new URL("jar:" + root.toURL().toString() + "!/");
                if (!SelectorUtils.hasWildcards(pattern)) {
                    ZipEntry entry = jarFile.getEntry(pattern);
                    if (entry != null) {
                        URL match = new URL(baseURL, entry.getName());
                        return Collections.singleton(match);
                    } else {
                        return Collections.EMPTY_SET;
                    }
                } else {
                    Set matches = new LinkedHashSet();
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
                return Collections.EMPTY_SET;
            } finally {
                close(jarFile);
            }
        }
    }

    public static Map listAllFileNames(File base) {
        return listAllFileNames(base, "");
    }

    private static Map listAllFileNames(File base, String prefix) {
        if (!base.canRead() || !base.isDirectory()) {
            throw new IllegalArgumentException(base.getAbsolutePath());
        }
        Map map = new LinkedHashMap();
        File[] hits = base.listFiles();
        for (int i = 0; i < hits.length; i++) {
            File hit = hits[i];
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
}
