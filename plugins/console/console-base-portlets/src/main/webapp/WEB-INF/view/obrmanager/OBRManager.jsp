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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<CommonMsg:commonMsg/>

<br/>
<table width="100%" class="BlankTableLine" summary="Add OBR URL">
    <tr>
        <td>
            <form id="addForm" method="POST" action="<portlet:actionURL><portlet:param name='action' value='add_url'/></portlet:actionURL>">
                Add an OBR URL:
                <input type="text" id="obrUrl" name="obrUrl" size="80" value=""/>&nbsp;
                <input type="submit" value="Add" />
            </form>
        </td>
    </tr>
</table>

<!-- show OBR list -->
<table width="100%" class="TableLine" summary="OBR Repositories">
    <tr class="DarkBackground">
        <th scope="col" width="80%">OSGi Bundle Repository Name</th>   
        <th scope="col" width="20%">Actions</th>
    </tr>

    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="repo" items="${repos}">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
      
      <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)">
        <!-- OBR URLs -->
        <c:choose>
            <c:when test="${repo.name} == null}">
              <td>&nbsp;${repo.URI}&nbsp;</td>
            </c:when>
            <c:otherwise>
                <td title="${repo.URI}">&nbsp;${repo.name}&nbsp;</td>
            </c:otherwise>
        </c:choose>
       
        <!-- Actions -->
        <td>
            <!-- obr:refreshurl -->
            <span>
                <a href="<portlet:actionURL><portlet:param name='uri' value='${repo.URI}'/><portlet:param name='action' value='refreshurl'/></portlet:actionURL>">Refresh</a>&nbsp;
            </span>
            <!-- obr:removeurl -->
            <span>
                <a href="<portlet:actionURL><portlet:param name='uri' value='${repo.URI}'/><portlet:param name='action' value='removeurl'/></portlet:actionURL>">Remove</a>&nbsp;
            </span>
        </td>
      </tr>

    </c:forEach>
</table>
<!-- search area -->
<table width="100%" class="BlankTableLine" summary="bundle finder">
    <tr>
        <td>
            <form id="listForm" method="POST" action="<portlet:actionURL><portlet:param name='action' value='listAll'/></portlet:actionURL>">
                <input type="submit" name="listAll" value="List All"/>
            </form>
        </td>
        <td align="right">
            <form id="searchForm" method="POST" action="<portlet:actionURL><portlet:param name='action' value='search'/></portlet:actionURL>">
                Search type:
                
                <select name="searchType">
                    <option id="symbolic-name" value="symbolic-name">Symbolic name</option>
                    <option id="bundle-name" value="bundle-name">Bundle name</option>
                    <option id="package-capability" value="package-capability">Package capability</option>
                    <option id="package-requirement" value="package-requirement">Package requirement</option>
                </select>
                
                <input type="text" id="searchString" name="searchString" value="${searchString}"/>&nbsp;
                <input type="submit" value="Go" />
                <input type="button" value="Reset" onclick="resetSearchForm()" />
            </form>
            <script language="javascript">
                function resetSearchForm(){
                    document.getElementById("searchString").value = "";
                    document.getElementById("searchForm").submit();
                }
                var searchOption = document.getElementById("${param.searchType}");
                if (searchOption) {
                	searchOption.selected = true;
                }
            </script>
        </td>
    </tr>
</table>

<!-- show resource bundle list -->
<c:if test="${resources!=null}">
<table width="100%" class="TableLine" summary="Bundle List" border="0">
    <tr class="DarkBackground">
        <th>Symbolic Name</th>
    </tr>
    <c:set var="backgroundClass" value='MediumBackground'/>

    <c:forEach var="resourceInfo" items="${resources}">
    <c:choose>
        <c:when test="${backgroundClass == 'MediumBackground'}" >
            <c:set var="backgroundClass" value='LightBackground'/>
        </c:when>
        <c:otherwise>
            <c:set var="backgroundClass" value='MediumBackground'/>
        </c:otherwise>
    </c:choose>
    <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)" onclick="showHideById('${resourceInfo.id}')">
        <!-- bundle symbolicName -->
        <td>
            <div style="cursor:pointer;clear:both;" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)" onclick="showHideById('${resourceInfo.id}">${resourceInfo.symbolicName}</div>
            <div id="${resourceInfo.id}" style="background-color:#F0F8FF;display:none">
                <table width="100%" class="TableLine" cellpadding="3">
                    <!-- bundle id -->
                    <tr valign="top" width="100px">
                        <td>Description:</td>
                        <td>
                            id: ${resourceInfo.id}</br>
                            presentation name: ${resourceInfo.presentationName}</br>
                            uri: ${resourceInfo.URI}</br>
                            version: ${resourceInfo.version}</br>
                            size: ${resourceInfo.size}</br>
                        </td>
                    </tr>
                    <tr><td colspan="2"></td></tr>
                    <tr valign="top" width="100px">
                        <td>Requirements:</td>
                        <td>
                            <c:if test="${resourceInfo.requirements!=null}">
                            <c:forEach var="requireinfo" items="${resourceInfo.requirements}">
                                ${requireinfo.name}: ${requireinfo.filter}</br>
                            </c:forEach>
                            </c:if>
                        </td>
                    </tr>
                    <tr><td colspan="2"></td></tr>
                    <tr valign="top" width="100px">
                        <td>Capabilities:</td>
                        <td>
                            <c:if test="${resourceInfo.capabilities!=null}">
                            <c:forEach var="capabilityinfo" items="${resourceInfo.capabilities}">
                                ${capabilityinfo.name}: ${capabilityinfo.propertiesAsMap}</br>
                            </c:forEach>
                            </c:if>
                        </td>
                    </tr>
                </table>
            </div>
        </td>
    </tr>
    </c:forEach>
</table>
</c:if>
