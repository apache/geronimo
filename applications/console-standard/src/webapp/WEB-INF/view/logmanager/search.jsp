<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<script language="JavaScript">
var numbers = new Array(0,1,2,3,4,5,6,7,8,9);
var max = ${lineCount};
function <portlet:namespace/>binarySearch(criteria, arr, left, right){
    var pos = parseInt((left+right)/2);
    if(criteria == arr[pos]) return pos;
    else if(left >= right) return -1;
    else if(criteria > arr[pos]) return <portlet:namespace/>binarySearch(criteria, arr, pos+1, right);
    else return <portlet:namespace/>binarySearch(criteria, arr, left, pos-1);
}

function <portlet:namespace/>search(criteria, arr){
    return <portlet:namespace/>binarySearch(criteria, arr, 0, arr.length)
}

function <portlet:namespace/>isNumeric(candidate){
    for(i = 0; i < candidate.length; i++){
        if(<portlet:namespace/>search(candidate.charAt(i),numbers) < 0){
            return false;
        }
    }
    return true;
}

function <portlet:namespace/>validateForm(){
    var startPos = document.<portlet:namespace/>searchForm.startPos.value;
    var endPos = document.<portlet:namespace/>searchForm.endPos.value;
    var maxRows = document.<portlet:namespace/>searchForm.maxRows.value;
    if(!<portlet:namespace/>isNumeric(startPos)){
        alert("Start Position must be a number.");
        document.<portlet:namespace/>searchForm.startPos.focus();
        return false;
    }
    if(!<portlet:namespace/>isNumeric(endPos)){
        alert("End Position must be a number.");
        document.<portlet:namespace/>searchForm.endPos.focus();
        return false;
    }
    if(!<portlet:namespace/>isNumeric(maxRows)){
        alert("Maximum results must be a number.");
        document.<portlet:namespace/>searchForm.maxRows.focus();
        return false;
    }
    return true;
}
</script>

<table>
<tr>
<td>
<a href="<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>">Refresh</a> 
</td>     
</tr>
<tr>
    <td class="Smaller" valign="middle">
    <form action="<portlet:renderURL/>" name="<portlet:namespace/>searchForm" onsubmit="return <portlet:namespace/>validateForm();">
    <b>Filter results:</b>
    <input type="hidden" value="search" name="action"/>
    File <select name="logFile">
        <c:forEach var="file" items="${logFiles}">
            <option value="${file.fullName}" <c:if test="${logFile eq file.fullName}"> selected</c:if>>${file.name}</option>
        </c:forEach>
    </select>
    Lines <input type="text" name="startPos" value="${startPos}" size="3"/>
    to <input type="text" name="endPos" value="${endPos}" size="3"/>
    Max Results <input type="text" name="maxRows" value="${maxRows}" size="3"/>
    Level
    <select name="logLevel">
        <option<c:if test="${logLevel eq 'TRACE' || logLevel eq ''}"> selected</c:if>>TRACE</option>
        <option<c:if test="${logLevel eq 'DEBUG'}"> selected</c:if>>DEBUG</option>
        <option<c:if test="${logLevel eq 'INFO'}"> selected</c:if>>INFO</option>
        <option<c:if test="${logLevel eq 'WARN'}"> selected</c:if>>WARN</option>
        <option<c:if test="${logLevel eq 'ERROR'}"> selected</c:if>>ERROR</option>
        <option<c:if test="${logLevel eq 'FATAL'}"> selected</c:if>>FATAL</option>
    </select>
    Containing text <input type="text" name="searchString" value="${searchString}"/>
    With Exceptions <input type="checkbox" name="stackTraces" <c:if test="${!empty stackTraces}">CHECKED </c:if>/>
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
            ${line.lineNumber}:&nbsp;<c:out escapeXml="true" value="${line.lineContent}" />
            </td>
        </tr>
    </c:forEach>
    </table>
</c:when>
<c:otherwise>
 No logs found with the specified criteria.
</c:otherwise>
</c:choose>  
</td>
</tr>
</table>
