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