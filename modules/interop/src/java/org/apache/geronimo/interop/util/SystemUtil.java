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

import org.apache.geronimo.interop.properties.StringProperty;
import org.apache.geronimo.interop.properties.SystemProperties;


public class SystemUtil {
    // properties

    public static final StringProperty vmVersionProperty =
            new StringProperty(SystemProperties.class, "java.vm.version");

    // private data

    private static String _vmVersion = vmVersionProperty.getString();

    private static boolean _isJDK13 = _vmVersion.startsWith("1.3")
                                      || _vmVersion.startsWith("CrE-ME V4.00");

    private static boolean _isJDK14 = _vmVersion.startsWith("1.4");

    // public methods

    public static String getExecutableSuffix() {
        return isWindows() ? ".exe" : "";
    }

    public static String getShellScriptSuffix() {
        return isWindows() ? ".bat" : ".sh";
    }

    public static boolean isJDK13() {
        return _isJDK13;
    }

    public static boolean isJDK14() {
        return _isJDK14;
    }

    public static boolean isWindows() {
        return java.io.File.separatorChar == '\\';
    }
}
