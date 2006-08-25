<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<h2>Add Plugin Repository</h2>

<c:if test="${!empty repositories}">
<p>The currently available plugin repositories are:</p>
<ul>
  <c:forEach var="repo" items="${repositories}">
    <li><tt>${repo}</tt></li>
  </c:forEach>
</ul>
</c:if>

<p>To add a new plugin repository to this list, enter the URL to the repository.  The
repository must have a <tt>geronimo-plugins.xml</tt> file in the root directory
listing the available plugins in the repository.</p>

<p>You can also download running configurations from another Geronimo server
just as if you're browsing and installing third-party plugins.
 If you want to point to a remote Geronimo server, enter a URL such as
<tt>http://geronimo-server:8080/console-standard/maven-repo/</tt></p>

<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="addRepository-after" />
    <b>New Repository:</b> <input type="text" name="newRepository" size="40" maxlength="200" />
    <br />
    <c:if test="${!empty repoError}">
      <p><font color="red">${repoError}</font></p>
    </c:if>
    <br />
    <input type="submit" value="Add Repository" />
</form>

<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="index-before" /></portlet:actionURL>">Cancel</a></p>
