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

<form name="<portlet:namespace/>JmsConnectorForm" action="<portlet:actionURL/>">
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
    <tr><th colspan="2" align="left">Add new ${protocol} connector for ${brokerName}</th></tr>
  </c:when>
  <c:otherwise>
    <tr><th colspan="2" align="left">Edit ${protocol} connector ${connectorName} for ${brokerName}.</th></tr>
  </c:otherwise>
</c:choose>

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right">Unique Name: </div></td>
    <td><input name="name" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>A name that is different than the name for any other JMS connectors in the server</td>
  </tr>
  <script language="JavaScript">
    <portlet:namespace/>requiredFields = new Array("name").concat(<portlet:namespace/>requiredFields);
  </script>
</c:if>
<!-- Host Field -->
  <tr>
    <td><div align="right">Host: </div></td>
    <td>
      <input name="host" type="text" size="30" value="${host}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The host name or IP to bind to.  The normal values are <tt>0.0.0.0</tt> (all interfaces) or <tt>localhost</tt> (local connections only)</td>
  </tr>
<!-- Port Field -->
  <tr>
    <td><div align="right">Port: </div></td>
    <td>
      <input name="port" type="text" size="5" value="${port}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The network port to bind to.</td>
  </tr>
<!-- Form buttons -->
  <tr>
    <td><div align="right"></div></td>
    <td>
      <input name="submit" type="submit" value="Save" onClick="return <portlet:namespace/>validateForm();">
      <input name="reset" type="reset" value="Reset">
    </td>
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List JMS connectors</a>
