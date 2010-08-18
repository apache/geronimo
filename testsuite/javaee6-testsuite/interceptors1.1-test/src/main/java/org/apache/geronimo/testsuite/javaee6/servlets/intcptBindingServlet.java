/**
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.apache.geronimo.testsuite.javaee6.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ejb.EJB;

import org.apache.geronimo.testsuite.javaee6.beans.IntcptBindingTestBean;

public class intcptBindingServlet extends HttpServlet {
   @EJB
   private IntcptBindingTestBean itb;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet IntcptBindingServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet IntcptBindingServlet at " + request.getContextPath () + "</h1>");
            itb.testInterceptorBinding();
            String output = itb.getResult();
            out.println("<p id = \"intcptBind\">"+output+"</p>");
            out.println("<br/><a href = \"index.jsp\">Return</a>");
            out.println("</body>");
            out.println("</html>");

        } finally {
            out.close();
        }
    } 


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        this.doGet(request, response);
    }

}
