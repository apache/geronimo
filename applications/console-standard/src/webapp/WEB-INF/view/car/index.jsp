<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This portlet lets you import or export Geronimo configurations from a Maven repository.
    This can be used to install new features into a Geronimo server at runtime.</p>

<h2>Export Configurations</h2>

<p>Choose a configuration in the current Geronimo server to export.  The configuration
  will be saved as a CAR file to your local filesystem.</p>

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
repository must have a geronimo-configs.xml file in the root directory
listing the available configurations in the repository.</p>

<p>If you want to point to a remote Geronimo server, enter a URL such as
<tt>http://geronimo-server:8080/console-standard/maven-repo/</tt> and the enter
the administrator username and password in the optional authentication fields.</p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="index-after" />
    Repository: <input type="text" name="repository" value="${repository}" size="20" maxlength="200" />
    <input type="submit" value="Search for Configurations" />
    <br />Optional Authentication:
       User: <input type="text" name="username" value="${repouser}" size="20" maxlength="200" />
       Password: <input type="password" name="password" value="${repopass}" size="20" maxlength="200" />

</form>
