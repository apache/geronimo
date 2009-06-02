<%@ page language="java" 
         contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setLocale value="<%=request.getLocale()%>"/>
<fmt:setBundle basename="portaldriver"/> 
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

<html>
<head>
<title><fmt:message key="console.common.ConsoleLogIn"/></title>
<link href="<%=request.getContextPath()%>/main.css" rel="stylesheet" type="text/css">
<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
</head>

<body onload="document.login.j_username.focus()" leftmargin="0" topmargin="0" rightmargin="0">

<form name="login" action="j_security_check" method="POST">
  <%--  Top table is the banner --%>
      <TABLE width="100%" HEIGHT="86" BORDER="0" CELLSPACING="0" CELLPADDING="0">
        <TR>
          <td height="86" class="LoginLogo" border="0"></td>
          <td height="86" class="Top" border="0">&nbsp; </TD>
          <td height="86" class="Top" border="0" width="40">
          </td>
        </TR>
        <TR>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </TR>
        <TR>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </TR>
      </TABLE>

  <%--  Main body --%>
  <table WIDTH="100%" BORDER="0" CELLSPACING="0" CELLPADDING="0">
    
    <tr>
      <td width="30%" >&nbsp;</td>
      <td class="Body" align="CENTER" valign="top">
        <table align="center" cellspacing="0" cellpadding="0" width="550" border="0" valign="top" style="background-color:#F7F7F7; border:1px solid #88A4D7; font-family:Verdana,Helvetica,sans-serif; font-size:100%;">
          <tbody>
            <tr valign="top">
              <td style="width: 20px;"><img height="16" align="baseline" width="16" title="Error" alt="Error" src="/console/images/msg_error.gif"/></td>
              <td><span style="color:#CC0000; font-family:Verdana,Helvetica,sans-serif;"><fmt:message key="console.login_error.invalid"/></span></td>
            </tr>
          </tbody>
        </table>
      </td> 
      <td width="30%" >&nbsp;</td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td>&nbsp;</td>
    </tr>    
    
    <TR CLASS="Content">
      <td width="30%" >&nbsp;</td>

      <td class="Body" align="CENTER" height="300" valign="top">
      <TABLE border>
      <TR>
      <TD>
      <table width="550" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td class="ReallyDarkBackground"><strong>&nbsp;<fmt:message key="console.common.loginToConsole"/></td>
        </tr>
        <tr>
          <td>
          <table width="100%"  border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td width="25%" class="MediumBackground">&nbsp;</td> 
                <td align="right" class="MediumBackground">&nbsp;</td>
                <td width="6" class="MediumBackground">&nbsp;</td>
                <td width="1" class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td width="25%" class="MediumBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="MediumBackground" ROWSPAN=3 ALIGN="center" ><img border="0" align="center" src="<%=request.getContextPath()%>/images/login_lock_64x55.gif" alt=""/></td> 
                <td align="right" class="MediumBackground"><strong><fmt:message key="console.common.username"/></strong></td>
                <td class="MediumBackground"><strong>:</strong></td>
                <td width="1" class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground"><input name="j_username" type="text" class="InputField" value="" size="20px" maxlength="25"/></td>
                <td width="17" class="MediumBackground">&nbsp;</td>
              </tr>
              <tr>
                <td align="right" class="MediumBackground"><strong><fmt:message key="console.common.password"/></strong></td>
                <td class="MediumBackground"><strong>:</strong></td>
                <td width="1" class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground"><input name="j_password" type="password" class="InputField" value="" size="20px" maxlength="25"/></td>
                <td class="MediumBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="MediumBackground">&nbsp;</td>
                <td colspan="4" align="center" class="MediumBackground"><input name="submit" type="submit" value="<fmt:message key="console.common.login"/>"/></td>
                <td class="MediumBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground">&nbsp;</td>
              </tr>
          </table>
          </td>
        </tr>

        <tr>
          <td><font size="1"><STRONG><fmt:message key="console.login.welcome"/></FONT></td>
        </TR>
        <tr>
          <TD>
          <table width="100%"  border="0" cellspacing="1" cellpadding="5">
            <tr>
              <td width="5">&nbsp;</td>
              <td><fmt:message key="console.login.introduction"/></td>
              <td width="5">&nbsp;</td>
            </tr>

            <tr>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
            </tr>

            <tr>
              <td>&nbsp;</td>
              <td><fmt:message key="console.login.components"/></td>
              <td>&nbsp;</td>
            </tr>
          </table>
          </TD>
        </TR>
      </table>
      </TD>
      </TR>
      </TABLE>

      <td width="30%" >&nbsp;</td>
    </tr>
  </table>
</form>
</body>
</html>

