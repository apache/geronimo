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
    <form action="<portlet:renderURL/>" name="<portlet:namespace/>searchForm" >
    <b>Filter results:</b>
    <input type="hidden" value="search" name="action"/>
    Lines <input type="text" name="startPos" value="${startPos}" size="3"/>
    to <input type="text" name="endPos" value="${endPos}" size="3"/>
    Max Results <input type="text" name="maxRows" value="${maxRows}" size="3"/>
    Containing text <input type="text" name="searchString" value="${searchString}"/>
    <input type="submit" value="Go"/>
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
            ${line.lineNumber}:&nbsp;${line.lineContent}
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
