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
package org.apache.geronimo.system.main;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.URLClassPath;

/**
 * @version $Revision: 1.1 $ $Date: 2004/09/09 02:27:31 $
 */
public class ToolsJarHack {
    private static boolean installed = false;
    private static Log log;

    public static void install() {
        if (installed) {
            return;
        }

        if (log == null) {
            log = LogFactory.getLog(ToolsJarHack.class);
        }

        // Is the compiler already available
        ClassLoader myClassLoader = ToolsJarHack.class.getClassLoader();
        Class compilerClass = null;
        try {
            compilerClass = myClassLoader.loadClass("sun/tools/javac/Main");
        } catch (ClassNotFoundException ignored) {
        }
        if (compilerClass != null) {
            installed = true;
            return;
        }

        File toolsJarFile = findToolsJarFile();
        if (toolsJarFile == null) {
            // could not locate the tools jar file... message alredy logged
            return;
        }

        URL toolsJarURL;
        try {
            toolsJarURL = toolsJarFile.toURL();
        } catch (MalformedURLException e) {
            log.warn("Could not all find java compiler: tools.jar file not a regular file: " + toolsJarFile.getAbsolutePath(), e);
            return;
        }
        addJarToPath(toolsJarURL);
        installed = true;
    }

    private static File findToolsJarFile() {
        String javaHome = System.getProperty("java.home");
        if (javaHome == null) {
            log.warn("Could not all find java compiler: java.home system property is null");
            return null;
        }

        File javaHomeDir = new File(javaHome);
        if (!javaHomeDir.isDirectory()) {
            log.warn("Could not all find java compiler: java.home system property does not refer to a directory: java.home=" + javaHome);
            return null;
        }

        File toolsJarFile = findToolsJarFile(javaHomeDir);
        if (toolsJarFile != null) {
            return toolsJarFile;
        }

        toolsJarFile = findToolsJarFile(javaHomeDir.getParentFile());
        if (toolsJarFile != null) {
            return toolsJarFile;
        }

        log.warn("Could not all find java compiler: tools.jar file not found at " + toolsJarFile.getAbsolutePath() +
                " or " + toolsJarFile.getParentFile().getAbsolutePath());
        return null;
    }

    private static File findToolsJarFile(File javaHomeDir) {
        File toolsJarFile;
        toolsJarFile = new File(javaHomeDir, "lib" + File.separator + "tools.jar");
        if (!toolsJarFile.exists()) {
            log.warn("Could not all find java compiler: tools.jar file not found at " + toolsJarFile.getAbsolutePath());
            return null;
        }
        if (!toolsJarFile.isFile()) {
            log.warn("Could not all find java compiler: tools.jar file not a regular file: " + toolsJarFile.getAbsolutePath());
            return null;
        }
        return toolsJarFile;
    }


    private static void addJarToPath(URL jar) {
        //System.out.println("[|] SYSTEM "+jar.toExternalForm());
        URLClassLoader urlClassLoader = null;
        try {
            urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        } catch (Throwable e) {
            log.warn("Could not install compiler: Could not obtain access to system class loader", e);
            return;
        }

        URLClassPath urlClassPath = getURLClassPath(urlClassLoader);
        if (urlClassPath == null) {
            // couldn't get the class path... error was already logged
            return;
        }
        urlClassPath.addURL(jar);

        rebuildJavaClassPathVariable(urlClassPath);
    }

    private static URLClassPath getURLClassPath(URLClassLoader loader) {
        Field ucpField = (Field) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                Field ucp = null;
                try {
                    ucp = URLClassLoader.class.getDeclaredField("ucp");
                    ucp.setAccessible(true);
                } catch (Exception e) {
                    log.warn("Could not install compiler: Could not obtain access to ucp field of the URLClassLoader", e);
                }
                return ucp;
            }
        });

        if (ucpField == null) {
            return null;
        }
        try {
            return (URLClassPath) ucpField.get(loader);
        } catch (IllegalAccessException e) {
            log.warn("Could not install compiler: Could not obtain access to ucp field of the URLClassLoader", e);
            return null;
        }
    }

    private static void rebuildJavaClassPathVariable(URLClassPath urlClassPath) {
        URL[] urls = urlClassPath.getURLs();
        if (urls.length < 1) {
            return;
        }

        StringBuffer path = new StringBuffer(urls.length * 32);

        for (int i = 0; i < urls.length; i++) {
            if (i != 0) {
                path.append(File.pathSeparator);
            }

            path.append(new File(urls[i].getFile()).getPath());
        }
        try {
            System.setProperty("java.class.path", path.toString());
        } catch (Exception e) {
            log.warn("Error installing compiler: Could not update java.class.path property which may cause compiler to not work correctly", e);
        }
    }
}
