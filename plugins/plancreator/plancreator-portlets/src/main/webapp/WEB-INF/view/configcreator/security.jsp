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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
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

function <portlet:namespace/>toggleAdvancedSecuritySettings() {
  var checkBox = document.getElementById("<portlet:namespace/>advancedSecuritySettingsCheckbox");
  if (checkBox.checked) {
    <portlet:namespace/>showElement("advancedSecuritySettings");
    var advancedSettingsFlag = document.getElementById("<portlet:namespace/>advancedSecuritySettingsFlag");
    advancedSettingsFlag.value = "true";
  } else {
    <portlet:namespace/>hideElement("advancedSecuritySettings");
    var advancedSettingsFlag = document.getElementById("<portlet:namespace/>advancedSecuritySettingsFlag");
    advancedSettingsFlag.value = "false";
  }
  for (i = 0; i >= 0; i++) { //infinite loop
    var runAsSubjectId = "security.roleMappings." + i + ".runAsSubject";
    var runAsSubjectCheckBox = document.getElementById("<portlet:namespace/>" + runAsSubjectId + ".checkBox");
    if (runAsSubjectCheckBox == null) {
      break;
    }
    if (checkBox.checked) {
      runAsSubjectCheckBox.disabled = false;
    } else {
      runAsSubjectCheckBox.disabled = true;
      runAsSubjectCheckBox.checked = false;
      <portlet:namespace/>hideElement(runAsSubjectId + ".subElements");
    }
  }
}

function <portlet:namespace/>toggleRunAsSubject(runAsSubjectId) {
  var checkBox = document.getElementById("<portlet:namespace/>" + runAsSubjectId + ".checkBox");
  if (checkBox.checked) {
    <portlet:namespace/>showElement(runAsSubjectId + ".subElements");
  } else {
    <portlet:namespace/>hideElement(runAsSubjectId + ".subElements");
  }
}

function <portlet:namespace/>handleAddClick(roleId, type) {
  if (type == "principal") {
    <portlet:namespace/>showElement(roleId + '.principal.ui');
    <portlet:namespace/>hideElement(roleId + '.loginDomainPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.realmPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.distinguishedName.ui');
  } else if (type == "loginDomainPrincipal") {
    <portlet:namespace/>showElement(roleId + '.loginDomainPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.principal.ui');
    <portlet:namespace/>hideElement(roleId + '.realmPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.distinguishedName.ui');
  } else if (type == "realmPrincipal") {
    <portlet:namespace/>showElement(roleId + '.realmPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.principal.ui');
    <portlet:namespace/>hideElement(roleId + '.loginDomainPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.distinguishedName.ui');
  } else if (type == "distinguishedName") {
    <portlet:namespace/>showElement(roleId + '.distinguishedName.ui');
    <portlet:namespace/>hideElement(roleId + '.principal.ui');
    <portlet:namespace/>hideElement(roleId + '.loginDomainPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.realmPrincipal.ui');
  } else {
    <portlet:namespace/>hideElement(roleId + '.principal.ui');
    <portlet:namespace/>hideElement(roleId + '.loginDomainPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.realmPrincipal.ui');
    <portlet:namespace/>hideElement(roleId + '.distinguishedName.ui');
  }
}

