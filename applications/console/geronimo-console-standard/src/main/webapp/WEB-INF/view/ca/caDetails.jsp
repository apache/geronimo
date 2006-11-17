<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Certification Authority Details</b></p>

<p>This screen shows the details of CA's certificate and keypair.  <i>Highest Serial Number</i> shows
the highest of serial number of any certificate issued by this CA.  <i>Certificate Text</i> shows the CA's
certificate in base64 encoded form.  This text can be used by the certificate requestors to designate
this CA as a trusted CA in their software.</p>

<jsp:include page="_header.jsp" />

<c:if test="${empty(caLocked) || !caLocked}">
  <table border="0">
    <tr>
        <th colspan="2" align="left" class="DarkBackground">Certificate Details</th>
    </tr>
    <tr>
        <th class="LightBackground" align="right">Version:</th>
        <td class="LightBackground">${cert.version}</td>
    </tr>
    <tr>
        <th class="MediumBackground" align="right">Subject:</th>
        <td class="MediumBackground">${cert.subjectDN.name}</td>
    </tr>
    <tr>
        <th class="LightBackground" align="right">Issuer:</th>
        <td class="LightBackground">${cert.issuerDN.name}</td>
    </tr>
    <tr>
        <th class="MediumBackground" align="right">Serial Number:</th>
        <td class="MediumBackground">${cert.serialNumber}</td>
    </tr>
    <tr>
        <th class="LightBackground" align="right">Valid From:</th>
        <td class="LightBackground">${cert.notBefore}</td>
    </tr>
    <tr>
        <th class="MediumBackground" align="right">Valid To:</th>
        <td class="MediumBackground">${cert.notAfter}</td>
    </tr>
    <tr>
        <th class="LightBackground" align="right">Signature Alg:</th>
        <td class="LightBackground">${cert.sigAlgName}</td>
    </tr>
    <tr>
        <th class="MediumBackground" align="right">Public Key Alg:</th>
        <td class="MediumBackground">${cert.publicKey.algorithm}</td>
    </tr>
    <tr>
        <th class="LightBackground" align="right">Key Size:</th>
        <td class="LightBackground">${keySize}</td>
    </tr>
  <c:set var="backgroundClass" value='LightBackground'/> <!-- This should be set from the row above. -->
  <c:forEach items="${cert.criticalExtensionOIDs}" var="extoid">
    <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
            <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
            <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
    </c:choose>
    <tr>
        <th class="${backgroundClass}" align="right">critical ext: </th>
        <td class="${backgroundClass}">${extoid}</td>
    </tr>
  </c:forEach>
  <c:forEach items="${cert.nonCriticalExtensionOIDs}" var="extoid">
    <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
            <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
            <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
    </c:choose>
    <tr>
        <th class="${backgroundClass}" align="right">non-critical ext: </th>
        <td class="${backgroundClass}">${extoid}</td>
    </tr>
  </c:forEach>
    <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
            <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
            <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
    </c:choose>
    <tr>
        <th class="${backgroundClass}" align="right">Finger prints:</th>
        <td class="${backgroundClass}">
  <c:forEach items="${fingerPrints}" var="fp">
            ${fp.key} = &nbsp; ${fp.value} <br/>
  </c:forEach>
        </td>
    </tr>
    <tr><td>&nbsp;</td></tr>
    <tr>
        <th align="right">Highest Serial Number:</th>
        <td>${highestSerial}</td>
    </tr>
    <tr><td>&nbsp;</td></tr>
    <tr>
        <th colspan="2" align="left">Base64 encoded Certificate Text</th>
    </tr>
    <tr>
        <td colspan="2"><form><textarea rows="15" cols="80" READONLY>${certText}</textarea></form></td>
    </tr>
  </table>
</c:if>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Back to CA home</a></p>
