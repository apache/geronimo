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

package org.apache.geronimo.testsuite.corba.helloworld;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

public class ServletController extends HttpServlet {

    protected static final String HOME_PAGE = "/greeting.jsp";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(true);
        ServletContext context = getServletContext();
        HelloWorldEJBLocal ejbLocal = null;

        try {
            Context ic = new InitialContext();
            Object o = ic.lookup("java:comp/env/HelloWorldEJB");
            HelloWorldEJBLocalHome ejbLocalHome = (HelloWorldEJBLocalHome) o;
            ejbLocal = ejbLocalHome.create();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        String [] greetings = (String []) ejbLocal.getGreetings();
        session.setAttribute("greetings", greetings);
        context.getRequestDispatcher(HOME_PAGE).forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

}
