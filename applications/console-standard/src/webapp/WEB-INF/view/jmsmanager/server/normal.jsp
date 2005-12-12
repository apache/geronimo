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
            <td class="DarkBackground">Name</td>
            <td class="DarkBackground" align="center">State</td>
<!--
            <td class="DarkBackground" align="center">Actions</td>
-->
          </tr>
<c:forEach var="entry" items="${brokers}">
          <tr>
            <td>${entry.key}</td>
            <td>${entry.value.stateInstance}</td>
<!--
            <td>
             <c:choose>
               <c:when test="${entry.value.stateInstance.name eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="objectName" value="${entry.value.objectName}" />
               </portlet:actionURL>">stop</a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="objectName" value="${entry.value.objectName}" />
               </portlet:actionURL>">start</a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="edit" />
                 <portlet:param name="objectName" value="${entry.value.objectName}" />
               </portlet:actionURL>">edit</a>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="delete" />
                 <portlet:param name="objectName" value="${entry.value.objectName}" />
               </portlet:actionURL>">delete</a>
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