function <portlet:namespace/>addElement(roleId, type)
{
  var prefix = "<portlet:namespace/>" + roleId + "." + type;
  if (document.getElementById(prefix + ".name").value == "") {
    alert("Name field is empty!");
    return;
  }
  if ((type == "loginDomainPrincipal" || type == "realmPrincipal") && 
      document.getElementById(prefix + ".domainName").value == "") {
    alert("Domain Name field is empty!");
    return;
  }

  var tbody = document.getElementById(prefix + ".table.tbody");
  if (tbody == null) {
    var placeHolder = document.getElementById(prefix + ".placeHolder");
    var table = document.createElement("table");
    table.setAttribute("id", prefix + ".table");
    table.setAttribute("border","0");
    table.setAttribute("width","100%");

    var numColumns = "2"; // default for type == "distinguishedName"
    var heading = "Distinguished Names";
    if (type == "principal") {
      numColumns = "3";
      heading = "Principals";
    } else if (type == "loginDomainPrincipal") {
      numColumns = "4";
      heading = "Login Domain Principals";
    } else if (type == "realmPrincipal") {
      numColumns = "5";
      heading = "Realm Principals";
    }

    var newTHead = document.createElement("thead");
    var newTR = document.createElement("tr");
    var newTD = document.createElement("td");
    newTD.setAttribute("colSpan", numColumns);
    newTD.setAttribute("style", "padding-top: 5px");
    newTD.innerHTML = 
      "<b>" + heading + 
      " <input type=\"hidden\" id=\"" + roleId + "." + type + ".count\" value=\"0\" /> " + 
      "</b>";
    newTR.appendChild(newTD);
    newTHead.appendChild(newTR);

    newTR = document.createElement("tr");
    var newTH = document.createElement("th");
    newTH.setAttribute("class", "DarkBackground");
    newTH.setAttribute("className", "DarkBackground");
    newTH.innerHTML = "Name";
    newTR.appendChild(newTH);
    if (type != "distinguishedName") {
      newTH = document.createElement("th");
      newTH.setAttribute("class", "DarkBackground");
     newTH.setAttribute("className", "DarkBackground");
      newTH.innerHTML = "Class";
      newTR.appendChild(newTH);
      if (type != "principal") {
        newTH = document.createElement("th");
        newTH.setAttribute("class", "DarkBackground");
        newTH.setAttribute("className", "DarkBackground");
        newTH.innerHTML = "Domain Name";
        newTR.appendChild(newTH);
        if (type != "loginDomainPrincipal") {
          newTH = document.createElement("th");
          newTH.setAttribute("class", "DarkBackground");
          newTH.setAttribute("className", "DarkBackground");
          newTH.innerHTML = "Realm Name";
          newTR.appendChild(newTH);
        }
      }
    }
    newTH = document.createElement("th");
    newTH.setAttribute("class", "DarkBackground");
    newTH.setAttribute("className", "DarkBackground");
    newTH.innerHTML = "Actions";
    newTR.appendChild(newTH);
    newTHead.appendChild(newTR);

    table.appendChild(newTHead);
    var newTFoot = document.createElement("tfoot");
    table.appendChild(newTFoot);
    tbody = document.createElement("tbody");
    tbody.setAttribute("id", prefix + ".table.tbody");
    table.appendChild(tbody);
    placeHolder.appendChild(table);
  }

  var lastIndex = document.getElementById(roleId + "." + type + ".lastIndex");
  var elementId = roleId + "." + type + "." + lastIndex.value;
  lastIndex.value = (lastIndex.value - 1) + 2;
  //lastIndex.value is a string, and using lastIndex.value += 1 might imply string concat
  var newTRId = "<portlet:namespace/>" + elementId + ".tr";
  var newTR = document.createElement("tr");
  newTR.setAttribute("id", newTRId);

  var nameBox = document.getElementById(prefix + ".name");
  var name = nameBox.value;
  nameBox.value="";
  var newTD = document.createElement("td");
  newTD.innerHTML = name + 
    "<input type=\"hidden\" name=\"" + elementId + ".name\" value=\"" + name + "\" /> ";
  newTR.appendChild(newTD);

  if (type != "distinguishedName") {
    var classSelector = document.getElementById(prefix + ".class");
    var class1 = classSelector.options[classSelector.selectedIndex].value;
    newTD = document.createElement("td");
    newTD.innerHTML = class1 + 
      "<input type=\"hidden\" name=\"" + elementId + ".class\" value=\"" + class1 + "\" /> ";
    newTR.appendChild(newTD);

    if (type != "principal") {
      var domainNameBox = document.getElementById(prefix + ".domainName");
      var domainName = domainNameBox.value;
      domainNameBox.value="";
      newTD = document.createElement("td");
      newTD.innerHTML = domainName + 
        "<input type=\"hidden\" name=\"" + elementId + ".domainName\" value=\"" + domainName + "\" /> ";
      newTR.appendChild(newTD);

      if (type != "loginDomainPrincipal") {
        var realmPrincipalSelector = document.getElementById(prefix + ".realmName");
        var realmName = realmPrincipalSelector.options[realmPrincipalSelector.selectedIndex].value;
        newTD = document.createElement("td");
        newTD.innerHTML = realmName + 
          "<input type=\"hidden\" name=\"" + elementId + ".realmName\" value=\"" + realmName + "\" /> ";
        newTR.appendChild(newTD);
      }
    }
  }

  newTD = document.createElement("td");
  newTD.innerHTML = "<a href=\"javascript:;\" " + 
    "onclick=\"<portlet:namespace/>removeElement(\'" + roleId + "\',\'" + type + "\',\'" + newTRId + "\')\">remove</a>";
  newTR.appendChild(newTD);

  tbody.appendChild(newTR);
  var counter = document.getElementById(roleId + "." + type + ".count");
  counter.value = (counter.value - 1) + 2;
  document.getElementById(roleId + ".selectList").selectedIndex = 0;
  <portlet:namespace/>hideElement(roleId + "." + type + ".ui");
}

