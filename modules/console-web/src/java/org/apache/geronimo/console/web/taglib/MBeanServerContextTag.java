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

import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.servlet.jsp.tagext.BodyTagSupport;

public class MBeanServerContextTag extends BodyTagSupport {
    private MBeanServer server;

    public int doStartTag() {
        server = getMBeanServer();
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public MBeanServer getMBeanServer() {
        Iterator servers = MBeanServerFactory.findMBeanServer(null).iterator();
        MBeanServer server = null;
        while (servers.hasNext()) {
            server = (MBeanServer) servers.next();
        }
        return server;
    }

    public String getObjectNameFilter() {
        String filter =
                pageContext.getRequest().getParameter("ObjectNameFilter");
        if (filter == null || filter == "") {
            return "*:*";
        }
        return filter;
    }

    public Set getMBeans() {
        try {
            if (server != null) {
                ObjectName objectName = new ObjectName(getObjectNameFilter());
                QueryExp query = null;
                return server.queryMBeans(objectName, query);
            } else {
                throw new Exception("MBean server has not been initialized");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
