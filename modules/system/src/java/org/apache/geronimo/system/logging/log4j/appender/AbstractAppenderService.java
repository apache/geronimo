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
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.system.logging.log4j.PatternLayout;
import org.apache.geronimo.system.logging.log4j.XLevel;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/06/02 05:33:05 $
 */
public abstract class AbstractAppenderService implements GBean {
    protected final WriterAppender appender;

    public AbstractAppenderService(WriterAppender appender) {
        this.appender = appender;
        appender.setLayout(new PatternLayout());
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() {
        appender.activateOptions();
        Logger root = Logger.getRootLogger();
        root.addAppender(appender);
    }

    public void doStop() {
        Logger root = Logger.getRootLogger();
        root.removeAppender(appender);
    }

    public void doFail() {
        doStop();
    }

    public String getLayoutPattern() {
        PatternLayout layout = (PatternLayout) appender.getLayout();
        return layout.getConversionPattern();
    }

    public void setLayoutPattern(String pattern) {
        PatternLayout layout = (PatternLayout) appender.getLayout();
        layout.setConversionPattern(pattern);
    }

    public String getThreshold() {
        return appender.getThreshold().toString();
    }

    public void setThreshold(String threshold) {
        appender.setThreshold(XLevel.toLevel(threshold));
    }

    public String getEncoding() {
        return appender.getEncoding();
    }

    public void setEncoding(String value) {
        appender.setEncoding(value);
    }

    public void setImmediateFlush(boolean value) {
        appender.setImmediateFlush(value);
    }

    public boolean getImmediateFlush() {
        return appender.getImmediateFlush();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractAppenderService.class);
        infoFactory.addAttribute("LayoutPattern", String.class, true);
        infoFactory.addAttribute("Threshold", String.class, true);
        infoFactory.addAttribute("Encoding", String.class, true);
        infoFactory.addAttribute("ImmedateFlush", boolean.class, true);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
