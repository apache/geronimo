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

<br />
<br />
<table width="100%">
    <tr class="DarkBackground">
        <th align="left">&nbsp;Component Name</th>
        <c:if test="${showWebInfo}"><th>URL</th></c:if>
        <th>&nbsp;State</th>
        <th align="center" colspan="3">Commands</th>
        <th align="left">Parent Components</th>
        <th align="left">Child Components</th>
    </tr>
  <c:set var="backgroundClass" value='MediumBackground'/>
  <c:forEach var="moduleDetails" items="${configurations}">
        <c:choose>
            <c:when test="${backgroundClass == 'MediumBackground'}" >
                <c:set var="backgroundClass" value='LightBackground'/>
            </c:when>
            <c:otherwise>
                <c:set var="backgroundClass" value='MediumBackground'/>
            </c:otherwise>
        </c:choose>
    <tr>
        <td class="${backgroundClass}">&nbsp;${moduleDetails.configId}</td>
        <c:if test="${showWebInfo}">
            <td class="${backgroundClass}">&nbsp;<c:if test="${moduleDetails.state.running}"><a href="${moduleDetails.urlFor}">${moduleDetails.contextPath}</a></c:if></td>
        </c:if>
        <td width="100" class="${backgroundClass}">&nbsp;${moduleDetails.state}</td>
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="stop"/></portlet:actionURL>">Stop</a></c:if>
            <c:if test="${moduleDetails.state.stopped && (moduleDetails.type.name ne 'CAR')}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="start"/></portlet:actionURL>">Start</a></c:if>
            <c:if test="${moduleDetails.state.failed}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="stop"/></portlet:actionURL>">Stop</a></c:if>
        </td>
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="restart"/></portlet:actionURL>">Restart</a></c:if>
        </td>
        <td width="75" class="${backgroundClass}">
            <a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="uninstall"/></portlet:actionURL>" onClick="return confirm('Are you sure you want to uninstall ${moduleDetails.configId}?');">Uninstall</a>
        </td>
        <td class="${backgroundClass}">
            <c:forEach var="parent" items="${moduleDetails.parents}">
                ${parent} <br>
            </c:forEach>
        </td>
        <td class="${backgroundClass}">
        <c:forEach var="child" items="${moduleDetails.children}">
            ${child} <br>
        </c:forEach>
        </td>
    </tr>
  </c:forEach>
</table>

<p>${messageInstalled} ${messageStatus}</p>
