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
<fmt:setBundle basename="systemdatabase"/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>searchForm";
var <portlet:namespace/>integerFields = new Array("startPos", "endPos", "maxRows");
function <portlet:namespace/>validateForm(){
    for (i in <portlet:namespace/>integerFields) {
        if("" != document.forms[<portlet:namespace/>formName].elements[<portlet:namespace/>integerFields[i]].value && !checkIntegral(<portlet:namespace/>formName,<portlet:namespace/>integerFields[i])) {
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="derbylogmanager.view.integer"/>');
            return false;    
        }
    }
    return true;
}
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<table>
    <tr>
        <td><button onclick="location='<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>'"><fmt:message key="derbylogmanager.view.refresh"/></button>
            <br/>
            <br/>
        </td>
    </tr>
    <tr>
        <td class="Smaller" valign="middle">
            <form action="<portlet:actionURL/>" name="<portlet:namespace/>searchForm" method="POST" onsubmit="return <portlet:namespace/>validateForm();">
                <b><fmt:message key="derbylogmanager.view.filterResults"/>:</b>
                <input type="hidden" value="search" name="action"/>
                <br/>
                <label for="<portlet:namespace/>startPos"><fmt:message key="derbylogmanager.view.Lines"/> </label><input type="text" name="startPos" id="<portlet:namespace/>startPos" value="${startPos}" size="3"/>
                <label for="<portlet:namespace/>endPos"><fmt:message key="derbylogmanager.view.to"/> </label><input type="text" name="endPos" id="<portlet:namespace/>endPos" value="${endPos}" size="3"/>
                <label for="<portlet:namespace/>maxRows"><fmt:message key="derbylogmanager.view.maxResults"/> </label><input type="text" name="maxRows" id="<portlet:namespace/>maxRows" value="${maxRows}" size="3"/>
                <label for="<portlet:namespace/>searchString"><fmt:message key="derbylogmanager.view.containingText"/> </label><input type="text" name="searchString" id="<portlet:namespace/>searchString" value="${searchString}"/>
                <br/>
                <input type="submit" value="<fmt:message key="derbylogmanager.view.filterLog"/>"/>
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
                                <b>
                                    <fmt:message key="derbylogmanager.view.matchCriterial" >
                                        <fmt:param   value="${lineCount}" />
                                        <fmt:param   value="${fn:length(searchResults)}" />
                                    </fmt:message>
                                    <c:if test="${!empty capped}">&nbsp;(<fmt:message key="derbylogmanager.view.numberOfResultsCapped"/>)</c:if>.
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
                    <fmt:message key="derbylogmanager.view.noEntries"/>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>
