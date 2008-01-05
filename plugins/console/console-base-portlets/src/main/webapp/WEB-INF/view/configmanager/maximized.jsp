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
<table width="100%">
    <c:forEach var="configInfo" items="${configurations}">
        <tr>
            <td width="500">${configInfo.state}</td>
            <td>
<c:if test="${configInfo.state.running}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="action" value="stop"/></portlet:actionURL>"><fmt:message key="consolebase.common.stop"/></a></c:if>
<c:if test="${configInfo.state.stopped}"><a href="<portlet:actionURL><portlet:param name="configId" value="${configInfo.configID}"/><portlet:param name="action" value="start"/></portlet:actionURL>"><fmt:message key="consolebase.common.start"/></a></c:if>
</td>
            <td>${configInfo.configID}</td>
        </tr>
    </c:forEach>
</table>
