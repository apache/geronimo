<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="keystore" value="${id}" />
    <input type="hidden" name="mode" value="unlockEdit-after" />
    <b>Enter keystore password:</b>
    <input type="password" name="password" size="20" maxlength="200" />
    <br />
    <input type="submit" value="Unlock Keystore" />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>">Cancel</a></p>
