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

<p>Thread pools statistics for ${poolName}:</p>

<table>
  <tr>
    <th align="right">Pool Max:</th>
    <td>${stats.threadsInUse.upperBound}</td>
  </tr>
  <tr>
    <th align="right">Lowest Recorded:</th>
    <td>${stats.threadsInUse.lowWaterMark}</td>
  </tr>
  <tr>
    <th align="right">Highest Recorded:</th>
    <td>${stats.threadsInUse.highWaterMark}</td>
  </tr>
  <tr>
    <th align="right">Threads in Use:</th>
    <td>${stats.threadsInUse.current}</td>
  </tr>
</table>

<c:if test="${! empty consumers}">
<p>Current consumers of threads in this pool:</p>

<table>
  <tr>
    <th>Description</th>
    <th># of Threads</th>
  </tr>
<c:forEach var="client" items="${consumers}">
  <tr>
    <td>${client.name}</td>
    <td>${client.threadCount}</td>
  </tr>
</c:forEach>
</table>
</c:if>
