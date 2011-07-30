<!DOCTYPE html>
<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%@ page language="java" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page session="true" buffer="none" %>

<fmt:setLocale value="<%=request.getLocale()%>"/>
<fmt:setBundle basename="portaldriver"/>

<html lang="en">
<head>
    <title><fmt:message key="console.head.title"/></title>
    <link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
    <link rel="stylesheet" href="/console/dojo/dojo/resources/dojo.css" type="text/css"/>
    <link rel="stylesheet" href="/console/dojo/dijit/themes/claro/claro.css" type="text/css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/pluto.css" type="text/css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/main.css" type="text/css"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/CommonMsg.css" type="text/css"/>
    <script language="Javascript" src="<%=request.getContextPath()%>/js/forms.js" type="text/javascript"></script>
    <script language="Javascript" src="<%=request.getContextPath()%>/CommonMsg.js" type="text/javascript"></script>
</head>
<style type="text/css">
html { 
    height: 100%;
} 
</style>
<c:choose>
    <c:when test="${param.noxssShowTree!=null}">
        <jsp:include page="portlets-with-tree.jsp" />
    </c:when>
    <c:otherwise>
        <jsp:include page="portlets-only.jsp" />
    </c:otherwise>
</c:choose>
</html>
