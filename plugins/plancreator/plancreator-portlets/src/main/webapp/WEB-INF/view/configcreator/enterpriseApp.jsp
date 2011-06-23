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

<style type="text/css">
  @import "/console/dojo/dijit/themes/claro/claro.css";
  @import "/console/dojo/dojo/resources/dojo.css";
  @import "<%=request.getContextPath()%>/enterpriseApp.css";  
</style>

<% String dwrForwarderServlet = "/console/dwr5"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/EARHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script type="text/javascript" src="/console/dojo/dojo/dojo.js" djConfig="parseOnLoad: true"></script>
<script type="text/javascript" src="/console/dojo/dijit/dijit.js"></script>
<script type="text/javascript">
  dojo.require("dojo.parser");
  dojo.require("dijit.layout.TabContainer");
  dojo.require("dijit.layout.ContentPane");
  dojo.require("dijit.layout.SplitContainer");
  dojo.require("dijit.layout.AccordionContainer");
  dojo.require("dojo.data.ItemFileReadStore");
  dojo.require("dojo.data.ItemFileWriteStore");
  dojo.require("dijit.Tree");
  dojo.require("dijit.form.Form");
  dojo.require("dijit.form.ValidationTextBox");
  dojo.require("dijit.form.ComboBox");
  dojo.require("dijit.form.FilteringSelect");
  dojo.require("dijit.form.CheckBox");
  dojo.require("dijit.form.DateTextBox");
  dojo.require("dijit.form.CurrencyTextBox");
  dojo.require("dijit.form.NumberSpinner");
  dojo.require("dijit.form.Slider");
  dojo.require("dijit.form.Textarea");
  dojo.require("dijit.form.TextBox");
  dojo.require("dijit.form.Button");
  dojo.require("dijit.Editor");
  dojo.require("dijit.TitlePane");
  dojo.require("dijit.Dialog");
</script>

<script type='text/javascript' src='<%=request.getContextPath()%>/js/enterpriseApp.js'></script>

<body class="claro">
<table width="750px">
    <tr>
        <td align="left" width="100px"><button dojoType="dijit.form.Button" onClick="doPrevious()">&lt; <fmt:message key="plancreator.common.previous"/></button></td>
        <td align="left"><button dojoType="dijit.form.Button" onClick="doNext()"><fmt:message key="plancreator.common.next"/> &gt;</button></td>
    </tr>
