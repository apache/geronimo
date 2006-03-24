<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This portlet lets you import or export Geronimo configurations from a Maven repository.
    This can be used to install new features into a Geronimo server at runtime.</p>

<h2>Export Configurations</h2>

<p>Choose a configuration in the current Geronimo server to export:</p>

<form action="<%=request.getContextPath()%>/car-export">
    <select name="configId">
        <option />
      <c:forEach var="config" items="${configurations}">
        <option>${config.configID}</option>
      </c:forEach>
    </select>
    <input type="submit" value="Export Configuration" />
</form>

<h2>Import Configurations</h2>

<p>Choose a remote repository to inspect for available Geronimo configurations.  The
repository must have a geronimo-configurations.properties file in the root directory
listing the available configurations in the repository (this requirement should be
relaxed in a future update).</p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="index-after" />
    Repository: <input type="text" name="repository" value="http://localhost/maven/" size="20" maxlength="200" />
    <input type="submit" value="Search for Configurations" />
</form>
