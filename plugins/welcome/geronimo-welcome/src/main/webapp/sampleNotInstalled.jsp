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
<!-- $Rev$ $Date$ -->
<!doctype html public "-//w3c//dtd html 4.0 transitional//en" "http://www.w3.org/TR/REC-html40/strict.dtd">
<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         session="false" %>
<html lang="en">
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Apache Geronimo Sample Application</title>
    <link rel="stylesheet" href="/main.css" type="text/css"/>
</head>

<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" rightmargin="0">

<!-- Header -->
<table width="100%">
  <tr>
    <td>
      <table width="100%" height="86"  border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td height="86" class="WelcomeLogo" border="0"></td>
          <td height="86" class="Top"  border="0">&nbsp;</TD>
        </tr>
        <tr>
          <td align="right" border="0">&nbsp;</td>
          <td align="right" border="0"><b><%= application.getServerInfo() %>&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<br />

<p>This sample has not been installed yet.  If this server can connect to the internet,
<a href="?install=true">click here</a> to install and connect to the sample application.
It will take a few minutes to download and install the sample and then load it.</p>

</body>
</html>
