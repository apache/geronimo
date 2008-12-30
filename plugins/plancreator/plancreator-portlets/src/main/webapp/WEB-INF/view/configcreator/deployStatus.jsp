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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="plancreator-portlet"/>
<portlet:defineObjects />

<script>
// toggle the display state of an element
function <portlet:namespace/>toggleDisplay(id) {
  var element = document.getElementById("<portlet:namespace/>"+id);
  if (element.style.display == 'inline') {
      element.style.display='none';
  } else {
      element.style.display='inline';
  }
}
</script>

<!-- Abbreviated status message -->
<c:if test="${!(empty abbrStatusMessage)}">
    <div id="<portlet:namespace/>abbrStatusMessage" style="display:inline">
     ${abbrStatusMessage}<br/>
    <c:if test="${!(empty fullStatusMessage)}">
    <button onclick="<portlet:namespace/>toggleDisplay('fullStatusMessage');<portlet:namespace/>toggleDisplay('abbrStatusMessage');return false;"><fmt:message key="plancreator.status.detail"/></button>
    </c:if>
    </div>
</c:if>
<!-- Full status message -->
<c:if test="${!(empty fullStatusMessage)}">
    <div id="<portlet:namespace/>fullStatusMessage" style="display:none">
    <pre>
<c:out escapeXml="true" value="${fullStatusMessage}"/>
    </pre>
    </div>
</c:if>

<c:if test="${empty fullStatusMessage}">
<p><a href="/${data.webApp.contextRoot}"><fmt:message key="plancreator.status.launch"/></a></p>
</c:if>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="plancreator.common.finish"/></a></p>
