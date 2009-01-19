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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<table>
<tr>
<td>
<a href="<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>">Refresh</a>
</td>
</tr>
<tr>
    <td class="Smaller" valign="middle">
    <form action="<portlet:renderURL/>" name="<portlet:namespace/>searchForm" method="POST">
    <b>Filter results:</b>
    <input type="hidden" value="search" name="action"/>
    Lines <input type="text" name="startPos" value="${startPos}" size="3"/>
    to <input type="text" name="endPos" value="${endPos}" size="3"/>
    Max Results <input type="text" name="maxRows" value="${maxRows}" size="3"/>
    Containing text <input type="text" name="searchString" value="${searchString}"/>
    <br/><input type="submit" value="Go"/>
    </form>
    </td>
</tr>
<tr>
    <td>
<c:choose>
<c:when test="${searchResults != null && fn:length(searchResults) > 0}">
    <table>
        <tr>
            <td class="Smaller">
            <b>${lineCount} total message(s) in log file. ${fn:length(searchResults)} matched your criteria<c:if test="${!empty capped}"> (number of results capped)</c:if>.</b>
            </td>
        </tr>

    <c:forEach var="line" items="${searchResults}">
        <tr>
            <td class="Smaller">
            ${line.lineNumber}:&nbsp;<c:out escapeXml="true" value="${line.lineContent}" />
            </td>
        </tr>
    </c:forEach>
    </table>
</c:when>
<c:otherwise>
 No entries found with the specified criteria.
</c:otherwise>
</c:choose>
</td>
</tr>
</table>
