<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page lists the thread pools defined in the Geronimo server.  <i>Note: Currently
not all threads used by Geronimo come from one of these thread pools.  We're working
on migrating the different components of Geronimo toward these thread pools.</i></p>

<table width="100%">
  <tr>
    <th class="DarkBackground">Name</th>
    <th class="DarkBackground" align="center">Size</th>
    <th class="DarkBackground" align="center">Actions</th>
  </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="pool" items="${pools}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
    <td class="${backgroundClass}">${pool.name}</td>
    <td class="${backgroundClass}">${pool.poolSize}</td>
    <td class="${backgroundClass}">
      <a href="<portlet:actionURL portletMode="view">
        <portlet:param name="mode" value="monitor-before" />
        <portlet:param name="abstractName" value="${pool.abstractName}" />
      </portlet:actionURL>">monitor</a>
    </td>
  </tr>
</c:forEach>
</table>
