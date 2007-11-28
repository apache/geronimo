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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<script language="Javascript">
var <portlet:namespace/>formName = "<portlet:namespace/>searchForm";
var <portlet:namespace/>dateFields = new Array("fromDate", "toDate");

function <portlet:namespace/>validateForm(){
    if (document.forms[<portlet:namespace/>formName].ignoreDates.checked)
        return true;
    for (i in <portlet:namespace/>dateFields) {
        if (!checkDateMMDDYYYY(<portlet:namespace/>formName, <portlet:namespace/>dateFields[i]))
            return false;
    }
    // Check if to date is after from date
    var fromDate = new Date(document.forms[<portlet:namespace/>formName].fromDate.value);
    var toDate = new Date(document.forms[<portlet:namespace/>formName].toDate.value);
    if (fromDate > toDate) {
        alert('to date must be after from date.');
        return false;
    }
    return true;
}
</script>
<table>
    <tr>
        <td><button onclick="location='<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>'">Refresh</button>
            <br/>
            <br/>
        </td>
    </tr>
    <tr>
        <td>
            <form action="<portlet:actionURL/>" name="<portlet:namespace/>searchForm" method="post" onSubmit="return <portlet:namespace/>validateForm();">
                <b>Filter Criteria:</b>
                <input type="hidden" value="search" name="action"/>
                <table width="680">
                    <c:choose>
                        <c:when test="${fn:length(webContainers) > 1}">
                            <tr>
                                <td colspan="4" class="DarkBackground"><b>Container:</b></td>
                            </tr>
                            <tr>
                                <td>Search Web Container:</td>
                                <td>
                                    <select name="selectedContainer">
                                        <c:forEach var="webContainer" items="${webContainers}">
                                            <option value="${webContainer.value}"<c:if test="${webContainer.value eq selectedContainer}">selected</c:if>>${webContainer.key}</option>
                                        </c:forEach>
                                    </select>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="webContainer" items="${webContainers}">
                                <tr>
                                    <td>
                                        <input type="hidden" name="selectedContainer" value="${webContainer.value}" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    <%-- todo: When the user changes the selected container, we need to change the log selection options!!!
                         need some AJAX here.  :)  --%>
                    <c:choose>
                        <c:when test="${fn:length(webLogs) > 1}">
                            <tr>
                                <td colspan="4" class="DarkBackground"><b>Log:</b></td>
                            </tr>
                            <tr>
                                <td>Search Web Log:</td>
                                <td>
                                    <select name="selectedLog">
                                        <c:forEach var="webLog" items="${webLogs}">
                                            <option<c:if test="${webLog eq selectedLog}">selected</c:if>>${webLog}</option>
                                        </c:forEach>
                                    </select>
                                </td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="webLog" items="${webLogs}">
                                <tr>
                                    <td>
                                        <input type="hidden" name="selectedLog" value="${webLog}" />
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    <tr>
                        <td colspan="4" class="DarkBackground"><b>Date:</b></td>
                    </tr>
                    <tr>
                        <td>From (MM/DD/YYYY):</td>
                        <td>
                            <input type="text" name="fromDate" value="${fromDate}">
                        </td>
                        <td>To (MM/DD/YYYY):</td>
                        <td>
                            <input type="text" name="toDate" value="${toDate}">
                        </td>
                    </tr>
                    <tr>
                        <td>Ignore Dates:</td>
                        <td>
                            <input type="checkbox" name="ignoreDates" < c:if test="${ignoreDates}">checked</c:if>/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="4" class="DarkBackground"><b>Identity:</b></td>
                    </tr>
                    <tr>
                        <td>Remote Address:</td>
                        <td>
                            <input type="text" name="requestHost" value="${requestHost}"/>
                        </td>
                        <td>Authenticated User:</td>
                        <td>
                            <input type="text" name="authUser" value="${authUser}"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="4" class="DarkBackground"><b>Request:</b></td>
                    </tr>
                    <tr>
                        <td>Request Method:</td>
                        <td>
                            <select name="requestMethod">
                                <option value="" < c:if test="${empty requestMethod or requestMethod eq ''}">selected</c:if>>ANY</option>
                                <option <c:if test="${requestMethod == 'GET'}">selected</c:if>>GET</option>
                                <option <c:if test="${requestMethod == 'POST'}">selected</c:if>>POST</option>
                                <option <c:if test="${requestMethod == 'PUT'}">selected</c:if>>PUT</option>
                                <option <c:if test="${requestMethod == 'DELETE'}">selected</c:if>>DELETE</option>
                            </select>
                        </td>
                        <td>Requested URI:</td>
                        <td>
                            <input type="text" name="requestedURI" value="${requestedURI}"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="4" class="DarkBackground"><b>Result Size:</b></td>
                    </tr>
                    <tr>
                        <td>Start Result:</td>
                        <td>
                            <input type="text" name="startResult" value="${startResult}"/>
                        </td>
                        <td>Max Results:</td>
                        <td>
                            <input type="text" name="maxResult" value="${maxResult}"/>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="4" align="left">
                            <input type="submit" value="Filter Log"/>
                        </td>
                    </tr>
                </table>
            </form>
        </td>
    </tr>
    <tr>
        <td>
            <c:choose>
                <c:when test="${logs != null && fn:length(logs) > 0}">
                    <table>
                        <tr>
                            <td><b>Found ${fn:length(logs)} matches in logfile (${logLength} lines searched).</b></td>
                        </tr>
                        <c:forEach var="line" items="${logs}">
                        <tr>
                            <td class="Smaller">${line.lineNumber}&nbsp;
                                <c:out escapeXml="true" value="${line.lineContent}" />
                            </td>
                        </tr>
                        </c:forEach>
                    </table>
                </c:when>
                <c:otherwise>
                    No log entries found.
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
