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

package org.apache.geronimo.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class TestServlet extends HttpServlet {
        
    private static Integer expectedValue = new Integer(20);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mode = request.getParameter("mode");

        if ("forward".equals(mode)) {
            getServletConfig().getServletContext().getContext("/dispatch1").getRequestDispatcher("/TestServlet").forward(request, response);
        } else {
            PrintWriter out = response.getWriter();
            Integer value = lookup("java:comp/env/value", request);
        
            out.println("TestServlet2: " + value);

            // create new session for testing
            HttpSession session = request.getSession(true);
            session.setAttribute("sessAttr1", "value21");
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }         

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        testLookup("servletInit");
    }

    public void destroy() {
        testLookup("servletDestroy");
    }

    private Integer lookup(String name, HttpServletRequest request) {
        try {
            Context ctx = new InitialContext();
            Integer value = (Integer)ctx.lookup(name);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void testLookup(String name) {
        System.out.println(name);

        Integer value;
        try {
            Context ctx = new InitialContext();
            value = (Integer)ctx.lookup("java:comp/env/value");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(name, e);
        }
        
        if (!expectedValue.equals(value)) {
            throw new RuntimeException("Values do not match: " + expectedValue + " " + value);
        }
    }
    
}
