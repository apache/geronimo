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
<portlet:defineObjects/>
<table width="50%" cellspacing="5">
    ${message}
    <tr>
        <td><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="new"/></portlet:actionURL>">Create New Group</a></td>
        <td></td>
    </tr>
    <tr class="DarkBackground">
        <th>Group Name</th>
        <th>Actions</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="group" items="${groupsInfo}">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <tr>
            <td class="${backgroundClass}"> ${group.key} </td>
            <td class="${backgroundClass}">
            <a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="edit"/><portlet:param name="group" value="${group.key}"/></portlet:actionURL>">Edit</a>
            &nbsp;
            <a href="<portlet:actionURL><portlet:param name="group" value="${group.key}"/><portlet:param name="action" value="delete"/></portlet:actionURL>" onclick="return confirm('Confirm Delete group ${group.key}?');">Delete</a>
            </td>
        </tr>
        </form>
    </c:forEach>

</table>
