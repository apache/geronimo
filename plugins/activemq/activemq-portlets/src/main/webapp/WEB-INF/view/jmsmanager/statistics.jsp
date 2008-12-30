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
<a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDestinations"/></portlet:renderURL>"><fmt:message key="jmsmanager.common.backToDest"/> </a>
<br><br>
 <table width="100%">
        <tr>
           <th colspan="2"> <fmt:message key="jmsmanager.common.statiscticFor" >
           <fmt:param value="${statistics.destinationName}"/>
           </fmt:message>
           </th>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.description"/></td>
            <td width="200"><c:out value="${statistics.description}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.currentDepth" /></td>
            <td width="200"><c:out value="${statistics.currentDepth}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.openOutputCount" /></td>
            <td width="200"><c:out value="${statistics.openOutputCount}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.openInputCount" /></td>
            <td width="200"><c:out value="${statistics.openInputCount}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.inhibitGet" /></td>
            <td width="200"><c:out value="${statistics.inhibitGet}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.inhibitPut" /></td>
            <td width="200"><c:out value="${statistics.inhibitPut}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.sharable" /></td>
            <td width="200"><c:out value="${statistics.sharable}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.maximumDepth" /></td>
            <td width="200"><c:out value="${statistics.maximumDepth}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.triggerControl" /></td>
            <td width="200"><c:out value="${statistics.triggerControl}"/></td>
        </tr>
        <tr>
            <td width="250"><fmt:message key="jmsmanager.common.maximumMessageLength" /></td>
            <td width="200"><c:out value="${statistics.maximumMessageLength}"/></td>
        </tr>
</table>
