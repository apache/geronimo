<html>
<head>
<title>Geronimo Console Login</title>
<link href="<%=request.getContextPath()%>/main.css" rel="stylesheet" type="text/css">
<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
</head>

<body onload="document.login.j_username.focus()" leftmargin="0" topmargin="0" rightmargin="0">

<form name="login" action="j_security_check" method="POST">
  <%--  Top table is the banner --%>
  <table width="100%"  border="0" cellspacing="0" cellpadding="0">
    <tr>
      <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="0">
        <TR>
          <td width="200"><img src="<%=request.getContextPath()%>/images/head_left_login_586x86.gif"></td>
          <td class="Top" width="100%" border="0"></td>
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
    </tr>
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
          <td class="LightBackground">&nbsp;</td>
        </tr>
        <tr>
          <td>
          <table width="100%"  border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td width="25%" class="LightBackground">&nbsp;</td> 
                <td align="right" class="LightBackground">&nbsp;</td>
                <td width="6" class="LightBackground">&nbsp;</td>
                <td width="1" class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td width="25%" class="LightBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="LightBackground" ROWSPAN=3 ALIGN="center" ><img border="0" align="center" src="<%=request.getContextPath()%>/images/login_lock_64x55.gif"></td> 
                <td align="right" class="LightBackground"><strong>Username</strong></td>
                <td class="LightBackground"><strong>:</strong></td>
                <td width="1" class="LightBackground">&nbsp;</td>
                <td class="LightBackground"><input name="j_username" type="text" class="InputField" value="" size="20px" maxlength="25"/></td>
                <td width="17" class="LightBackground">&nbsp;</td>
              </tr>
              <tr>
                <td align="right" class="LightBackground"><strong>Password</strong></td>
                <td class="LightBackground"><strong>:</strong></td>
                <td width="1" class="LightBackground">&nbsp;</td>
                <td class="LightBackground"><input name="j_password" type="password" class="InputField" value="" size="20px" maxlength="25"/></td>
                <td class="LightBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="LightBackground">&nbsp;</td>
                <td colspan="4" align="center" class="LightBackground"><input name="submit" type="submit" value="Login"/></td>
                <td class="LightBackground">&nbsp;</td>
              </tr>
              <tr>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
                <td class="LightBackground">&nbsp;</td>
              </tr>
          </table>
          </td>
        </tr>
<%--
        <tr>
          <td class="LightBackground">&nbsp;</td>
        </tr>
--%>
        <tr>
          <td><font size="1"><STRONG>Welcome to the Geronimo Console&#8482;</FONT></td>
        </TR>
        <tr>
<%--           <TD><font size="1">All the other stuff that we need to say and that will take up several lines worth of information on the screen</FONT></TD>  --%>
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
              <td> <strong>Geronimo Console&#8482;</strong> has integrated the following components:<BR/>
                &nbsp;&#149;&nbsp; Application server (Apache Geronimo)<br/>
                &nbsp;&#149;&nbsp; Web server and servlet engine (Jetty)<br/>
                &nbsp;&#149;&nbsp; JSP compiler (Jasper)<br/>
                &nbsp;&#149;&nbsp; Relational database (Apache Derby)<br/>
                &nbsp;&#149;&nbsp; JSR 168 portlet container (Apache Pluto)<br/>
                &nbsp;&#149;&nbsp; Messaging (ActiveMQ)<br/>
                &nbsp;&#149;&nbsp; User management services<br/>
                &nbsp;&#149;&nbsp; Centralized administration console<br/>
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
<%--      <td class="Body" width="30%" align="Center" valign="top">     --%>
      <td width="30%" align="Center" valign="top">   
          <table class=BrightBox width="200" border cellspacing="0" cellpadding="0">
              <tr>
                <td width="50%" class="BrightTitle"><STRONG>&nbsp;Related Links</td>
              </tr>
              <tr>
                <td>
                <table width="100%"  border="0" cellspacing="0" cellpadding="0">
                  <tr>
                    <td align="center">&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td width="20" align="center"><img src="<%=request.getContextPath()%>/images/bullet.gif"></td>
                    <td><a href="http://">Technical Support</a></td>
                    <td width="10">&nbsp;</td>
                  </tr>
                  <tr>
                    <td align="center">&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td width="20" align="center"><img src="<%=request.getContextPath()%>/images/bullet.gif"></td>
                    <td><a href="http://">Download Updates</a> </td>
                    <td width="10">&nbsp;</td>
                  </tr>
                  <tr>
                    <td align="center">&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td width="20" align="center"><img src="<%=request.getContextPath()%>/images/bullet.gif"></td>
                    <td><a href="mailto:user@geronimo.apache.org">Mailing List</a></td>
                    <td width="10">&nbsp;</td>
                  </tr>
                  <tr>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                  <tr>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </table>
                </td>
              </tr>
            </table>
      </TD>
    </tr>
  </table>
</form>
</body>
</html>
