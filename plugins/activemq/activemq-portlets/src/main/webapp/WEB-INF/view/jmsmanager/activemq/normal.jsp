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
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>
<table width="100%">
    <tr class="DarkBackground">
        <th class="LightBackground">&nbsp;</th>
        <th align="left"><fmt:message key="jmsmanager.common.name"/></th>
        <th align="left"><fmt:message key="jmsmanager.common.state"/></th>
        <th align="left"><fmt:message key="jmsmanager.activemq.common.testResult" /></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="info" items="${cFactories}"><tr>
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
        <td class="${backgroundClass}"><a href='<portlet:renderURL>
            <portlet:param name="name" value="${info.objectName}"/>
            <portlet:param name="mode" value="detail"/>
            </portlet:renderURL>'><fmt:message key="jmsmanager.common.detail"/></a>
        </td>
        <td class="${backgroundClass}">${info.name}</td>
        <td class="${backgroundClass}"><c:choose>
            <c:when test='${info.state == 0}'>Starting</c:when>
            <c:when test='${info.state == 1}'>Running</c:when>
            <c:when test='${info.state == 2}'>Stopping</c:when>
            <c:when test='${info.state == 3}'>Stopped</c:when>
            <c:when test='${info.state == 4}'>Failed</c:when>
        </c:choose></td>
        <td class="${backgroundClass}">
                <c:if test="${!info.working}">
                    <a href='<portlet:renderURL>
                            <portlet:param name="name" value="${info.objectName}"/>
                            <portlet:param name="mode" value="list"/>
                            <portlet:param name="check" value="true"/></portlet:renderURL>'>
                            test connection
                    </a>
                </c:if>
                <c:if test="${info.working}">
                    ${info.message}
                </c:if>
        </td>
    </tr></c:forEach>
</table>
<br>
<a href="<portlet:actionURL portletMode="view">
    <portlet:param name="mode" value="addACF" />
    </portlet:actionURL>"><fmt:message key="jmsmanager.activemq.common.addNewJMSConnFactory" /></a>
