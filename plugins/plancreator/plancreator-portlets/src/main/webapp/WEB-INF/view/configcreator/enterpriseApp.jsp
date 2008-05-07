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
<portlet:defineObjects />

<style type="text/css">
  @import "/dojo/dijit/themes/tundra/tundra.css";
  @import "/dojo/dojo/resources/dojo.css"
</style>

<script type="text/javascript" src="/dojo/dojo/dojo.js" djConfig="parseOnLoad: true"></script>
<script type="text/javascript">
  dojo.require("dojo.parser");
  dojo.require("dijit.layout.TabContainer");
  dojo.require("dijit.layout.ContentPane");
  dojo.require("dijit.layout.SplitContainer");
  dojo.require("dojo.data.ItemFileReadStore");
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
  dojo.require("dijit.form.Button");
  dojo.require("dijit.Editor");
  dojo.require("dijit.TitlePane");
</script>

<% String dwrForwarderServlet = "/console/dwr5"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/EARHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script type="text/javascript" charset="utf-8">
  function updateEarTreeStore(earTree) {
    //alert("earTree received from server: " + dojo.toJson(earTree));
    var earTreeStore = new dojo.data.ItemFileReadStore({data: earTree});
    new dijit.Tree({id: 'referencesTree', store: earTreeStore, label: 'EAR'}, dojo.byId("referencesTreeHolder"));
    new dijit.Tree({id: 'securityTree', store: earTreeStore, label: 'EAR'}, dojo.byId("securityTreeHolder"));
  }

  function updateDependenciesTree(dependenciesJson) {
    var dependenciesStore = new dojo.data.ItemFileReadStore({data: dependenciesJson});
    var placeHolder = dojo.byId("dependenciesTree");
    <ul dojoType="dijit.Menu" id="tree_menu" style="display: none;">
      <li dojoType="dijit.MenuItem" onClick="alert('Hello world');">Delete</li>
    </ul>
    new dijit.Tree({id: 'dependenciesTree', store: dependenciesStore, label: 'Dependencies'}, placeHolder);
  }

  function populateEnvironment(envJson) {
    var envForm = dijit.byId("environmentForm");
    envForm.setValues(envJson);
  }

  function saveEnvironment(envJson) {
    EARHelper.saveEnvironmentJson(envJson, saveEnvironmentCallBack);
  }

  function saveEnvironmentCallBack() {
    EARHelper.getGeneratedPlan(function(plan) {
      alert(plan);
    });
  }

  function addNewDependency(value) {
    alert("Dependency to be added: " + value);
  }

  dojo.addOnLoad(function(){
    EARHelper.getEnvironmentJson(populateEnvironment);
    EARHelper.getDependenciesJsonTree(updateDependenciesTree);
    EARHelper.getEarTree(updateEarTreeStore);
  });
</script>

