<%@ page language="java" contentType="text/html" session="false" %>
<%@ taglib uri="http://geronimo.apache.org/tlds/geronimo_jmx-console_v0-1.tld" prefix="jmx" %>

<html>
<head>
    <title>Geronimo Management Console -- Frequently Asked Questions</title>
    <link rel="stylesheet" href="/geronimo-web-console/style.css"/>
</head>

<body>
<img src="images/geronimo_logo_console.gif" border="0" alt="geronimo management console"/>
<div id="topNavBar"><div class="topNav">JMX Agent View</div></div>

<jsp:include page="leftNavigation.jsp"/>

<div id="panelTitle">Frequently Asked Questions</div>
<div id="panel"><a name="objectName"></a>

<p><strong>How do I construct an ObjectName filter?</strong></p>
<p>To construct an ObjectName filter you must first list the domain of the MBean.  The domain is the part of the MBean's CanonicalName which comes before the colon (:).  The part which comes after the colon is a list of the MBean's properties. In order to create a query, you must type in a domain substring, a colon, and a "&lt;name&gt;=&lt;value&gt;" pair followed by a comma. Wildcards (*) are also accepted as substrings in a limited fashion.</p>

<div class="listHead">These queries will work:</div>
<ul>
    <li>*:*</li>
    <li>jmi*:*</li>
    <li>*:name=RMI,*</li>
</ul>

<div class="listHead">These queries will not:</div>
<ul>
    <li>*</li>
    <li>jmi*</li>
    <li>*:*name*</li>
</ul>

<p>The console's capacity to filter the display results through this method is limited by the functionality of the underlying JMX implementation and the details of the JMX specification.</p>

</div>

</body>
</html>