</table>
<div id="mainTabContainer" dojoType="dijit.layout.TabContainer" style="width:750px; height: 700px">
  <div id="environment" dojoType="dijit.layout.ContentPane" title="Environment">
    <form dojoType="dijit.form.Form" id="environmentForm" 
      execute="saveEnvironment(arguments[0]); saveDependencies()"
      onsubmit="return false;">

    <!-- ENTRY FIELD: Module Id -->
    <div dojoType="dijit.TitlePane" title="Module Id" open="true" style="width:100%; margin:0; padding:0;">
      <table border="0">
        <tr>
          <th colspan="2" align="center"> <fmt:message key="plancreator.env.id"/>:</th>
          <td id="moduleIdHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="moduleIdHelp" 
            label="<fmt:message key="plancreator.env.idDesc"/>">
          </div>
        </tr>
        <tr>
          <th><div align="right"><label for="groupId"><fmt:message key="plancreator.env.groupId"/></label>:</div></th>
          <td>
            <input type="text" id="groupId" name="groupId"
              dojoType="dijit.form.ValidationTextBox" required="false" trim="true"/>
          </td>
          <td id="groupIdHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="groupIdHelp" 
            label="<fmt:message key="plancreator.env.groupIdDesc"/>">
          </div>
        </tr>
        <tr>
          <th><div align="right"><label for="artifactId"><fmt:message key="plancreator.env.artifactId"/></label>:</div></th>
          <td>
            <input type="text" id="artifactId" name="artifactId"
              dojoType="dijit.form.ValidationTextBox" required="true" trim="true"/>
          </td>
          <td id="artifactIdHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="artifactIdHelp" 
            label="<fmt:message key="plancreator.env.artifactIdDesc"/>">
          </div>
        </tr>
        <tr>
          <th><div align="right"><label for="version"><fmt:message key="plancreator.env.version"/></label>:</div></th>
          <td>
            <input type="text" id="version" name="version"
              dojoType="dijit.form.ValidationTextBox" required="false" trim="true"/>
          </td>
          <td id="versionHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="versionHelp" 
            label="<fmt:message key="plancreator.env.versionDesc"/>">
          </div>
        </tr>
        <tr>
          <th><div align="right"><label for="type"><fmt:message key="plancreator.env.type"/></label>:</div></th>
          <td>
            <select dojoType="dijit.form.ComboBox" id="type" name="type" 
                value="" autocomplete="false" hasDownArrow="false">
              <option></option>
              <option>CAR</option>
              <option>EAR</option>
              <option>WAR</option>
              <option>JAR</option>
            </select>
          </td>
          <td id="typeHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="typeHelp" 
            label="<fmt:message key="plancreator.env.typeDesc"/>">
          </div>
        </tr>
      </table>
    </div>

    <!-- ENTRY FIELD: Hidden Classes, Non Overridable Classes and Inverse Class Loading -->
    <div dojoType="dijit.TitlePane" title="Class Path Settings" open="false" 
        style="width:100%; margin:0; padding:0;">
      <table border="0">
        <tr>
          <th colspan="2" align="center"> <fmt:message key="plancreator.env.classpathSetting"/>:</th>
          <td></td>
        </tr>
        <tr>
          <th><div align="right"><label for="hiddenClasses"><fmt:message key="plancreator.env.hiddenClasses"/></label>:</div></th>
          <td>
            <input type="text" id="hiddenClasses" name="hiddenClasses"
              dojoType="dijit.form.ValidationTextBox" trim="true"/>
          </td>
          <td id="hiddenClassesHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="hiddenClassesHelp" 
            label="<fmt:message key="plancreator.env.hiddenClassesDesc"/>">
          </div>
        </tr>
        <tr>
          <th><div align="right"><label for="nonOverridableClasses"><fmt:message key="plancreator.env.nonOverridableClass"/></label>:</div></th>
          <td>
            <input type="text" id="nonOverridableClasses" name="nonOverridableClasses" 
              dojoType="dijit.form.ValidationTextBox" trim="true"/>
          </td>
          <td id="nonOverridableClassesHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="nonOverridableClassesHelp" 
            label="<fmt:message key="plancreator.env.nonOverridableClassDesc"/>">
          </div>
        </tr>
        <tr>
          <th><div align="right"><label for="inverseClassLoading"><fmt:message key="plancreator.env.inverseClassLoading"/></label>:</div></th>
          <td>
            <input type="checkBox" name="inverseClassLoading" id="inverseClassLoading" value="true" 
              dojoType="dijit.form.CheckBox" />
          </td>
          <td id="inverseClassLoadingHelp"><a class="helpIcon">&nbsp;</a></td>
          <div dojoType="dijit.Tooltip" connectId="inverseClassLoadingHelp" 
            label="<fmt:message key="plancreator.env.inverseClassLoadingDesc"/>">
          </div>
        </tr>
      </table>
    </div>

    <!-- Dependencies -->
    <div dojoType="dijit.TitlePane" title="Dependencies" open="false" 
        style="width:100%; margin:0; padding:0;">
      <table border="0" width="720px">
        <tr>
          <th><fmt:message key="plancreator.dependency.dependency"/>:</th><th><a id="dependenciesHelp" class="helpIcon">&nbsp;</a>
          <div dojoType="dijit.Tooltip" connectId="dependenciesHelp"
            label="<fmt:message key="plancreator.dependency.desc"/>">
          </div>
          </th>
        </tr>
        <tr>
          <td valign="top">
            <div id="dependenciesTree"><fmt:message key="plancreator.dependency.tree"/></div>
          </td>
          <td align="right" valign="top">
            <button dojoType="dijit.form.Button" id="btnAdd" onclick="doAddDependencies()">
              <div style="width:58px"><fmt:message key="plancreator.common.add"/></div>
            </button><br/>
            <button dojoType="dijit.form.Button" onclick="doEditDependency()">
              <div style="width:58px"><fmt:message key="plancreator.common.edit"/></div>
            </button><br/>
            <button dojoType="dijit.form.Button" onclick="doDeleteDependency()">
              <div style="width:58px"><fmt:message key="plancreator.common.del"/></div>
            </button><br/>
          </td>
        </tr>
      </table>
    </div>
    </form>
  </div>
  <div id="references" dojoType="dijit.layout.ContentPane" title="References">
    <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="7"
        activeSizing="true" style="border: 1px solid #bfbfbf;">
      <div dojoType="dijit.layout.ContentPane" sizeMin="100" sizeShare="20">
        <div id="referencesTreeHolder"><fmt:message key="plancreator.reference.tree"/></div>
      </div>
      <div dojoType="dijit.layout.ContentPane" sizeMin="200" sizeShare="80">
        <fmt:message key="plancreator.reference.editor"/>
      </div>
    </div>
  </div>
  <!-- Security -->
  <div id="security" dojoType="dijit.layout.ContentPane" title="Security">
    <div dojoType="dijit.layout.AccordionContainer" id="securityAccordionContainer" duration="200">
        <c:forEach var="webModule" items="${data.webModules}"> 
            <c:set var="moduleName" value="${webModule.key}"/>                       
                <div dojoType="dijit.layout.AccordionPane" selected="true" title="${webModule.key}" id="${webModule.key}">
                    <form dojoType="dijit.form.Form" id="${moduleName}.form" 
                    execute="saveSecurity(arguments[0]);" onsubmit="return false;">
                        <table width="720px" cellspacing="15px">
                            <tr>
                                <td colspan="2"><label for="${moduleName}.form.txtSecurityRealmName"><fmt:message key="plancreator.security.realmName"/></label>:
                                    <select dojoType="dijit.form.FilteringSelect" name="securityRealmName" id="${moduleName}.form.txtSecurityRealmName" value="">
                                        <c:forEach var="securityRealm" items="${deployedSecurityRealms}"><option value="${securityRealm.realmName}">${securityRealm.realmName}</option></c:forEach>
                                    </select>
                                </td>                    
                            </tr>
                        </table>
                        <div dojoType="dijit.TitlePane" title="Role Mappings" open="true" style="margin:0px 10px; padding:0;">
                            <table width="700px">
                                <tr>
                                    <td valign="top">
                                        <div id="${moduleName}.form.securityTree"><fmt:message key="plancreator.security.roleTree"/></div>
                                    </td>
                                    <td align="right" valign="top">
                                        <button dojoType="dijit.form.Button" id="${moduleName}.form.btnAdd" onclick="doAddOrEditRoleMapping('${moduleName}.form.securityTree',true)" disabled="true">
                                          <div style="width:58px"><fmt:message key="plancreator.common.add"/></div>
                                        </button><br/>
                                        <button dojoType="dijit.form.Button" id="${moduleName}.form.btnEdit" onclick="doAddOrEditRoleMapping('${moduleName}.form.securityTree',false)" disabled="true">
                                          <div style="width:58px"><fmt:message key="plancreator.common.edit"/></div>
                                        </button><br/>
                                        <button dojoType="dijit.form.Button" id="${moduleName}.form.btnDelete" onclick="doDeleteRoleMapping('${moduleName}.form.securityTree')" disabled="true">
                                          <div style="width:58px"><fmt:message key="plancreator.common.del"/></div>
                                        </button><br/>
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div dojoType="dijit.TitlePane" title="Advanced Settings" open="false" style="margin:0px 10px; padding:0;">
                            <table width="700px" cellspacing="10px">
                                <tr>
                                    <td><b><label for="${moduleName}.form.selCredentialStore"><fmt:message key="plancreator.security.credentialStore"/></label>:</b></td>
                                    <td>
                                        <select name="credentialStoreRef" dojoType="dijit.form.FilteringSelect" id="${moduleName}.form.selCredentialStore" style="width:520px" value="">
                                        <option value=""></option>
                                        <c:forEach var="credentialStore" items="${deployedCredentialStores}"><option value="${credentialStore.patternName}">${credentialStore.displayName}</option></c:forEach>
                                        </select>
                                     </td>
                                </tr>
                                <tr><td colspan="2" align="left"><b><fmt:message key="plancreator.security.subject"/>:</b></td></tr>
                                <tr>
                                    <td align="right" width="120px"><label for="defaultSubjectRealm"><fmt:message key="plancreator.security.realm"/></label>:</td>
                                    <td><input name="defaultSubjectRealm" id="defaultSubjectRealm" dojotype="dijit.form.TextBox" type="text" size="25"/></td>
                                </tr>
                                <tr>
                                    <td align="right"><label for="defaultSubjectId"><fmt:message key="plancreator.common.id"/></label>:</td>
                                    <td><input name="defaultSubjectId" id="defaultSubjectId" dojotype="dijit.form.TextBox" type="text" size="25"/></td>
                                </tr>
                            </table>
                            <table cellspacing="10px">
                                <tr>
                                    <td align="right"><input name="doasCurrentCaller" id="doasCurrentCaller" dojotype="dijit.form.CheckBox" type="checkbox" value="true"/></td>                                    
                                    <td><label for="doasCurrentCaller"><fmt:message key="plancreator.security.doAsCurrentCaller"/></label></td>
                                </tr>
                                <tr>
                                    <td align="right"><input name="useContextHandler" id="useContextHandler" dojotype="dijit.form.CheckBox" type="checkbox" value="true"/></td>                                    
                                    <td><label for="useContextHandler"><fmt:message key="plancreator.security.useContextHandler"/></label></td>
                                </tr>
                            </table>
                            <table width="700px">
                                <tr>
                                    <td valign="top">
                                        <div id="${moduleName}.form.runAsSubjectsTree"><fmt:message key="plancreator.security.runAsSubjectsTree"/></div>
                                    </td>
                                    <td align="right" valign="top">
                                        <button dojoType="dijit.form.Button" id="${moduleName}.form.btnAddRunAsSubject" onclick="doAddOrEditRunAsSubject('${moduleName}.form.runAsSubjectsTree',true)" disabled="true">
                                          <div style="width:58px"><fmt:message key="plancreator.common.add"/></div>
                                        </button><br/>
                                        <button dojoType="dijit.form.Button" id="${moduleName}.form.btnEditRunAsSubject" onclick="doAddOrEditRunAsSubject('${moduleName}.form.runAsSubjectsTree',false)" disabled="true">
                                          <div style="width:58px"><fmt:message key="plancreator.common.edit"/></div>
                                        </button><br/>
                                        <button dojoType="dijit.form.Button" id="${moduleName}.form.btnDeleteRunAsSubject" onclick="doDeleteRunAsSubject('${moduleName}.form.runAsSubjectsTree')" disabled="true">
                                          <div style="width:58px"><fmt:message key="plancreator.common.del"/></div>
                                        </button><br/>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </form>
                </div>
            <c:remove var="moduleName"/>
        </c:forEach>
    </div>
  </div>
  <div id="generatedPlan" dojoType="dijit.layout.ContentPane" title="Generated Plan">
    <pre id="generatedPlanDisplayer" class="dijitTextarea" contentEditable="true" style="padding:0px; width:744px; height: 668px; font-family: monospace">${data.deploymentPlan}</pre>
  </div>
