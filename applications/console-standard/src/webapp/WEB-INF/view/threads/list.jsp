<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists the thread pools defined in the Geronimo server.  <i>Note: Currently
not all threads used by Geronimo come from one of these thread pools.  We're working
on migrating the different components of Geronimo toward these thread pools.</i></p>

<table width="100%">
  <tr>
    <td class="DarkBackground">Name</td>
    <td class="DarkBackground" align="center">Size</td>
    <td class="DarkBackground" align="center">Actions</td>
  </tr>
<c:forEach var="pool" items="${pools}">
  <tr>
    <td>${pool.name}</td>
    <td>${pool.poolSize}</td>
    <td>
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="monitor-before" />
        <portlet:param name="abstractName" value="${pool.abstractName}" />
      </portlet:actionURL>">monitor</a>
    </td>
  </tr>
</c:forEach>
</table>
