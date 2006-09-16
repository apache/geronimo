<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>KeystoreForm";
var <portlet:namespace/>requiredFields = new Array("certificate", "alias");
function <portlet:namespace/>validateForm(){
    return textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields);
}
</script>

<!-- Uploading certificate using a disk file fails on Windows.  Certificate text is used instead.
<p>This screen lets you select a certificate to import into the keystore.  Select the
certificate file and specify an alias to store it under in the keystore.  The next
step will be to review the certificate before committing it to the keystore.</p>
-->
<p>This screen lets you input a certificate to import into the keystore.  Paste the content of the
certificate file in the text area and specify an alias to store it under in the keystore.  The next
step will let you review the certificate before committing it to the keystore.</p>

<form enctype="multipart/form-data" method="POST" name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="id" value="${id}" />
    <input type="hidden" name="mode" value="uploadCertificate-after" />
    <table border="0">
        <th align="left"> Trusted Certificate </th>
<!-- Uploading certificate using a disk file fails on Windows.  Certificate text is used instead.
        <tr>
            <th align="right">Certificate file:</th>
            <td>
                <input type="file" name="certificate" size="40" />
            </td>
        </tr>
 -->
        <tr>
            <td colspan="2">
                <textarea rows="15" cols="80" name="certificate">...paste trusted certificate text here...</textarea>
            </td>
        </tr>
        <tr>
            <th align="left">Alias for certificate:</th>
            <td>
                <input type="text" name="alias" size="20" maxlength="200" />
            </td>
        </tr>
    </table>
    <input type="submit" value="Review Certificate" onclick="return <portlet:namespace/>validateForm()"/>
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${id}" />
            </portlet:actionURL>">Cancel</a></p>
