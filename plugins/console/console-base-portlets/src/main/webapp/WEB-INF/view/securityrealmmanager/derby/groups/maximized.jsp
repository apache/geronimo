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
<fmt:setBundle basename="securityrealmmanager"/>
<fmt:setBundle basename="standard_common" var="standard_common"/>
<portlet:defineObjects/>
<p><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>"><fmt:message key="securityrealmmanager.derby.groups.maximized.createNewGroup" /></a></p>
<table width="100%" class="TableLine" summary="Console Realm Groups">
    ${message}
        <tr class="DarkBackground">
            <th scope="col" width="100"><fmt:message key="consolebase.common.groupName"/></th>
            <th scope="col" width="150"><fmt:message key="consolebase.common.description"/></th>
            <th scope="col"></th>
        </tr>
    <c:forEach var="group" items="${groups}">
        <tr>
            <td width="100"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="group" value="${group.key}"/></portlet:actionURL>">${group.key}</a></td>
            <td width="150">${group.value}</td>
            <td><a href="<portlet:actionURL><portlet:param name="group" value="${group.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete?');"><fmt:message key="consolebase.common.delete"/></a></td>
        </tr>
    </c:forEach>
</table>
