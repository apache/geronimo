package org.apache.geronimo.console.web.taglib;

import javax.management.MBeanServer;
import javax.servlet.jsp.tagext.BodyTagSupport;

/*
 * This class provides a set of common methods for accessing the
 * MBeanServerContextTag and its contents, as well as for accessing
 * the BodyTagSupport class required for JSP tag libraries.
 */

public class MBeanServerContextSupport extends BodyTagSupport {

    protected MBeanServerContextTag getMBeanServerContext() {
        return (MBeanServerContextTag)
                findAncestorWithClass(this, MBeanServerContextTag.class);
    }

    protected MBeanServer getMBeanServer() {
        return getMBeanServerContext().getMBeanServer();
    }
}
