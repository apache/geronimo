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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="plancreator-portlet"/>
<portlet:defineObjects />

<script>
function <portlet:namespace/>showElement(id) {
  var element = document.getElementById("<portlet:namespace/>"+id);
  if (element.style.display != 'inline') {
    element.style.display = 'inline';
  }
}

function <portlet:namespace/>hideElement(id) {
  var element = document.getElementById("<portlet:namespace/>"+id);
  if (element.style.display != 'none') {
    element.style.display = 'none';
  }
}

function <portlet:namespace/>addPort(serviceRefId)
{
  var prefix = "<portlet:namespace/>" + serviceRefId;
  if (document.getElementById(prefix + ".newPort.portName").value == "") {
    alert("Port Name field is empty!");
    return;
  }

  var lastIndex = document.getElementById(prefix + ".port.lastIndex");
  var elementId = serviceRefId + ".port." + lastIndex.value;
  lastIndex.value = (lastIndex.value - 1) + 2;
  //lastIndex.value is a string, and using lastIndex.value += 1 might imply string concat
  var newTRId = "<portlet:namespace/>" + elementId + ".tr";
  var newTR = document.createElement("tr");
  newTR.setAttribute("id", newTRId);
  var newTD = document.createElement("td"); //empty first field
  newTR.appendChild(newTD);

  var portNameField = document.getElementById(prefix + ".newPort.portName");
  var portName = portNameField.value;
  portNameField.value="";
  newTD = document.createElement("td");
  newTD.innerHTML = portName + 
    "<input type=\"hidden\" name=\"" + elementId + ".portName\" value=\"" + portName + "\" /> ";
  newTR.appendChild(newTD);

  var url = "";
  var innerHTML = "";
  var protocolField = document.getElementById(prefix + ".newPort.protocol");
  var protocol = protocolField.value;
  if (protocol != "") {
    url += protocol + "://";
    innerHTML += "<input type=\"hidden\" name=\"" + elementId + ".protocol\" value=\"" + protocol + "\" /> ";
  }
  var hostField = document.getElementById(prefix + ".newPort.host");
  var host = hostField.value;
  if (host != "") {
    url += host;
    innerHTML += "<input type=\"hidden\" name=\"" + elementId + ".host\" value=\"" + host + "\" /> ";
  }
  var portField = document.getElementById(prefix + ".newPort.port");
  var port = portField.value;
  if (port != "") {
    url += ":" + port;
    innerHTML += "<input type=\"hidden\" name=\"" + elementId + ".port\" value=\"" + port + "\" /> ";
  }
  var uriField = document.getElementById(prefix + ".newPort.uri");
  var uri = uriField.value;
  if (uri != "") {
    if(uri.charAt(0) != "/") {
      url += "/";;
    }
    url += uri;
    innerHTML += "<input type=\"hidden\" name=\"" + elementId + ".uri\" value=\"" + uri + "\" /> ";
  }
  newTD = document.createElement("td");
  newTD.innerHTML = url + innerHTML;
  newTR.appendChild(newTD);

  var credentialsNameField = document.getElementById(prefix + ".newPort.credentialsName");
  var credentialsName = credentialsNameField.value;
  innerHTML = "";
  if(credentialsName != "") {
    innerHTML = 
      "<input type=\"hidden\" name=\"" + elementId + ".credentialsName\" value=\"" + credentialsName + "\" /> ";
  }
  newTD = document.createElement("td");
  newTD.innerHTML = credentialsName + innerHTML;
  newTR.appendChild(newTD);

  newTD = document.createElement("td");
  newTD.innerHTML = "<a href=\"javascript:;\" " + 
    "onclick=\"<portlet:namespace/>removePort(\'" + serviceRefId + "\', \'" + newTRId + "\')\">remove</a>";
  newTR.appendChild(newTD);

  var tbody = document.getElementById(prefix + ".tableBody");
  var insertBeforeTR = document.getElementById(prefix + ".insertBefore");
  tbody.insertBefore(newTR, insertBeforeTR);
  <portlet:namespace/>hideElement(serviceRefId + ".addNewPort");
}

function <portlet:namespace/>removePort(serviceRefId, trId)
{
  var tbody = document.getElementById("<portlet:namespace/>" + serviceRefId + ".tableBody");
  var tr = document.getElementById(trId);
  tbody.removeChild(tr);
}
</script>

<CommonMsg:commonMsg/>

<p><fmt:message key="plancreator.reference.title"/></p>

<p><fmt:message key="plancreator.reference.desc"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>ReferencesForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="references-after" />

