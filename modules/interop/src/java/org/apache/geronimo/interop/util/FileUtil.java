/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.properties.SystemProperties;


public abstract class FileUtil {
    // private data

    private static int _tempIndex;

    private static Object _tempIndexLock = new Object();

    // public methods

    public static int compareLines(String file1, String file2) {
        return compareLines(file1, file2, false);
    }

    public static int compareLines(String file1, String file2, boolean removeTopLevelComments) {
        String lines1 = readLines(file1, removeTopLevelComments);
        String lines2 = readLines(file2, removeTopLevelComments);
        return lines1.compareTo(lines2);
    }

    public static void copyDir(String fromDir, String toDir) {
        copyDir(fromDir, toDir, true);
    }

    public static void copyDir(String fromDir, String toDir, boolean rec) {
        File dirFile = new File(fromDir);
        if (!dirFile.exists()) {
            return;
        }

        File toDirFile = new File(toDir);
        if (!toDirFile.exists()) {
            toDirFile.mkdir();
        }
        String[] fileList = dirFile.list();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                String name = fileList[i];
                String from = fromDir + File.separator + name;
                String to = toDir + File.separatorChar + name;
                File file = new File(from);
                if (file.isDirectory()) {
                    if (rec) {
                        copyDir(from, to);
                    }
                } else {
                    copyFile(from, to);
                }
            }
        }
    }

    public static void copyFile(String from, String to) {
        mkdirs(to);
        try {
            InputStream input = new BufferedInputStream(new FileInputStream(from));
            OutputStream output = new BufferedOutputStream(new FileOutputStream(to));
            int c;
            while ((c = input.read()) != -1) {
                output.write(c);
            }
            input.close();
            output.close();
        } catch (IOException ex) {
            throw new SystemException(ex);
        }
    }

    public static void copyFiles(String fromDir, String toDir, List files) {
        for (Iterator i = files.iterator(); i.hasNext();) {
            String file = (String) i.next();
            copyFile(fromDir + "/" + file, toDir + "/" + file);
        }
    }

    public static void deleteDir(String dir) {
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            deleteFilesInDir(dir);
            dirFile.delete();
        }
    }

    public static void deleteFile(String file) {
        new File(file).delete();
    }

    public static void deleteFiles(List files) {
        for (Iterator i = files.iterator(); i.hasNext();) {
            String fileName = (String) i.next();
            File file = new File(fileName);
            file.delete();
        }
    }

    public static void deleteFilesInDir(String dir) {
        File dirFile = new File(dir);
        String[] fileList = dirFile.list();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                String path = dir + File.separator + fileList[i];
                File file = new File(path);
                if (file.isDirectory()) {
                    deleteDir(path);
                }
                file.delete();
            }
        }
    }

    public static String expandHomeRelativePath(String path) {
        if (path.startsWith("~")) {
            path = SystemProperties.getHome() + path.substring(1);
        }
        return path;
    }

    public static List findFiles(String baseDir) {
        return findFiles(baseDir, "", true, true, "");
    }

    public static List findFiles(String baseDir, String pattern) {
        return findFiles(baseDir, pattern, true, true, "");
    }

    public static List findFiles(String baseDir, String pattern, boolean fullPath, boolean recursive) {
        return findFiles(baseDir, pattern, fullPath, recursive, "");
    }

    private static List findFiles(String baseDir, String pattern, boolean fullPath, boolean recursive, String relativeBase) {
        if (pattern.equals("**")) {
            pattern = ""; // Equivalent to "*"
            recursive = true;
        }
        final String prefix = StringUtil.beforeFirst("*", pattern);
        final String suffix = StringUtil.afterFirst("*", pattern);
        final boolean finalRecursive = recursive;
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File file, String name) {
                if (finalRecursive && new File(file.getPath() + File.separator + name).isDirectory()) {
                    return true;
                }
                return name.startsWith(prefix) && name.endsWith(suffix);
            }
        }
                ;
        List list = new LinkedList();
        File dirFile = new File(baseDir);
        String[] files = dirFile.list(filter);
        if (files != null) {
            int n = files.length;
            for (int i = 0; i < n; i++) {
                String fileName = files[i];
                String fullName = baseDir.length() == 0 ? fileName
                                  : (baseDir + (fullPath ? File.separatorChar : '/') + fileName);
                File file = new File(fullName);
                if (file.isDirectory()) {
                    if (recursive) {
                        String relativeName = relativeBase.length() == 0 ? fileName
                                              : (relativeBase + '/' + fileName);
                        list.addAll(findFiles(fullName, pattern, fullPath,
                                              recursive, relativeName));
                    }
                } else if (fullPath) {
                    list.add(fullName);
                } else {
                    String relativeName = relativeBase.length() == 0 ? fileName
                                          : (relativeBase + '/' + fileName);
                    list.add(relativeName);
                }
            }
        }
        return list;
    }

    public static void mkdir(String dir) {
        try {
            new File(dir).mkdirs();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public static void mkdirs(String file) {
        try {
            file = file.replace('/', File.separatorChar);
            int pos = file.lastIndexOf(File.separatorChar);
            if (pos != -1) {
                String dir = file.substring(0, pos);
                mkdir(dir);
            }
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public static String newTempDir() {
        String tempDir = SystemProperties.getTempDir();
        synchronized (_tempIndexLock) {
            tempDir += "/" + (++_tempIndex);
        }
        tempDir = pretty(tempDir);
        deleteFilesInDir(tempDir);
        mkdirs(tempDir + "/x.x");
        return tempDir;
    }

    public static String pretty(String file) {
        try {
            return new File(file).getCanonicalPath();
        } catch (Exception ignore) {
            return file.replace('/', File.separatorChar);
        }
    }

    /**
     * * Read all bytes of a file into an array.
     */
    public static byte[] readBytes(String fileName) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            InputStream input = new BufferedInputStream(new FileInputStream(fileName));
            int c;
            while ((c = input.read()) != -1) {
                bytes.write((byte) c);
            }
            input.close();
            return bytes.toByteArray();
        } catch (IOException ex) {
            throw new SystemException(ex);
        }
    }

    public static String readLines(String fileName) {
        return readLines(fileName, false);
    }

    /**
     * * Read all lines of a file into a string, optionally removing comments.
     */
    public static String readLines(String fileName, boolean removeTopLevelComments) {
        try {
            StringBuffer code = new StringBuffer();
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = input.readLine()) != null) {
                if (removeTopLevelComments && line.length() >= 3) {
                    char c1 = line.charAt(1);
                    char c2 = line.charAt(2);
                    if (c1 == '*' && c2 == '*') {
                        continue;
                    }
                }
                code.append(line);
                code.append('\n');
            }
            input.close();
            return code.toString();
        } catch (IOException ex) {
            throw new SystemException(ex);
        }
    }
}
