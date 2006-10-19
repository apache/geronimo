<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<p>This screen lists the contents of a keystore.</p>

<table width="100%">
  <tr>
    <td class="DarkBackground">&nbsp;</td>
    <td class="DarkBackground">Alias</td>
    <td class="DarkBackground" align="center">Type</td>
    <td class="DarkBackground" align="center">Certificate Fingerprint</td>
  </tr>
<c:forEach var="alias" items="${keystore.certificates}">
  <tr>
    <td><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        view
        </a>    
    </td>
    <td><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        ${alias}
        </a>    
    </td>
    <td>Trusted Certificate</td>
    <td>${keystore.fingerprints[alias]}</td>
  </tr>
</c:forEach>
<c:forEach var="alias" items="${keystore.keys}">
  <tr>
    <td><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        view
        </a>    
    </td>
    <td><a href="<portlet:actionURL portletMode="view">
                 <portlet:param name="mode" value="certificateDetails-before" />
                 <portlet:param name="id" value="${keystore.name}" />
                 <portlet:param name="alias" value="${alias}" /></portlet:actionURL>">
        ${alias}
        </a>    
    </td>
    <td>Private Key</td>
    <td>${keystore.fingerprints[alias]}</td>
  </tr>
</c:forEach>
</table>

<p>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="uploadCertificate-before" /><portlet:param name="id" value="${keystore.name}" /></portlet:actionURL>">Add Trust Certificate</a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="configureKey-before" /><portlet:param name="keystore" value="${keystore.name}" /></portlet:actionURL>">Create Private Key</a>
    <a href="<portlet:actionURL portletMode="view"><portlet:param name="mode" value="list-before" /></portlet:actionURL>">Return to keystore list</a>
</p>