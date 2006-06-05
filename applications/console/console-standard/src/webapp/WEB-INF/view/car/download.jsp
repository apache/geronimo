<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>Processing ${configId}...</p>

<p>Found the following dependencies for this plugin.  Any missing dependencies
    will be installed for you automatically if you proceed.</p>
<ul>
<c:forEach var="jar" items="${dependencies}">
    <li>${jar}</li>
</c:forEach>
</ul>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>">
    <input type="hidden" name="file" value="${file}" />
    <input type="hidden" name="configId" value="${configId}" />
    <input type="hidden" name="mode" value="download-after" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
    <input type="hidden" name="proceed" value="true" />
    <input type="submit" value="Install Plugin" />
</form>

<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>">Cancel</a></p>
