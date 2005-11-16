<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<!-- Show existing connectors -->
<c:choose>
  <c:when test="${empty(containers)}">There are no Web Containers defined</c:when>
  <c:otherwise>
    <c:forEach var="container" items="${containers}">
      <c:if test="${fn:length(containers) > 1}"><p><b>Connectors for ${container.name}:</b></p></c:if>
        <c:choose>
          <c:when test="${empty(container.connectors)}"><p>There are no connectors defined for ${container.name}</p></c:when>
          <c:otherwise>
<table width="100%">
  <tr>
    <td style="padding: 0 20px">
          <tr>
            <td class="DarkBackground">Name</td>
            <td class="DarkBackground" align="center">Protocol</td>
            <td class="DarkBackground" align="center">Port</td>
            <td class="DarkBackground" align="center">State</td>
            <td class="DarkBackground" align="center">Actions</td>
            <td class="DarkBackground" align="center">Type</td>
          </tr>
<c:forEach var="info" items="${container.connectors}">
          <tr>
            <td>${info.displayName}</td>
            <td>${info.protocol}</td>
            <td>${info.port}</td>
            <td>${info.stateName}</td>
            <td>
             <c:choose>
               <c:when test="${info.stateName eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">edit</a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="name" value="${info.objectName}" />
                 <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
                 <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
               </portlet:actionURL>">delete</a>
             </td>
            <td>${info.description}</td>
          </tr>
</c:forEach>
</table>
          </c:otherwise>
        </c:choose>


<!-- Links to add new connectors -->
<c:forEach var="protocol" items="${container.protocols}">
<br />
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="protocol" value="${protocol}" />
           <portlet:param name="managerObjectName" value="${container.managerObjectName}" />
           <portlet:param name="containerObjectName" value="${container.containerObjectName}" />
         </portlet:actionURL>">Add new ${protocol} listener for ${container.name}</a>
</c:forEach>

    </c:forEach>
  </c:otherwise>
</c:choose>
