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