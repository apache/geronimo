<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.rmi.AccessException" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.apache.geronimo.itest.TestSession" %>
<%@ page import="org.apache.geronimo.itest.TestSessionHome" %>
<%@ page import="org.apache.geronimo.security.ContextManager" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    PrintWriter myout = response.getWriter();
    if (request.getUserPrincipal() == null) {
        myout.println("TestServlet principal is null, current caller Subject: " + ContextManager.getCurrentCaller());
    } else {
        myout.println("TestServlet principal: " + request.getUserPrincipal().getName());
    }
    myout.println("TestServlet isUserInRole foo: " + request.isUserInRole("foo"));
    myout.println("TestServlet isUserInRole bar: " + request.isUserInRole("bar"));
    myout.println("TestServlet isUserInRole baz: " + request.isUserInRole("baz"));
    try {
        InitialContext ctx = new InitialContext();

        //test ejb access using geronimo plan refs
        TestSessionHome home = (TestSessionHome)ctx.lookup("java:comp/env/TestSession");
        TestSession testSession = home.create();
        try {
            myout.print(testSession.testAccessFoo());
        } catch (AccessException e) {
            myout.println("security exception on testAccessFoo method");
        }
        try {
            myout.print(testSession.testAccessBar());
        } catch (AccessException e) {
            myout.println("security exception on testAccessBar method");
        }
        try {
            myout.print(testSession.testAccessBaz());
        } catch (AccessException e) {
            myout.println("security exception on testAccessBaz method");
        }

    } catch (Exception e) {
        myout.println("Exception:");
        e.printStackTrace(myout);
    }
    if (request.getUserPrincipal() == null) {
        myout.println("TestServlet principal is null, current caller Subject: " + ContextManager.getCurrentCaller());
    } else {
        myout.println("TestServlet principal: " + request.getUserPrincipal().getName());
    }
    myout.println("TestServlet isUserInRole foo: " + request.isUserInRole("foo"));
    myout.println("TestServlet isUserInRole bar: " + request.isUserInRole("bar"));
    myout.println("TestServlet isUserInRole baz: " + request.isUserInRole("baz"));
%>