<div class="tundra"> 
<div id="mainTabContainer" dojoType="dijit.layout.TabContainer" 
    style="width:750px;height:1000px">
  <div id="environment" dojoType="dijit.layout.ContentPane" title="Environment">
    <form dojoType="dijit.form.Form" id="environmentForm" 
      execute=" alert('Execute form w/values:\n'+dojo.toJson(arguments[0],true)); 
      saveEnvironment(arguments[0]);">

    <!-- ENTRY FIELD: Module Id -->
    <div dojoType="dijit.TitlePane" title="Module Id" open="true" style="width:500px; margin:0; padding:0;">
      <table border="0">
        <tr>
          <th colspan="2" align="center"> Module Id:</th>
          <td id="moduleIdHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="moduleIdHelp" 
            label="Every module in Geronimo is uniquely identified by it's ModuleID which consists of four 
              components: groupId/artifactId/version/type. 
              Example: 'org.apache.geronimo.plugins/plancreator-tomcat/2.1/car'.">
          </div>
        </tr>
        <tr>
          <th><div align="right">Group Id:</div></th>
          <td>
            <input type="text" id="groupId" name="groupId"
              dojoType="dijit.form.ValidationTextBox" required="true" trim="true"/>
          </td>
          <td id="groupIdHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="groupIdHelp" 
            label="A name identifying a group of related modules. This may be a project name, a company name, etc. 
              The important thing is that each artifactID should be unique within the group.">
          </div>
        </tr>
        <tr>
          <th><div align="right">Artifact Id:</div></th>
          <td>
            <input type="text" id="artifactId" name="artifactId"
              dojoType="dijit.form.ValidationTextBox" required="true" trim="true"/>
          </td>
          <td id="artifactIdHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="artifactIdHelp" 
            label="A name identifying the specific module within the group.">
          </div>
        </tr>
        <tr>
          <th><div align="right">Version:</div></th>
          <td>
            <input type="text" id="version" name="version"
              dojoType="dijit.form.ValidationTextBox" required="true" trim="true"/>
          </td>
          <td id="versionHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="versionHelp" 
            label="Version number for the module.">
          </div>
        </tr>
        <tr>
          <th><div align="right">Type:</div></th>
          <td>
            <select dojoType="dijit.form.ComboBox" id="type" name="type" 
                value="" autocomplete="true" hasDownArrow="false">
              <option></option>
              <option>CAR</option>
              <option>EAR</option>
              <option>WAR</option>
              <option>JAR</option>
            </select>
          </td>
          <td id="typeHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="typeHelp" 
            label="A module's type is normally either CAR (for a system module) or the file extension for an 
              application module (ear,war,jar,etc).">
          </div>
        </tr>
      </table>
    </div>

    <!-- ENTRY FIELD: Hidden Classes, Non Overridable Classes and Inverse Class Loading -->
    <div dojoType="dijit.TitlePane" title="Class Path Settings" open="false" 
        style="width:500px; margin:0; padding:0;">
      <table border="0">
        <tr>
          <th colspan="2" align="center"> Classpath Settings:</th>
          <td></td>
        </tr>
        <tr>
          <th><div align="right">Hidden Classes:</div></th>
          <td>
            <input type="text" id="hiddenClasses" name="hiddenClasses"
              dojoType="dijit.form.ValidationTextBox" trim="true"/>
          </td>
          <td id="hiddenClassesHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="hiddenClassesHelp" 
            label="List packages or classes that may be in a parent class loader, but should not be exposed 
              from there to the Web application. This is typically used when the Web application wants to use a 
              different version of a library than that of it's parent configuration (or Geronimo itself) uses. 
              Separate multiple package/class names with a semicolon ';'">
          </div>
        </tr>
        <tr>
          <th><div align="right">Non Overridable Classes:</div></th>
          <td>
            <input type="text" id="nonOverridableClasses" name="nonOverridableClasses" 
              dojoType="dijit.form.ValidationTextBox" trim="true"/>
          </td>
          <td id="nonOverridableClassesHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="nonOverridableClassesHelp" 
            label="List packages or classes that the Web application should always load from a parent class 
              loader, and never load from WEB-INF/lib or WEB-INF/classes. This might be used to force a Web 
              application to share the same instance of a common library with other Web applications, even if 
              they each include it in their own WAR. Separate multiple package/class names with a semicolon ';'">
          </div>
        </tr>
        <tr>
          <th><div align="right">Inverse Class Loading:</div></th>
          <td>
            <input type="checkBox" name="inverseClassLoading" id="inverseClassLoading" value="true" 
              dojoType="dijit.form.CheckBox" />
          </td>
          <td id="inverseClassLoadingHelp">?</td>
          <div dojoType="dijit.Tooltip" connectId="inverseClassLoadingHelp" 
            label="Normally (if this element is not checked), the module's class loader will work normally - 
              classes will be loaded from the parent class loader if available before checking the current class 
              loader. If this element is checked, that behavior is reversed and the current class loader will 
              always be checked first before looking in the parent class loader. This is often enabled to give 
              the JARs in WEB-INF/lib precedence over anything that might be in a parent class loader.">
          </div>
        </tr>
      </table>
    </div>

    <!-- Dependencies -->
    <div dojoType="dijit.TitlePane" title="Dependencies" open="false" 
        style="width:500px; margin:0; padding:0;">
      <table border="0">
        <tr>
          <th colspan="2" align="center"> Dependencies: <i id="dependenciesHelp">?</i></th>
          <div dojoType="dijit.Tooltip" connectId="dependenciesHelp"
            label="All the modules available in the server repository are shown below. Select the ones on which 
            your web-application is dependent. Default selections should be sufficient in most scenarios.">
          </div>
        </tr>
        <tr>
          <td colspan="2">
            <div id=dependenciesTree>Dependencies tree goes here</div>
          </td>
          <td></td>
        </tr>
        <tr>
          <td>Add Dependency:</td>
          <td>
            <select dojoType="dijit.form.FilteringSelect" autocomplete="true" value=" " 
                onChange="addNewDependency">
              <option value=" "> </option>
              <c:forEach var="commonLib" items="${commonLibs}">
                <option value="${commonLib}">${commonLib}</option>
              </c:forEach>
            </select>
          </td>
        </tr>
      </table>
    </div>

      <CENTER>
        <!-- Save button -->
        <button dojoType="dijit.form.Button" iconClass="dijitEditorIcon dijitEditorIconSave" type="submit">
          Save
        </button>
      </CENTER>
    </form>
  </div>

  <div id="references" dojoType="dijit.layout.ContentPane" title="References">
    <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="7"
        activeSizing="true" style="border: 1px solid #bfbfbf;">
      <div dojoType="dijit.layout.ContentPane" sizeMin="100" sizeShare="20">
        <div id=referencesTreeHolder>EAR tree goes here</div>
      </div>
      <div dojoType="dijit.layout.ContentPane" sizeMin="200" sizeShare="80">
        Editors for references go here
      </div>
    </div>
  </div>
  <div id="security" dojoType="dijit.layout.ContentPane" title="Security">
    <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="7"
        activeSizing="true" style="border: 1px solid #bfbfbf;">
      <div dojoType="dijit.layout.ContentPane" sizeMin="100" sizeShare="20">
        <div id=securityTreeHolder>EAR tree goes here</div>
      </div>
      <div dojoType="dijit.layout.ContentPane" sizeMin="200" sizeShare="80">
        Editors for security go here
      </div>
    </div>
  </div>
  <div id="generatedPlan" dojoType="dijit.layout.ContentPane" title="Generated Plan">
    <textarea rows="30" cols="85" name="deploymentPlan">${data.deploymentPlan}</textarea>
    <!-- <textarea id="generatedPlanDisplayer" dojoType="dijit.form.Textarea" style="width:600px">${data.deploymentPlan}</textarea> -->
  </div>
</div>
</div>
