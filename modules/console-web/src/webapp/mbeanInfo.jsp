<%@ page language="java" contentType="text/html" session="false" %>
<%@ taglib uri="/WEB-INF/geronimo_jmx-console_v0-1.tld" prefix="jmx" %>

<html>
<head>
    <title>Geronimo Management Console</title>
    <link rel="stylesheet" href="/jmx-console/style.css"/>
</head>

<body>
<jmx:MBeanServerContext>
<img src="images/geronimo_logo_console.gif" border="0" alt="geronimo jmx console"/>
<div id="topNavBar"><div class="topNav">JMX Web Console</div></div>

<jsp:include page="leftNavigation.jsp"/>

<div id="panelTitle">MBean Attributes View</div>

<div id="panel" class="two">
<jmx:MBeanAttributes/>
</div>

</jmx:MBeanServerContext>
</body>
</html>
