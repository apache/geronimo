/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.testsuite.servlet30.main;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginServlet extends javax.servlet.http.HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
            String userName = request.getParameter("UserName");
            String password = request.getParameter("Password");

            //Before Login
            String bli1= String.valueOf(request.isUserInRole("RoleB"));
            String bli2 = String.valueOf(request.getRemoteUser());
            String bli3 = String.valueOf(request.getUserPrincipal());
            try {
                request.login(userName, password);
            }catch(ServletException se) {
                out.println("request.login method occurs a ServletException: " + se.getMessage());
                return;
            }
           //Login
            String ali1= String.valueOf(request.isUserInRole("RoleB"));
            String ali2 = String.valueOf(request.getRemoteUser());
            String ali3 = String.valueOf(request.getUserPrincipal());

            request.logout();
           //Logout
            String alo1= String.valueOf(request.isUserInRole("RoleB"));
            String alo2 = String.valueOf(request.getRemoteUser());
            String alo3 = String.valueOf(request.getUserPrincipal());


            out.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"+
        "<title>Programmatic Security</title></head>");
            out.println("<body>");
            out.println("<table cellpadding=\"1\" border=\"1\">");

            out.println("<tr><th>Value/Status</th><th>BeforeLogin</th><th>AfterLogin</th><th>AfterLogout</th></tr>");

            out.println("<tr>");
            out.println("<th>isUserInRole</th>");
            out.println("<td id=\"bli1\">"+bli1+"</td>");
            out.println("<td id=\"ali1\">"+ali1+"</td>");
            out.println("<td id=\"alo1\">"+alo1+"</td>");
            out.println("</tr>");

            out.println("<tr>");
            out.println("<th>getRemoteUser</th>");
            out.println("<td id=\"bli2\">"+bli2+"</td>");
            out.println("<td id=\"ali2\">"+ali2+"</td>");
            out.println("<td id=\"alo2\">"+alo2+"</td>");
            out.println("</tr>");

            out.println("<tr>");
            out.println("<th>getUserPrincipal</th>");
            out.println("<td id=\"bli3\">"+bli3+"</td>");
            out.println("<td id=\"ali3\">"+ali3+"</td>");
            out.println("<td id=\"alo3\">"+alo3+"</td>");
            out.println("</tr>");

            out.println("</table>");
            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doGet(request, response);
    }

}
