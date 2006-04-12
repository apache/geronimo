<%@ page import="org.apache.geronimo.console.util.PortletManager"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>Please confirm that this is the correct certificate to import:</p>

<form name="<portlet:namespace/>KeystoreForm" action="<portlet:actionURL/>">
    <input type="hidden" name="id" value="${id}" />
    <input type="hidden" name="alias" value="${alias}" />
    <input type="hidden" name="certificate" value="${certificate}" />
    <input type="hidden" name="mode" value="confirmCertificate-after" />

    <table border="0">
        <tr>
            <th align="right">Fingerprint:</th>
            <td>${fingerprint}</td>
        </tr>
        <tr>
            <th align="right">Subject:</th>
            <td>${subject}</td>
        </tr>
        <tr>
            <th align="right">Issuer:</th>
            <td>${issuer}</td>
        </tr>
        <tr>
            <th align="right">Validity:</th>
            <td>${validStart} to ${validEnd}</td>
        </tr>
        <tr>
            <th align="right">Serial Number:</th>
            <td>${serial}</td>
        </tr>
    </table>

    <input type="submit" value="Import Certificate" />
</form>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="viewKeystore-before" />
              <portlet:param name="id" value="${id}" />
            </portlet:actionURL>">Cancel</a></p>
