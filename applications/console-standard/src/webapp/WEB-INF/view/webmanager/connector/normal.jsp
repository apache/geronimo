<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>There are still updates necessary to this portlet --
there should be a separate add/edit JSP with additional logic for HTTPS connectors.</p>

<!-- Show existing connectors -->
<c:if test="${empty(connectors)}">There are no Connectors defined</c:if>
<c:if test="${!empty(connectors)}">
<table width="100%">
  <tr>
    <td style="padding: 0 20px">
          <tr>
            <td class="DarkBackground">Name
            <td class="DarkBackground" align="center">Protocol
            <td class="DarkBackground" align="center">Port
            <td class="DarkBackground" align="center">State
            <td class="DarkBackground" align="center">Actions
            <td class="DarkBackground" align="center">Type
            </td>
          </tr>
<c:forEach var="info" items="${connectors}">
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
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="name" value="${info.objectName}" />
               </portlet:actionURL>">start</a>
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
