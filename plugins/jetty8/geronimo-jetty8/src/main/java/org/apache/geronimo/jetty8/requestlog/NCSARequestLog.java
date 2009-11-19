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
package org.apache.geronimo.jetty8.requestlog;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.jetty8.JettyContainer;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class NCSARequestLog implements GBeanLifecycle, JettyRequestLog {
    private final JettyContainer container;
    private final ServerInfo serverInfo;
    private final org.eclipse.jetty.server.NCSARequestLog requestLog;
    private boolean preferProxiedForAddress;
    private String filename;

    public NCSARequestLog(JettyContainer container, ServerInfo serverInfo) {
        this.container = container;
        this.serverInfo = serverInfo;
        requestLog = new org.eclipse.jetty.server.NCSARequestLog();
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setLogDateFormat(String format) {
        requestLog.setLogDateFormat(format);
    }

    public String getLogDateFormat() {
        return requestLog.getLogDateFormat();
    }

    public void setLogTimeZone(String tz) {
        requestLog.setLogTimeZone(tz);
    }

    public String getLogTimeZone() {
        return requestLog.getLogTimeZone();
    }

    public int getRetainDays() {
        return requestLog.getRetainDays();
    }

    public void setRetainDays(int retainDays) {
        requestLog.setRetainDays(retainDays);
    }

    public boolean isExtended() {
        return requestLog.isExtended();
    }

    public void setExtended(boolean e) {
        requestLog.setExtended(e);
    }

    public boolean isAppend() {
        return requestLog.isAppend();
    }

    public void setAppend(boolean a) {
        requestLog.setAppend(a);
    }

    public void setIgnorePaths(String[] ignorePaths) {
        requestLog.setIgnorePaths(ignorePaths);
    }

    public String[] getIgnorePaths() {
        return requestLog.getIgnorePaths();
    }

    public void setPreferProxiedForAddress(boolean value) {
        this.preferProxiedForAddress = value;
        requestLog.setPreferProxiedForAddress(value);
    }

    public boolean isPreferProxiedForAddress() {
        return preferProxiedForAddress;
    }

    public String getAbsoluteFilePath() {
        return requestLog == null ? null : requestLog.getDatedFilename();
    }

    public void doStart() throws Exception {
        requestLog.setFilename(serverInfo.resolveServerPath(filename));
        container.setRequestLog(requestLog);
        requestLog.start();
    }

    public void doStop() throws Exception {
        container.setRequestLog(null);
    }

    public void doFail() {
        container.setRequestLog(null);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("NCSA Request Log", NCSARequestLog.class);
        infoFactory.addReference("JettyContainer", JettyContainer.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addReference("ServerInfo", ServerInfo.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);

        infoFactory.addInterface(JettyRequestLog.class, new String[]{"filename", "logDateFormat", "logTimeZone",
                "retainDays", "extended", "append", "ignorePaths", "preferProxiedForAddress", });

        infoFactory.setConstructor(new String[]{"JettyContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
