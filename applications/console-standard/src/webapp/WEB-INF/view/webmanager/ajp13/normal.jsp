<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This portlet shows all available network listeners.  It obsoletes the separate HTTP, HTTPS, and AJP portlets.
The two portlets above should be removed, though the HTTP portlet currently has a nicer appearance that should
be migrated first.  Also the start/stop actions below need to be implemented.  Finally, the add/edit screen
should have more fields as they become supported by the Jetty interface, and there should be a separate add/edit
JSP with additional logic for HTTPS connectors.</p>
<!-- Show existing connectors -->
<c:if test="${empty(connectors)}">There are no Connectors defined</c:if>
<c:if test="${!empty(connectors)}"><table>
    <tr><th>Protocol</th><th>Port</th><th>State</th><th>Actions</th><th>Type</th></tr>
<c:forEach var="info" items="${connectors}">
    <tr>
      <td>${info.protocol}</td>
      <td>${info.port}</td>
      <td>${info.stateName}</td>
      <td>
       <c:choose>
         <c:when test="${info.stateName eq 'running'}">
           [stop]
         </c:when>
         <c:otherwise>
           [start]
         </c:otherwise>
       </c:choose>
         <a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="edit" />
           <portlet:param name="name" value="${info.objectName}" />
         </portlet:actionURL>">edit</a>
         <a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="delete" />
           <portlet:param name="name" value="${info.objectName}" />
         </portlet:actionURL>">delete</a>
       </td>
      <td>${info.description}</td>
    </tr>
</c:forEach>
</table>
</c:if>

<!-- Links to add new connectors -->
<c:forEach var="protocol" items="${protocols}">
<br />
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="protocol" value="${protocol}" />
         </portlet:actionURL>">Add new ${protocol} listener</a>
</c:forEach>
