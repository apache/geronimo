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
package org.apache.geronimo.interop.properties;

import org.apache.geronimo.interop.util.FileUtil;


public class SystemProperties extends PropertyMap {
    // Not a component as it is required for bootstrapping
    // and we want to avoid circular build dependencies.

    // properties

    public static final BooleanProperty debugProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.debug");

    public static final BooleanProperty quietProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.quiet");

    public static final BooleanProperty verboseProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.verbose");

    public static final StringProperty homeProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.home")
            .isDirName();

    public static final StringProperty repositoryProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.repository")
            .isDirName();

    public static final StringProperty logFileProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.logFile")
            .defaultValue("~/logs/default.log")
            .isFileName();

    public static final StringProperty tempDirProperty =
            new StringProperty(SystemProperties.class, "org.apache.geronimo.interop.tempDir")
            .isFileName();

    public static final IntProperty rmiNamingContextCacheTimeoutProperty =
            new IntProperty(SystemProperties.class, "org.apache.geronimo.interop.rmi.namingContextCacheTimeout")
            .defaultValue(600); // 10 minutes

    public static final IntProperty rmiSocketTimeoutProperty =
            new IntProperty(SystemProperties.class, "org.apache.geronimo.interop.rmi.socketTimeout")
            .defaultValue(600); // 10 minutes

    public static final BooleanProperty rmiTraceProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.rmiTrace");

    public static final BooleanProperty useThreadLocalRmiConnectionPoolsProperty =
            new BooleanProperty(SystemProperties.class, "org.apache.geronimo.interop.rmi.useThreadLocalConnectionPools");

    // privata data

    private static SystemProperties _instance;
    private boolean _canAccessFileSystem = true;
    private boolean _rmiTrace;
    private boolean _debug;
    private boolean _quiet;
    private boolean _verbose;
    private String _home;
    private String _repository;
    private String _tempDir;

    static {
        _instance = new SystemProperties();
        _instance.init();
    }

    public static SystemProperties getInstance() {
        return _instance;
    }

    // private methods

    private SystemProperties() {
        try {
            putAll(System.getProperties());
        } catch (Exception ignore) // e.g. due to Applet Security Manager
        {
            _canAccessFileSystem = false;
        }
    }

    private void init() {
        _debug = debugProperty.getBoolean();
        _quiet = quietProperty.getBoolean();
        _verbose = verboseProperty.getBoolean();

        if (_verbose) {
            System.out.println("System Property org.apache.geronimo.interop.debug = " + _debug);
            System.out.println("System Property org.apache.geronimo.interop.verbose = true");
        }

        _rmiTrace = rmiTraceProperty.getBoolean();

        homeProperty.defaultValue("/org.apache.geronimo.interop");
        _home = homeProperty.getString();

        repositoryProperty.defaultValue(_home + "/Repository");
        _repository = repositoryProperty.getString();

        tempDirProperty.defaultValue(_home + "/temp");
        _tempDir = tempDirProperty.getString();
    }

    // public methods

    public static boolean rmiTrace() {
        return getInstance()._rmiTrace;
    }

    public static boolean debug() {
        return getInstance()._debug;
    }

    public static boolean quiet() {
        return getInstance()._quiet;
    }

    public static boolean verbose() {
        return getInstance()._verbose;
    }

    public static boolean canAccessFileSystem() {
        return getInstance()._canAccessFileSystem;
    }

    public static String logFile() {
        // Note: this is not necessarily a constant.
        // Application might call System.setProperty to change "org.apache.geronimo.interop.logFile".
        try {
            String file = System.getProperty(logFileProperty.getPropertyName(), logFileProperty.getDefaultValue());
            file = FileUtil.expandHomeRelativePath(file);
            return file;
        } catch (Exception ex) // e.g. SecurityException in Applet
        {
            return logFileProperty.getString();
        }
    }

    public static String getHome() {
        return getInstance()._home;
    }

    public static String getRepository() {
        return getInstance()._repository;
    }

    public static String getTempDir() {
        return getInstance()._tempDir;
    }
}
