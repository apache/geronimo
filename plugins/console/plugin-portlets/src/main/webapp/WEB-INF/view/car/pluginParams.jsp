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
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>name"><fmt:message key="car.pluginParams.humanReadableName" /></label>:</div></th>
            <td><input name="name" id="<portlet:namespace/>name" type="text" size="30" value="${name}" /></td>
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
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>repository"><fmt:message key="car.common.downloadRepositories" /></label>:</div></th>
            <td><textarea rows="5" cols="60" name="repository" id="<portlet:namespace/>repository">${repository}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.downloadRepositoriesExp" />
              
            </td>
          </tr>
        <!-- ENTRY FIELD: Category -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>category"><fmt:message key="car.common.category" /></label>:</div></th>
            <td><input name="category" id="<portlet:namespace/>category" type="text" size="30" value="${category}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.categoryExp" />               
            </td>
          </tr>
        <!-- ENTRY FIELD: Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>description"><fmt:message key="consolebase.common.description"/></label>:</div></th>
            <td><textarea rows="10" cols="60" name="description" id="<portlet:namespace/>description">${description}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.descriptionExp" />
              
            </td>
          </tr>
        <!-- ENTRY FIELD: URL -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>url"><fmt:message key="car.common.pluginURL" /></label>:</div></th>
              <td><input name="url" id="<portlet:namespace/>url" type="text" size="30" value="${url}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.pluginURLExp" />             
            </td>
          </tr>
        <!-- ENTRY FIELD: Author -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>author"><fmt:message key="car.common.author" /></label>:</div></th>
              <td><input name="author" id="<portlet:namespace/>author" type="text" size="30" value="${author}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.authorExp" />               
            </td>
          </tr>
        <!-- ENTRY FIELD: License -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>license"><fmt:message key="car.common.license" /></label>:</div></th>
            <td><input name="license" id="<portlet:namespace/>license" type="text" size="30" value="${license}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.licenseExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: License Is Open Source-->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>licenseOSI"><fmt:message key="car.common.openSource" /></label>:</div></th>
            <td>
                <input type="checkbox" name="licenseOSI" id="<portlet:namespace/>licenseOSI"<c:if test="${!(empty licenseOSI)}"> checked="checked"</c:if> />
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
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>geronimoVersion"><fmt:message key="car.common.geronimoVersions" /></label>:</div></th>
            <td><input type="text" cols="30" name="geronimoVersion" id="<portlet:namespace/>geronimoVersion" value="${geronimoVersion}"/></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.geronimoVersionsExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: JVM Versions -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>jvmVersions"><fmt:message key="consolebase.common.JVMVersions"/></label>:</div></th>
            <td><textarea rows="5" cols="60" name="jvmVersions" id="<portlet:namespace/>jvmVersions">${jvmVersions}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.JVMVersionsExp" />
             
            </td>
          </tr>
        <!-- ENTRY FIELD: Dependencies -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>dependencies"><fmt:message key="car.common.dependencies" /></label>:</div></th>
            <td><textarea rows="5" cols="60" name="dependencies" id="<portlet:namespace/>dependencies">${dependencies}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.dependenciesExp" />
              
            </td>
          </tr>
        <!-- ENTRY FIELD: Obsoletes -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>obsoletes"><fmt:message key="car.common.obsoletes" /></label>:</div></th>
            <td><textarea rows="5" cols="60" name="obsoletes" id="<portlet:namespace/>obsoletes">${obsoletes}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.obsoletesExp" />
             
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq1"><fmt:message key="car.common.prerequisite1ID" /></label>:</div></th>
            <td><input name="prereq1" id="<portlet:namespace/>prereq1" type="text" size="30" value="${prereq1}" /></td>
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
            <label for="<portlet:namespace/>prereq1type"><fmt:message key="car.common.prerequisite1Type" /></label>
           :</div></th>
            <td><input name="prereq1type" id="<portlet:namespace/>prereq1type" type="text" size="30" value="${prereq1type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
            <fmt:message key="car.pluginParams.prerequisiteTypeExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 1 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq1desc"><fmt:message key="car.common.prereq1Description" /></label>:</div></th>
            <td><textarea rows="5" cols="60" name="prereq1desc" id="<portlet:namespace/>prereq1desc">${prereq1desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prereqDescriptionExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq2"><fmt:message key="car.common.prerequisite2ID" /></label>:</div></th>
            <td><input name="prereq2" id="<portlet:namespace/>prereq2" type="text" size="30" value="${prereq2}" /></td>
          </tr>
          <tr>
            <td></td>
            <td>
              <fmt:message key="car.pluginParams.prerequisiteIDExp" /> 
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq2type"><fmt:message key="car.common.prerequisite2Type" /></label>:</div></th>
            <td><input name="prereq2type" id="<portlet:namespace/>prereq2type" type="text" size="30" value="${prereq2type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prerequisiteTypeExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 2 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq2desc"><fmt:message key="car.common.prereq2Description" /></label>:</div></th>
            <td><textarea rows="5" cols="60" name="prereq2desc" id="<portlet:namespace/>prereq2desc">${prereq2desc}</textarea></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prereqDescriptionExp" /> 
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq3"><fmt:message key="car.common.prerequisite3ID" /></label>:</div></th>
            <td><input name="prereq3" id="<portlet:namespace/>prereq3" type="text" size="30" value="${prereq3}" /></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prerequisiteIDExp" />
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 Type -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq3type"><fmt:message key="car.common.prerequisite3Type" /></label>:</div></th>
            <td><input name="prereq3type" id="<portlet:namespace/>prereq3type" type="text" size="30" value="${prereq3type}" /></td>
          </tr>
          <tr>
            <td></td>
            <td><fmt:message key="car.pluginParams.prerequisiteTypeExp" />             
            </td>
          </tr>
        <!-- ENTRY FIELD: Prerequisite 3 Description -->
          <tr>
            <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>prereq3desc"><fmt:message key="car.common.prereq3Description" /></label>:</div></th>
            <td><textarea rows="5" cols="60" name="prereq3desc" id="<portlet:namespace/>prereq3desc">${prereq3desc}</textarea></td>
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
