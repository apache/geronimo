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
var <portlet:namespace/>requiredFields = new Array("rar");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="jmswizard.common.emptyText"/>');
        return false;    
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div>

<p><fmt:message key="jmswizard.provider.title" /></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="provider-after" />
    <table border="0">
    <!-- ENTRY FIELD: JMS Connection RAR -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>rar"><fmt:message key="jmswizard.provider.JMSProviderRAR" /></label>:</div></th>
        <td>
          <select name="rar" id="<portlet:namespace/>rar">
            <option></option>
        <c:forEach var="rar" items="${rars}">
            <option <c:if test="${rar == pool.rar}">selected</c:if> value="${rar}">${rar}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="jmswizard.provider.JMSProviderRARExp" />
        </td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="jmswizard.common.next"/>' onClick="return <portlet:namespace/>validateForm()"/></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>"><fmt:message key="jmswizard.common.cancel"/></a></p>
