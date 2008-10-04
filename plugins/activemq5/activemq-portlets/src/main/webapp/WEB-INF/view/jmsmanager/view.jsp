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

<br>
<table>
<!--
<tr>
<td align=LEFT colspan="3"> <a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="createDestination"/></portlet:renderURL>">Add Queue/Topic </a> </td>
</tr>
-->
<c:if test="${!destinationsMsg}">

   <tr>
      <td colspan="3">${destinationsMsg}</td>
   </tr>

</c:if>
<tr class="DarkBackground">
  <th>
     <fmt:message key="jmsmanager.common.messageDestinationName" />
  </th>
  <th>
     <fmt:message key="jmsmanager.common.physicalName" />
  </th>
  <th>
     <fmt:message key="jmsmanager.common.type"/>
  </th>
  <th>
     <fmt:message key="jmsmanager.common.applicationName" />
  </th>
  <th>
     <fmt:message key="jmsmanager.common.moduleName" />
  </th>
  <th>
     <fmt:message key="jmsmanager.common.actions"/>
  </th>
</tr>
  <c:set var="backgroundClass" value='MediumBackground'/>
  <c:forEach var="destination" items="${destinations}">
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
  <tr>
      <td class="${backgroundClass}" align=CENTER>
            <c:out value="${destination.name}"/>
      </td>
      <td class="${backgroundClass}" align=CENTER>
            <c:out value="${destination.physicalName}"/>
      </td>
      <td class="${backgroundClass}" align=CENTER>
            <c:out value="${destination.type}"/>
            &nbsp;
      </td>
      <td class="${backgroundClass}" align=CENTER>
            <c:out value="${destination.applicationName}"/>
      </td>
      <td class="${backgroundClass}" align=CENTER>
            <c:out value="${destination.moduleName}"/>
      </td>
      <td class="${backgroundClass}" align=CENTER>
        <table border="0">
        <tr>
         <td>
        <c:if test="${destination.removable}">
         <a href="<portlet:actionURL portletMode="view"><portlet:param name="processaction" value="removeDestination"/><portlet:param name="destinationConfigURI" value="${destination.configURI}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:actionURL>"><fmt:message key="jmsmanager.common.remove"/> </a>
         </c:if>
         </td>
         <!--a href="<portlet:actionURL portletMode="view"><portlet:param name="processaction" value="statistics"/><portlet:param name="destinationName" value="${destination.name}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:actionURL>">statistics</a-->
         <td>
        <c:if test="${destination.viewable}">
         <a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewMessages"/><portlet:param name="destinationName" value="${destination.name}"/><portlet:param name="destinationApplicationName" value="${destination.applicationName}"/><portlet:param name="destinationModuleName" value="${destination.moduleName}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:renderURL>"><fmt:message key="jmsmanager.common.viewMessages" /></a>
         </c:if>
         </td>
         <td>
        <c:if test="${destination.viewable}">
         <a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDLQ"/><portlet:param name="destinationName" value="${destination.name}"/><portlet:param name="destinationApplicationName" value="${destination.applicationName}"/><portlet:param name="destinationModuleName" value="${destination.moduleName}"/><portlet:param name="destinationType" value="${destination.type}"/></portlet:renderURL>"><fmt:message key="jmsmanager.common.viewDLQ" /></a></td>
         </c:if>
        </tr>        
        </table>

      </td>

  </tr>
  </c:forEach>
 </table>
