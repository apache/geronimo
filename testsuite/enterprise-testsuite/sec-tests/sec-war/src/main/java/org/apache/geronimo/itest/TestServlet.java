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
package org.apache.geronimo.itest;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.AccessException;
import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ejb.CreateException;

import org.apache.geronimo.security.ContextManager;


/**
 * @version $Rev$ $Date$
 */
public class TestServlet extends HttpServlet {

    public void init() {
        System.out.println("Test Servlet init");
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        showServletState(request, out);
        try {
            TestSession session = getSession();
            try {
                out.print(session.testAccessFoo());
            } catch (AccessException e) {
                out.println("security exception on testAccessFoo method");
            }
            try {
                out.print(session.testAccessBar());
            } catch (AccessException e) {
                out.println("security exception on testAccessBar method");
            }
            try {
                out.print(session.testAccessBaz());
            } catch (AccessException e) {
                out.println("security exception on testAccessBaz method");
            }

        } catch (Exception e) {
            out.println("Exception:");
            e.printStackTrace(out);
        }
        showServletState(request, out);
        out.flush();
    }

    protected void showServletState(HttpServletRequest request, PrintWriter out) {
        if (request.getUserPrincipal() == null) {
            out.println("TestServlet principal is null, current caller Subject: " + ContextManager.getCurrentCaller());
        } else {
            out.println("TestServlet principal: " + request.getUserPrincipal().getName());
        }
        out.println("TestServlet isUserInRole foo: " + request.isUserInRole("foo"));
        out.println("TestServlet isUserInRole bar: " + request.isUserInRole("bar"));
        out.println("TestServlet isUserInRole baz: " + request.isUserInRole("baz"));
    }

    protected TestSession getSession() throws NamingException, RemoteException, CreateException {
        InitialContext ctx = new InitialContext();

        //test ejb access using geronimo plan refs
        TestSessionHome home = (TestSessionHome)ctx.lookup("java:comp/env/TestSession");
        TestSession session = home.create();
        return session;
    }


}
