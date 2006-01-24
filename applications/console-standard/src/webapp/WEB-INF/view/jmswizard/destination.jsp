<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Create JMS Resource</b> -- Add Connection Factory</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="destination-after" />
    <input type="hidden" name="rar" value="${data.rarURI}" />
    <input type="hidden" name="dependency" value="${data.dependency}" />
    <input type="hidden" name="instanceName" value="${data.instanceName}" />
    <input type="hidden" name="workManager" value="${data.workManager}" /> <%-- todo: pick list for WorkManager --%>
    <c:forEach var="prop" items="${data.instanceProps}">
      <input type="hidden" name="${prop.key}" value="${prop.value}" />
    </c:forEach>
    <input type="hidden" name="currentFactoryID" value="${data.currentFactoryID}" />
    <input type="hidden" name="currentDestinationID" value="${data.currentDestinationID}" />
    <input type="hidden" name="factoryType" value="${data.factoryType}" />
    <input type="hidden" name="destinationType" value="${data.destinationType}" />
    <c:forEach var="factory" items="${data.connectionFactories}" varStatus="status">
      <input type="hidden" name="factory.${status.index}.factoryType" value="${factory.factoryType}" />
      <input type="hidden" name="factory.${status.index}.instanceName" value="${factory.instanceName}" />
      <input type="hidden" name="factory.${status.index}.transaction" value="${factory.transaction}" />
      <input type="hidden" name="factory.${status.index}.xaTransaction" value="${factory.xaTransactionCaching}" />
      <input type="hidden" name="factory.${status.index}.xaThread" value="${factory.xaThreadCaching}" />
      <input type="hidden" name="factory.${status.index}.poolMinSize" value="${factory.poolMinSize}" />
      <input type="hidden" name="factory.${status.index}.poolMaxSize" value="${factory.poolMaxSize}" />
      <input type="hidden" name="factory.${status.index}.poolIdleTimeout" value="${factory.poolIdleTimeout}" />
      <input type="hidden" name="factory.${status.index}.poolBlockingTimeout" value="${factory.poolBlockingTimeout}" />
      <c:forEach var="prop" items="${factory.instanceProps}">
        <input type="hidden" name="factory.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>
    <c:forEach var="dest" items="${data.adminObjects}" varStatus="status">
      <input type="hidden" name="destination.${status.index}.destinationType" value="${dest.destinationType}" />
      <c:if test="${status.index != data.currentDestinationID}">
        <input type="hidden" name="destination.${status.index}.name" value="${dest.name}" />
        <c:forEach var="prop" items="${dest.instanceProps}">
          <input type="hidden" name="destination.${status.index}.${prop.key}" value="${prop.value}" />
        </c:forEach>
      </c:if>
    </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: Admin Object Name -->
      <tr>
        <th><div align="right">Message Destination Name:</div></th>
        <td><input name="destination.${data.currentDestinationID}.name" type="text" size="20" value="${data.currentDestination.name}" /></td>
      </tr>
      <tr>
        <td></td>
        <td>A unique name for the connection factory; used to refer to this connection
            factory when mapping resource references from application components.</td>
      </tr>

    <!-- ENTRY FIELD: Config Properties -->
<c:if test="${!empty(provider.adminObjectDefinitions[data.destinationType].configProperties)}">
      <tr>
        <th colspan="2">Destination Configuration Settings</th>
      </tr>
  <c:forEach var="prop" items="${provider.adminObjectDefinitions[data.destinationType].configProperties}" varStatus="status">
      <c:set var="index" value="instance-config-${status.index}" />
      <tr>
        <th><div align="right">${prop.name}:</div></th>
        <td><input name="destination.${data.currentDestinationID}.instance-config-${status.index}" type="text" size="20" value="${data.currentDestination.instanceProps[index] == null ? prop.defaultValue : data.currentDestination.instanceProps[index]}" /></td>
      </tr>
      <tr>
        <td></td>
        <td><c:out value="${prop.description}" /></td>
      </tr>
  </c:forEach>
</c:if>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="hidden" name="nextAction" value="factoryType" />
            <input type="submit" value="Add Connection Factory" />
            <input type="button" value="Add Destination" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='destinationType';document.<portlet:namespace/>JMSForm.submit();return false;" />
            <input type="button" value="Show Plan" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='plan';document.<portlet:namespace/>JMSForm.submit();return false;" />
            <input type="button" value="Deploy Now" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='deploy';document.<portlet:namespace/>JMSForm.submit();return false;" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><b>Current Status for JMS Resource <c:out value="${data.instanceName}" /></b></p>
<ul>
  <li><c:out value="${data.connectionFactoryCount}" /> Connection Factories
      <c:if test="${data.connectionFactoryCount > 0}">
          <ul>
              <c:forEach var="factory" items="${data.connectionFactories}">
                  <li>
                      <c:choose>
                          <c:when test="${empty(factory.instanceName)}">
                              <i>In Process</i>
                          </c:when>
                          <c:otherwise>
                              <c:out value="${factory.instanceName}" />
                          </c:otherwise>
                      </c:choose>
                  </li>
              </c:forEach>
          </ul>
      </c:if>
  </li>
  <li><c:out value="${data.destinationCount}" /> Destinations
      <c:if test="${data.destinationCount > 0}">
          <ul>
              <c:forEach var="dest" items="${data.adminObjects}">
                  <li>
                      <c:choose>
                          <c:when test="${empty(dest.name)}">
                              <i>In Process</i>
                          </c:when>
                          <c:otherwise>
                              <c:out value="${dest.name}" />
                          </c:otherwise>
                      </c:choose>
                  </li>
              </c:forEach>
          </ul>
      </c:if>
  </li>
</ul>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>">Cancel</a></p>