</div>
<div id="dependenciesDialog" dojotype="dijit.Dialog"
    title="Select dependencies to add" execute="addDependencies(arguments[0])" extractContent="false">
  <div style="height:400px; width:700px; overflow:auto">
    <table width="100%" cellspacing="0">
      <c:forEach var="commonLib" items="${commonLibs}" varStatus="vs">
        <tr>
          <td>
            <input dojotype="dijit.form.CheckBox" id="depChkBox_${vs.index}" 
            name="dependencies" value="${commonLib}" type="checkbox"/>
          </td>
          <td valign="middle" align="left">
            <label for="depChkBox_${vs.index}">${commonLib}</label>
          </td>
        </tr>
      </c:forEach>
    </table>
  </div>
  <center><button dojoType="dijit.form.Button" type="submit">OK</button></center>
</div>
<!-- 
    Dependency Dialog 
-->
<div id="editDependencyDialog" dojotype="dijit.Dialog" 
    title="Edit dependency" execute="editDependencyTo(arguments[0])" extractContent="false">
  <input type="hidden" name="prevName" id="depEditPrevName" value="none"/>
  <table cellspacing="15">
    <tr>
      <td><label for="txtGroupId"><fmt:message key="plancreator.env.groupId"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.TextBox" id="txtGroupId" size="20" name="groupId" trim="true"/>
      </td>
    </tr>
    <tr>
      <td><label for="txtArtifactId"><fmt:message key="plancreator.env.artifactId"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.TextBox" id="txtArtifactId" size="20" 
          name="artifactId" trim="true" onChange="checkDepEditFields()"/>
      </td>
    </tr>
    <tr>
      <td><label for="txtVersion"><fmt:message key="plancreator.env.version"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.TextBox" id="txtVersion" size="20" name="version" trim="true"/>
      </td>
    </tr>
    <tr>
      <td><label for="txtType"><fmt:message key="plancreator.env.type"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.TextBox" id="txtType" size="20" name="type" trim="true"/>
      </td>
    </tr>
    <tr><td colspan="2"><div id="depEditStatus"></div></td></tr>
  </table>
  <center><button dojoType="dijit.form.Button" type="submit" id="btnDepEditOK">OK</button></center>
