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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.interop.properties.StringProperty;
import org.apache.geronimo.interop.properties.SystemProperties;


public abstract class JavaClass {

    public static final StringProperty classDirProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.classDir")
            .defaultValue(FileUtil.pretty(SystemProperties.getHome() + "/genfiles/java/classes"));

    public static final StringProperty classPathProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.classPath")
            .defaultValue(FileUtil.pretty(SystemProperties.getHome() + "/genfiles/java/classes"));

    public static final StringProperty sourceDirProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.sourceDir")
            .defaultValue(FileUtil.pretty(SystemProperties.getHome() + "/genfiles/java/src"));

    public static final StringProperty sourcePathProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.sourcePath")
            .defaultValue(FileUtil.pretty(SystemProperties.getHome() + "/src/java")
                          + File.pathSeparator
                          + FileUtil.pretty(SystemProperties.getHome() + "/genfiles/java/src"));

    private static String _classDir = classDirProperty.getString();
    private static List _classPath = ListUtil.getPathList(classPathProperty.getString());
    private static String _sourceDir = sourceDirProperty.getString();
    private static List _sourcePath = ListUtil.getPathList(sourcePathProperty.getString());

    public static String addPackageSuffix(String className, String suffix) {
        String jp = getPackagePrefix(className);
        if (jp.length() == 0) {
            jp = suffix;
        } else {
            jp += "." + suffix;
        }
        return jp + "." + getNameSuffix(className);
    }

    public static String getClassDir() {
        return _classDir;
    }

    public static List getClassPath() {
        return _classPath;
    }

    public static File getClassFile(Class theClass) {
        return getClassFile(theClass.getName());
    }

    public static File getClassFile(String className) {
        for (Iterator i = _classPath.iterator(); i.hasNext();) {
            String dir = (String) i.next();
            String fileName = FileUtil.pretty(dir + "/" + className.replace('.', '/') + ".class");
            File classFile = new File(fileName);
            if (classFile.exists()) {
                return classFile;
            }
        }
        return null;
    }

    public static String getName(String packagePrefix, String nameSuffix) {
        if (packagePrefix == null || packagePrefix.length() == 0) {
            return nameSuffix;
        } else {
            return packagePrefix + "." + nameSuffix;
        }
    }

    public static String getNamePrefix(String className) {
        return StringUtil.beforeLast(".", className);
    }

    public static String getNameSuffix(String className) {
        return StringUtil.afterLast(".", className);
    }

    public static String getPackagePrefix(String className) {
        return getNamePrefix(className);
    }

    /**
     * * Compute the JVM signature for a class.
     */
    public static String getSignature(Class clazz) {
        String type = null;
        if (clazz.isArray()) {
            Class cl = clazz;
            int dimensions = 0;
            while (cl.isArray()) {
                dimensions++;
                cl = cl.getComponentType();
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dimensions; i++) {
                sb.append("[");
            }
            sb.append(getSignature(cl));
            type = sb.toString();
        } else if (clazz.isPrimitive()) {
            if (clazz == Integer.TYPE) {
                type = "I";
            } else if (clazz == Byte.TYPE) {
                type = "B";
            } else if (clazz == Long.TYPE) {
                type = "J";
            } else if (clazz == Float.TYPE) {
                type = "F";
            } else if (clazz == Double.TYPE) {
                type = "D";
            } else if (clazz == Short.TYPE) {
                type = "S";
            } else if (clazz == Character.TYPE) {
                type = "C";
            } else if (clazz == Boolean.TYPE) {
                type = "Z";
            } else if (clazz == Void.TYPE) {
                type = "V";
            }

        } else {
            type = "L" + clazz.getName().replace('.', '/') + ";";
        }
        return type;
    }

    public static String getSourceDir() {
        return _sourceDir;
    }

    public static File getSourceFile(Class theClass) {
        return getSourceFile(theClass.getName());
    }

    public static File getSourceFile(String className) {
        for (Iterator i = _sourcePath.iterator(); ;) {
            String dir;
            if (i.hasNext()) {
                dir = (String) i.next();
            } else {
                dir = _sourceDir;
            }
            String fileName = FileUtil.pretty(dir + "/" + className.replace('.', '/') + ".java");
            File sourceFile = new File(fileName);
            if (sourceFile.exists()) {
                return sourceFile;
            }
            if (dir == _sourceDir) {
                break;
            }
        }
        return null;
    }

    public static List getSourcePath() {
        return _sourcePath;
    }
}
