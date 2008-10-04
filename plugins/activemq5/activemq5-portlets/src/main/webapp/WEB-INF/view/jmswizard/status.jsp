<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<p><fmt:message key="jmswizard.status.title" /></p>

<c:choose>
    <c:when test="${data.connectionFactoryCount == 0 && data.destinationCount == 0}">
        <p><fmt:message key="jmswizard.status.noFactoriesDestinations" /></p>
    </c:when>
    <c:otherwise>
        <p><fmt:message key="jmswizard.status.factoriesDestinationsAdded" /></p>

        <table border="0" width="100%">
            <tr><th colspan="3"><fmt:message key="jmswizard.status.resourceGroup" /> <c:out value="${data.instanceName}"/></th></tr>
            <tr>
                <td class="DarkBackground"><fmt:message key="jmswizard.common.type"/></td>
                <td class="DarkBackground"><fmt:message key="jmswizard.common.name"/></td>
                <td class="DarkBackground"><fmt:message key="jmswizard.common.interface"/></td>
            </tr>
            <c:forEach var="factory" items="${data.connectionFactories}">
                <tr>
                    <td><fmt:message key="jmswizard.common.connFactory" /></td>
                    <td><c:out value="${factory.instanceName}" /></td>
                    <td><c:out value="${provider.connectionDefinitions[factory.factoryType].connectionFactoryInterface}" /></td>
                </tr>
            </c:forEach>
            <c:forEach var="dest" items="${data.adminObjects}">
                <tr>
                    <td><fmt:message key="jmswizard.common.destination" /></td>
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
            <input type="submit" value='<fmt:message key="jmswizard.status.addConnFactory" />' />
            <input type="button" value='<fmt:message key="jmswizard.status.addDestination" />' onclick="document.<portlet:namespace/>JMSForm.nextAction.value='destinationType';document.<portlet:namespace/>JMSForm.submit();return false;" />
<c:if test="${data.connectionFactoryCount > 0 || data.destinationCount > 0}">
            <input type="button" value='<fmt:message key="jmswizard.status.showPlan" />'  onclick="document.<portlet:namespace/>JMSForm.nextAction.value='plan';document.<portlet:namespace/>JMSForm.submit();return false;" />
            <input type="button" value='<fmt:message key="jmswizard.status.deployNow" />' onclick="document.<portlet:namespace/>JMSForm.nextAction.value='deploy';document.<portlet:namespace/>JMSForm.submit();return false;" />
</c:if>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>"><fmt:message key="jmswizard.common.cancel"/></a></p>
