/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.testsuite.servlet3.app;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

@WebFilter(filterName = "MessageFilter", urlPatterns = { "/showServlet", "/showServletWa" })
public class FileMessageFilter implements Filter {

    private FilterConfig filterConfig;
    public static final String FILTERED_STRING = "b, it's filtered because the file size is limited to 10 kb";

    public FileMessageFilter() {
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        String message = "";
        HttpServletRequest httpServletRequest = null;
        if (request instanceof HttpServletRequest) {
            httpServletRequest = (HttpServletRequest) request;
            message += "<p>" + "Http Servlet Request content type: "
                    + httpServletRequest.getContentType() + "</p><br>";
        }

        Collection<Part> parts = httpServletRequest.getParts();
        if (!parts.isEmpty()) {
            int i = 0;
            for (Part apart : parts) {
                message += "<p>"+ (++i) + ". Name=" + apart.getName()
                        + ";ContentType=" + apart.getContentType() + ";Size="
                        + apart.getSize() + "</p><br>";
            }
        } else {
            message += "<p><b>HttpServletRequest.getParts() returns an empty collection!</b></p><br>";
        }        

        Throwable problem = null;
        try {
            Part p = ((HttpServletRequest) request).getPart("testFile");
            if (null != p) {
                String part = p.toString();
                String pname = p.getName();
                long size = p.getSize();
                String contentType = p.getContentType();
                String targetDirectory = ((HttpServletRequest) request).getServletContext().getRealPath("/test-3.0-servlet-war");
                if (size > 10000) {
                    message += "The file size is "
                            + size
                            + FILTERED_STRING + targetDirectory;
                } else {
                    message += "<font color=green><b>Part:</b> </font>"
                            + part
                            + "<br><font color=green><b>Part Name:</b> </font>"
                            + pname
                            + "<br><font color=green><b>Size:</b> </font>"
                            + size
                            + "<br><font color=green><b>ContentType:</b> </font>"
                            + contentType
                            + "<br><font color=green><b>HeadNames:</b> </font>";
                    for (String name : p.getHeaderNames()) {
                        message += name + ";";
                    }
                    java.io.InputStreamReader in = new java.io.InputStreamReader(
                            p.getInputStream());
                    String content = "";
                    int c = in.read();
                    while (c != -1) {
                        if (c == '\n') {
                            content += "<br>";
                        }
                        content += (char) c;
                        c = in.read();
                    }
                    if (content.equals("")) {
                        message += "<br> Sorry, this is not a plain text, so we can not display it.";
                    } else {
                        message += "<br><font color=green><b>The text file content is:</b></font><br>"
                                + content;
                        message += "<hr>";
                    }
                }
            } else {
                message += "<p><b>HttpServletRequest.getPart(String name) returns null!</b></p><br>";
            }
            request.setAttribute("message", message);
            chain.doFilter(request, response);
        } catch (Throwable t) {
            problem = t;
            t.printStackTrace();
        }

    }

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("MessageFilter()");
        }
        StringBuilder sb = new StringBuilder("MessageFilter()");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
}
