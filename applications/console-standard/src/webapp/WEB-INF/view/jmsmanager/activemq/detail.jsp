<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<form name="datasource_form" action="<portlet:actionURL portletMode="view"/>">
<br>
<strong>Connection Name:</strong>&nbsp;${attributeMap.name}
<br><br>
<table width="100%">
        <tr>
            <th>Property</th>
            <th>Value</th>
        </tr>
    <c:forEach var="entry" items="${attributeMap}">
        <tr>
            <td><strong>${entry.key}</strong></td>
            <td>${entry.value}</td>
        </tr>
    </c:forEach>
    		<tr>
    			<td colspan="2">
    				<table width="100%">
    					<tr>
    						<td width="10%">&nbsp</td>
    						<td>
    							<input type="submit" name="btnBack" value="Back to JMS Connection Factories">
								</td>
							</tr>
						</table>
					</td>
				</tr>	
</table>
<input type="hidden" name="name" value="back">
</form>