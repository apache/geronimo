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

package org.apache.geronimo.console.infomanager;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.util.ObjectNameConstants;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public class ServerInfoPortlet extends GenericPortlet {

    public static final String SVRINFO_BASEDIR = "baseDirectory";

    public static final String SVRINFO_VERSION = "version";

    public static final String SVRINFO_BUILDDATE = "buildDate";

    public static final String SVRINFO_BUILDTIME = "buildTime";

    public static final String SVRINFO_COPYRIGHT = "copyright";

    public static final String SVRINFO_PLATFORMARCH = "platformArch";

    public static final String SVRINFO_GERONIMO_BUILD_VERSION = "geronimoBuildVersion";

    public static final String SVRINFO_GERONIMO_SPEC_VERSION = "geronimoSpecVersion";

    public static final String SVRINFO_PORTAL_CORE_VERSION = "portalCoreVersion";

    public static final String JVMIMPL_JAVAVER = "javaVersion";

    public static final String JVMIMPL_JAVAVENDOR = "javaVendor";

    public static final String JVMIMPL_NODE = "node";

    public static final String JVMIMPL_FREEMEM = "freeMemory";

    public static final String JVMIMPL_MAXMEM = "maxMemory";

    public static final String JVMIMPL_TOTALMEM = "totalMemory";

    public static final String JVMIMPL_AVAILABLEPROCS = "availableProcessors";

    private static final String NORMALVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoNormal.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoMaximized.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoHelp.jsp";

    private static Map svrProps = new HashMap();

    private static Map jvmProps = new HashMap();

    private Kernel kernel;

    private PortletRequestDispatcher normalView;

    private PortletRequestDispatcher maximizedView;

    private PortletRequestDispatcher helpView;

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
    }

    protected void doView(RenderRequest renderRequest,
            RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }

        try {
            Object o = null;

            // Kernel boot time
            Date bootDate = kernel.getBootTime();
            long bootTime = bootDate.getTime() / 1000;
            long currentTime = System.currentTimeMillis() / 1000;
            long elapsedTime = currentTime - bootTime;
            svrProps.put("Kernel Boot Time", bootDate);
            svrProps.put("Kernel Up Time", calcElapsedTime(elapsedTime));

            // Server info
            /*
             * ObjectName svrInfo = new
             * ObjectName(ObjectNameConstants.SERVER_INFO_OBJECT_NAME); o =
             * kernel.getAttribute(svrInfo, SVRINFO_BASEDIR); svrProps.put("Base
             * Directory", o); ObjectName joeSvrInfo = new
             * ObjectName(ObjectNameConstants.SE_SERVER_INFO_NAME); o =
             * kernel.getAttribute(joeSvrInfo, SVRINFO_PLATFORMARCH);
             * svrProps.put("Platform Architecture", o); o =
             * kernel.getAttribute(joeSvrInfo, SVRINFO_VERSION);
             * svrProps.put("Version", o); o = kernel.getAttribute(joeSvrInfo,
             * SVRINFO_GERONIMO_BUILD_VERSION); svrProps.put("Apache Geronimo
             * Build Version", o); o = kernel.getAttribute(joeSvrInfo,
             * SVRINFO_GERONIMO_SPEC_VERSION); svrProps.put("J2EE Specifications
             * Version", o); o = kernel.getAttribute(joeSvrInfo,
             * SVRINFO_PORTAL_CORE_VERSION); svrProps.put("JSR 168 Portal
             * Version", o); o = kernel.getAttribute(joeSvrInfo,
             * SVRINFO_BUILDDATE); svrProps.put("Build Date", o); o =
             * kernel.getAttribute(joeSvrInfo, SVRINFO_BUILDTIME);
             * svrProps.put("Build Time", o); o =
             * kernel.getAttribute(joeSvrInfo, SVRINFO_COPYRIGHT);
             * svrProps.put("Copyright", o);
             */
            renderRequest.setAttribute("svrProps", svrProps);

            // JVM info
            ObjectName jvmImpl = new ObjectName(
                    ObjectNameConstants.JVM_IMPL_NAME);
            o = kernel.getAttribute(jvmImpl, JVMIMPL_JAVAVER);
            jvmProps.put("Java Version", o);
            o = kernel.getAttribute(jvmImpl, JVMIMPL_JAVAVENDOR);
            jvmProps.put("Java Vendor", o);
            o = kernel.getAttribute(jvmImpl, JVMIMPL_NODE);
            jvmProps.put("Node", o);
            o = kernel.getAttribute(jvmImpl, JVMIMPL_MAXMEM);
            jvmProps.put("Max Memory", calcMemory((Long) o));
            o = kernel.getAttribute(jvmImpl, JVMIMPL_TOTALMEM);
            jvmProps.put("Total Memory", calcMemory((Long) o));
            o = kernel.getAttribute(jvmImpl, JVMIMPL_FREEMEM);
            jvmProps.put("Free Memory", calcMemory((Long) o));
            o = kernel.getAttribute(jvmImpl, JVMIMPL_AVAILABLEPROCS);
            jvmProps.put("Available Processors", o);
            renderRequest.setAttribute("jvmProps", jvmProps);
        } catch (Exception e) {
            e.printStackTrace();
            //throw new PortletException(e);
        }

        if (WindowState.NORMAL.equals(renderRequest.getWindowState())) {
            normalView.include(renderRequest, renderResponse);
        } else {
            maximizedView.include(renderRequest, renderResponse);
        }
    }

    protected void doHelp(RenderRequest renderRequest,
            RenderResponse renderResponse) throws PortletException, IOException {
        helpView.include(renderRequest, renderResponse);
    }

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);
    }

    public void destroy() {
        kernel = null;
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }

    public static Map getServerInfo() {
        return svrProps;
    }

    public static Map getJVMInfo() {
        return jvmProps;
    }

    private String calcElapsedTime(long timeInSeconds) {
        long days, hrs, mins, secs;
        days = timeInSeconds / 86400;
        timeInSeconds = timeInSeconds - (days * 86400);
        hrs = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hrs * 3600);
        mins = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (mins * 60);
        secs = timeInSeconds;

        StringBuffer sb = new StringBuffer();
        sb.append(days);
        sb.append(" day(s) ");
        sb.append(hrs);
        sb.append(" hour(s) ");
        sb.append(mins);
        sb.append(" minute(s) ");
        sb.append(secs);
        sb.append(" second(s)");

        return sb.toString();
    }

    private String calcMemory(Long memory) {
        long mb, kb;
        long mem = memory.longValue();
        mb = mem / 1048576;
        // If less than 10MB return as KB
        if (mb < 10) {
            kb = mem / 1024;
            return kb + "KB";
        }
        return mb + "MB";
    }

}
