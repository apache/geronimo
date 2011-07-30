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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>
    
<CommonMsg:commonMsg/>

<p><fmt:message key="jmsmanager.server.normal.title" />:</p>

<table width="50%" class="TableLine" summary="JMS Server Manager - Brokers">
        <tr>
            <th scope="col" class="DarkBackground"><fmt:message key="jmsmanager.common.name"/></th>
            <th scope="col" class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.state"/></th>
            <%--<th class="DarkBackground" align="center"><fmt:message key="jmsmanager.common.actions"/></th>--%>
          </tr>
          <c:forEach var="entry" items="${brokers}" varStatus="status">
          <c:choose>
              <c:when test="${status.count%2==0}">
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr class="${backgroundClass}">
            <td>${entry.brokerName}</td>
            <td>${entry.state.name}</td>
            <%--
            <td>
             <c:choose>
               <c:when test="${entry.state.name eq 'running'}">
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="stop" />
                 <portlet:param name="brokerURI" value="${entry.brokerURI}" />
                 <portlet:param name="brokerName" value="${entry.brokerName}" />   
               </portlet:actionURL>"><fmt:message key="jmsmanager.common.stop"/></a>
               </c:when>
               <c:otherwise>
               <a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="start" />
                 <portlet:param name="brokerURI" value="${entry.brokerURI}" />
                 <portlet:param name="brokerName" value="${entry.brokerName}" />   
               </portlet:actionURL>"><fmt:message key="jmsmanager.common.start"/></a>
               </c:otherwise>
             </c:choose>
               <a href="<portlet:renderURL portletMode="view">
                 <portlet:param name="mode" value="update" />
                 <portlet:param name="brokerURI" value="${entry.brokerURI}" />
                 <portlet:param name="brokerName" value="${entry.brokerName}" />   
               </portlet:renderURL>"><fmt:message key="jmsmanager.common.edit"/></a>
               <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="delete" /><portlet:param name="brokerURI" value="${entry.brokerURI}" /><portlet:param name="brokerName" value="${entry.brokerName}" /></portlet:actionURL>"
                   onClick="return showGlobalConfirmMessage('<fmt:message key="jmsmanager.broker.confirmMsg01"/>${entry.brokerName}?');">
                   <fmt:message key="jmsmanager.common.delete"/>
               </a>
             </td>
             --%>
          </tr>
</c:forEach>
</table>

<%--
<br />
<a href="<portlet:renderURL portletMode="view">
           <portlet:param name="mode" value="create" />
         </portlet:renderURL>"><fmt:message key="jmsmanager.server.normal.addJMSBroker"/></a>
--%>
