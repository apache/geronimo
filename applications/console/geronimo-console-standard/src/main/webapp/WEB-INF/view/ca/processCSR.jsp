<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>ProcessCSRForm";
var <portlet:namespace/>requiredFields = new Array("pkcs10certreq");

function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields))
        return false;
    return true;
}
</script>

<b>Issue New Certificate</b> - Step 1: Process Certificate Signing Request (CSR)

<p>This screen lets you process Certificate Signing Request (CSR) text and view the details
of the requestor.  Paste the content of CSR text file you received from the requestor and click on
<i>Process CSR</i> button.  The next screen will show the details of the requestor and allow you
to input information required to issue a certificate.</p>

<jsp:include page="_header.jsp" />

<form name="<portlet:namespace/>ProcessCSRForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="processCSR-after" />
    <table border="0">
        <tr>
            <th colspan="2" align="left">CSR Text</th>
        </tr>
        <tr>
            <td colspan="2">
                <textarea rows="20" cols="80" name="pkcs10certreq">
                ...paste pkcs10 encoded certificate request here...
                </textarea>
            </td>
        </tr>
    </table>
    <input type="submit" value="Process CSR" onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value="Reset">
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before"/>
            </portlet:actionURL>">Cancel</a></p>