function <portlet:namespace/>removeElement(roleId, type, trId)
{
  var prefix = "<portlet:namespace/>" + roleId + "." + type;
  var counter = document.getElementById(roleId + "." + type + ".count");
  counter.value--;
  if(counter.value == "0") {
    var lastIndex = document.getElementById(roleId + "." + type + ".lastIndex");
    lastIndex.value = "0";
    var placeHolder = document.getElementById(prefix + ".placeHolder");
    var table = document.getElementById(prefix + ".table");
    placeHolder.removeChild(table);
  } else {
    var tbody = document.getElementById(prefix + ".table.tbody");
    var tr = document.getElementById(trId);
    tbody.removeChild(tr);
  }
}
</script>

<p><fmt:message key="plancreator.security.title"/></p>

<p><fmt:message key="plancreator.security.desc"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>SecurityForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="security-after" />

<table border="0">
  <!-- ENTRY FIELD: Security Realm Name -->
  <tr>
    <th>
    <div align="right"><label for="<portlet:namespace/>securityRealmName"><fmt:message key="plancreator.security.securityRealmName"/></label>:</div>
    </th>
    <td>
      <select name="securityRealmName" id="<portlet:namespace/>securityRealmName">
        <c:forEach var="securityRealm" items="${deployedSecurityRealms}">
          <option value="${securityRealm.realmName}">${securityRealm.realmName}</option>
        </c:forEach>
      </select>
    </td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.security.securityRealmNameSelection"/></td>
  </tr>
</table>

