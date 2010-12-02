/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.javaee6.jpa20.action;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.geronimo.javaee6.jpa20.bean.Facade;

import javax.ejb.EJB;

@WebServlet(name="nullIfJPQL", urlPatterns={"/nullIfJPQL"})
public class nullIfJPQL extends HttpServlet {
    @EJB
    private Facade facade = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    	PrintWriter out = response.getWriter();
    	String studentId = request.getParameter("sid");
    	int sid = Integer.parseInt(studentId);
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>JPQL</title>");
            out.println("</head>");
            out.println("<body>");
            String result = new String();
            result = facade.nullIf(sid);
            out.println("<p>Nullif is "+result+".</p>");
            out.println("</body>");
            out.println("</html>");
        }
        finally{
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
