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

import java.util.Enumeration;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;

/**
 * An extention of the default Log4j DailyRollingFileAppender
 * which will make the directory structure for the set log file.
 *
 * @version $Rev$ $Date$
 */
public class ConsoleAppenderService extends AbstractAppenderService {
    public ConsoleAppenderService() {
        super(getConsoleAppender());
    }

    private static ConsoleAppender getConsoleAppender() {
        Logger root = Logger.getRootLogger();
        for(Enumeration e = root.getAllAppenders(); e.hasMoreElements();) {
            Appender appender = (Appender)e.nextElement();
            if (appender instanceof ConsoleAppender) {
                return (ConsoleAppender)appender;
            }
        }
        return new ConsoleAppender();
    }

    public void doStart() {
        appender.activateOptions();
        Logger root = Logger.getRootLogger();
        root.addAppender(appender);
    }

    public void doStop() {
    }

    public void doFail() {
    }

    public String getTarget() {
        return ((ConsoleAppender) appender).getTarget();
    }

    public void setTarget(String target) {
        ((ConsoleAppender) appender).setTarget(target);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ConsoleAppenderService.class, AbstractAppenderService.GBEAN_INFO);
        infoFactory.addAttribute("target", String.class, true);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