<div>
  <input type="checkbox" id="<portlet:namespace/>advancedSecuritySettingsCheckbox"
    onClick="<portlet:namespace/>toggleAdvancedSecuritySettings();"/>
  <b><label for="<portlet:namespace/>advancedSecuritySettingsCheckbox"><fmt:message key="plancreator.security.advancedSetting"/></label></b>
  <div id="<portlet:namespace/>advancedSecuritySettings" style="display:none">
    <input type="hidden" id="<portlet:namespace/>advancedSecuritySettingsFlag"
      name="security.advancedSettings.isPresent" value="false"/>
    <table border="0" class="MediumBackground">
      <!-- Credential Store Ref -->
      <tr>
        <th><div align="left"><label for="<portlet:namespace/>security.credentialStoreRef"><fmt:message key="plancreator.security.credentialStore"/></label>:</div></th>
        <td>
          <select name="security.credentialStoreRef" id="<portlet:namespace/>security.credentialStoreRef">
            <c:forEach var="credentialStore" items="${deployedCredentialStores}">
              <option value="${credentialStore.patternName}">${credentialStore.displayName}</option>
            </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <th></th>
        <td><fmt:message key="plancreator.security.credentialStoreSelection"/></td>
      </tr>

      <!-- Default Subject -->
      <tr>
        <td colspan="2"><div align="left"><b><fmt:message key="plancreator.security.subject"/>:</b></div></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>security.defaultSubject.realm"><fmt:message key="plancreator.security.realm"/></label>:</div></th>
        <td><input name="security.defaultSubject.realm" id="<portlet:namespace/>security.defaultSubject.realm" type="text" size="25"/></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>security.defaultSubject.id"><fmt:message key="plancreator.common.id"/></label>:</div></th>
        <td><input name="security.defaultSubject.id" id="<portlet:namespace/>security.defaultSubject.id" type="text" size="25"/></td>
      </tr>
      <tr>
        <th></th>
        <td><fmt:message key="plancreator.security.subjectDesc"/></td>
      </tr>

      <!-- doas-current-caller -->
      <tr>
        <td colspan="2" align="left">
          <b><label for="<portlet:namespace/>security.doasCurrentCaller"><fmt:message key="plancreator.security.doAsCurrentCaller"/></label>:</b>
          <input name="security.doasCurrentCaller" id="<portlet:namespace/>security.doasCurrentCaller" type="checkbox" value="true"/>
        </td>
      </tr>
      <tr>
        <th></th>
        <td><fmt:message key="plancreator.security.doAsCurrentCallerDesc"/></td>
      </tr>
      <!-- use-context-handler -->
      <tr>
        <td colspan="2" align="left">
          <b><label for="<portlet:namespace/>security.useContextHandler"><fmt:message key="plancreator.security.useContextHandler"/></label>:</b>
          <input name="security.useContextHandler" id="<portlet:namespace/>security.useContextHandler" type="checkbox" value="true"/>
        </td>
      </tr>
      <tr>
        <th></th>
        <td><fmt:message key="plancreator.security.useContextHandlerDesc"/></td>
      </tr>
    </table>
  </div>
</div>
<br>

