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
 **/
package org.apache.geronimo.testsuite.servlet3.app;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@WebServlet(name = "showServlet", urlPatterns = {"/showServlet"})
//@MultipartConfig(location = "target/")
public class ShowFileServlet extends HttpServlet {

    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>File Upload System</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h2><font color=\"green\">A listener is dectecting online person number.</font></h2>");
            out.println("<h3>Currently,there are " + "<font color=\"green\">" + getServletContext().getAttribute("onLineNumber") + "</font> people visiting this file upload system!<br/><hr></h3>");
            String message = request.getAttribute("message").toString();
            if (message.indexOf("returns null!") < 0) {
                out.println("<h2><font color=\"green\">Attributes and content of the file:</font></h2>");
            } 
            out.println(message + "<br/>");
            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
        }
    }

    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
