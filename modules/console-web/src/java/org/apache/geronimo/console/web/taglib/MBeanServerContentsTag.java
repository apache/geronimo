package org.apache.geronimo.console.web.taglib;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.servlet.jsp.JspWriter;

import org.apache.geronimo.console.web.util.MBeanComparator;

/**
 * This class displays the contents of the MBeanServer, arranged in groups, in
 * alphabetical order by MBean domain and then by the MBean's canonical name.
 *
 */
public final class MBeanServerContentsTag extends MBeanServerContextSupport {
    private MBeanServerContextTag ctx;
    private MBeanServer server;

    public int doStartTag() {
        ctx = getMBeanServerContext();
        server = ctx.getMBeanServer();
        JspWriter out = pageContext.getOut();

        try {
            if (server != null) {

                ObjectName objectName = new ObjectName(ctx.getObjectNameFilter());
                QueryExp query = null;
                Set results = server.queryMBeans(objectName, query);
                List mbeans = toList(results);
                printMBeanStack(out, mbeans);
            }
        } catch (MalformedObjectNameException e) {
            try {
                String s = "Your query string was improperly formatted. " +
                        "Please try another query.";
                out.println("<div class='paragraphHead'> " +
                        "Invalid Query String </div>");
                out.println("<p>" + s + "</p>");
            } catch (IOException ex) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() {
        return EVAL_PAGE;
    }

    private void printMBeanStack(JspWriter out, List mbeans)
            throws IOException {
        Iterator iter = mbeans.iterator();
        String currentDomain = "";
        int i = 0;
        while (iter.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iter.next();
            ObjectName name = instance.getObjectName();

            if (!(name.getDomain().equals(currentDomain))) {
                if (i != 0) {
                    out.println("</ul>\n");
                }
                currentDomain = name.getDomain();
                out.println(
                        "\n<div class='paragraphHead'>" + currentDomain + "</div>");
                out.println("<ul class='mbeanList'>");

            }

            String cName = name.getCanonicalName();
            String encodedName = URLEncoder.encode(cName, "UTF-8");
            String output = cName.substring(cName.indexOf(":") + 1);

            out.println("<li><a href=\"mbeanInfo.jsp?MBeanName=" +
                    encodedName + "\">" + URLDecoder.decode(output, "UTF-8") + "</a></li>");

            i++;
        }

        out.println("</ul>\n");
        out.println("<br/> Number of MBeans == " + i);
    }

    /*
     * The idea behind this method is to build a tree structure in the list
     * of MBeans and sort out the objects by subgroups.  This would make them
     * a lot easier to read on the screen.
     *
     * Unfortunately, this method isn't ready yet.
     */
    private void printCascadingDefinition(JspWriter out, String output) {
        //TODO: Format the JSR77 stuff so it's more readable.
    }

    private List toList(Set set) {
        List list = new ArrayList();
        list.addAll(set);
        MBeanComparator comparator = new MBeanComparator();
        Collections.sort(list, comparator);
        return list;
    }

}