<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<form name="<portlet:namespace/>" action="<portlet:actionURL/>">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="protocol" value="${protocol}">
<input type="hidden" name="containerObjectName" value="${containerObjectName}">
<input type="hidden" name="managerObjectName" value="${managerObjectName}">
<c:if test="${mode eq 'save'}">
  <input type="hidden" name="objectName" value="${objectName}">
</c:if>
<table width="100%%"  border="0">

<%-- THIS PART SHOULD BE THE SAME AS THE HTTP CONNECTOR --%>

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right">Unique Name: </div></td>
    <td><input name="name" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>A name that is different than the name for any other web connectors in the server (no spaces in the name please)</td>
  </tr>
</c:if>
<!-- Host Field -->
  <tr>
    <td><div align="right">Host: </div></td>
    <td>
      <input name="host" type="text" size="30" value="${host}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The host name or IP to bind to.  The normal values are <tt>0.0.0.0</tt> (all interfaces) or <tt>localhost</tt> (local connections only)</td>
  </tr>
<!-- Port Field -->
  <tr>
    <td><div align="right">Port: </div></td>
    <td>
      <input name="port" type="text" size="5" value="${port}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The network port to bind to.</td>
  </tr>
<!-- Min Threads Field (Jetty only) -->
<c:if test="${server eq 'jetty'}">
  <tr>
    <td><div align="right">Min Threads: </div></td>
    <td>
      <input name="minThreads" type="text" size="3" value="${minThreads}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The minimum number of threads this connector should use to handle incoming requests</td>
  </tr>
</c:if>
<!-- Max Threads Field -->
  <tr>
    <td><div align="right">Max Threads: </div></td>
    <td>
      <input name="maxThreads" type="text" size="3" value="${maxThreads}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum number of threads this connector should use to handle incoming requests</td>
  </tr>

<%-- END OF PART THAT SHOULD BE THE SAME AS THE HTTP CONNECTOR --%>

  <tr>
    <th colspan="2"><div align="left">SSL Settings</div></th>
  </tr>

<!-- Keystore File Field -->
  <tr>
    <td><div align="right">Keystore File: </div></td>
    <td>
      <input name="keystoreFile" type="text" size="30" value="${keystoreFile}">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The file that holds the keystore (relative to the Geronimo install dir)</td>
  </tr>

<!-- Keystore Password Field -->
  <tr>
    <td><div align="right"><c:if test="${mode eq 'save'}">Change </c:if>Keystore Password: </div></td>
    <td>
      <input name="keystorePassword" type="password" size="10">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the password used to access the keystore file.<c:if test="${server ne 'jetty'}">  This is also the
      password used to access the server private key within the keystore (so the two passwords must be
      set to be the same on the keystore).</c:if><c:if test="${mode eq 'save'}">  Leave
      this empty if you don't want to change the current password.</c:if></td>
  </tr>

<!-- Key Password Field (Jetty only) -->
<c:if test="${server eq 'jetty'}">
  <tr>
    <td><div align="right"><c:if test="${mode eq 'save'}">Change </c:if>Server Key Password: </div></td>
    <td>
      <input name="privateKeyPassword" type="password" size="10">
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the password used to access the private key in the keystore.<c:if test="${mode eq 'save'}">  Leave
      this empty if you don't want to change the current password.</c:if></td>
  </tr>
</c:if>

<!-- Keystore Type Field -->
  <tr>
    <td><div align="right">Keystore Type: </div></td>
    <td>
      <select name="keystoreType">
        <option<c:if test="${keystoreType eq 'JKS' || logLevel eq ''}"> selected</c:if>>JKS</option>
        <option<c:if test="${keystoreType eq 'PKCS12'}"> selected</c:if>>PKCS12</option>
      </select>
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the keystore type.  There is normally no reason not to use the default (<tt>JKS</tt>).</td>
  </tr>

<!-- Algorithm Field -->
  <tr>
    <td><div align="right">HTTPS Algorithm: </div></td>
    <td>
      <select name="algorithm">
        <option value="SunX509"<c:if test="${algorithm eq 'SunX509' || logLevel eq ''}"> selected</c:if>>Sun</option>
        <option value="IbmX509"<c:if test="${algorithm eq 'IbmX509'}"> selected</c:if>>IBM</option>
      </select>
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the HTTPS algorithm.  This should normally be set to match the JVM vendor.</td>
  </tr>

<!-- Secure Protocol Field -->
  <tr>
    <td><div align="right">HTTPS Protocol: </div></td>
    <td>
      <select name="secureProtocol">
        <option<c:if test="${secureProtocol eq 'TLS' || logLevel eq ''}"> selected</c:if>>TLS</option>
        <option<c:if test="${secureProtocol eq 'SSL'}"> selected</c:if>>SSL</option>
      </select>
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the HTTPS protocol.  This should normally be set to <tt>TLS</tt>, though some (IBM) JVMs don't work properly
      with popular browsers unless it is changed to <tt>SSL</tt>.</td>
  </tr>

<!-- Client Auth Field -->
  <tr>
    <td><div align="right">Client Auth Required: </div></td>
    <td>
      <input type="checkbox" name="clientAuth" <c:if test="${!empty clientAuth}">CHECKED </c:if>/>
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>If set, then clients connecting through this connector must supply a valid client certificate.  By default, the
      validity is based on the CA certificates in the server keystore (<i>need to confirm not the JVM default
      trust keystore</i>).</td>
  </tr>



<!-- Submit Button -->
  <tr>
    <td><div align="right"></div></td>
    <td><input name="submit" type="submit" value="Save"></td>
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List connectors</a>