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
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="org.apache.geronimo.ejb.*,javax.naming.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Security EJB Test</title>
<% SecurityRemote securityRemote=null;
   try{
	    InitialContext ic = new InitialContext();
	    securityRemote =(SecurityRemote)ic.lookup("java:comp/env/ejb/securitybean");
   }catch(Exception e){
	   
   }
   
   SecurityRunAsRemote securityRemoteRunAs=null;
   try{
	    InitialContext ic = new InitialContext();
	    securityRemoteRunAs =(SecurityRunAsRemote)ic.lookup("java:comp/env/ejb/securitybeanRunAs");
   }catch(Exception e){
	   
   }
%>
</head>
<body>
   <% String temp=null;%>
   <hr/>
   1.
   <% 
       try{
    	   temp=securityRemote.permitAllMethod();
       }catch(Throwable t){
    	   temp="SecurityBean.permitAllMethod:false";
       }
   %>
   <%=temp%><hr>
   2.
    <% 
       try{
    	   temp=securityRemote.rolesAllowedUserMethod();
       }catch(Throwable t){
    	   temp="SecurityBean.rolesAllowedUserMethod:false";
       }
   %>
   <%=temp%><hr>
   3.
    <% 
       try{
    	   temp=securityRemote.rolesAllowedAdminMethod();
       }catch(Throwable t){
    	   temp="SecurityBean.rolesAllowedAdminMethod:false";
       }
   %>
   <%=temp%><hr>
   4.
    <% 
       try{
    	   temp=securityRemote.denyAllMethod();
       }catch(Throwable t){
    	   temp="SecurityBean.denyAllMethod:false";;
       }
   %>
   <%=temp%><hr>
   
   <h4>Run As</h4>
    <hr/>
    5.
   <% 
       try{
    	   temp=securityRemoteRunAs.permitAllMethod();
       }catch(Throwable t){
    	   temp="SecurityRunAsBean.permitAllMethod:false";
       }
   %>
   <%=temp%><hr>
   6.
    <% 
       try{
    	   temp=securityRemoteRunAs.rolesAllowedUserMethod();
       }catch(Throwable t){
    	   temp="SecurityRunAsBean.rolesAllowedUserMethod:false";
       }
   %>
   <%=temp%><hr>
   7.
    <% 
       try{
    	   temp=securityRemoteRunAs.rolesAllowedAdminMethod();
       }catch(Throwable t){
    	   temp="SecurityRunAsBean.rolesAllowedAdminMethod:false";
       }
   %>
   <%=temp%><hr>
   8.
    <% 
       try{
    	   temp=securityRemoteRunAs.denyAllMethod();
       }catch(Throwable t){
    	   temp="SecurityRunAsBean.denyAllMethod:false";
       }
   %>
   <%=temp%><hr>
   <br><a href="<%=request.getContextPath()%>/logout.jsp">Logout</a>
</body>
</html>