<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE>Trade Login</TITLE>
<LINK rel="stylesheet" href="style.css" type="text/css" />
</HEAD>
<BODY bgcolor="#ffffff" link="#000099">
<%@ page session="false" %>
<TABLE style="font-size: smaller">
  <TBODY>
    <TR>
            <TD bgcolor="#8080c0" align="left" width="500" height="10" colspan="5"><FONT color="#ffffff"><B>Trade Login</B></FONT></TD>
            <TD align="center" bgcolor="#000000" width="100" height="10"><FONT color="#ffffff"><B>Trade</B></FONT></TD>
    </TR>
  </TBODY>
</TABLE>
<TABLE width="100%" height="30">
  <TBODY>
        <TR>
      <TD></TD>
      <TD><FONT color="#ff0033"><FONT color="#ff0033"><FONT color="#ff0033"><% String results;
results = (String) request.getAttribute("results");
if ( results != null )out.print(results);
%></FONT></FONT></FONT></TD>
      <TD></TD>
    </TR>
  </TBODY>
</TABLE>
<DIV align="left"></DIV>
<TABLE width="616">
  <TBODY>
    <TR>
      <TD width="2%" bgcolor="#e7e4e7" rowspan="3"></TD>
      <TD width="98%"><B>Log in</B>
      <HR>
      </TD>
    </TR>
    <TR>
      <TD align="right"><FONT size="-1">Username &nbsp; &nbsp; &nbsp;&nbsp; &nbsp;
      &nbsp; &nbsp; &nbsp; &nbsp; Password &nbsp;
      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</FONT></TD>
    </TR>
    <TR>
      <TD align="right">
            <FORM action="app" method="POST"><INPUT size="10" type="text" name="uid" value="uid:0"> &nbsp; &nbsp; &nbsp; &nbsp; <INPUT size="10" type="password" name="passwd" value="xxx"> &nbsp; <INPUT type="submit" value="Log in"><INPUT type="hidden" name="action" value="login"></FORM>
            </TD>
    </TR>
    </TBODY>
</TABLE>
<TABLE width="616">
  <TBODY>
    <TR>
      <TD width="2%"></TD>
      <TD width="98%">
      <HR>
      </TD>
    </TR>
    <TR>
      <TD bgcolor="#e7e4e7" rowspan="4"></TD>
      <TD><B><FONT size="-1" color="#000000">First time user? &nbsp;Please Register</FONT></B></TD>
    </TR>
    <TR>
      <TD>
      </TD>
    </TR>
    <TR>
      <TD align="right">
      <BLOCKQUOTE>
      <A href="register.jsp">Register&nbsp;With&nbsp;Trade</A>
      </BLOCKQUOTE>
      </TD>
    </TR>
    <TR>
      <TD>
      <HR>
      </TD>
    </TR>
  </TBODY>
</TABLE>
<TABLE height="54" style="font-size: smaller">
  <TBODY>
        <TR>
            <TD colspan="2">
            <HR>
            </TD>
        </TR>
        <TR>
            <TD colspan="2"></TD>
        </TR>
        <TR>
            <TD bgcolor="#8080c0" align="left" width="500" height="10"><B><FONT color="#ffffff">Trade Login</FONT></B></TD>
            <TD align="center" bgcolor="#000000" width="100" height="10"><FONT color="#ffffff"><B>Trade</B></FONT></TD>
        </TR>
        <TR>
            <TD colspan="2" align="center"> Created&nbsp;with&nbsp;IBM WebSphere Application Server and WebSphere Studio Application Developer<BR>

 
Copyright 2000, IBM Corporation</TD>
        </TR>
    </TBODY>
</TABLE>
</BODY>
</HTML>
