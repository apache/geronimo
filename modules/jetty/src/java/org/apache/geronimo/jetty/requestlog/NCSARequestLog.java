/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.jetty.requestlog;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.jetty.JettyContainer;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class NCSARequestLog implements GBeanLifecycle {
    private final JettyContainer container;
    private final ServerInfo serverInfo;
    private final org.mortbay.http.NCSARequestLog requestLog;
    private boolean preferProxiedForAddress;
    private String filename;

    public NCSARequestLog(JettyContainer container, ServerInfo serverInfo) {
        this.container = container;
        this.serverInfo = serverInfo;
        requestLog = new org.mortbay.http.NCSARequestLog();
    }

    public boolean isBuffered() {
        return requestLog.isBuffered();
    }

    public void setBuffered(boolean buffered) {
        requestLog.setBuffered(buffered);
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

    public void doStart() throws Exception {
        requestLog.setFilename(serverInfo.resolvePath(filename));
        container.setRequestLog(requestLog);
        requestLog.start();
    }

    public void doStop() throws Exception {
        requestLog.stop();
        container.setRequestLog(null);
    }

    public void doFail() {
        container.setRequestLog(null);
        requestLog.stop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("NCSA Request Log", NCSARequestLog.class);
        infoFactory.addReference("JettyContainer", JettyContainer.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addAttribute("filename", String.class, true);
        infoFactory.addAttribute("logDateFormat", String.class, true);
        infoFactory.addAttribute("logTimeZone", String.class, true);
        infoFactory.addAttribute("retainDays", int.class, true);
        infoFactory.addAttribute("extended", boolean.class, true);
        infoFactory.addAttribute("append", boolean.class, true);
        infoFactory.addAttribute("buffered", boolean.class, true);
        infoFactory.addAttribute("ingorePaths", String[].class, true);
        infoFactory.addAttribute("preferProxiedForAddress", boolean.class, true);

        infoFactory.setConstructor(new String[]{"JettyContainer", "ServerInfo"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
