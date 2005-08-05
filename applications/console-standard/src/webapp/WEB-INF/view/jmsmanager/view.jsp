<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<br>
<table>
<tr>
<td align=LEFT colspan="3"> <a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="createDestination"/></portlet:renderURL>">Add Queue/Topic </a> </td>
</tr>
<c:if test="${!destinationsMsg}">

   <tr>
      <td colspan="3">${destinationsMsg}</td>
   </tr>

</c:if>
<tr>
  <th>
     Message Destination Name
  </th>
  <th>
     Physical Name
  </th>
  <th>
     Type
  </th>
  <th>
     Application Name
  </th>
  <th>
     Module Name
  </th>
  <th>
     Actions
  </th>
</tr>
  <c:forEach var="destination" items="${destinations}">
  <tr>
      <td  align=CENTER>
            <c:out value="${destination.name}"/>
      </td>
      <td  align=CENTER>
            <c:out value="${destination.physicalName}"/>
      </td>
      <td  align=CENTER>
            <c:out value="${destination.type}"/>
            &nbsp;
      </td>
      <td  align=CENTER>
            <c:out value="${destination.applicationName}"/>
      </td>
      <td  align=CENTER>
            <c:out value="${destination.moduleName}"/>
      </td>
      <td  align=CENTER>
		<table border="0">
		<tr>
         <td>
		<c:if test="${destination.removable}">
         <a href="<portlet:actionURL portletMode="view"><portlet:param name="processaction" value="removeDestination"/><portlet:param name="destinationConfigURI" value="${destination.configURI}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:actionURL>">remove </a>
         </c:if>
         </td>
         <!--a href="<portlet:actionURL portletMode="view"><portlet:param name="processaction" value="statistics"/><portlet:param name="destinationName" value="${destination.name}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:actionURL>">statistics</a-->
         <td>
		<c:if test="${destination.viewable}">
         <a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewMessages"/><portlet:param name="destinationName" value="${destination.name}"/><portlet:param name="destinationApplicationName" value="${destination.applicationName}"/><portlet:param name="destinationModuleName" value="${destination.moduleName}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:renderURL>">view messages</a>
         </c:if>
         </td>
         <td>
		<c:if test="${destination.viewable}">
         <a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDLQ"/><portlet:param name="destinationName" value="${destination.name}"/><portlet:param name="destinationApplicationName" value="${destination.applicationName}"/><portlet:param name="destinationModuleName" value="${destination.moduleName}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:renderURL>">view DLQ</a></td>
         </c:if>
		</tr>		
		</table>

      </td>

  </tr>
  </c:forEach>
 </table>