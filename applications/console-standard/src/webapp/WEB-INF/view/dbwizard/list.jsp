<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists all the available database pools.</p>
<ul>
<c:forEach var="pool" items="${pools}">
  <li>${pool.name} (<a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="editExisting" />
              <portlet:param name="adapterObjectName" value="${pool.adapterObjectName}" />
              <portlet:param name="objectName" value="${pool.factoryObjectName}" />
            </portlet:actionURL>">edit</a>)</li>
</c:forEach>
</ul>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="rdbms" />
            </portlet:actionURL>">Add new database pool</a></p>
