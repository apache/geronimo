<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>The JMS brokers available in the server are:</p>

<!-- Show existing connectors -->
<c:if test="${empty(brokers)}">There are no JMS brokers defined</c:if>
<c:if test="${!empty(brokers)}">
<table width="50%">
  <tr><td style="padding: 0 20px"></td></tr>
          <tr>
            <th class="DarkBackground">Name</th>
            <th class="DarkBackground" align="center">State</th>
<!--
            <th class="DarkBackground" align="center">Actions</th>
-->
          </tr>
<c:set var="backgroundClass" value='MediumBackground'/>
<c:forEach var="entry" items="${brokers}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">${entry.brokerName}</td>
            <td class="${backgroundClass}">${entry.broker.stateInstance}</td>
<!--
            <td class="${backgroundClass}">
             <c:choose>
               <c:when test="${entry.broker.stateInstance.name eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="objectName" value="${entry.brokerURI}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="objectName" value="${entry.brokerURI}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="objectName" value="${entry.brokerURI}" />
               </portlet:actionURL>">edit</a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="objectName" value="${entry.brokerURI}" />
               </portlet:actionURL>" onClick="return confirm('Are you sure you want to delete ${entry.brokerName}?');">delete</a>
             </td>
-->
          </tr>
</c:forEach>
</table>
</c:if>
<!--
<br />
<a href="<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="new" />
         </portlet:actionURL>">Add new JMS Broker</a>
-->