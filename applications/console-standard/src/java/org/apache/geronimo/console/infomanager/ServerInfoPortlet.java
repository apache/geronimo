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

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.j2ee.management.geronimo.JVM;

/**
 * Calculates various information about the server to display in the server
 * info portlet view (on of several JSPs depending on the portlet state).
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ServerInfoPortlet extends GenericPortlet {
    private static final String NORMALVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoNormal.jsp";

    private static final String MAXIMIZEDVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoMaximized.jsp";

    private static final String HELPVIEW_JSP = "/WEB-INF/view/infomanager/svrInfoHelp.jsp";

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

        Map svrProps = new HashMap();
        Map jvmProps = new HashMap();

        JVM jvm = PortletManager.getCurrentJVM(renderRequest);

        Date bootDate = jvm.getKernelBootTime();
        long bootTime = bootDate.getTime() / 1000;
        long currentTime = System.currentTimeMillis() / 1000;
        long elapsedTime = currentTime - bootTime;
        svrProps.put("Kernel Boot Time", bootDate);
        svrProps.put("Kernel Up Time", calcElapsedTime(elapsedTime));
        renderRequest.setAttribute("svrProps", svrProps);

        jvmProps.put("Java Version", jvm.getJavaVersion());
        jvmProps.put("Java Vendor", jvm.getJavaVendor());
        jvmProps.put("Node", jvm.getNode());
        jvmProps.put("Max Memory", calcMemory(jvm.getMaxMemory()));
        jvmProps.put("Total Memory", calcMemory(jvm.getTotalMemory()));
        jvmProps.put("Free Memory", calcMemory(jvm.getFreeMemory()));
        jvmProps.put("Available Processors", new Integer(jvm.getAvailableProcessors()));
        renderRequest.setAttribute("jvmProps", jvmProps);

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
        normalView = portletConfig.getPortletContext().getRequestDispatcher(
                NORMALVIEW_JSP);
        maximizedView = portletConfig.getPortletContext().getRequestDispatcher(
                MAXIMIZEDVIEW_JSP);
        helpView = portletConfig.getPortletContext().getRequestDispatcher(
                HELPVIEW_JSP);
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        super.destroy();
    }

    private static String calcElapsedTime(long timeInSeconds) {
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

    private static String calcMemory(long mem) {
        long mb, kb;
        mb = mem / 1048576;
        // If less than 10MB return as KB
        if (mb < 10) {
            kb = mem / 1024;
            return kb + "KB";
        }
        return mb + "MB";
    }

}
