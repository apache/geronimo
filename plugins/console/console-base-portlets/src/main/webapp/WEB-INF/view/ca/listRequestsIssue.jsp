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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<jsp:include page="_header.jsp" /><br>

<b><fmt:message key="ca.listRequestsIssue.CertRequestsAwaiting"/>
</b>

<p><fmt:message key="ca.listRequestsIssue.CertRequestsAwaitingExp"/>
</p>

<table border="0">
    <tr>
        <th class="DarkBackground" align="left"><fmt:message key="ca.common.certRequests"/></th>
    </tr>
  <c:choose>
    <c:when test="${!empty(csrIds)}">
      <c:set var="backgroundClass" value='MediumBackground'/>
      <c:forEach items="${csrIds}" var="csrId">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr>
          <td class="${backgroundClass}">
            <a href="<portlet:actionURL portletMode="view">
                       <portlet:param name="mode" value="listRequestsIssue-after"/>
                       <portlet:param name="requestId" value="${csrId}"/>
                     </portlet:actionURL>">${csrId}</a>
          </td>
        </tr>
      </c:forEach>
    </c:when>
    <c:otherwise>
        <tr>
          <td><fmt:message key="ca.common.noRequests"/></td>
        </tr>
    </c:otherwise>
  </c:choose>
</table>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="ca.common.backToCAHome"/></a></p>
