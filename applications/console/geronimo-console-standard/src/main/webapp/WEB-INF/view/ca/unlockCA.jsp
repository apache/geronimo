<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>UnlockCAForm";
var <portlet:namespace/>requiredFields = new Array("password");

function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields))
        return false;
    return true;
}
</script>
<b>Unlock Certification Authority</b>

<p> This screen lets you unlock the CA by providing the password used to protect
the CA's private key.  Once unlocked, the CA functions will be available.</p>

<jsp:include page="_header.jsp" />

<form name="<portlet:namespace/>UnlockCAForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="unlockCA-after" />
    <b>Enter the CA's private key password:</b>
    <input type="password" name="password" size="20" maxlength="200" />
    <br />

    <input type="submit" value="Unlock Certification Authority" onClick="return <portlet:namespace/>validateForm();"/>
    <input type="reset" name="reset" value="Reset">
</form>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
