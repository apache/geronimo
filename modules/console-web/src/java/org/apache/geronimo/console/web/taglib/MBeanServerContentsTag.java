/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
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

import org.apache.geronimo.console.web.util.ObjectInstanceComparator;

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
        ObjectInstanceComparator comparator = new ObjectInstanceComparator();
        Collections.sort(list, comparator);
        return list;
    }

}