<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Export Plugin</b> -- Save to Disk</p>

<p>Use the button below to save the plugin "${name}" to disk.</p>

<%-- todo: calculate the /console prefix somehow --%>
<form action="/console/car-export" method="GET">
    <input type="hidden" name="configId" value="${configId}" />
    <input type="submit" value="Export Plugin" />
</form>
