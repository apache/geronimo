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

<p><b>WAR - Environment</b> -- Configure Web Application Identity and Class Path</p>

<p>Defaults in this page should suffice for typical scenarios.</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>EnvironmentForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="environment-after" />

<table border="0">
  <!-- SUBMIT BUTTON -->
  <tr>
    <td></td>
    <td><input type="submit" value="Next" /></td>
  </tr>

  <!-- ENTRY FIELD: Context Root -->
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>contextRoot">Web Context Root</label>:</div></th>
    <td><input name="contextRoot" id="<portlet:namespace/>contextRoot" type="text" size="25" value="${data.webApp.contextRoot}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>This is the first part of the URL used to access the Web application by the client.
    For example, if the context-root is entered as "HelloWorld", then a typical URL to the application would 
    start with "http://host:port/HelloWorld/".</td>
  </tr>

  <!-- ENTRY FIELD: Module Id -->
  <tr>
    <th colspan="2">Web Application Identity</th>
  </tr>
  <tr>
    <td></td>
    <td>Every module in Geronimo is uniquely identified by it's ModuleID which consists of four components: 
    groupId/artifactId/version/type. Example: "org.apache.geronimo.plugins/plancreator-tomcat/2.1/car".</td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>groupId">Group Id</label>:</div></th>
    <td><input name="groupId" id="<portlet:namespace/>groupId" type="text" size="25" value="${data.webApp.environment.moduleId.groupId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>A name identifying a group of related modules. This may be a project name, a company name, etc. 
    The important thing is that each artifactID should be unique within the group.</td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>artifactId">Artifact Id</label>:</div></th>
    <td><input name="artifactId" id="<portlet:namespace/>artifactId" type="text" size="25" value="${data.webApp.environment.moduleId.artifactId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>A name identifying the specific module within the group.</td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>version">Version</label>:</div></th>
    <td><input name="version" id="<portlet:namespace/>version" type="text" size="25" value="${data.webApp.environment.moduleId.version}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Version number for the module.</td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>type">Type</label>:</div></th>
    <td><input name="type" id="<portlet:namespace/>type" type="text" size="25" value="${data.webApp.environment.moduleId.type}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>A module's type is normally either CAR (for a system module) or the file extension for an application 
    module (ear,war,jar,etc).</td>
  </tr>

  <!-- ENTRY FIELD: Hidden Classes, Non Overridable Classes and Inverse Class Loading -->
  <tr>
    <th colspan="2">Class Path Settings</th>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>hiddenClasses">Hidden Classes</label>:</div></th>
    <td><input name="hiddenClasses" id="<portlet:namespace/>hiddenClasses" type="text" size="25" value="${data.environmentConfig.hiddenClassesString}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>List packages or classes that may be in a parent class loader, but should not be exposed from there to 
    the Web application. This is typically used when the Web application wants to use a different version of a 
    library than that of it's parent configuration (or Geronimo itself) uses.
    Separate multiple package/class names with a semicolon ';'</td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>nonOverridableClasses">Non Overridable Classes</label>:</div></th>
    <td><input name="nonOverridableClasses" id="<portlet:namespace/>nonOverridableClasses" type="text" size="25" value="${data.environmentConfig.nonOverridableClassesString}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>List packages or classes that the Web application should always load from a parent class loader, and 
    never load from WEB-INF/lib or WEB-INF/classes. This might be used to force a Web application to share the 
    same instance of a common library with other Web applications, even if they each include it in their own WAR. 
    Separate multiple package/class names with a semicolon ';'</td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>inverseClassLoading">Inverse Class Loading</label>:</div></th>
    <td><input name="inverseClassLoading" id="<portlet:namespace/>inverseClassLoading" type="checkbox" value="true" 
      <c:if test="${data.environmentConfig.inverseClassLoading}">CHECKED </c:if> /></td>
  </tr>
  <tr>
    <td></td>
    <td>Normally (if this element is not checked), the module's class loader will work normally - classes will be 
    loaded from the parent class loader if available before checking the current class loader. If this element is 
    checked, that behavior is reversed and the current class loader will always be checked first before looking 
    in the parent class loader. This is often enabled to give the JARs in WEB-INF/lib precedence over anything 
    that might be in a parent class loader. </td>
  </tr>

  <!-- SUBMIT BUTTON -->
  <tr>
    <td></td>
    <td><input type="submit" value="Next" /></td>
  </tr>
</table>

</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
