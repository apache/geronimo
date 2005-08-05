<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<br>
<strong>Connection Name:</strong>&nbsp;${ds.name}
<br><br>

<table width="100%">
    <tr><td><strong>User</strong></td><td>${ds.user}</td></tr>
    <!-- <tr><td><strong>Password</strong></td><td>${ds.password}</td></tr> -->
    <tr><td><strong>Server Name</strong></td><td>${ds.serverName}</td></tr>
    <tr><td><strong>Port no.</strong></td><td>${ds.portNumber}</td></tr>
    <tr><td><strong>DB Name</strong></td><td>${ds.databaseName}</td></tr>
    <tr><td><strong>Global JNDI Name</strong></td><td>${ds.globalJNDIName}</td></tr>
    <tr><td colspan="2" align="center">
        <a href='<portlet:renderURL><portlet:param name="name" value="${ds.objectName}"/><portlet:param name="mode" value="config"/></portlet:renderURL>'>change</a>
        &nbsp;
        <a href='<portlet:renderURL><portlet:param name="mode" value="list"/></portlet:renderURL>'>back</a>
    </td></tr>
</table>