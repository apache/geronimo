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
var <portlet:namespace/>requiredFields = new Array("instanceName");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="jmswizard.common.emptyText"/>');
        return false;    
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="jmswizard.raInstance.title" /></p>

<p><fmt:message key="jmswizard.raInstance.titleExp" /></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="ra-after" />
    <input type="hidden" name="rar" value="${data.rarURI}" />
    <input type="hidden" name="dependency" value="${data.dependency}" />
    <input type="hidden" name="workManager" value="${data.workManager}" /> <%-- todo: pick list for WorkManager --%>
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
    <!-- ENTRY FIELD: RA Instance Name -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>instanceName"><fmt:message key="jmswizard.raInstance.resourceGroupName" /></label>:</div></th>
        <td><input name="instanceName" id="<portlet:namespace/>instanceName" type="text" size="20" value="${data.instanceName}" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="jmswizard.raInstance.resourceGroupNameExp" /></td>
      </tr>
    <!-- ENTRY FIELD: Config Properties -->
      <tr>
        <th colspan="2"><fmt:message key="jmswizard.raInstance.basicConfigSettings" /></th>
      </tr>
  <c:forEach var="prop" items="${provider.instanceConfigProperties}" varStatus="status">
      <c:set var="index" value="instance-config-${status.index}" />
      <tr>
        <th><div align="right">${prop.name}:</div></th>
        <td><input name="${index}" title="${prop.name}" type="text" size="20" value="${data.instanceProps[index] == null ? prop.defaultValue : data.instanceProps[index]}" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="jmswizard.raInstance.${prop.name}"/></td>
      </tr>
  </c:forEach>
    <!-- SUBMIT BUTTON -->
      <tr>
        <th><div align="right"></div></th>
        <td>
            <input type="hidden" name="nextAction" value="review" />
            <input type="submit" value='<fmt:message key="jmswizard.common.next"/>' onClick="return <portlet:namespace/>validateForm()"/>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>"><fmt:message key="jmswizard.common.cancel"/></a></p>
