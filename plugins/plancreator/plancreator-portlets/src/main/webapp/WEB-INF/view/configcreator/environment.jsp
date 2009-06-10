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

<p><fmt:message key="plancreator.env.title"/></p>

<p><fmt:message key="plancreator.env.desc"/></p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>EnvironmentForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="environment-after" />

<table border="0">
  <!-- SUBMIT BUTTON -->
  <tr>
    <td></td>
    <td><input type="submit" value="<fmt:message key="plancreator.common.next"/>" /></td>
  </tr>

  <!-- ENTRY FIELD: Context Root -->
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>contextRoot"><fmt:message key="plancreator.env.context"/></label>:</div></th>
    <td><input name="contextRoot" id="<portlet:namespace/>contextRoot" type="text" size="25" value="${data.webApp.contextRoot}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.contextDisc"/></td>
  </tr>

  <!-- ENTRY FIELD: Module Id -->
  <tr>
    <th colspan="2"><fmt:message key="plancreator.env.id"/></th>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.idDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>groupId"><fmt:message key="plancreator.env.groupId"/></label>:</div></th>
    <td><input name="groupId" id="<portlet:namespace/>groupId" type="text" size="25" value="${data.webApp.environment.moduleId.groupId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.groupIdDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>artifactId"><fmt:message key="plancreator.env.artifactId"/></label>:</div></th>
    <td><input name="artifactId" id="<portlet:namespace/>artifactId" type="text" size="25" value="${data.webApp.environment.moduleId.artifactId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.artifactIdDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>version"><fmt:message key="plancreator.env.version"/></label>:</div></th>
    <td><input name="version" id="<portlet:namespace/>version" type="text" size="25" value="${data.webApp.environment.moduleId.version}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.versionDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>type"><fmt:message key="plancreator.env.type"/></label>:</div></th>
    <td><input name="type" id="<portlet:namespace/>type" type="text" size="25" value="${data.webApp.environment.moduleId.type}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.typeDesc"/></td>
  </tr>

  <!-- ENTRY FIELD: Hidden Classes, Non Overridable Classes and Inverse Class Loading -->
  <tr>
    <th colspan="2"><fmt:message key="plancreator.env.classpathSetting"/></th>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>hiddenClasses"><fmt:message key="plancreator.env.hiddenClasses"/></label>:</div></th>
    <td><input name="hiddenClasses" id="<portlet:namespace/>hiddenClasses" type="text" size="25" value="${data.environmentConfig.hiddenClassesString}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.hiddenClassesDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>nonOverridableClasses"><fmt:message key="plancreator.env.nonOverridableClass"/></label>:</div></th>
    <td><input name="nonOverridableClasses" id="<portlet:namespace/>nonOverridableClasses" type="text" size="25" value="${data.environmentConfig.nonOverridableClassesString}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.nonOverridableClassDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><label for="<portlet:namespace/>inverseClassLoading"><fmt:message key="plancreator.env.inverseClassLoading"/></label>:</div></th>
    <td><input name="inverseClassLoading" id="<portlet:namespace/>inverseClassLoading" type="checkbox" value="true" 
      <c:if test="${data.environmentConfig.inverseClassLoading}"><fmt:message key="plancreator.env.checked"/> </c:if> /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.inverseClassLoadingDesc"/></td>
  </tr>

  <!-- SUBMIT BUTTON -->
  <tr>
    <td></td>
    <td><input type="submit" value="<fmt:message key="plancreator.common.next"/>" /></td>
  </tr>
</table>

</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="plancreator.common.cancel"/></a></p>
