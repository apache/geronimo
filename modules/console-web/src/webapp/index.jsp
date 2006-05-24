<%@ page language="java" contentType="text/html" session="false" %>
<%@ taglib uri="http://geronimo.apache.org/tlds/geronimo_jmx-console_v0-1.tld" prefix="jmx" %>

<html>
<head>
    <title>Geronimo Management Console</title>
    <link rel="stylesheet" href="/geronimo-web-console/style.css"/>
</head>

<body>
<jmx:MBeanServerContext>
<img src="images/geronimo_logo_console.gif" border="0" alt="geronimo managment console"/>
<div id="topNavBar"><div class="topNav">JMX Agent View</div></div>

<jsp:include page="leftNavigation.jsp"/>

<div id="panelTitle">MBean Stack</div>
<div id="panel">

<form method="GET" action="index.jsp">
<input class="textInput" type="text" size="30" name="ObjectNameFilter" value="<jmx:MBeanServerContextValue type='ObjectNameFilter'/>"/>
<input class="submit" type="submit" tabindex="1" value="Filter Output"/>
<jmx:ClearFilter/>
(<a href="/geronimo-web-console/faq.jsp#objectName"/>help</a>)
</form>

<jmx:MBeanServerContents/>

</div>

</jmx:MBeanServerContext>
</body>
</html>