</div>
<!-- 
    Role Mapping Dialog 
-->
<div id="roleMappingDialog" dojotype="dijit.Dialog" 
    title="Role Mapping" execute="addOrEditRoleMapping(arguments[0])" extractContent="false">
  <table cellspacing="15">
    <tr>
      <td width="100px"><label for="selRoleMappingType"><fmt:message key="plancreator.common.type"/></label>:</td>
      <td>
        <select dojoType="dijit.form.FilteringSelect" name="type" id="selRoleMappingType" 
        onchange="modifyRoleMappingForm(this.getValue())" style="width:200px">
            <option value="Principal"><fmt:message key="plancreator.security.principal"/></option>
            <option value="LoginDomainPrincipal"><fmt:message key="plancreator.security.loginDomainPrincipal"/></option>
            <option value="RealmPrincipal"><fmt:message key="plancreator.security.realmPrincipal"/></option>
            <option value="DistinguishedName"><fmt:message key="plancreator.security.distinguishedName"/></option>
        </select>
      </td>
    </tr>
    <tr>
      <td><label for="txtRoleMappingName"><fmt:message key="plancreator.common.name"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.ValidationTextBox" name="principalName" required="true" style="width:200px"
        id="txtRoleMappingName" trim="true" validator="validatePrincipalName" onBlur="checkRoleMappingFields"
        invalidMessage="<fmt:message key="plancreator.security.roleMappingErr"/>"/>
      </td>
    </tr>
    <tr>
      <td><label for="selRoleMappingClass"><fmt:message key="plancreator.common.class"/></label>:</td>
      <td>
        <select dojoType="dijit.form.FilteringSelect" name="className" id="selRoleMappingClass" style="width:200px"
         onBlur="checkRoleMappingFields">
            <option value="org.apache.geronimo.security.realm.providers.GeronimoGroupPrincipal"><fmt:message key="plancreator.security.groupPrincipal"/></option>
            <option value="org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal"><fmt:message key="plancreator.security.userPrincipal"/></option>
        </select>
      </td>
    </tr>
    <tr style="display:none">
      <td><label for="txtRoleMappingDomainName"><fmt:message key="plancreator.security.domainName"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.ValidationTextBox" name="domainName" style="width:200px" disabled="true"
        id="txtRoleMappingDomainName" trim="true" required="true" onBlur="checkRoleMappingFields"
        invalidMessage="<fmt:message key="plancreator.security.domainNameErr"/>"/>
      </td>
    </tr>
    <tr style="display:none">
      <td><label for="selRoleMappingRealmName"><fmt:message key="plancreator.security.realmName"/></label>:</td>
      <td>
        <select dojoType="dijit.form.FilteringSelect" name="realmName" style="width:200px" id="selRoleMappingRealmName" disabled="true">
            <c:forEach var="securityRealm" items="${deployedSecurityRealms}"><option value="${securityRealm.realmName}">${securityRealm.realmName}</option></c:forEach>
        </select>
      </td>
    </tr>
  </table>
  <center><button dojoType="dijit.form.Button" type="submit" id="btnRoleMappingOK">OK</button></center>
