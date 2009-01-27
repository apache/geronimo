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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>

<script language="JavaScript">
var numbers = new Array(0,1,2,3,4,5,6,7,8,9);
var max = ${lineCount};
function <portlet:namespace/>binarySearch(criteria, arr, left, right){
    var pos = parseInt((left+right)/2);
    if (criteria == arr[pos]) return pos;
    else if (left >= right) return -1;
    else if (criteria > arr[pos]) return <portlet:namespace/>binarySearch(criteria, arr, pos+1, right);
    else return <portlet:namespace/>binarySearch(criteria, arr, left, pos-1);
}

function <portlet:namespace/>search(criteria, arr){
    return <portlet:namespace/>binarySearch(criteria, arr, 0, arr.length)
}

function <portlet:namespace/>isNumeric(candidate){
    for (i = 0; i < candidate.length; i++) {
        if (<portlet:namespace/>search(candidate.charAt(i),numbers) < 0) {
            return false;
        }
    }
    return true;
}

function <portlet:namespace/>validateForm(){
    var startPos = document.<portlet:namespace/>searchForm.startPos.value;
    var endPos = document.<portlet:namespace/>searchForm.endPos.value;
    var maxRows = document.<portlet:namespace/>searchForm.maxRows.value;
    if (!<portlet:namespace/>isNumeric(startPos)) {
        alert("Start Position must be a number.");
        document.<portlet:namespace/>searchForm.startPos.focus();
        return false;
    }
    if (!<portlet:namespace/>isNumeric(endPos)) {
        alert("End Position must be a number.");
        document.<portlet:namespace/>searchForm.endPos.focus();
        return false;
    }
    if (!<portlet:namespace/>isNumeric(maxRows)) {
        alert("Maximum results must be a number.");
        document.<portlet:namespace/>searchForm.maxRows.focus();
        return false;
    }
    return true;
}
</script>

<table>
    <tr>
        <td><button onclick="location='<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>'"><fmt:message key="consolebase.common.refresh"/></button>
            <br/>
            <br/>
        </td>
    </tr>
    <tr>
        <td class="Smaller" valign="middle">
            <form action="<portlet:actionURL/>" name="<portlet:namespace/>searchForm" onsubmit="return <portlet:namespace/>validateForm();" method="POST">
                <b><fmt:message key="logmanager.common.filterCriteria"/>:</b>
                <input type="hidden" value="search" name="action"/>
                <br/>
                <fmt:message key="consolebase.common.file"/>&nbsp;
                <select name="logFile">
                    <c:forEach var="file" items="${logFiles}">
                        <option value="${file.fullName}" < c:if test="${logFile eq file.fullName}">selected</c:if>>${file.name}</option>
                    </c:forEach>
                </select>
                <fmt:message key="logmanager.search.lines"/>&nbsp;<input type="text" name="startPos" value="${startPos}" size="3"/>
                <fmt:message key="logmanager.search.to"/>&nbsp;<input type="text" name="endPos" value="${endPos}" size="3"/>
                <fmt:message key="logmanager.search.maxResults"/>&nbsp;<input type="text" name="maxRows" value="${maxRows}" size="3"/>
                <fmt:message key="logmanager.search.level"/>&nbsp;
                <select name="logLevel">
                    <option <c:if test="${logLevel == 'TRACE' || logLevel == ''}">selected</c:if>>TRACE</option>
                    <option <c:if test="${logLevel == 'DEBUG'}">selected</c:if>>DEBUG</option>
                    <option <c:if test="${logLevel == 'INFO'}">selected</c:if>>INFO</option>
                    <option <c:if test="${logLevel == 'WARN'}">selected</c:if>>WARN</option>
                    <option <c:if test="${logLevel == 'ERROR'}">selected</c:if>>ERROR</option>
                    <option <c:if test="${logLevel == 'FATAL'}">selected</c:if>>FATAL</option>
                </select>
                <fmt:message key="logmanager.search.containingText"/>&nbsp;<input type="text" name="searchString" value="${searchString}"/>
                <fmt:message key="logmanager.search.withExceptions"/>&nbsp;<input type="checkbox" name="stackTraces" < c:if test="${!empty stackTraces}">CHECKED </c:if>/>
                <br/>
                <input type="submit" value="<fmt:message key="logmanager.search.fileterLog"/>"/>
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
                        <b><fmt:message key="logmanager.search.messagesMatched">
                              <fmt:param value="${lineCount}"/>
                              <fmt:param value="${fn:length(searchResults)}"/>
                           </fmt:message>
                           <c:if test="${!empty capped}">&nbsp;(<fmt:message key="logmanager.search.numberOfResultsCapped"/>)</c:if>.
                        </b>
                    </td>
                </tr>

                <c:forEach var="line" items="${searchResults}">
                    <tr>
                        <td class="Smaller">${line.lineNumber}:&nbsp;
                            <c:out escapeXml="true" value="${line.lineContent}" />
                        </td>
                    </tr>
                </c:forEach>
            </table>
            </c:when>
            <c:otherwise>
                <fmt:message key="logmanager.search.noLogs"/> 
            </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
