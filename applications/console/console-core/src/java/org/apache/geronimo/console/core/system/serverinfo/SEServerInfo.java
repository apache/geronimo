/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.core.system.serverinfo;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

public class SEServerInfo {
    private static final String PLATFORM_ARCH = "os.arch";

    public String getVersion() {
        return ServerConstants.getVersion();
    }

    public String getBuildDate() {
        return ServerConstants.getBuildDate();
    }

    public String getBuildTime() {
        return ServerConstants.getBuildTime();
    }

    public String getCopyright() {
        return ServerConstants.getCopyright();
    }

    public String getGeronimoBuildVersion() {
        return ServerConstants.getGeronimoBuildVersion();
    }

    public String getGeronimoSpecVersion() {
        return ServerConstants.getGeronimoSpecVersion();
    }

    public String getPortalCoreVersion() {
        return ServerConstants.getPortalCoreVersion();
    }

    public String getPlatformArch() {
        return System.getProperty(PLATFORM_ARCH);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SEServerInfo.class);

        infoFactory.addAttribute("version", String.class, false);
        infoFactory.addAttribute("buildDate", String.class, false);
        infoFactory.addAttribute("buildTime", String.class, false);
        infoFactory.addAttribute("copyright", String.class, false);
        infoFactory.addAttribute("geronimoBuildVersion", String.class, false);
        infoFactory.addAttribute("geronimoSpecVersion", String.class, false);
        infoFactory.addAttribute("portalCoreVersion", String.class, false);
        infoFactory.addAttribute("platformArch", String.class, false);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
