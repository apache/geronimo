<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>Currently available JMS network connectors:</p>

<!-- Show existing connectors -->
<c:if test="${empty(connectors)}">There are no JMS network connectors defined</c:if>
<c:if test="${!empty(connectors)}">
<table width="100%">
  <tr>
    <td style="padding: 0 20px">
          <tr>
            <td class="DarkBackground">Name</td>
            <td class="DarkBackground" align="center">Broker</td>
            <td class="DarkBackground" align="center">Protocol</td>
            <td class="DarkBackground" align="center">Port</td>
            <td class="DarkBackground" align="center">State</td>
            <td class="DarkBackground" align="center">Actions</td>
          </tr>
<c:forEach var="info" items="${connectors}">
          <tr>
            <td>${info.displayName}</td>
            <td>${info.brokerName}</td>
            <td>${info.connector.protocol}</td>
            <td>${info.connector.port}</td>
            <td>${info.connector.stateInstance}</td>
            <td>
             <c:choose>
               <c:when test="${info.connector.stateInstance.name eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="objectName" value="${info.connector.objectName}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="objectName" value="${info.connector.objectName}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="objectName" value="${info.connector.objectName}" />
               </portlet:actionURL>">edit</a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="objectName" value="${info.connector.objectName}" />
               </portlet:actionURL>">delete</a>
             </td>
          </tr>
</c:forEach>
</table>
</c:if>

<!-- Links to add new connectors -->
<c:forEach var="entry" items="${brokers}">
<p>Add connector to ${entry.key}:</p>
<ul>
<c:forEach var="protocol" items="${protocols}">
<li><a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="brokerName" value="${entry.value.objectName}" />
           <portlet:param name="protocol" value="${protocol}" />
         </portlet:actionURL>">Add new <b>${protocol}</b> listener</a></li>
</c:forEach>
</c:forEach>