<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>

<%-- $Rev$ $Date$ --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>HttpsForm";
var <portlet:namespace/>requiredFields = new Array("host");
var <portlet:namespace/>numericFields = new Array("port", "maxThreads");
var <portlet:namespace/>passwordFields = new Array();
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) {
        return false;
    }    
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i])) {
            return false;
        }
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        return false;
    }
    return true;
}
</script>

<form name="<portlet:namespace/>HttpsForm" action="<portlet:actionURL/>">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="protocol" value="${protocol}">
<input type="hidden" name="containerURI" value="${containerURI}">
<input type="hidden" name="managerURI" value="${managerURI}">
<c:if test="${mode eq 'save'}">
  <input type="hidden" name="connectorURI" value="${connectorURI}">
</c:if>
<table width="100%%"  border="0">

<%-- THIS PART SHOULD BE THE SAME AS THE HTTP CONNECTOR --%>

<!-- Current Task -->
<c:choose>
  <c:when test="${mode eq 'add'}">
    <tr><th colspan="2" align="left">Add new ${protocol} listener for ${containerDisplayName}</th></tr>
  </c:when>
  <c:otherwise>
    <tr><th colspan="2" align="left">Edit connector ${displayName}</th></tr>
  </c:otherwise>
</c:choose>

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right">Unique Name: </div></td>
    <td><input name="displayName" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>A name that is different than the name for any other web connectors in the server (no spaces in the name please)</td>
  </tr>
  <script language="JavaScript">
    <portlet:namespace/>requiredFields = new Array("displayName").concat(<portlet:namespace/>requiredFields);
  </script>
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
  <script language="JavaScript">
    <portlet:namespace/>numericFields = <portlet:namespace/>numericFields.concat(new Array("minThreads"));
  </script>
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

<!-- ====================================== JETTY ONLY ====================================== -->
<c:if test="${server eq 'jetty'}">
    <!-- Key Store Field -->
      <tr>
        <td><div align="right">Key Store: </div></td>
        <td>
          <select name="unlockKeyStore">
              <c:forEach var="store" items="${keyStores}">
                  <option<c:if test="${unlockKeyStore eq store}"> selected</c:if>>${store}</option>
              </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td><div align="right"></div></td>
        <td>The keystore to use for accessing the server's private key</td>
      </tr>

    <!-- Trust Store Field -->
      <tr>
        <td><div align="right">Trust Store: </div></td>
        <td>
          <select name="unlockTrustStore">
              <option />
              <c:forEach var="store" items="${trustStores}">
                  <option<c:if test="${unlockTrustStore eq store}"> selected</c:if>>${store}</option>
              </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td><div align="right"></div></td>
        <td>The keystore containing the trusted certificate entries, including
            Certification Authority (CA) certificates</td>
      </tr>

</c:if>


<!-- ========================== TOMCAT ONLY ====================================== -->
<c:if test="${server eq 'tomcat'}">
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
  <script language="JavaScript">
    <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("keystoreFile"));
  </script>

<!-- Keystore Password Field -->
  <tr>
    <td><div align="right"><c:if test="${mode eq 'save'}">Change </c:if>Keystore Password: </div></td>
    <td>
      <input name="keystorePassword" type="password" size="10">
	</td>
  </tr>
  <tr>
    <td><div align="right">Confirm Password: </div></td>
    <td>
      <input name="confirm-keystorePassword" type="password" size="10">
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
  <c:if test="${mode eq 'add'}">
    <script language="JavaScript">
      <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("keystorePassword"));
    </script>
  </c:if>
    <script language="JavaScript">
      <portlet:namespace/>passwordFields = <portlet:namespace/>passwordFields.concat(new Array("keystorePassword"));
    </script>

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

  <!-- Truststore File Field -->
  <tr>
    <td><div align="right">Truststore File: </div></td>
    <td>
      <input name="truststoreFile" type="text" size="30" value="${truststoreFile}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The file that holds the truststore (relative to the Geronimo install dir)</td>
  </tr>

<!-- Truststore Password Field -->
  <tr>
    <td><div align="right"><c:if test="${mode eq 'save'}">Change </c:if>Truststore Password: </div></td>
    <td>
      <input name="truststorePassword" type="password" size="10">
    </td>
  </tr>
  <tr>
    <td><div align="right">Confirm Password: </div></td>
    <td>
      <input name="confirm-truststorePassword" type="password" size="10">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the password used to verify the truststore file.<c:if test="${mode eq 'save'}">  Leave
      this empty if you don't want to change the current password.</c:if></td>
  </tr>
    <script language="JavaScript">
      <portlet:namespace/>passwordFields = <portlet:namespace/>passwordFields.concat(new Array("truststorePassword"));
    </script>

<!-- Truststore Type Field -->
  <tr>
    <td><div align="right">Truststore Type: </div></td>
    <td>
      <select name="truststoreType">
        <option<c:if test="${truststoreType eq 'JKS' || logLevel eq ''}"> selected</c:if>>JKS</option>
        <option<c:if test="${truststoreType eq 'PKCS12'}"> selected</c:if>>PKCS12</option>
      </select>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><c:choose><c:when test="${mode eq 'save'}">Change</c:when><c:otherwise>Set</c:otherwise></c:choose>
      the truststore type.  There is normally no reason not to use the default (<tt>JKS</tt>).</td>
  </tr>
</c:if>

<!-- Algorithm Field -->
  <tr>
    <td><div align="right">HTTPS Algorithm: </div></td>
    <td>
      <select name="algorithm">
        <option value="Default"<c:if test="${algorithm eq 'Default' || algorithm eq ''}"> selected</c:if>>JVM Default</option>
        <option value="SunX509"<c:if test="${algorithm eq 'SunX509'}"> selected</c:if>>Sun</option>
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
        <option<c:if test="${secureProtocol eq 'TLS' || secureProtocol eq ''}"> selected</c:if>>TLS</option>
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
    <td>If set, then clients connecting through this connector must supply a valid client certificate.  The
        validity is checked using the CA certificates stored in the first of these to be found:
        <ol>
          <c:if test="${server eq 'tomcat'}">
            <li>The trust store configured above</li>
          </c:if>
            <li>A keystore file specified by the <tt>javax.net.ssl.trustStore</tt> system property</li>
            <li><i>java-home</i><tt>/lib/security/jssecacerts</tt></li>
            <li><i>java-home</i><tt>/lib/security/cacerts</tt></li>
        </ol>
    </td>
  </tr>



<!-- Submit Button -->
  <tr>
    <td><div align="right"></div></td>
    <td>
      <input name="submit" type="submit" value="Save" onClick="return <portlet:namespace/>validateForm();">
      <input name="reset" type="reset" value="Reset">
      <input name="submit" type="submit" value="Cancel">
    </td>    
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List connectors</a>
