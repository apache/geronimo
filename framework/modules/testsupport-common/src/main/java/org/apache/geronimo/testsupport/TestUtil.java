/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.testsupport;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class TestUtil {

    private final Class owningClass;

    /**
     * The base-directory which tests should be run from.
     *
     * @see #initBaseDir()   This field is initialized from the return of this method on instance construction.
     */
    protected final File baseDir;
    
    /**
     * Instance logger which tests should use to produce tracing information.
     *
     * <p>
     * Unless you have a really good reason to, do not change this field from your sub-class.
     * And if you do, please document why you have done so.
     */
    protected final Logger log;

    
    public TestUtil(Class owningClass) {
        if (null == owningClass) {
            throw new IllegalArgumentException("owningClass is required");
        }
        this.owningClass = owningClass;
        
        baseDir = initBaseDir();

        //
        // NOTE: Logging must be initialized after BASEDIR has been discovered, as it is used
        //       by the log4j logging-config properties to set the target/test.log file.
        //
        log = LoggerFactory.getLogger(owningClass);
    }
    
    public File getBaseDir() {
        return baseDir;
    }

    public Logger getLog() {
        return log;
    }

    /**
     * Determine the value of <tt>${basedir}</tt>, which should be the base directory of
     * the module which the concreate test class is defined in.
     *
     * <p>
     * If The system property <tt>basedir</tt> is already set, then that value is used,
     * otherwise we determine the value from the codesource of the containing concrete class
     * and set the <tt>basedir</tt> system property to that value.
     *
     * @see #baseDir    This field is always initialized to the value which this method returns.
     *
     * @return  The base directory of the module which contains the concreate test class.
     */
    protected final File initBaseDir() {
        File dir;

        // If ${basedir} is set, then honor it
        String tmp = System.getProperty("basedir");
        if (tmp != null) {
            dir = new File(tmp);
        }
        else {
            // Find the directory which this class (or really the sub-class of TestSupport) is defined in.
            String path = owningClass.getProtectionDomain().getCodeSource().getLocation().getFile();

            // We expect the file to be in target/test-classes, so go up 2 dirs
            dir = new File(path).getParentFile().getParentFile();

            // Set ${basedir} which is needed by logging to initialize
            System.setProperty("basedir", dir.getPath());
        }

        // System.err.println("Base Directory: " + dir);

        return dir;
    }
    
    /**
     * Resolve the given path to a file rooted to {@link #baseDir}.
     *
     * @param path  The path to resolve.
     * @return      The resolved file for the given path.
     */
    public final File resolveFile(final String path) {
        assert path != null;
        
        File file = new File(path);
        
        // Complain if the file is already absolute... probably an error
        if (file.isAbsolute()) {
            log.warn("Given path is already absolute; nothing to resolve: {}", file);
        }
        else {
            file = new File(baseDir, path);
        }
        
        return file;
    }
    
    /**
     * Resolve the given path to a path rooted to {@link #baseDir}.
     *
     * @param path  The path to resolve.
     * @return      The resolved path for the given path.
     *
     * @see #resolveFile(String)
     */
    public final String resolvePath(final String path) {
        assert path != null;
        
        return resolveFile(path).getPath();
    }

}
