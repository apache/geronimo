<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%!
java.util.Calendar cal = java.util.Calendar.getInstance();
int startYear = 1990;
int currentYear = cal.get(java.util.Calendar.YEAR);
%>
<script language="Javascript">

function <portlet:namespace/>loadDates(monthElemName,dateElemName,yearElemName){
    var yearElem = eval("document.<portlet:namespace/>searchForm." + yearElemName);
    var monthElem = eval("document.<portlet:namespace/>searchForm." + monthElemName);
    var dateElem = eval("document.<portlet:namespace/>searchForm." + dateElemName);
    var monthIndex = parseInt(monthElem.options[monthElem.selectedIndex].value);
    var selectedYear = parseInt(yearElem.options[yearElem.selectedIndex].value);
    var totalDays = 0;
    // February
    if(monthIndex == 1){
        totalDays = (isLeapYear(selectedYear)? 29 : 28) 
    }else if(monthIndex == 0 || monthIndex == 2 || monthIndex == 4 || monthIndex == 6 || monthIndex == 7 ||
            monthIndex == 9 || monthIndex == 11){
        totalDays = 31;
    }else{
        totalDays = 30;
    }
    dateElem.options.length = 0;
    for(i = 0; i < totalDays; i++){
        dateElem.options.length += 1; 
        dateElem.options[i].text = dateElem.options[i].value = i+1;        
    }            
}

function isLeapYear(year) {
    return (year%4 == 0 && (year % 100 != 0 || year % 400 == 0));
}
function <portlet:namespace/>refresh(){
    document.<portlet:namespace/>searchForm.action="<portlet:renderURL><portlet:param name="action" value="refresh"/></portlet:renderURL>";
    document.<portlet:namespace/>searchForm.submit();
}
</script>
<table>
<tr>
<td>
<a href="javascript:<portlet:namespace/>refresh()">Refresh</a>
</td>     
</tr>
<tr>
    <td>
    <form action="<portlet:renderURL/>" name="<portlet:namespace/>searchForm" method="post">
    <b>Filter results:</b>
    <table width="680">
        <tr>
            <td colspan="4" class="DarkBackground"><b>Date:</b></td>
        </tr>
        <tr>
            <td>From:</td> 
            <td>
                <select name="startMonth" onchange="<portlet:namespace/>loadDates('startMonth','startDate','startYear');">
                    <option value="0">January</option><option value="1">February</option>
                    <option value="2">March</option><option value="3">April</option>
                    <option value="4">May</option><option value="5">June</option>
                    <option value="6">July</option><option value="7">August</option>
                    <option value="8">September</option><option value="9">October</option>
                    <option value="10">November</option><option value="11">December</option>
                </select>
                /
                <select name="startDate">
                </select>
                /
                <select name="startYear">
                <%
                for(int i = startYear;i <= currentYear; i++){
                %>
                <option value="<%=i%>"><%=i%></option>
                <%}%>
                </select>
            </td>
            <td>To:</td>
            <td>
                <select name="endMonth" onchange="<portlet:namespace/>loadDates('endMonth','endDate','endYear');">
                    <option value="0">January</option><option value="1">February</option>
                    <option value="2">March</option><option value="3">April</option>
                    <option value="4">May</option><option value="5">June</option>
                    <option value="6">July</option><option value="7">August</option>
                    <option value="8">September</option><option value="9">October</option>
                    <option value="10">November</option><option value="11">December</option>
                </select>
                /
                <select name="endDate">
                </select>
                /
                <select name="endYear">
                    <%
                    for(int i = startYear;i <= currentYear; i++){
                    %>
                    <option value="<%=i%>"><%=i%></option>
                    <%}%>
                </select>
            </td>
        </tr>
        <tr>
            <td>Ignore Dates:</td>
            <td>
            <input type="checkbox" name="ignoreDates" <c:if test="${ignoreDate}"> checked</c:if>/>
            </td>
        </tr>    
        <tr>
            <td colspan="4" class="DarkBackground"><b>Identity:</b></td>
        </tr>
        <tr>
            <td>Remote Address:</td>
            <td><input type="text" name="requestHost" value="${requestHost}"/></td>
            <td>Authenticated User:</td>
            <td><input type="text" name="authUser" value="${authUser}"/></td>
        </tr>
        <tr>
            <td colspan="4" class="DarkBackground"><b>Request:</b></td>
        </tr>
        <tr>
            <td>Request Method:</td>
            <td>
                <select name="requestMethod">
                    <option value="" <c:if test="${empty requestMethod or requestMethod eq ''}"> selected</c:if>>ANY</option>
                    <option <c:if test="${requestMethod == 'GET'}"> selected</c:if>>GET</option>
                    <option <c:if test="${requestMethod == 'POST'}"> selected</c:if>>POST</option>
                </select>
            </td>
            <td>Requested URI:</td>
            <td><input type="text" name="requestedURI" value="${requestedURI}"/></td>
            </td>
        <tr>
            <td colspan="4" align="center">
                <input type="submit" value="Go"/>
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
            <td><b>Found ${fn:length(logs)} matches in logfile.</b></td>
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
<script language="Javascript">
var <portlet:namespace/>form = document.<portlet:namespace/>searchForm;
<c:if test="${!empty fromDate}">
<portlet:namespace/>form.startMonth.selectedIndex = ${fromDate.month};
</c:if>
<c:if test="${!empty toDate}">
<portlet:namespace/>form.endMonth.selectedIndex = ${toDate.month};
</c:if>
<portlet:namespace/>loadDates('startMonth','startDate','startYear');
<portlet:namespace/>loadDates('endMonth','endDate','endYear');
<c:if test="${!empty fromDate}">
<portlet:namespace/>form.startDate.selectedIndex = ${fromDate.date}-1;
with(<portlet:namespace/>form){
    for(var i = 0; i < startYear.options.length; i++){
        if(startYear.options[i].value == ${fromDate.year} + 1900){
            startYear.selectedIndex = i;
            break;
        }
    }
}
</c:if>
<c:if test="${!empty toDate}">
<portlet:namespace/>form.endDate.selectedIndex = ${toDate.date}-1;
with(<portlet:namespace/>form){
    for(var i = 0; i < endYear.options.length; i++){
        if(endYear.options[i].value == ${toDate.year} + 1900){
            endYear.selectedIndex = i;
            break;
        }
    }
}
</c:if>
</script>