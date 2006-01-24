<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>JMS Resource Group</b> -- Current Progress</p>

<c:choose>
    <c:when test="${data.connectionFactoryCount == 0 && data.destinationCount == 0}">
        <p>So far, you've entered the basic configuration information required for
          a JMS resource group.  Now you can create connection factories and
          destinations.  When you're finished adding connection factories and
          destinations, you can review the Geronimo deployment plan for this JMS
          resource group, or go ahead and deploy it.</p>
    </c:when>
    <c:otherwise>
        <p>These are the connection factories and destinations you've added to the
          JMS resource group so far.  When you're finished adding connection factories and
          destinations, you can review the Geronimo deployment plan for this resource
          group, or go ahead and deploy it.</p>

        <table border="0" width="100%">
            <tr><th colspan="3">Resource Group <c:out value="${data.instanceName}"/></th></tr>
            <tr>
                <td class="DarkBackground">Type</td>
                <td class="DarkBackground">Name</td>
                <td class="DarkBackground">Interface</td>
            </tr>
            <c:forEach var="factory" items="${data.connectionFactories}">
                <tr>
                    <td>Connection Factory</td>
                    <td><c:out value="${factory.instanceName}" /></td>
                    <td><c:out value="${provider.connectionDefinitions[factory.factoryType].connectionFactoryInterface}" /></td>
                </tr>
            </c:forEach>
            <c:forEach var="dest" items="${data.adminObjects}">
                <tr>
                    <td>Destination</td>
                    <td><c:out value="${dest.name}" /></td>
                    <td><c:out value="${provider.adminObjectDefinitions[dest.destinationType].adminObjectInterface}" /></td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="review-after" />
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
      <input type="hidden" name="destination.${status.index}.name" value="${dest.name}" />
      <c:forEach var="prop" items="${dest.instanceProps}">
        <input type="hidden" name="destination.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>
    <table border="0">
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
            <input type="hidden" name="nextAction" value="factoryType" />
            <input type="submit" value="Add Connection Factory" />
            <input type="button" value="Add Destination" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='destinationType';document.<portlet:namespace/>JMSForm.submit();return false;" />
<c:if test="${data.connectionFactoryCount > 0 || data.destinationCount > 0}">
            <input type="button" value="Show Plan" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='plan';document.<portlet:namespace/>JMSForm.submit();return false;" />
            <input type="button" value="Deploy Now" onclick="document.<portlet:namespace/>JMSForm.nextAction.value='deploy';document.<portlet:namespace/>JMSForm.submit();return false;" />
</c:if>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>">Cancel</a></p>
