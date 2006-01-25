<html>
<head>
<title>Geronimo Console Login</title>
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
<a href="<%=request.getContextPath()%>/about.jsp"><img border="0" src="<%=request.getContextPath()%>/images/head_about_51x86.gif"></a>
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
    <TR CLASS="Content">
      <td width="30%" >&nbsp;</td>

      <td class="Body" align="CENTER" height="300" valign="top">
      <TABLE border>
      <TR>
      <TD>
      <table width="550" cellpadding="0" cellspacing="0" border="0">
        <tr>
          <td class="ReallyDarkBackground"><strong>&nbsp;Log In to the Geronimo Console</td>
        </tr>
        <tr>
          <td class="MediumBackground">&nbsp;</td>
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
                <td class="MediumBackground" ROWSPAN=3 ALIGN="center" ><img border="0" align="center" src="<%=request.getContextPath()%>/images/login_lock_64x55.gif"></td> 
                <td align="right" class="MediumBackground"><strong>Username</strong></td>
                <td class="MediumBackground"><strong>:</strong></td>
                <td width="1" class="MediumBackground">&nbsp;</td>
                <td class="MediumBackground"><input name="j_username" type="text" class="InputField" value="" size="20px" maxlength="25"/></td>
                <td width="17" class="MediumBackground">&nbsp;</td>
              </tr>
              <tr>
                <td align="right" class="MediumBackground"><strong>Password</strong></td>
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
                <td colspan="4" align="center" class="MediumBackground"><input name="submit" type="submit" value="Login"/></td>
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
          <td><font size="1"><STRONG>Welcome to the Geronimo&#8482; Console</FONT></td>
        </TR>
        <tr>
          <TD>
          <table width="100%"  border="0" cellspacing="1" cellpadding="5">
            <tr>
              <td width="5">&nbsp;</td>
              <td> <strong>GERONIMO&#8482;</strong> is a Java-certified, production-grade platform designed to allow developers to rapidly deploy and manage their applications. The result is an integrated, highly functional application platform that leverages the latest innovations from the open source community and simplifies application deployment and maintenance. </td>
              <td width="5">&nbsp;</td>
            </tr>

            <tr>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
            </tr>

            <tr>
              <td>&nbsp;</td>
              <td> <strong>Geronimo&#8482;</strong> has integrated the following components:<BR/>
                &nbsp;&nbsp;&#149;&nbsp; Application server (Apache Geronimo)<br/>
                &nbsp;&nbsp;&#149;&nbsp; Web server and servlet engine (Tomcat)<br/>
                &nbsp;&nbsp;&#149;&nbsp; Web server and servlet engine (Jetty)<br/>
                &nbsp;&nbsp;&#149;&nbsp; JSP compiler (Jasper)<br/>
                &nbsp;&nbsp;&#149;&nbsp; Relational database (Apache Derby)<br/>
                &nbsp;&nbsp;&#149;&nbsp; Messaging (ActiveMQ)<br/>
                &nbsp;&nbsp;&#149;&nbsp; User management services<br/>
                &nbsp;&nbsp;&#149;&nbsp; Centralized administration console<br/>
              <td>&nbsp;</td>
            </tr>
          </table>
          </TD>
        </TR>
      </table>
      </TD>
      </TR>
      </TABLE>
      </td>

      <td width="30%" >&nbsp;</td>
    </tr>
  </table>
</form>
</body>
</html>
