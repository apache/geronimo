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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>JmsConnectorForm";
var <portlet:namespace/>requiredFields = new Array("host");
var <portlet:namespace/>numericFields = new Array("port");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields))
        return false;
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i]))
            return false;
    }
    return true;
}
</script>

<form name="<portlet:namespace/>JmsConnectorForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="protocol" value="${protocol}">
<c:choose>
<c:when test="${mode eq 'save'}">
  <input type="hidden" name="connectorURI" value="${connectorURI}">
</c:when>
<c:otherwise>
  <input type="hidden" name="brokerURI" value="${brokerURI}">
</c:otherwise>
</c:choose>
<table width="100%%"  border="0">

<!-- Current Task -->
<c:choose>
  <c:when test="${mode eq 'add'}">
    <tr><th colspan="2" align="left"><fmt:message key="jmsmanager.server.connector.editGeneric.addJMSConnector"><fmt:param value="${protocol}"/><fmt:param value="${brokerName}"/></fmt:message></th></tr>
  </c:when>
  <c:otherwise>
    <tr><th colspan="2" align="left"><fmt:message key="jmsmanager.server.connector.editGeneric.editJMSConnector"><fmt:param value="${protocol}"/><fmt:param value="${connectorName}"/><fmt:param value="${brokerName}"/></fmt:message></th></tr>
  </c:otherwise>
</c:choose>

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>name"><fmt:message key="jmsmanager.server.connector.editGeneric.uniqueName" /></label>: </div></td>
    <td><input name="name" id="<portlet:namespace/>name" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><fmt:message key="jmsmanager.server.connector.editGeneric.namingJMSConnectors" /></td>
  </tr>
  <script language="JavaScript">
    <portlet:namespace/>requiredFields = new Array("name").concat(<portlet:namespace/>requiredFields);
  </script>
</c:if>
<!-- Host Field -->
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>host"><fmt:message key="jmsmanager.common.host"/></label>: </div></td>
    <td>
      <input name="host" id="<portlet:namespace/>host" type="text" size="30" value="${host}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><fmt:message key="jmsmanager.common.hostExp" /></td>
  </tr>
<!-- Port Field -->
  <tr>
    <td><div align="right"><label for="<portlet:namespace/>port"><fmt:message key="jmsmanager.common.port" /></label>: </div></td>
    <td>
      <input name="port" id="<portlet:namespace/>port" type="text" size="5" value="${port}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> <fmt:message key="jmsmanager.common.portExp" /></td>
  </tr>
<!-- Form buttons -->
  <tr>
    <td><div align="right"></div></td>
    <td>
      <input name="submit" type="submit" value='<fmt:message key="jmsmanager.common.save"/>' onClick="return <portlet:namespace/>validateForm();">
      <input name="reset" type="reset" value='<fmt:message key="jmsmanager.common.reset"/>' >
    </td>
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'><fmt:message key="jmsmanager.common.listJMSConnectors" /></a>
