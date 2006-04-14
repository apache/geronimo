<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>The keystore has been unlocked.  However, a private key within the keystore
also needs to be unlocked in order for SSL to work properly.  Please specify
the password for the private key within the keystore.</p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="keystore" value="${keystore}" />
    <input type="hidden" name="mode" value="unlockKey-after" />
    <b>Unlock Private Key:</b>
    <select name="keyAlias">
        <c:forEach var="alias" items="${keys}">
            <option>${alias}</option>
        </c:forEach>
    </select>
    Password:
    <input type="password" name="keyPassword" size="20" maxlength="200" />
    <br />

    <input type="submit" value="Unlock Private Key" />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>">Cancel</a></p>
