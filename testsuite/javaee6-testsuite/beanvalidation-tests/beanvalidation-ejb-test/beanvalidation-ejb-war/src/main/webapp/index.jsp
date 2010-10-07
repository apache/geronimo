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
<%@ page import="org.apache.geronimo.ejb.ValidationRemote,javax.naming.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>BeanValidation EJB Test</title>
<% org.apache.geronimo.ejb.ValidationRemote validationRemote = null;
   try{
	    InitialContext ic = new InitialContext();
	    validationRemote = (ValidationRemote) ic.lookup("java:comp/env/ejb/ValidationBean");
   }catch(Exception e){
   }
%>
</head>
<body>
<%      String result = validationRemote.validatorInfo();
		boolean hasValidatorFactory = result.indexOf("JNDIValidatorFactory=") != -1 && result.indexOf("JNDIValidatorFactory=null") == -1;
		boolean hasValidator = result.indexOf("JNDIValidator=") != -1 && result.indexOf("JNDIValidator=null") == -1;
		boolean hasInjectedValidatorFactory = result.indexOf("injectedValidatorFactory=null") == -1;
		boolean hasInjectedValidator = result.indexOf("injectedValidator=null") == -1;
%>
<%=result %><br>
hasInjectedValidatorFactory = <%=hasInjectedValidatorFactory%> <br>
hasInjectedValidator = <%=hasInjectedValidator%> <br>
hasJNDIValidatorFactory = <%=hasValidatorFactory%> <br>
hasJNDIValidator = <%=hasValidator%> <br>
</body>
</html>