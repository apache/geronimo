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
