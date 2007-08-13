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

<p>Enter description about the configuration elements being created in this page</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>EnvironmentForm" action="<portlet:actionURL/>" method="POST">
<input type="hidden" name="mode" value="environment-after" />

<table border="0">
  <!-- ENTRY FIELD: Context Root -->
  <tr>
    <th>
    <div align="right">Web Context Root:</div>
    </th>
    <td><input name="contextRoot" type="text" size="20" value="${data.contextRoot}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about context-root here</td>
  </tr>
  <!-- ENTRY FIELD: Module Id -->
  <tr>
    <th colspan="2">Web Application Identity</th>
  </tr>
  <tr>
    <th>
    <div align="right">GroupId:</div>
    </th>
    <td><input name="groupId" type="text" size="20" value="${data.groupId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about groupId here</td>
  </tr>
  <tr>
    <th>
    <div align="right">ArtifactId:</div>
    </th>
    <td><input name="artifactId" type="text" size="20" value="${data.artifactId}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about artifactId here</td>
  </tr>
  <tr>
    <th>
    <div align="right">Version:</div>
    </th>
    <td><input name="version" type="text" size="20" value="${data.version}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about version here</td>
  </tr>
  <tr>
    <th>
    <div align="right">Type:</div>
    </th>
    <td><input name="type" type="text" size="20" value="${data.type}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about type here</td>
  </tr>
  <!-- ENTRY FIELD: Hidden Classes, Non Overridable Classes and Inverse Class Loading -->
  <tr>
    <th colspan="2">Class Path Settings</th>
  </tr>
  <tr>
    <th>
    <div align="right">HiddenClasses:</div>
    </th>
    <td><input name="hiddenClasses" type="text" size="20" value="${data.hiddenClasses}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about HiddenClasses here. Separate multiple entries with a semicolon(;)</td>
  </tr>
  <tr>
    <th>
    <div align="right">NonOverridableClasses:</div>
    </th>
    <td><input name="nonOverridableClasses" type="text" size="20" value="${data.nonOverridableClasses}" /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about nonOverridableClasses here. Separate multiple entries with a semicolon(;)</td>
  </tr>
  <tr>
    <th>
    <div align="right">InverseClassLoading:</div>
    </th>
    <td><input name="inverseClassLoading" type="checkbox" value="true" <c:if test="${data.inverseClassLoading}">CHECKED </c:if> /></td>
  </tr>
  <tr>
    <td></td>
    <td>Enter description about inverseClassLoading here</td>
  </tr>
  <!-- SUBMIT BUTTON -->
  <tr>
    <th>
    <div align="right"></div>
    </th>
    <td><input type="submit" value="Next" /></td>
  </tr>
</table>

</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
