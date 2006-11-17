<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<b>Confirm Certificate Request</b>

<p> This screen shows the details of the Certficate Signing Request (CSR) and allows you to approve the request.
Once the request is approved, it will be considered for issue of a certificate.</p>

<jsp:include page="_header.jsp" />

<form name="<portlet:namespace/>confirmCertReqForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="confirmCertReq-after"/>
    <input type="hidden" name="requestId" value="${requestId}"/>
    <table border="0">
        <tr>
            <th class="DarkBackground" colspan="2" align="left">Certificate Requestor Details</th>
        </tr>
        <tr>
            <th class="LightBackground" align="right">Subject:</th>
            <td class="LightBackground">
                ${subject}
            </td>
        </tr>
        <tr>
            <th class="MediumBackground" align="right">Public Key:</th>
            <td class="MediumBackground">
                <pre>${publickey}</pre>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
    </table>
    <input type="submit" name="approve" value="Approve CSR"/>
    <input type="submit" name="reject" value="Reject CSR">
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>">Cancel</a></p>
