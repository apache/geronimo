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
package org.apache.geronimo.system.configuration.condition;

/**
 * Provides access to operating system details for use in condition expressions.
 *
 * @version $Rev$ $Date$
 */
public class OsVariable
{
    public String getArch() {
        return SystemUtils.OS_ARCH;
    }

    public String getName() {
        return SystemUtils.OS_NAME;
    }

    public String getVersion() {
        return SystemUtils.OS_VERSION;
    }

    public boolean getIsAIX() {
        return SystemUtils.IS_OS_AIX;
    }

    public boolean getIsHPUX() {
        return SystemUtils.IS_OS_HP_UX;
    }

    public boolean getIsIrix() {
        return SystemUtils.IS_OS_IRIX;
    }

    public boolean getIsLinux() {
        return SystemUtils.IS_OS_LINUX;
    }

    public boolean getIsMac() {
        return SystemUtils.IS_OS_MAC;
    }

    public boolean getIsMacOSX() {
        return SystemUtils.IS_OS_MAC_OSX;
    }

    public boolean getIsOS2() {
        return SystemUtils.IS_OS_OS2;
    }

    public boolean getIsSolaris() {
        return SystemUtils.IS_OS_SOLARIS;
    }

    public boolean getIsSunOS() {
        return SystemUtils.IS_OS_SUN_OS;
    }

    public boolean getIsUnix() {
        return SystemUtils.IS_OS_UNIX;
    }

    public boolean getIsWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    public boolean getIsWindows2000() {
        return SystemUtils.IS_OS_WINDOWS_2000;
    }

    public boolean getIsWindows95() {
        return SystemUtils.IS_OS_WINDOWS_95;
    }

    public boolean getIsWindows98() {
        return SystemUtils.IS_OS_WINDOWS_98;
    }

    public boolean getIsWindowsME() {
        return SystemUtils.IS_OS_WINDOWS_ME;
    }

    public boolean getIsWindowsNT() {
        return SystemUtils.IS_OS_WINDOWS_NT;
    }

    public boolean getIsWindowsXP() {
        return SystemUtils.IS_OS_WINDOWS_XP;
    }
}
