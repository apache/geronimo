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
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.log4j.RollingFileAppender;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:30 $
 */
public class RollingFileAppenderService extends FileAppenderService {
    public RollingFileAppenderService(ServerInfo serverInfo) {
        super(serverInfo, new RollingFileAppender());
    }

    public int getMaxBackupIndex() {
        return ((RollingFileAppender) appender).getMaxBackupIndex();
    }

    public void setMaxBackupIndex(int maxBackupIndex) {
        ((RollingFileAppender) appender).setMaxBackupIndex(maxBackupIndex);
    }

    public String getMaximumFileSize() {
        return "" + ((RollingFileAppender) appender).getMaximumFileSize();
    }

    public void setMaxFileSize(String maxFileSize) {
        ((RollingFileAppender) appender).setMaxFileSize(maxFileSize);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(RollingFileAppenderService.class.getName(), FileAppenderService.GBEAN_INFO);
        infoFactory.addAttribute(new GAttributeInfo("MaxBackupIndex", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxFileSize", true));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
