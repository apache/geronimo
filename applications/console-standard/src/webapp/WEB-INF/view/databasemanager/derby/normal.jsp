<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<br>
<strong>Connection Name:</strong>&nbsp;${ds.name}
<br><br>
<table width="100%">
    <tr><td><strong>UserName</strong></td><td>${ds.userName}</td></tr>
    <tr><td><strong>Database Name</strong></td><td>${ds.databaseName}</td></tr>
    <tr><td><strong>Create Database</strong></td><td>${ds.createDatabase}</td></tr>
    <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td><strong>ConnectionCount</strong></td><td>${connectionManagerInfo.connectionCount}</td></tr>
    <tr><td><strong>IdleConnectionCount</strong></td><td>${connectionManagerInfo.idleConnectionCount}</td></tr>
    <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td><strong>PartitionCount</strong></td><td>${connectionManagerInfo.partitionCount}</td></tr>
    <tr><td><strong>PartitionMaxSize</strong></td><td>${connectionManagerInfo.partitionMaxSize}</td></tr>
    <tr><td><strong>PartitionMinSize</strong></td><td>${connectionManagerInfo.partitionMinSize}</td></tr>
    <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
    <tr><td><strong>Login Timeout (Seconds)</strong></td><td>${ds.loginTimeout}</td></tr>
    <tr><td><strong>BlockingTimeout (Milliseconds)</strong></td><td>${connectionManagerInfo.blockingTimeoutMilliseconds}</td></tr>
    <tr><td><strong>Idle Timeout (Minutes)</strong></td><td>${connectionManagerInfo.idleTimeoutMinutes}</td></tr>
    <tr><td colspan="2" align="center">
        <a href='<portlet:renderURL><portlet:param name="name" value="${ds.objectName}"/><portlet:param name="mode" value="config"/></portlet:renderURL>'>change</a>
        &nbsp;
        <a href='<portlet:renderURL><portlet:param name="mode" value="list"/></portlet:renderURL>'>back</a>
    </td></tr>
</table>

