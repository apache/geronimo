<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This portlet lets you install or create Geronimo plugins.
    This can be used to install new features into a Geronimo server at runtime.</p>

<h2>Install Geronimo Plugins</h2>

<p>Choose a remote repository to inspect for available Geronimo plugins.  The
repository must have a <tt>geronimo-plugins.xml</tt> file in the root directory
listing the available plugins in the repository.</p>

<p>You can also download running configurations from another Geronimo server
just as if you're browsing and installing third-party plugins.
 If you want to point to a remote Geronimo server, enter a URL such as
<tt>http://geronimo-server:8080/console-standard/maven-repo/</tt> and the enter
the administrator username and password in the optional authentication fields.</p>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="index-after" />
    <b>Repository:</b> <input type="text" name="repository" value="${repository}" size="30" maxlength="200" />
    <input type="submit" value="Search for Plugins" />
    <br /><b>Optional Authentication:</b>
       User: <input type="text" name="username" value="${repouser}" size="20" maxlength="200" />
       Password: <input type="password" name="password" value="${repopass}" size="20" maxlength="200" />

</form>

<h2>Create Geronimo Plugin</h2>

<p>Choose a configuration in the current Geronimo server to export as a Geronimo
   plugin.  The configuration will be saved as a CAR file to your local filesystem.
   <i>Note: at present, you must manually add a <tt>META-INF/geronimo-plugin.xml</tt>
   file to the CAR after you export it in order for it to be a valid plugin.</i></p>

<%-- todo: calculate the /console prefix somehow --%>
<form action="/console/car-export">
    <select name="configId">
        <option />
      <c:forEach var="config" items="${configurations}">
        <option>${config.configID}</option>
      </c:forEach>
    </select>
    <input type="submit" value="Export Plugin" />
</form>