<!-- ENTRY FIELD: EJB References -->
<c:if test="${!empty(data.webApp.ejbRefArray)}">
  <p><b><fmt:message key="plancreator.reference.ejbReference"/>:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.ejbRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.ejbDeployed"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="ejbRef" items="${data.webApp.ejbRefArray}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="ejbRef.${status.index}.refName" value="${ejbRef.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>ejbRef.${status.index}.refLink">${ejbRef.refName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="ejbRef.${status.index}.refLink" id="<portlet:namespace/>ejbRef.${status.index}.refLink">
            <c:forEach var="ejb" items="${deployedEjbs}">
              <option value="${ejb.patternName}">${ejb.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: EJB Local References -->
<c:if test="${!empty(data.webApp.ejbLocalRefArray)}">
  <p><b><fmt:message key="plancreator.reference.ejbLocalReference"/>:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.ejbLocalRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.ejbDeployed"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="ejbLocalRef" items="${data.webApp.ejbLocalRefArray}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="ejbLocalRef.${status.index}.refName" value="${ejbLocalRef.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>ejbLocalRef.${status.index}.refLink">${ejbLocalRef.refName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="ejbLocalRef.${status.index}.refLink" id="<portlet:namespace/>ejbLocalRef.${status.index}.refLink">
            <c:forEach var="ejb" items="${deployedEjbs}">
              <option value="${ejb.patternName}">${ejb.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JDBC Connection Pool References -->
<c:if test="${!empty(data.jndiRefsConfig.jdbcPoolRefs)}">
  <p><b><fmt:message key="plancreator.reference.jdbcReference"/>:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.jdbcRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.jdbc"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.jndiRefsConfig.jdbcPoolRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="jdbcPoolRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>jdbcPoolRef.${status.index}.refLink">${refData.refName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="jdbcPoolRef.${status.index}.refLink" id="<portlet:namespace/>jdbcPoolRef.${status.index}.refLink">
          <c:forEach var="jdbcPool" items="${deployedJdbcConnectionPools}">
            <option value="${jdbcPool.patternName}">${jdbcPool.displayName}</option>
          </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JMS Connection Factory References -->
<c:if test="${!empty(data.jndiRefsConfig.jmsConnectionFactoryRefs)}">
  <p><b><fmt:message key="plancreator.reference.jmsConnReference"/>:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.jmsConnRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.jmsConn"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.jndiRefsConfig.jmsConnectionFactoryRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="jmsConnectionFactoryRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>jmsConnectionFactoryRef.${status.index}.refLink">${refData.refName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="jmsConnectionFactoryRef.${status.index}.refLink" id="<portlet:namespace/>jmsConnectionFactoryRef.${status.index}.refLink">
            <c:forEach var="jmsFactory" items="${deployedJmsConnectionFactories}">
              <option value="${jmsFactory.patternName}">${jmsFactory.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JMS Destination References -->
<c:if test="${!empty(data.webApp.resourceEnvRefArray) || !empty(data.webApp.messageDestinationArray)}">
  <p><b><fmt:message key="plancreator.reference.jmsReference"/>:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.jmsRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.jms"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="resourceEnvRef" items="${data.webApp.resourceEnvRefArray}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="jmsDestinationRef.${status.index}.refName" value="${resourceEnvRef.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>jmsDestinationRef.${status.index}.refLink">${resourceEnvRef.refName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="jmsDestinationRef.${status.index}.refLink" id="<portlet:namespace/>jmsDestinationRef.${status.index}.refLink">
            <c:forEach var="jmsDestination" items="${deployedJmsDestinations}">
              <option value="${jmsDestination.patternName}" 
                <c:if test="${fn:startsWith(jmsDestination.displayName, resourceEnvRef.messageDestinationLink)}">selected="selected"</c:if>
              >${jmsDestination.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
    <c:forEach var="messageDestination" items="${data.webApp.messageDestinationArray}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="messageDestination.${status.index}.refName" value="${messageDestination.messageDestinationName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>messageDestination.${status.index}.refLink">${messageDestination.messageDestinationName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="messageDestination.${status.index}.refLink" id="<portlet:namespace/>messageDestination.${status.index}.refLink">
            <c:forEach var="jmsDestination" items="${deployedJmsDestinations}">
              <option value="${jmsDestination.patternName}" 
                <c:if test="${fn:startsWith(jmsDestination.displayName, messageDestination.adminObjectLink)}">selected="selected"</c:if>
              >${jmsDestination.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: JavaMail Session References -->
<c:if test="${!empty(data.jndiRefsConfig.javaMailSessionRefs)}">
  <p><b><fmt:message key="plancreator.reference.javamailReference"/>:</b></p>
  <table border="0" width="100%">
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.javamailRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.javamail"/></th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="refData" items="${data.jndiRefsConfig.javaMailSessionRefs}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="javaMailSessionRef.${status.index}.refName" value="${refData.refName}" />
      <tr>
        <td class="${backgroundClass}">
          <div align="right"><label for="<portlet:namespace/>javaMailSessionRef.${status.index}.refLink">${refData.refName}</label></div>
        </td>
        <td class="${backgroundClass}">
          <select name="javaMailSessionRef.${status.index}.refLink" id="<portlet:namespace/>javaMailSessionRef.${status.index}.refLink">
            <c:forEach var="javaMailSession" items="${deployedJavaMailSessions}">
              <option value="${javaMailSession.patternName}">${javaMailSession.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>

<!-- ENTRY FIELD: Web Service References -->
<c:if test="${!empty(data.webApp.serviceRefArray)}">
  <p><b><fmt:message key="plancreator.reference.wsReference"/>:</b></p>
  <p><fmt:message key="plancreator.reference.wsDesc"/></p>
  <table border="0" width="100%">
    <thead>
    <tr>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.reference.wsRef"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.common.port"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.common.url"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.security.credenitial"/></th>
      <th class="DarkBackground" align="center"><fmt:message key="plancreator.common.action"/></th>
    </tr>
    </thead>
    <tfoot></tfoot>
    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="serviceRef" items="${data.webApp.serviceRefArray}" varStatus="status">
      <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
      </c:choose>
      <c:set var="serviceRefId" value="serviceRef.${status.index}" />
      <tbody id="<portlet:namespace/>${serviceRefId}.tableBody" class="${backgroundClass}">
        <input type="hidden" name="${serviceRefId}.serviceRefName" value="${serviceRef.serviceRefName}" />
        <input type="hidden" id="<portlet:namespace/>${serviceRefId}.port.lastIndex" 
          name="${serviceRefId}.port.lastIndex" value="0">
        <tr>
          <td>
            <div align="right">${serviceRef.serviceRefName}</div>
          </td>
          <td colspan="4"></td>
        </tr>
        <!-- place before which new ports have to be added -->
        <tr id="<portlet:namespace/>${serviceRefId}.insertBefore">
          <td></td>
          <td colspan="4" align="left">
            <input type="button" value="Add Port" 
              onclick="<portlet:namespace/>showElement('${serviceRefId}.addNewPort');"/>
            <div id="<portlet:namespace/>${serviceRefId}.addNewPort" style="display:none">
              <table border="0">
                <tr>
                  <th><div align="right"><label for="<portlet:namespace/>${serviceRefId}.newPort.portName"><fmt:message key="plancreator.common.portName"/></label>:</div></th>
                  <td><input type="text" id="<portlet:namespace/>${serviceRefId}.newPort.portName"/></td>
                </tr>
                <tr>
                  <th><div align="right"><label for="<portlet:namespace/>${serviceRefId}.newPort.protocol"><fmt:message key="plancreator.common.protocol"/></label>:</div></th>
                  <td><input type="text" id="<portlet:namespace/>${serviceRefId}.newPort.protocol"/></td>
                </tr>
                <tr>
                  <th><div align="right"><label for="<portlet:namespace/>${serviceRefId}.newPort.host"><fmt:message key="plancreator.common.host"/></label>:</div></th>
                  <td><input type="text" id="<portlet:namespace/>${serviceRefId}.newPort.host"/></td>
                </tr>
                <tr>
                  <th><div align="right"><label for="<portlet:namespace/>${serviceRefId}.newPort.port"><fmt:message key="plancreator.common.port"/></label>:</div></th>
                  <td><input type="text" id="<portlet:namespace/>${serviceRefId}.newPort.port"/></td>
                </tr>
                <tr>
                  <th><div align="right"><label for="<portlet:namespace/>${serviceRefId}.newPort.uri"><fmt:message key="plancreator.common.uri"/></label>:</div></th>
                  <td><input type="text" id="<portlet:namespace/>${serviceRefId}.newPort.uri"/></td>
                </tr>
                <tr>
                  <th><div align="right"><label for="<portlet:namespace/>${serviceRefId}.newPort.credentialsName"><fmt:message key="plancreator.security.credenitialName"/></label>:</div></th>
                  <td><input type="text" id="<portlet:namespace/>${serviceRefId}.newPort.credentialsName"/></td>
                </tr>
                <tr>
                  <th><div align="right"></div></th>
                  <td>
                    <input type="button" value="<fmt:message key="plancreator.common.add"/>" 
                      onclick="<portlet:namespace/>addPort('${serviceRefId}');"/>
                    <input type="button" value="<fmt:message key="plancreator.common.cancel"/>" 
                      onclick="<portlet:namespace/>hideElement('${serviceRefId}.addNewPort');"/>
                  </td>
                </tr>
              </table>
            </div>
          </td>
        </tr>
      </tbody>
    </c:forEach>
  </table>
</c:if>

<!-- SUBMIT BUTTON -->
<table border="0">
  <tr>
    <th>
    <div align="right"></div>
    </th>
    <td><input type="submit" value="<fmt:message key="plancreator.common.next"/>" /></td>
  </tr>
</table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="plancreator.common.cancel"/></a></p>
