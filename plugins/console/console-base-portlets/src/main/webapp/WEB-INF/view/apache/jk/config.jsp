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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>
<p><fmt:message key="apache.jk.config.title"/></p>

<!-- FORM TO COLLECT DATA FOR THIS PAGE -->
<form name="<portlet:namespace/>ApacheForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="basic-after"/>
    <input type="hidden" name="addAjpPort" value="${model.addAjpPort}"/>
    <c:forEach var="webApp" items="${model.webApps}" varStatus="status">
        <input type="hidden" name="webapp.${status.index}.configId" value="${webApp.parentConfigId}"/>
        <input type="hidden" name="webapp.${status.index}.enabled" value="${webApp.enabled}"/>
        <input type="hidden" name="webapp.${status.index}.dynamicPattern" value="${webApp.dynamicPattern}"/>
        <input type="hidden" name="webapp.${status.index}.serveStaticContent" value="${webApp.serveStaticContent}"/>
        <input type="hidden" name="webapp.${status.index}.contextRoot" value="${webApp.contextRoot}"/>
        <input type="hidden" name="webapp.${status.index}.webAppDir" value="${webApp.webAppDir}"/>
    </c:forEach>
    <table border="0">
        <!-- ENTRY FIELD: OS -->
        <tr>
            <th><div align="right"><label for="<portlet:namespace/>os"><fmt:message key="apache.jk.config.operatingSystem"/></label>:</div></th>
            <td>
                <select name="os" id="<portlet:namespace/>os">
                    <option></option>
                    <option <c:if test="${model.os == 'Fedora Core 4'}">selected</c:if>>Fedora Core 4</option>
                    <option <c:if test="${model.os == 'SuSE Pro 9.0'}">selected</c:if>>SuSE Pro 9.0</option>
                    <option <c:if test="${model.os == 'SuSE Pro 9.1'}">selected</c:if>>SuSE Pro 9.1</option>
                    <option <c:if test="${model.os == 'SuSE Pro 9.2'}">selected</c:if>>SuSE Pro 9.2</option>
                    <option <c:if test="${model.os == 'SuSE Pro 9.3'}">selected</c:if>>SuSE Pro 9.3</option>
                    <option <c:if test="${model.os == 'SuSE Linux 10.0'}">selected</c:if>>SuSE Linux 10.0</option>
                    <option <c:if test="${model.os == 'Other'}">selected</c:if>>Other</option>
                </select>
            </td>
        </tr>
        <tr>
            <td></td>
            <td><fmt:message key="apache.jk.config.operatingSystemExplanation"/>            
            </td>
        </tr>

        <!-- ENTRY FIELD: workers.properties path -->
        <tr>
            <th><div align="right"><label for="<portlet:namespace/>workersPath"><fmt:message key="apache.jk.config.pathToProperties"/></label>:</div></th>
            <td><input name="workersPath" id="<portlet:namespace/>workersPath" type="text" size="30" maxlength="255"
                       value="${model.workersPath}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><fmt:message key="apache.jk.config.pathToPropertiesExplanation"/></td>
        </tr>

        <!-- ENTRY FIELD: log file path -->
        <tr>
            <th><div align="right"><label for="<portlet:namespace/>logFilePath"><fmt:message key="apache.jk.config.logFileLocation"/></label>:</div></th>
            <td><input name="logFilePath" id="<portlet:namespace/>logFilePath" type="text" size="30" maxlength="255"
                       value="${model.logFilePath}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><fmt:message key="apache.jk.config.logFileLoctionExplanation"/></td>
        </tr>

        <!-- SUBMIT BUTTON -->
        <tr>
            <td></td>
            <td><input type="submit" value='<fmt:message key="consolebase.common.next"/>'/></td>
        </tr>
    </table>
</form>
<!-- END OF FORM TO COLLECT DATA FOR THIS PAGE -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.cancel"/></a></p>
