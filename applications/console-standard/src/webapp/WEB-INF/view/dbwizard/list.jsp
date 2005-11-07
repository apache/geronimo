<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists all the available database pools.</p>
<ul>
<c:forEach var="pool" items="${pools}">
  <li>${pool.name}</li>
</c:forEach>
</ul>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="rdbms" />
            </portlet:actionURL>">Add new database pool</a></p>
