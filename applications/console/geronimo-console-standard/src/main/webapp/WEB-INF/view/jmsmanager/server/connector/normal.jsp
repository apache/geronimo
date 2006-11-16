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
            <th class="DarkBackground">Name</th>
            <th class="DarkBackground" align="center">Broker</th>
            <th class="DarkBackground" align="center">Protocol</th>
            <th class="DarkBackground" align="center">Port</th>
            <th class="DarkBackground" align="center">State</th>
            <th class="DarkBackground" align="center">Actions</th>
          </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="info" items="${connectors}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">${info.connectorName}</td>
            <td class="${backgroundClass}">${info.brokerName}</td>
            <td class="${backgroundClass}">${info.connector.protocol}</td>
            <td class="${backgroundClass}">${info.connector.port}</td>
            <td class="${backgroundClass}">${info.connector.stateInstance}</td>
            <td class="${backgroundClass}">
             <c:choose>
               <c:when test="${info.connector.stateInstance.name eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="brokerURI" value="${info.brokerURI}" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="brokerURI" value="${info.brokerURI}" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="brokerURI" value="${info.brokerURI}" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
               </portlet:actionURL>">edit</a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="brokerURI" value="${info.brokerURI}" />
                 <portlet:param name="connectorURI" value="${info.connectorURI}" />
               </portlet:actionURL>" onClick="return confirm('Are you sure you want to delete ${info.connectorName}?');">delete</a>
             </td>
          </tr>
</c:forEach>
</table>
</c:if>

<!-- Links to add new connectors -->
<c:forEach var="info" items="${brokers}">
<p>Add connector to ${info.brokerName}:</p>
<ul>
<c:forEach var="protocol" items="${protocols}">
<li><a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
           <portlet:param name="brokerURI" value="${info.brokerURI}" />
           <portlet:param name="protocol" value="${protocol}" />
         </portlet:actionURL>">Add new <b>${protocol}</b> listener</a></li>
</c:forEach>
</c:forEach>