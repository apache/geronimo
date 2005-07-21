<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<c:set var="increment" value="10"/>
<table>
<tr>
<td>
<a href="<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>">Refresh</a> 
</td>     
</tr>
<tr>
    <td>     
<c:choose>
<c:when test="${logs != null && fn:length(logs) > 0}">
    <table>
        <tr>
            <td class="Smaller">
            <b>${lines} total line(s) in log file.</b>
            </td>
        </tr>    
            
    <c:forEach var="line" items="${logs}">
        <tr>
            <td class="Smaller">
            ${line}
            </td>
        </tr>
    </c:forEach>
    </table>
</c:when>
<c:otherwise>
 No logs found.
</c:otherwise>
</c:choose>  
</td>     
</tr>
</table>
