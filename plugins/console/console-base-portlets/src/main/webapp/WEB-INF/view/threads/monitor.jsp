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

<p><fmt:message key="threads.monitor.title"><fmt:param value="${poolName}"/></fmt:message></p>

<table>
  <tr>
    <th align="right"><fmt:message key="threads.monitor.poolMax" />:</th>
    <td>${stats.threadsInUse.upperBound}</td>
  </tr>
  <tr>
    <th align="right"><fmt:message key="threads.monitor.lowestRecorded" />:</th>
    <td>${stats.threadsInUse.lowWaterMark}</td>
  </tr>
  <tr>
    <th align="right"><fmt:message key="threads.monitor.highestRecorded" />:</th>
    <td>${stats.threadsInUse.highWaterMark}</td>
  </tr>
  <tr>
    <th align="right"><fmt:message key="threads.monitor.threadsInUse" />:</th>
    <td>${stats.threadsInUse.current}</td>
  </tr>
</table>

<c:if test="${! empty consumers}">
<p><fmt:message key="threads.monitor.currentConsumersOfThreads" />:</p>

<table>
  <tr>
    <th><fmt:message key="threads.monitor.description" /></th>
    <th><fmt:message key="threads.monitor.ofThreads" /></th>
  </tr>
<c:forEach var="client" items="${consumers}">
  <tr>
    <td>${client.name}</td>
    <td>${client.threadCount}</td>
  </tr>
</c:forEach>
</table>
</c:if>
