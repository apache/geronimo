<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>The configuration ${configId} has been installed.  This included:</p>

<c:if test="${! empty configurations}">
  <p><b>Configurations:</b></p>
  <ul>
    <c:forEach var="config" items="${configurations}">
      <li>${config.name} (${config.action})</li>
    </c:forEach>
  </ul>
</c:if>

<c:if test="${! empty dependencies}">
  <p><b>Additional JARs:</b></p>
  <ul>
    <c:forEach var="dep" items="${dependencies}">
      <li>${dep.name} (${dep.action})</li>
    </c:forEach>
  </ul>
</c:if>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="results-after" />
    <input type="hidden" name="configId" value="${configId}" />
    <input type="hidden" name="repository" value="${repository}" />
    <input type="hidden" name="repo-user" value="${repouser}" />
    <input type="hidden" name="repo-pass" value="${repopass}" />
    <input type="submit" value="Start ${configId}" />
</form>
