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

package org.apache.geronimo.console.web.taglib;

import java.io.IOException;
import javax.management.MBeanServer;
import javax.servlet.jsp.JspWriter;

/**
 * This tag presents the contents of an attribute from the MBeanServerContext
 * tag to the screen.  The attribute type is defined with the "type" parameter
 * in the attribute tag.
 *
 */
public final class MBeanServerContextValueTag extends MBeanServerContextSupport {
    private String type = "";
    private MBeanServerContextTag ctx;
    private MBeanServer server;

    public int doStartTag() {
        ctx = getMBeanServerContext();
        server = ctx.getMBeanServer();
        JspWriter out = pageContext.getOut();

        try {
            if (server != null) {
                String output = getContextValue(getType());
                out.print(output);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String getContextValue(String type) {
        if (type.equals("ObjectNameFilter")) {
            return ctx.getObjectNameFilter();
        }
        return "error, attribute [" + type + "] not recognized";
    }
}