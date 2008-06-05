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

import javax.ejb.CreateException;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @version $Rev$ $Date$
 */
public class TestInjectionServlet extends HttpServlet {

    @EJB(name="TestSession")
    private TestSession session;


    public void init() {
        System.out.println("Test Servlet init");
    }

    protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter out = httpServletResponse.getWriter();
        out.println("TestServlet principal: " + httpServletRequest.getUserPrincipal().getName());
        out.println("TestServlet isUserInRole foo: " + httpServletRequest.isUserInRole("foo"));
        out.println("TestServlet isUserInRole bar: " + httpServletRequest.isUserInRole("bar"));
        try {
            String principalName = session.testAccess();
            out.println("Test EJB principal: " + principalName);
            try {
                String bad = session.testNoAccess();
                out.println("NoAccess method call succeeded with principal: " + bad);
            } catch (AccessException e) {
                out.println("Correctly received security exception on noAccess method");
            }
            out.println("TestSession isCallerInRole foo: " + session.isCallerInRole("foo"));
            out.println("TestSession isCallerInRole bar: " + session.isCallerInRole("bar"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        out.flush();
    }


}