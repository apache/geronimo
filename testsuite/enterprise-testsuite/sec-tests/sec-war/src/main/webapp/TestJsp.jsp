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
<%@ page import="org.apache.geronimo.itest.TestSession" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="org.apache.geronimo.itest.TestSessionHome" %>
<%@ page import="javax.naming.NamingException" %>
<%@ page import="java.rmi.AccessException" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="javax.ejb.CreateException" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    ServletOutputStream myout = response.getOutputStream();
    myout.println("TestServlet principal: " + request.getUserPrincipal().getName());
    try {
        InitialContext ctx = new InitialContext();

        //test ejb access using geronimo plan refs
        TestSessionHome home = (TestSessionHome) ctx.lookup("java:comp/env/TestSession");
        TestSession sessionBean = home.create();
        String principalName = sessionBean.testAccess();
        myout.println("Test EJB principal: " + principalName);
        try {
            String bad = sessionBean.testNoAccess();
            myout.println("NoAccess method call succeeded with principal: " + bad);
        } catch (AccessException e) {
            myout.println("Correctly received security exception on noAccess method");
        }

    } catch (NamingException e) {
        myout.print("Exception:");
        e.printStackTrace();
    } catch (RemoteException e) {
        e.printStackTrace();
    } catch (CreateException e) {
        e.printStackTrace();
    }
    myout.flush();
%>
