package org.apache.geronimo.console.web.taglib;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.servlet.jsp.JspWriter;

import org.apache.geronimo.console.web.util.MBeanAttributesComparator;

/**
 * This tag will display the contents of an MBean in a simple table of
 * name/value pairs.  The style of the table can be controlled with CSS.
 */
public final class MBeanAttributesTag
        extends MBeanServerContextSupport {
    private Hashtable properties;
    private MBeanServerContextTag ctx;
    private ObjectInstance instance;
    private Object[] keys;
    private Set keySet;
    private ObjectName name;
    private MBeanServer server;

    public int doEndTag() {
        return EVAL_PAGE;
    }

    public int doStartTag() {
        ctx = getMBeanServerContext();
        server = ctx.getMBeanServer();
        JspWriter out = pageContext.getOut();

        printMBeanProperties(out);
        printMBeanAttributes(out);


        return EVAL_BODY_INCLUDE;
    }

    /*
     *  This seems like a very backwards way to do this.  I don't know
     *  that creating an ObjectName, using it to get an ObjectInstance
     *  then creating another ObjectName is necessarily the way to go.
     *
     */
    private String getDomain() {
        try {
            ObjectName mbeanName = new ObjectName(getMBeanName());
            QueryExp query = null;
            Set results = server.queryMBeans(mbeanName, query);

            instance = (ObjectInstance) results.iterator().next();
            name = instance.getObjectName();
            return name.getDomain();

        } catch (MalformedObjectNameException e) {
            return "No object to introspect.  Choose one from the MBean Stack View.";
        }
    }

    /*
     * This gets the value of the MBeanName request parameter.  If it
     * Doesn't find anything, it returns null.
     */
    private String getMBeanName() {
        String s =
                pageContext.getRequest().getParameter("MBeanName");
        if (s == null || s == "") {
            return null;
        }
        return s;
    }

    private void printMBeanProperties(JspWriter out) {
        try {

            //String mbeanName;
            //out.println("<strong>MBean Name </strong>" + getMBeanName());
            out.println("<table cellpadding=\"0\" cellspacing=\"0\">");

            out.println("\t<tr class=\"head\">");
            out.println("\t\t<td class=\"head\" colspan=\"3\">" +
                    "MBean Properties</td>");
            out.println("\t</tr>");

            out.println("\t<tr class=\"one\">");
            out.println("\t\t<td class=\"name\">MBean Domain</td>");
            out.println("\t\t<td class=\"center\">=</td>");
            out.println("\t\t<td class=\"value\">" + getDomain() + "</td>");
            out.println("\t</tr>");

            printMBeanPropertiesStack(out);

            out.println("</table>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printMBeanPropertiesStack(JspWriter out) {
        properties = name.getKeyPropertyList();
        keySet = properties.keySet();
        keys = toList(keySet).toArray();

        try {
            //out.println("Hello");

            String key;
            String property;
            String trClass = "one";
            for (int i = 0; i < keys.length; i++) {
                key = (String) keys[i];
                property = (String) name.getKeyProperty(key);
                if (i % 2 == 0) {
                    trClass = "two";
                } else if (i % 2 == 1) {
                    trClass = "one";
                }

                out.println("\t<tr class=\"" + trClass + "\">");
                out.println("\t\t<td class=\"name\">" + key + "</td>");
                out.println("\t\t<td class=\"center\">=</td>");
                out.println("\t\t<td class=\"value\">" +
                        URLDecoder.decode(property, "UTF-8") + "</td>");
                out.println("\t</tr>");

            }

            /*
            out.println("\t<tr class=\"one\">");
            out.println("\t\t<td class=\"name\">About</td>");
            out.println("\t\t<td class=\"center\">=</td>");
            out.println("\t\t<td class=\"value\">Now</td>");
            out.println("\t</tr>");
            */

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printMBeanAttributes(JspWriter out) {
        try {
            //String mbeanName;
            //out.println("<strong>MBean Name </strong>" + getMBeanName());
            out.println("<table cellpadding=\"0\" cellspacing=\"0\">");

            out.println("\t<tr class=\"head\">");
            out.println("\t\t<td class=\"head\" colspan=\"3\">" +
                    "MBean Attributes & Info</td>");
            out.println("\t</tr>");

            printMBeanAttributesStack(out);


            out.println("</table>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printMBeanAttributesStack(JspWriter out) {
        try {
            MBeanInfo info = server.getMBeanInfo(name);
            MBeanAttributeInfo[] attributes = info.getAttributes();
            String className = info.getClassName();
            String description = info.getDescription();

            out.println("\t<tr class=\"one\">");
            out.println("\t\t<td class=\"name\">Class Name</td>");
            out.println("\t\t<td class=\"center\">=</td>");
            out.println("\t\t<td class=\"value\">" + className + "</td>");
            out.println("\t</tr>");

            out.println("\t<tr class=\"two\">");
            out.println("\t\t<td class=\"name\">Description</td>");
            out.println("\t\t<td class=\"center\">=</td>");
            out.println("\t\t<td class=\"value\">" + description + "</td>");
            out.println("\t</tr>");

            String attributeName = "name";
            String value = "value";
            String trClass = "one";
            for (int i = 0; i < attributes.length; i++) {

                attributeName = attributes[i].getName();
                //value = attributes[i].toString();
                value = server.getAttribute(name, attributeName).toString();
                ;
                if (i % 2 == 0) {
                    trClass = "one";
                } else if (i % 2 == 1) {
                    trClass = "two";
                }

                out.println("\t<tr class=\"" + trClass + "\">");
                out.println("\t\t<td class=\"name\">" + attributeName + "</td>");
                out.println("\t\t<td class=\"center\">=</td>");
                out.println("\t\t<td class=\"value\">" +
                        URLDecoder.decode(value, "UTF-8") + "</td>");
                out.println("\t</tr>");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List toList(Set set) {
        List list = new ArrayList();
        MBeanAttributesComparator comp = new MBeanAttributesComparator();
        list.addAll(set);
        Collections.sort(list, comp);
        return list;
    }
}