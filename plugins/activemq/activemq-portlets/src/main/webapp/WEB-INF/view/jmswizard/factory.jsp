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

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>JMSForm";
var <portlet:namespace/>requiredFields = new Array("factory.${data.currentFactoryID}.instanceName");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="jmswizard.common.emptyText"/>');
        return false;    
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="jmswizard.factory.title" /></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="factory-after" />
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
      <c:if test="${status.index != data.currentFactoryID}">
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
      </c:if>
    </c:forEach>
    <c:forEach var="dest" items="${data.adminObjects}" varStatus="status">
      <input type="hidden" name="destination.${status.index}.destinationType" value="${dest.destinationType}" />
      <input type="hidden" name="destination.${status.index}.name" value="${dest.name}" />
      <c:forEach var="prop" items="${dest.instanceProps}">
        <input type="hidden" name="destination.${status.index}.${prop.key}" value="${prop.value}" />
      </c:forEach>
    </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: Factory Instance Name -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>factory.${data.currentFactoryID}.instanceName"><fmt:message key="jmswizard.common.connectionFactoryName" /></label>:</div></th>
        <td><input name="factory.${data.currentFactoryID}.instanceName" id="<portlet:namespace/>factory.${data.currentFactoryID}.instanceName" type="text" size="20" value="${data.currentFactory.instanceName}" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="jmswizard.common.connectionFactoryExp" /></td>
      </tr>

    <!-- ENTRY FIELD: Transactions -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>factory.${data.currentFactoryID}.transaction"><fmt:message key="jmswizard.common.transactionSupport" /></label>:</div></th>
        <td>
          <select name="factory.${data.currentFactoryID}.transaction" id="<portlet:namespace/>factory.${data.currentFactoryID}.transaction">
            <option value="none"<c:if test="${data.currentFactory.transaction eq 'none'}"> selected</c:if>>None</option>
            <option value="local"<c:if test="${data.currentFactory.transaction eq 'local'}"> selected</c:if>>Local</option>
            <option value="xa"<c:if test="${data.currentFactory.transaction eq 'xa'}"> selected</c:if>>XA</option>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="jmswizard.common.transactionSupportExp" /></td>
      </tr>

    <tr>
      <th colspan="2"><fmt:message key="jmswizard.common.connectionPoolParameters" /></th>
    </tr>
  <!-- ENTRY FIELD: Min Size -->
    <tr>
      <th><div align="right"><label for="<portlet:namespace/>factory.${data.currentFactoryID}.poolMinSize"><fmt:message key="jmswizard.common.poolMinSize" /></label>:</div></th>
      <td><input name="factory.${data.currentFactoryID}.poolMinSize" id="<portlet:namespace/>factory.${data.currentFactoryID}.poolMinSize" type="text" size="5" value="${data.currentFactory.poolMinSize}"></td>
    </tr>
    <tr>
      <td></td>
      <td><fmt:message key="jmswizard.common.poolMinSizeExp" /></td>
    </tr>
  <!-- ENTRY FIELD: Max Size -->
    <tr>
      <th><div align="right"><label for="<portlet:namespace/>factory.${data.currentFactoryID}.poolMaxSize"><fmt:message key="jmswizard.common.poolMaxSize" /></label>:</div></th>
      <td><input name="factory.${data.currentFactoryID}.poolMaxSize" id="<portlet:namespace/>factory.${data.currentFactoryID}.poolMaxSize" type="text" size="5" value="${data.currentFactory.poolMaxSize}"></td>
    </tr>
    <tr>
      <td></td>
      <td><fmt:message key="jmswizard.common.poolMaxSizeExp" /></td>
    </tr>
  <!-- ENTRY FIELD: Blocking Timeout -->
    <tr>
      <th><div align="right"><label for="<portlet:namespace/>factory.${data.currentFactoryID}.poolBlockingTimeout"><fmt:message key="jmswizard.common.blockingTimeout"/></label>:</div></th>
      <td><input name="factory.${data.currentFactoryID}.poolBlockingTimeout" id="<portlet:namespace/>factory.${data.currentFactoryID}.poolBlockingTimeout" type="text" size="7" value="${data.currentFactory.poolBlockingTimeout}"> (in milliseconds)</td>
    </tr>
    <tr>
      <td></td>
      <td><fmt:message key="jmswizard.common.blockingTimeoutExp" /></td>
    </tr>
  <!-- ENTRY FIELD: Idle timeout -->
    <tr>
      <th><div align="right"><label for="<portlet:namespace/>factory.${data.currentFactoryID}.poolIdleTimeout"><fmt:message key="jmswizard.common.idleTimeout"/></label>:</div></th>
      <td><input name="factory.${data.currentFactoryID}.poolIdleTimeout" id="<portlet:namespace/>factory.${data.currentFactoryID}.poolIdleTimeout" type="text" size="5" value="${data.currentFactory.poolIdleTimeout}"> (in minutes)</td>
    </tr>
    <tr>
      <td></td>
      <td> <fmt:message key="jmswizard.common.idleTimeoutExp" /></td>
    </tr>

    <!-- ENTRY FIELD: Config Properties -->
<c:if test="${!empty(provider.connectionDefinitions[data.factoryType].configProperties)}">
      <tr>
        <th colspan="2"><fmt:message key="jmswizard.common.connectionFactoryConfigurationSettings" /></th>
      </tr>
  <c:forEach var="prop" items="${provider.connectionDefinitions[data.factoryType].configProperties}" varStatus="status">
      <c:set var="index" value="instance-config-${status.index}" />
      <tr>
        <th><div align="right">${prop.name}:</div></th>
        <td><input name="factory.${data.currentFactoryID}.instance-config-${status.index}" title="${prop.name}" type="text" size="20" value="${data.currentFactory.instanceProps[index] == null ? prop.defaultValue : data.currentFactory.instanceProps[index]}" /></td>
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
            <input type="hidden" name="nextAction" value="review" />
            <input type="submit" value='<fmt:message key="jmswizard.common.next"/>'  onClick="return <portlet:namespace/>validateForm()"/>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><b><fmt:message key="jmswizard.common.currentStatusForJMSResourceGroup" >
<fmt:param value="${data.instanceName}"  />
</fmt:message></b></p>
<ul>
  <li><c:out value="${data.connectionFactoryCount}" /> Connection Factor<c:choose><c:when test="${data.connectionFactoryCount == 1}">y</c:when><c:otherwise>ies</c:otherwise></c:choose>
      <c:if test="${data.connectionFactoryCount > 0}">
          <ul>
              <c:forEach var="factory" items="${data.connectionFactories}">
                  <li>
                      <c:choose>
                          <c:when test="${empty(factory.instanceName)}">
                              <i><fmt:message key="jmswizard.common.inProcess"/></i>
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
  <li><c:out value="${data.destinationCount}" /> Destination<c:if test="${data.destinationCount != 1}">s</c:if>
      <c:if test="${data.destinationCount > 0}">
          <ul>
              <c:forEach var="dest" items="${data.adminObjects}">
                  <li>
                      <c:choose>
                          <c:when test="${empty(dest.name)}">
                              <i><fmt:message key="jmswizard.common.inProcess"/></i>
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
            </portlet:actionURL>"><fmt:message key="jmswizard.common.cancel"/></a></p>
