<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">
<META name="GENERATOR" content="IBM WebSphere Page Designer V3.0.2 for Windows">
<META http-equiv="Content-Style-Type" content="text/css">
<!-- Don't cache on netscape! -->
<title>PingJsp</title>
</head>
<body>
<%! int hitCount = 0;
    String initTime = new java.util.Date().toString();
 %>
<HR>
<BR>
<FONT size="+2" color="#000066">PING JSP:<BR>
</FONT><FONT size="+1" color="#000066">Init time: <%= initTime %></FONT>
<% hitCount++; %>
<P><B>Hit Count: <%= hitCount %></B></P>
</body>
</html>
