<%@ page language="java" 
         contentType="text/html; charset=UTF-8" %>
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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="<%=request.getLocale()%>"/>
<fmt:setBundle basename="portaldriver"/>

<html lang="en">
<head>
<title><fmt:message key="console.common.ConsoleLogIn"/></title>
<link href="<%=request.getContextPath()%>/main.css" rel="stylesheet" type="text/css"/>
<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
</head>

<%-- Avoid the login page displayed in the iframe --%>
<script type="text/javascript">
    if(window.parent!=window.self){
        window.parent.location.reload();
    }
</script>

<body onload="document.login.j_username.focus()" leftmargin="0" topmargin="0" rightmargin="0">

<%--  Top table is the banner --%>
<table width="100%" height="86" cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td height="86" class="LoginLogo" border="0"></td>
        <td height="86" class="Top" border="0">&nbsp;</td>
    </tr>
</table>

<%--  Main body --%>

<form name="login" action="j_security_check" method="POST">
<p align="center">
<table width="550" cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td class="ReallyDarkBackground">
            &nbsp;<fmt:message key="console.common.loginToConsole"/>
        </td>
    </tr>
    <tr>
        <td class="MediumBackground" align="center">
            <br/>
            <table border="0" cellspacing="0" cellpadding="5">
                <tr>
                    <td>
                        <img border="0" align="center" src="<%=request.getContextPath()%>/images/login_lock_64x55.gif" alt=""/>
                    </td>
                    <td>
                        <table width="100%" border="0" cellspacing="2" cellpadding="0">
                            <tr>
                                <td align="right"><strong><label for="<portlet:namespace/>j_username"><fmt:message key="console.common.username"/></label>:</strong></td>
                                <td><input name="j_username" id="<portlet:namespace/>j_username" type="text" class="InputField" value="" size="20px"/></td>
                            </tr>
                            <tr>
                                <td align="right"><strong><label for="<portlet:namespace/>j_password"><fmt:message key="console.common.password"/></label>:</strong></td>
                                <td><input name="j_password" id="<portlet:namespace/>j_password" type="password" class="InputField" value="" size="20px"/></td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td><input name="submit" type="submit" value="<fmt:message key="console.common.login"/>"/></td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
            <br/>
        </td>
    </tr>
    <tr>
        <td class="MediumBackground" align="center">
            <br/>
            <table style="TableLine" width="90%" border="0">
                <tr>
                    <td class="DarkBackground">
                        <strong>&nbsp;<fmt:message key="console.login.welcome"/></strong>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div style="padding:10px;">
                            <fmt:message key="console.login.introduction"/>
                            <br/><br/>    
                            <fmt:message key="console.login.components"/>
                        </div>
                    </td>
                </tr>
            </table>
            <br/>
        </td>
    </tr>
</table>
</p>
</form>

</body>
</html>
