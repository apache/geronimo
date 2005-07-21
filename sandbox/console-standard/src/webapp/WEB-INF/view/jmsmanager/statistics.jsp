<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<br>
<a href="<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDestinations"/></portlet:renderURL>">Back To Destination List </a>
<br><br>
 <table width="100%">
        <tr>
           <th colspan="2"> Statisctic for <c:out value="${statistics.destinationName}"/></th>
        </tr>
        <tr>
            <td width="250">Description</td>
            <td width="200"><c:out value="${statistics.description}"/></td>
        </tr>
        <tr>
            <td width="250">Current Depth</td>
            <td width="200"><c:out value="${statistics.currentDepth}"/></td>
        </tr>
        <tr>
            <td width="250">Open Output Count</td>
            <td width="200"><c:out value="${statistics.openOutputCount}"/></td>
        </tr>
        <tr>
            <td width="250">Open Input Count</td>
            <td width="200"><c:out value="${statistics.openInputCount}"/></td>
        </tr>
        <tr>
            <td width="250">Inhibit Get</td>
            <td width="200"><c:out value="${statistics.inhibitGet}"/></td>
        </tr>
        <tr>
            <td width="250">Inhibit Put</td>
            <td width="200"><c:out value="${statistics.inhibitPut}"/></td>
        </tr>
        <tr>
            <td width="250">Sharable</td>
            <td width="200"><c:out value="${statistics.sharable}"/></td>
        </tr>
        <tr>
            <td width="250">Maximum Depth</td>
            <td width="200"><c:out value="${statistics.maximumDepth}"/></td>
        </tr>
        <tr>
            <td width="250">Trigger Control</td>
            <td width="200"><c:out value="${statistics.triggerControl}"/></td>
        </tr>
        <tr>
            <td width="250">Maximum Message Length</td>
            <td width="200"><c:out value="${statistics.maximumMessageLength}"/></td>
        </tr>
</table>