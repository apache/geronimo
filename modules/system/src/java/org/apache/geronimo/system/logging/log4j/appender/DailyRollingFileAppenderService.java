/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.system.logging.log4j.appender;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.DailyRollingFileAppender;

/**
 * An extention of the default Log4j DailyRollingFileAppender
 * which will make the directory structure for the set log file.
 *
 * @version $Rev$ $Date$
 */
public class DailyRollingFileAppenderService extends FileAppenderService {
    public DailyRollingFileAppenderService(ServerInfo serverInfo) {
        super(serverInfo, new DailyRollingFileAppender());
    }

    public String getDatePattern() {
        return ((DailyRollingFileAppender) appender).getDatePattern();
    }

    public void setDatePattern(String pattern) {
        ((DailyRollingFileAppender) appender).setDatePattern(pattern);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(DailyRollingFileAppenderService.class, FileAppenderService.GBEAN_INFO);
        infoFactory.addAttribute("datePattern", String.class, true);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