<!-- Security Role Mappings -->
<p>
<b><fmt:message key="plancreator.security.roleMapping"/>:</b>
<br><br>
<fmt:message key="plancreator.security.roleMappingDesc"/>
</p>
<table border="0">
  <c:set var="backgroundClass" value='MediumBackground'/>
  <c:forEach var="role" items="${data.security.roleMappings.roleArray}" varStatus="status1">
    <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
        <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
        <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
    </c:choose>
    <c:set var="roleId" value="security.roleMappings.${status1.index}" />
    <input type="hidden" name="${roleId}.roleName" value="${role.roleName}" />
    <tr>
      <th valign="top" class="${backgroundClass}"><div align="right">${role.roleName}:</div></th>
      <td class="${backgroundClass}">
        <div id="<portlet:namespace/>${roleId}.principal.placeHolder"></div>
        <div id="<portlet:namespace/>${roleId}.loginDomainPrincipal.placeHolder"></div>
        <div id="<portlet:namespace/>${roleId}.realmPrincipal.placeHolder"></div>
        <div id="<portlet:namespace/>${roleId}.distinguishedName.placeHolder"></div>
        <div align="justify"><label for="${roleId}.selectList"><fmt:message key="plancreator.common.add"/>:</label> 
        <select id="${roleId}.selectList" 
          onchange="<portlet:namespace/>handleAddClick('${roleId}', this.value)">
          <option value=""></option>
          <option value="principal"><fmt:message key="plancreator.security.principal"/></option>
          <option value="loginDomainPrincipal"><fmt:message key="plancreator.security.loginDomainPrincipal"/></option>
          <option value="realmPrincipal"><fmt:message key="plancreator.security.realmPrincipal"/></option>
          <option value="distinguishedName"><fmt:message key="plancreator.security.distinguishedName"/></option>
        </select></div>

        <div id="<portlet:namespace/>${roleId}.principal.ui" style="display:none">
          <input type="hidden" id="${roleId}.principal.lastIndex" 
            name="${roleId}.principal.lastIndex" value="0">
          <table border="0">
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.principal.name"><fmt:message key="plancreator.common.name"/></label>:</div></th>
              <td><input type="text" id="<portlet:namespace/>${roleId}.principal.name"/></td>
            </tr>
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.principal.class"><fmt:message key="plancreator.common.class"/></label>:</div></th>
              <td>
                <select id="<portlet:namespace/>${roleId}.principal.class">
                  <option value="org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal"><fmt:message key="plancreator.security.groupPrincipal"/></option>
                  <option value="org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal"><fmt:message key="plancreator.security.userPrincipal"/></option>
                </select>
              </td>
            </tr>
            <tr>
              <th><div align="right"></div></th>
              <td>
                <input type="button" value="<fmt:message key="plancreator.common.add"/>" onclick="<portlet:namespace/>addElement('${roleId}', 'principal')"/>
                <input type="button" value="<fmt:message key="plancreator.common.cancel"/>" 
                  onclick="<portlet:namespace/>hideElement('${roleId}.principal.ui');
                  document.getElementById('${roleId}.selectList').selectedIndex = 0;"/>
              </td>
            </tr>
          </table>
        </div>

        <div id="<portlet:namespace/>${roleId}.loginDomainPrincipal.ui" style="display:none">
          <input type="hidden" id="${roleId}.loginDomainPrincipal.lastIndex" 
            name="${roleId}.loginDomainPrincipal.lastIndex" value="0">
          <table border="0">
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.loginDomainPrincipal.name"><fmt:message key="plancreator.common.name"/></label>:</div></th>
              <td><input type="text" id="<portlet:namespace/>${roleId}.loginDomainPrincipal.name"/></td>
            </tr>
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.loginDomainPrincipal.class"><fmt:message key="plancreator.common.class"/></label>:</div></th>
              <td>
                <select id="<portlet:namespace/>${roleId}.loginDomainPrincipal.class">
                  <option value="org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal"><fmt:message key="plancreator.security.groupPrincipal"/></option>
                  <option value="org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal"><fmt:message key="plancreator.security.userPrincipal"/></option>
                </select>
              </td>
            </tr>
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.loginDomainPrincipal.domainName"><fmt:message key="plancreator.security.domainName"/></label>:</div></th>
              <td><input type="text" id="<portlet:namespace/>${roleId}.loginDomainPrincipal.domainName"/></td>
            </tr>
            <tr>
              <th><div align="right"></div></th>
              <td>
                <input type="button" value="<fmt:message key="plancreator.common.add"/>" onclick="<portlet:namespace/>addElement('${roleId}', 'loginDomainPrincipal')"/>
                <input type="button" value="<fmt:message key="plancreator.common.cancel"/>" 
                  onclick="<portlet:namespace/>hideElement('${roleId}.loginDomainPrincipal.ui');
                  document.getElementById('${roleId}.selectList').selectedIndex = 0;"/>
              </td>
            </tr>
          </table>
        </div>

        <div id="<portlet:namespace/>${roleId}.realmPrincipal.ui" style="display:none">
          <input type="hidden" id="${roleId}.realmPrincipal.lastIndex" 
            name="${roleId}.realmPrincipal.lastIndex" value="0">
          <table border="0">
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.realmPrincipal.name"><fmt:message key="plancreator.common.name"/></label>:</div></th>
              <td><input type="text" id="<portlet:namespace/>${roleId}.realmPrincipal.name"/></td>
            </tr>
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.realmPrincipal.class"><fmt:message key="plancreator.common.class"/></label>:</div></th>
              <td>
                <select id="<portlet:namespace/>${roleId}.realmPrincipal.class">
                  <option value="org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal"><fmt:message key="plancreator.security.groupPrincipal"/></option>
                  <option value="org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal"><fmt:message key="plancreator.security.userPrincipal"/></option>
                </select>
              </td>
            </tr>
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.realmPrincipal.domainName"><fmt:message key="plancreator.security.domainName"/></label>:</div></th>
              <td><input type="text" id="<portlet:namespace/>${roleId}.realmPrincipal.domainName"/></td>
            </tr>
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.realmPrincipal.realmName"><fmt:message key="plancreator.security.realmName"/></label>:</div></th>
              <td>
                <select id="<portlet:namespace/>${roleId}.realmPrincipal.realmName">
                  <c:forEach var="securityRealm" items="${deployedSecurityRealms}">
                    <option value="${securityRealm.realmName}">${securityRealm.realmName}</option>
                  </c:forEach>
                </select>
              </td>
            </tr>
            <tr>
              <th><div align="right"></div></th>
              <td>
                <input type="button" value="<fmt:message key="plancreator.common.add"/>" onclick="<portlet:namespace/>addElement('${roleId}', 'realmPrincipal')"/>
                <input type="button" value="<fmt:message key="plancreator.common.cancel"/>" 
                  onclick="<portlet:namespace/>hideElement('${roleId}.realmPrincipal.ui');
                  document.getElementById('${roleId}.selectList').selectedIndex = 0;"/>
              </td>
            </tr>
          </table>
        </div>

        <div id="<portlet:namespace/>${roleId}.distinguishedName.ui" style="display:none">
          <input type="hidden" id="${roleId}.distinguishedName.lastIndex" 
            name="${roleId}.distinguishedName.lastIndex" value="0">
          <table border="0">
            <tr>
              <th><div align="right"><label for="<portlet:namespace/>${roleId}.distinguishedName.name"><fmt:message key="plancreator.common.name"/>/label>:</div></th>
              <td><input type="text" id="<portlet:namespace/>${roleId}.distinguishedName.name"/></td>
            </tr>
            <tr>
              <th><div align="right"></div></th>
              <td>
                <input type="button" value="<fmt:message key="plancreator.common.add"/>" onclick="<portlet:namespace/>addElement('${roleId}', 'distinguishedName')"/>
                <input type="button" value="<fmt:message key="plancreator.common.cancel"/>" 
                  onclick="<portlet:namespace/>hideElement('${roleId}.distinguishedName.ui');
                  document.getElementById('${roleId}.selectList').selectedIndex = 0;"/>
              </td>
            </tr>
          </table>
        </div>

        <c:set var="runAsSubjectId" value="${roleId}.runAsSubject" />
        <div id="<portlet:namespace/>${runAsSubjectId}.ui">
          <input type="checkbox" id="<portlet:namespace/>${runAsSubjectId}.checkBox" disabled="disabled" 
            onClick="<portlet:namespace/>toggleRunAsSubject('${runAsSubjectId}');"/>
          <label for="<portlet:namespace/>${runAsSubjectId}.checkBox"><fmt:message key="plancreator.security.specifyRunAsSubject"/>*</label>
          <div id="<portlet:namespace/>${runAsSubjectId}.subElements" style="display:none">
            <table border="0">
              <tr>
                <th><div align="right"><label for="<portlet:namespace/>${runAsSubjectId}.realm"><fmt:message key="plancreator.security.realm"/></label>:</div></th>
                <td><input name="${runAsSubjectId}.realm" id="<portlet:namespace/>${runAsSubjectId}.realm" type="text" size="25"/></td>
              </tr>
              <tr>
                <th><div align="right"><label for="<portlet:namespace/>${runAsSubjectId}.id">Id</label>:</div></th>
                <td><input name="${runAsSubjectId}.id" id="<portlet:namespace/>${runAsSubjectId}.id" type="text" size="25"/></td>
              </tr>
              <tr>
                <th></th>
                <td><fmt:message key="plancreator.security.runAsSubjectDesc"/></td>
              </tr>
            </table>
          </div>
        </div>
        <br>

      </td>
    </tr>
  </c:forEach>
  <tr>
  <td colspan="2" align="left">* <fmt:message key="plancreator.security.advancedSettingDesc"/></td>
  </tr>
</table>
<br>

<table border="0">
  <!-- SUBMIT BUTTON -->
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
