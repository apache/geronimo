/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    public static final String SERVER_NAME_SYS_PROP = "org.apache.geronimo.server.name";
    public static final String SERVER_DIR_SYS_PROP = "org.apache.geronimo.server.dir";
    public static final String HOME_DIR_SYS_PROP = "org.apache.geronimo.home.dir";
    public static final String LOG4J_CONFIG_PROP = "org.apache.geronimo.log4jservice.configuration";

    public static File getGeronimoHome() throws IOException {
        File rc = null;

        // Use the system property if specified.
        String path = System.getProperty(HOME_DIR_SYS_PROP);
        if (path != null) {
            rc = validateDirectory(path, "Invalid Geronimo home directory.", false);
        }

        // Try to figure it out using the jar file this class was loaded from.
        if (rc == null) {
            // guess the home from the location of the jar
            URL url = Main.class.getClassLoader().getResource(Main.class.getName().replace(".", "/") + ".class");
            if (url != null) {
                try {
                    JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                    url = jarConnection.getJarFileURL();
                    rc = new File(new URI(url.toString())).getCanonicalFile().getParentFile().getParentFile();
                } catch (Exception ignored) {
                }
            }
        }

        if (rc == null) {
            throw new IOException("The Geronimo install directory could not be determined.  Please set the " + HOME_DIR_SYS_PROP + " system property");
        }

        return rc;
    }

    public static File getGeronimoBase(File base) {
        File baseServerDir;

        // first check if the base server directory has been provided via
        // system property override.
        String baseServerDirPath = System.getProperty(SERVER_DIR_SYS_PROP);
        if (baseServerDirPath == null) {
            // then check if a server name has been provided
            String serverName = System.getProperty(SERVER_NAME_SYS_PROP);
            if (serverName == null) {
                // default base server directory.
                baseServerDir = base;
            } else {
                baseServerDir = new File(base, serverName);
            }
        } else {
            baseServerDir = new File(baseServerDirPath);
            if (!baseServerDir.isAbsolute()) {
                baseServerDir = new File(base, baseServerDirPath);
            }
        }

        validateDirectory(baseServerDir, "Invalid Geronimo server directory.", false);

        return baseServerDir;
    }

    public static void setTempDirectory(File base) {
        String tmpDirPath = System.getProperty("java.io.tmpdir", "temp");
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.isAbsolute()) {
            tmpDir = new File(base, tmpDirPath);
        }

        validateDirectory(tmpDir, "Invalid temporary directory.", true);

        System.setProperty("java.io.tmpdir", tmpDir.getAbsolutePath());
    }

    public static void setLog4jConfigurationFile(File base, String defaultFile) {
        String log4jFilePath = System.getProperty(LOG4J_CONFIG_PROP, defaultFile);
        if (log4jFilePath != null) {
            File log4jFile = new File(log4jFilePath);
            if (!log4jFile.isAbsolute()) {
                log4jFile = new File(base, log4jFilePath);
            }
            System.setProperty(LOG4J_CONFIG_PROP, log4jFile.getAbsolutePath());
        }
    }

    public static File validateDirectory(String path, String errPrefix, boolean checkWritable) {
        return validateDirectory(new File(path), errPrefix, checkWritable);
    }

    public static File validateDirectory(File path, String errPrefix, boolean checkWritable) {
        File rc;
        try {
            rc = path.getCanonicalFile();
        } catch (IOException e) {
            throw new IllegalArgumentException(errPrefix + " '" + path + "' : " + e.getMessage());
        }
        if (!rc.exists()) {
            throw new IllegalArgumentException(errPrefix + " The '" + path + "' path does not exist.");
        }
        if (!rc.isDirectory()) {
            throw new IllegalArgumentException(errPrefix + " The '" + path + "' path is not a directory.");
        }
        if (!rc.canRead()) {
            throw new IllegalArgumentException(errPrefix + " The '" + path + "' directory is not readable.");
        }
        if (checkWritable && !rc.canWrite()) {
            throw new IllegalArgumentException(errPrefix + " The '" + path + "' directory is not writable.");
        }
        return rc;
    }

    public static Properties loadPropertiesFile(File file, boolean critical) throws IOException {
        // Read the properties file.
        Properties configProps = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            configProps.load(is);
        } catch (FileNotFoundException ex) {
            if (critical) {
                throw ex;
            }
        } catch (IOException ex) {
            System.err.println("Error loading properties from " + file);
            return null;
        } finally {
            if (is != null) {
                try { is.close(); } catch (Exception e) {}
            }
        }
        return configProps;
    }

    private static final String DELIM_START = "${";
    private static final String DELIM_STOP = "}";

    /**
     * <p>
     * This method performs property variable substitution on the
     * specified value. If the specified value contains the syntax
     * <tt>${&lt;prop-name&gt;}</tt>, where <tt>&lt;prop-name&gt;</tt>
     * refers to either a configuration property or a system property,
     * then the corresponding property value is substituted for the variable
     * placeholder. Multiple variable placeholders may exist in the
     * specified value as well as nested variable placeholders, which
     * are substituted from inner most to outer most. Configuration
     * properties override system properties.
     * </p>
     *
     * @param val         The string on which to perform property substitution.
     * @param currentKey  The key of the property being evaluated used to
     *                    detect cycles.
     * @param cycleMap    Map of variable references used to detect nested cycles.
     * @param configProps Set of configuration properties.
     * @return The value of the specified string after system property substitution.
     * @throws IllegalArgumentException If there was a syntax error in the
     *                                  property placeholder syntax or a recursive variable reference.
     */
    public static String substVars(String val, String currentKey,
                                   Map<String, String> cycleMap,
                                   Properties configProps)
            throws IllegalArgumentException {
        // If there is currently no cycle map, then create
        // one for detecting cycles for this invocation.
        if (cycleMap == null) {
            cycleMap = new HashMap<String, String>();
        }

        // Put the current key in the cycle map.
        cycleMap.put(currentKey, currentKey);

        // Assume we have a value that is something like:
        // "leading ${foo.${bar}} middle ${baz} trailing"

        // Find the first ending '}' variable delimiter, which
        // will correspond to the first deepest nested variable
        // placeholder.
        int stopDelim = val.indexOf(DELIM_STOP);

        // Find the matching starting "${" variable delimiter
        // by looping until we find a start delimiter that is
        // greater than the stop delimiter we have found.
        int startDelim = val.indexOf(DELIM_START);
        while (stopDelim >= 0) {
            int idx = val.indexOf(DELIM_START, startDelim + DELIM_START.length());
            if ((idx < 0) || (idx > stopDelim)) {
                break;
            } else if (idx < stopDelim) {
                startDelim = idx;
            }
        }

        // If we do not have a start or stop delimiter, then just
        // return the existing value.
        if ((startDelim < 0) && (stopDelim < 0)) {
            return val;
        }
        // At this point, we found a stop delimiter without a start,
        // so throw an exception.
        else if (((startDelim < 0) || (startDelim > stopDelim))
                && (stopDelim >= 0)) {
            throw new IllegalArgumentException(
                    "stop delimiter with no start delimiter: "
                            + val);
        }

        // At this point, we have found a variable placeholder so
        // we must perform a variable substitution on it.
        // Using the start and stop delimiter indices, extract
        // the first, deepest nested variable placeholder.
        String variable =
                val.substring(startDelim + DELIM_START.length(), stopDelim);

        // Verify that this is not a recursive variable reference.
        if (cycleMap.get(variable) != null) {
            throw new IllegalArgumentException(
                    "recursive variable reference: " + variable);
        }

        // Get the value of the deepest nested variable placeholder.
        // Try to configuration properties first.
        String substValue = (configProps != null)
                ? configProps.getProperty(variable, null)
                : null;
        if (substValue == null) {
            // Ignore unknown property values.
            substValue = System.getProperty(variable, "");
        }

        // Remove the found variable from the cycle map, since
        // it may appear more than once in the value and we don't
        // want such situations to appear as a recursive reference.
        cycleMap.remove(variable);

        // Append the leading characters, the substituted value of
        // the variable, and the trailing characters to get the new
        // value.
        val = val.substring(0, startDelim)
                + substValue
                + val.substring(stopDelim + DELIM_STOP.length(), val.length());

        // Now perform substitution again, since there could still
        // be substitutions to make.
        val = substVars(val, currentKey, cycleMap, configProps);

        // Return the value.
        return val;
    }

    public static boolean recursiveDelete(File root) {
        if (root == null) {
            return true;
        }

        boolean ok = true;

        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        ok = recursiveDelete(file) && ok;
                    } else {
                        ok = file.delete() && ok;
                    }
                }
            }
        }

        ok = root.delete() && ok;

        return ok;
    }

    public static void clearSunJarFileFactoryCache(final String jarLocation) {
        clearSunJarFileFactoryCacheImpl(jarLocation, 5);
    }

    /**
     * Due to several different implementation changes in various JDK releases the code here is not as
     * straight forward as reflecting debug items in your current runtime. There have even been breaking changes
     * between 1.6 runtime builds, let alone 1.5.
     * <p/>
     * If you discover a new issue here please be careful to ensure the existing functionality is 'extended' and not
     * just replaced to match your runtime observations.
     * <p/>
     * If you want to look at the mess that leads up to this then follow the source code changes made to
     * the class sun.net.www.protocol.jar.JarFileFactory over several years.
     *
     * @param jarLocation String
     * @param attempt     int
     */
    @SuppressWarnings({"unchecked"})
    private static synchronized void clearSunJarFileFactoryCacheImpl(final String jarLocation, final int attempt) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Clearing Sun JarFileFactory cache for directory " + jarLocation);
        }
        try {
            final Class<?> jarFileFactory = Class.forName("sun.net.www.protocol.jar.JarFileFactory");

            //Do not generify these maps as their contents are NOT stable across runtimes.
            final Field fileCacheField = jarFileFactory.getDeclaredField("fileCache");
            fileCacheField.setAccessible(true);
            final Map fileCache = (Map) fileCacheField.get(null);
            final Map fileCacheCopy = new HashMap(fileCache);

            final Field urlCacheField = jarFileFactory.getDeclaredField("urlCache");
            urlCacheField.setAccessible(true);
            final Map urlCache = (Map) urlCacheField.get(null);
            final Map urlCacheCopy = new HashMap(urlCache);

            //The only stable item we have here is the JarFile/ZipFile in this map
            Iterator iterator = urlCacheCopy.entrySet().iterator();
            final List urlCacheRemoveKeys = new ArrayList();

            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final Object key = entry.getKey();

                if (key instanceof ZipFile) {
                    final ZipFile zf = (ZipFile) key;
                    final File file = new File(zf.getName());  //getName returns File.getPath()
                    if (isParent(jarLocation, file)) {
                        //Flag for removal
                        urlCacheRemoveKeys.add(key);
                    }
                } else {
                    logger.warning("Unexpected key type: " + key);
                }
            }

            iterator = fileCacheCopy.entrySet().iterator();
            final List fileCacheRemoveKeys = new ArrayList();

            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final Object value = entry.getValue();

                if (urlCacheRemoveKeys.contains(value)) {
                    fileCacheRemoveKeys.add(entry.getKey());
                }
            }

            //Use these unstable values as the keys for the fileCache values.
            iterator = fileCacheRemoveKeys.iterator();
            while (iterator.hasNext()) {

                final Object next = iterator.next();

                try {
                    final Object remove = fileCache.remove(next);
                    if (logger.isLoggable(Level.FINE) && null != remove) {
                        logger.fine("Removed item from fileCache: " + remove);
                    }
                } catch (Throwable e) {
                    logger.warning("Failed to remove item from fileCache: " + next);
                }
            }

            iterator = urlCacheRemoveKeys.iterator();
            while (iterator.hasNext()) {

                final Object next = iterator.next();

                try {
                    final Object remove = urlCache.remove(next);

                    try {
                        ((ZipFile) next).close();
                    } catch (Throwable e) {
                        //Ignore
                    }

                    if (logger.isLoggable(Level.FINE) && null != remove) {
                        logger.fine("Removed item from urlCache: " + remove);
                    }
                } catch (Throwable e) {
                    logger.warning("Failed to remove item from urlCache: " + next);
                }

            }

        } catch (ConcurrentModificationException e) {
            if (attempt > 0) {
                clearSunJarFileFactoryCacheImpl(jarLocation, (attempt - 1));
            } else {
                logger.warning("Unable to clear Sun JarFileFactory cache after 5 attempts" + e.getMessage());
            }
        } catch (ClassNotFoundException e) {
            // not a sun vm
        } catch (NoSuchFieldException e) {
            // different version of sun vm?
        } catch (Throwable e) {
            logger.warning("Unable to clear Sun JarFileFactory cache " + e.getMessage());
        }
    }

    private static boolean isParent(String jarLocation, File file) {
        File dir = new File(jarLocation);
        while (file != null) {
            if (file.equals(dir)) {
                return true;
            }
            file = file.getParentFile();
        }
        return false;
    }
}
