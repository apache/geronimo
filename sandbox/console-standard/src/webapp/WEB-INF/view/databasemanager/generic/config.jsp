<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<form name="datasource_form" action="<portlet:actionURL portletMode="view"/>">
<input type="hidden" name="name" value="add">
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
            <td><input type="text" name="txt${entry.key}" value="${entry.value}" size="75"></td>
        </tr>
    </c:forEach>
    		<tr>
    			<td colspan="2">
    				<table width="100%">
    					<tr>
    						<td width="10%">&nbsp</td>
    						<td>
    							<input type="submit" name="btnSave" value="Save">
									<input type="reset" name="btnReset" value="Reset">
								</td>
							</tr>
						</table>
					</td>
				</tr>    			
</table>
</form>
