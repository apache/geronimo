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
    <td><input type="submit" value='<fmt:message key="plancreator.common.next"/>' /></td>
  </tr>

  <!-- ENTRY FIELD: Context Root -->
  <tr>
    <th><div align="right"><fmt:message key="plancreator.env.context"/>:</div></th>
    <td><input name="contextRoot" type="text" size="25" value="${data.contextRoot}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.contextDesc"/></td>
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
    <th><div align="right"><fmt:message key="plancreator.env.groupId"/>:</div></th>
    <td><input name="groupId" type="text" size="25" value="${data.groupId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.groupIdDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><fmt:message key="plancreator.env.artifactId"/>:</div></th>
    <td><input name="artifactId" type="text" size="25" value="${data.artifactId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.artifactIdDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><fmt:message key="plancreator.env.version"/>:</div></th>
    <td><input name="version" type="text" size="25" value="${data.version}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.versionDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><fmt:message key="plancreator.env.type"/>:</div></th>
    <td><input name="type" type="text" size="25" value="${data.type}" /></td>
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
    <th><div align="right"><fmt:message key="plancreator.env.hiddenClasses"/>:</div></th>
    <td><input name="hiddenClasses" type="text" size="25" value="${data.hiddenClasses}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.hiddenClassesDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><fmt:message key="plancreator.env.nonOverridableClass"/>:</div></th>
    <td><input name="nonOverridableClasses" type="text" size="25" value="${data.nonOverridableClasses}" /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.nonOverridableClassDesc"/></td>
  </tr>
  <tr>
    <th><div align="right"><fmt:message key="plancreator.env.inverseClassLoading"/>:</div></th>
    <td><input name="inverseClassLoading" type="checkbox" value="true" 
      <c:if test="${data.inverseClassLoading}"><fmt:message key="plancreator.env.checked"/> </c:if> /></td>
  </tr>
  <tr>
    <td></td>
    <td><fmt:message key="plancreator.env.inverseClassLoadingDesc"/></td>
  </tr>

  <!-- SUBMIT BUTTON -->
  <tr>
    <td></td>
    <td><input type="submit" value='<fmt:message key="plancreator.common.next"/>' /></td>
  </tr>
</table>

</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="plancreator.common.cancel"/></a></p>
