<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<br>
<strong>Connection Name:</strong>&nbsp;${ds.name}
<br><br>
<table width="100%">
    <tr><td><strong>UserName</strong></td><td>${ds.userName}</td></tr>
    <tr><td><strong>Driver</strong></td><td>${ds.driver}</td></tr>
    <tr><td><strong>ConnectionURL</strong></td><td>${ds.connectionURL}</td></tr>
    <tr><td><strong>ExceptionSorterClass</strong></td><td>${ds.exceptionSorterClass}</td></tr>
    <tr><td><strong>PartitionCount</strong></td><td>${connectionManagerInfo.partitionCount}</td></tr>
    <tr><td><strong>ConnectionCount</strong></td><td>${connectionManagerInfo.connectionCount}</td></tr>
    <tr><td><strong>IdleConnectionCount</strong></td><td>${connectionManagerInfo.idleConnectionCount}</td></tr>
    <tr><td><strong>PartitionMaxSize</strong></td><td>${connectionManagerInfo.partitionMaxSize}</td></tr>
    <tr><td><strong>PartitionMinSize</strong></td><td>${connectionManagerInfo.partitionMinSize}</td></tr>
    <tr><td><strong>BlockingTimeout (Milliseconds)</strong></td><td>${connectionManagerInfo.blockingTimeoutMilliseconds}</td></tr>
    <tr><td><strong>Idle Timeout (Minutes)</strong></td><td>${connectionManagerInfo.idleTimeoutMinutes}</td></tr>
    <tr><td colspan="2" align="center">
        <a href='<portlet:renderURL><portlet:param name="name" value="${ds.objectName}"/><portlet:param name="mode" value="config"/></portlet:renderURL>'>change</a>
        &nbsp;
        <a href='<portlet:renderURL><portlet:param name="mode" value="list"/></portlet:renderURL>'>back</a>
    </td></tr>
</table>

