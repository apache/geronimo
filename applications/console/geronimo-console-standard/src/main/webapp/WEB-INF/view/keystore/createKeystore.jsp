<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="createKeystore-after" />
    <table border="0">
        <tr>
            <th align="right">Keystore file name:</th>
            <td>
                <input type="text" name="filename" size="20" maxlength="100" />
            </td>
        </tr>
        <tr>
            <th align="right">Password for new keystore:</th>
            <td>
                <input type="password" name="password" size="20" maxlength="200" />
            </td>
        </tr>
    </table>
    <input type="submit" value="Create Keystore" />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>">Cancel</a></p>
