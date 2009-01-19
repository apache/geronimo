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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>


<p><fmt:message key="dbwizard.importUpload.title"/></p>

<p><fmt:message key="dbwizard.importUpload.summary"/></p>

<form enctype="multipart/form-data" method="POST" name="<portlet:namespace/>ImportForm"
      action="<portlet:actionURL><portlet:param name="mode" value="importUpload"/><portlet:param name="importSource" value="${pool.importSource}"/></portlet:actionURL>">
    <table width="100%">
      <tr>
        <td class="DarkBackground" colspan="2"><fmt:message key="dbwizard.common.import"><fmt:param value="${pool.importSource}"/></fmt:message></td>
      </tr>
      <tr>
        <th align="right" style="min-width: 140px"><label for="<portlet:namespace/>configFile"><fmt:message key="dbwizard.common.configFile"/></label>:</th>
        <td><input type="file" name="configFile" id="<portlet:namespace/>configFile" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.importUpload.pleaseSelect"/> ${from}.</td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="dbwizard.common.next"/>' /></td>
      </tr>
    </table>
</form>

<c:if test="${pool.importSource eq 'WebLogic 8.1'}">
<br />
<br />
<br />
<form name="<portlet:namespace/>WebLogicImportForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="weblogicImport" />
    <input type="hidden" name="importSource" value="${pool.importSource}" />
    <input type="hidden" name="from" value="${from}" />
    <table width="100%">
      <tr>
        <td class="DarkBackground" colspan="2"><fmt:message key="dbwizard.importUpload.alternateImport">
        <fmt:param value="${pool.importSource}" />
        </fmt:message></td>
      </tr>
      <tr>
        <td colspan="2"><fmt:message key="dbwizard.importUpload.aboutWebLogic"/></td>
      </tr>
      <tr>
        <th align="right" style="min-width: 140px"><label for="<portlet:namespace/>weblogicDomainDir"><fmt:message key="dbwizard.importUpload.domainDirectoryPath"/></label>:</th>
        <td><input type="text" name="weblogicDomainDir" id="<portlet:namespace/>weblogicDomainDir" size="40" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.importUpload.domainDirectoryPathExp"/> (e.g. C:\bea\user_projects\domains\mydomain).</td>
      </tr>
      <tr>
        <th align="right"><tt>weblogic81/server/lib</tt> <label for="<portlet:namespace/>weblogicLibDir"><fmt:message key="dbwizard.importUpload.path"/></label>:</th>
        <td><input type="text" name="weblogicLibDir" id="<portlet:namespace/>weblogicLibDir" size="40" /></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.importUpload.enterFullPath"/> (e.g. C:\bea\weblogic81\server\lib).</td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="dbwizard.common.next"/>' /></td>
      </tr>
    </table>
</form>
</c:if>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="dbwizard.common.cancel"/></a></p>
