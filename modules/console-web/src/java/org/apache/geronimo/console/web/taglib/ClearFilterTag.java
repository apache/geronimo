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

package org.apache.geronimo.console.web.taglib;

import java.io.IOException;
import javax.management.MBeanServer;
import javax.servlet.jsp.JspWriter;

/**
 * If the application detects that a filter other than "*:*" has been used in
 * the console for limiting the components displayed, this button will appear,
 * allowing the user to revert to the default filter.  If the default filter is
 * being used, no button shall appear.
 */
public final class ClearFilterTag extends MBeanServerContextSupport {
    private MBeanServerContextTag ctx;
    private MBeanServer server;

    public int doStartTag() {
        ctx = getMBeanServerContext();
        server = ctx.getMBeanServer();
        JspWriter out = pageContext.getOut();

        try {
            if (server != null) {
                if (filtered()) {
                    out.println("<input class=\"submit\" type=\"button\" " +
                            "tabindex=\"2\" value=\"Clear Filter\" " +
                            "onclick=\"window.location='index.jsp'\"/>");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    private boolean filtered() {
        return (!ctx.getObjectNameFilter().equals("*:*"));
    }
}