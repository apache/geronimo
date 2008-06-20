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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>
<p><fmt:message key="car.pluginParams.title" /></p>


<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>PluginForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="configure-after" />
    <input type="hidden" name="configId" value="${configId}" />
    <table border="0">


        <!-- ENTRY FIELD: Name -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.pluginParams.humanReadableName" />:</div></th>
            <td><input name="name" type="text" size="30" value="${name}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.humanReadableNameExp" />              
            </td>
          </tr>
        <!-- ENTRY FIELD: Config ID -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.uniqueID" />:</div></th>
            <td><b>${configId}</b></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.uniqueIDExp" />         
            </td>
          </tr>
          </tr>
        <!-- ENTRY FIELD: Repositories -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.downloadRepositories" />:</div></th>
            <td><textarea rows="5" cols="60" name="repository">${repository}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.downloadRepositoriesExp" />
              
            </td>
          </tr>
        <!-- ENTRY FIELD: Category -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.category" />:</div></th>
            <td><input name="category" type="text" size="30" value="${category}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.categoryExp" />               
            </td>
          </tr>
        <!-- ENTRY FIELD: Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="consolebase.common.description"/>:</div></th>
            <td><textarea rows="10" cols="60" name="description">${description}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.descriptionExp" />
              
            </td>
          </tr>
        <!-- ENTRY FIELD: URL -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.pluginURL" />:</div></th>
              <td><input name="url" type="text" size="30" value="${url}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.pluginURLExp" />             
            </td>
          </tr>
        <!-- ENTRY FIELD: Author -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.author" />:</div></th>
              <td><input name="author" type="text" size="30" value="${author}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.authorExp" />               
            </td>
          </tr>
        <!-- ENTRY FIELD: License -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.license" />:</div></th>
            <td><input name="license" type="text" size="30" value="${license}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.licenseExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: License Is Open Source-->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.openSource" />:</div></th>
            <td>
                <input type="checkbox" name="licenseOSI"<c:if test="${!(empty licenseOSI)}"> checked="checked"</c:if> />
            </td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.openSourceExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Geronimo Versions -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.geronimoVersions" />:</div></th>
            <td><input type="text" cols="30" name="geronimoVersion" value="${geronimoVersion}"/></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.geronimoVersionsExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: JVM Versions -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="consolebase.common.JVMVersions"/>:</div></th>
            <td><textarea rows="5" cols="60" name="jvmVersions">${jvmVersions}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.JVMVersionsExp" />
             
            </td>
          </tr>
        <!-- ENTRY FIELD: Dependencies -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.dependencies" />:</div></th>
            <td><textarea rows="5" cols="60" name="dependencies">${dependencies}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.dependenciesExp" />
              
            </td>
          </tr>
        <!-- ENTRY FIELD: Obsoletes -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.obsoletes" />:</div></th>
            <td><textarea rows="5" cols="60" name="obsoletes">${obsoletes}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.obsoletesExp" />
             
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prerequisite1ID" />:</div></th>
            <td><input name="prereq1" type="text" size="30" value="${prereq1}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.prerequisiteIDExp" /> 
              
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right">
            <fmt:message key="car.common.prerequisite1Type" />
           :</div></th>
            <td><input name="prereq1type" type="text" size="30" value="${prereq1type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.prerequisiteTypeExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prereq1Description" />:</div></th>
            <td><textarea rows="5" cols="60" name="prereq1desc">${prereq1desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prereqDescriptionExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prerequisite2ID" />:</div></th>
            <td><input name="prereq2" type="text" size="30" value="${prereq2}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              <fmt:message key="car.pluginParams.prerequisiteIDExp" /> 
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prerequisite2Type" />:</div></th>
            <td><input name="prereq2type" type="text" size="30" value="${prereq2type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prerequisiteTypeExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prereq2Description" />:</div></th>
            <td><textarea rows="5" cols="60" name="prereq2desc">${prereq2desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prereqDescriptionExp" /> 
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prerequisite3ID" />:</div></th>
            <td><input name="prereq3" type="text" size="30" value="${prereq3}" /></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prerequisiteIDExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prerequisite3Type" />:</div></th>
            <td><input name="prereq3type" type="text" size="30" value="${prereq3type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prerequisiteTypeExp" />             
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><fmt:message key="car.common.prereq3Description" />:</div></th>
            <td><textarea rows="5" cols="60" name="prereq3desc">${prereq3desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prereqDescriptionExp" />              
            </td>
          </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value='<fmt:message key="car.common.savePluginData" />' /> <input type="submit" value='<fmt:message key="consolebase.common.cancel" />' onclick="history.go(-1); return false;" /></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->
