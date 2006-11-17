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
<portlet:defineObjects/>
<p><b>Apache mod_jk</b> -- Basic Configuration</p>

<!-- FORM TO COLLECT DATA FOR THIS PAGE -->
<form name="<portlet:namespace/>ApacheForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="basic-after"/>
    <input type="hidden" name="addAjpPort" value="${model.addAjpPort}"/>
    <c:forEach var="webApp" items="${model.webApps}" varStatus="status">
        <input type="hidden" name="webapp.${status.index}.configId" value="${webApp.configId}"/>
        <input type="hidden" name="webapp.${status.index}.enabled" value="${webApp.enabled}"/>
        <input type="hidden" name="webapp.${status.index}.dynamicPattern" value="${webApp.dynamicPattern}"/>
        <input type="hidden" name="webapp.${status.index}.serveStaticContent" value="${webApp.serveStaticContent}"/>
        <input type="hidden" name="webapp.${status.index}.contextRoot" value="${webApp.contextRoot}"/>
        <input type="hidden" name="webapp.${status.index}.webAppDir" value="${webApp.webAppDir}"/>
    </c:forEach>
    <table border="0">
        <!-- ENTRY FIELD: OS -->
        <tr>
            <th><div align="right">Operating System:</div></th>
            <td>
                <select name="os">
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
            <td>The process for installing <tt>mod_jk</tt> depends on the operating system. For some,
                we can provide specific guidance. For others, you'll have to install <tt>mod_jk</tt>
                on your own.
            </td>
        </tr>

        <!-- ENTRY FIELD: workers.properties path -->
        <tr>
            <th><div align="right">Path to <tt>workers.properties</tt>:</div></th>
            <td><input name="workersPath" type="text" size="30" maxlength="255"
                       value="${model.workersPath}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><tt>mod_jk</tt> requires a file called <tt>workers.properties</tt> to specify which
              app servers can be contacted on which network ports, etc.  This tool will generate the
              contents for the <tt>workers.properties</tt> file, but it also needs to point to this
              file in the Apache configuration data, so we need to know where you plan to put this
              file.</td>
        </tr>

        <!-- ENTRY FIELD: log file path -->
        <tr>
            <th><div align="right"><tt>mod_jk</tt> log file location:</div></th>
            <td><input name="logFilePath" type="text" size="30" maxlength="255"
                       value="${model.logFilePath}"/></td>
        </tr>
        <tr>
            <td></td>
            <td><tt>mod_jk</tt> writes to a log file in a location you choose.  The log file
              location needs to be set in the Apache configuration.  Please select the location
              where the <tt>mod_jk</tt> log file should be written.</td>
        </tr>

        <!-- SUBMIT BUTTON -->
        <tr>
            <td></td>
            <td><input type="submit" value="Next"/></td>
        </tr>
    </table>
</form>
<!-- END OF FORM TO COLLECT DATA FOR THIS PAGE -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Cancel</a></p>