</div>
<!-- 
    Run-as-subject Dialog 
-->
<div id="runAsSubjectDialog" dojotype="dijit.Dialog" 
    title="Run-as-subject" execute="addOrEditRunAsSubject(arguments[0])" extractContent="false">
  <table cellspacing="15">
    <tr id="selRunAsSubjectRoleRow">
      <td align="right"><label for="selRunAsSubjectRole"><fmt:message key="plancreator.security.role"/></label>:</td>
      <td>
        <select dojoType="dijit.form.FilteringSelect" name="name" id="selRunAsSubjectRole" onBlur="checkRunAsSubjectFields">
        </select>
      </td>
    </tr>
    <tr>
      <td align="right"><label for="realm"><fmt:message key="plancreator.security.realm"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.ValidationTextBox" id="realm" name="realm" required="true" style="width:200px" trim="true" onBlur="checkRunAsSubjectFields"/>
      </td>
    </tr>
    <tr>
      <td align="right"><label for="id"><fmt:message key="plancreator.common.id"/></label>:</td>
      <td>
        <input type="text" dojoType="dijit.form.ValidationTextBox" id="id" name="id" required="true" style="width:200px" trim="true" onBlur="checkRunAsSubjectFields"/>
      </td>
    </tr>
  </table>
  <center><button dojoType="dijit.form.Button" type="submit" id="btnRunAsSubjectOK" disabled="true">OK</button></center>
</div>
</body>
