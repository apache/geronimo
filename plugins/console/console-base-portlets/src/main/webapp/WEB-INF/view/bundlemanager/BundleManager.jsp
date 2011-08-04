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
 
<script>
function uninstallPrompt(target, bundleId, bundleName) {
    var msg = '<fmt:message key="configmanager.normal.confirmMsg10"/>: ' + bundleName + '?';
    return showGlobalConfirmMessage(msg);
}

function refreshPrompt(target, bundleId, bundleName) {
    var msg = '<fmt:message key="configmanager.normal.confirmMsg11"/>: ' + bundleName + '?';
    return showGlobalConfirmMessage(msg);
}
</script>

<CommonMsg:commonMsg/>

<br/>
<table width="100%" class="BlankTableLine" summary="OSGi install">
    <tr>
        <td>
            <form id="installForm" enctype="multipart/form-data" method="POST" action="<portlet:actionURL><portlet:param name='action' value='install'/></portlet:actionURL>">
                Install New:
                <input type="file" name="bundleFile"/>
                            
                Start:
                <input type="checkbox" name="startAfterInstalled" value="yes"/>
                            
                Start Level:
                <input type="text" id="startLevel" name="startLevel" value="${initStartLevel}" style="width:30" size="4" />
            
                <input type="submit" style="width:80px" onClick="return checkStartLevel()" value="install" />
            </form>
            <script language="javascript">
                function checkStartLevel(){
                    if (document.getElementById("startLevel").value < ${initStartLevel}) {
                        if (!confirm("The start-level you are setting is less than the framework initial start-level(" + ${initStartLevel} + "). Will you continue?")) {
                            return false;
                        }
                    }
                }
            </script>
        </td>
        <td align="right">
            <form id="packageForm" method="POST" action="<portlet:actionURL><portlet:param name='page' value='find_packages'/></portlet:actionURL>">
                Find Packages:
                <input type="text" id="packageString" name="packageString" value="" title="Input nothing to list all packages"/>&nbsp;
                <input type="submit" value="Go" />
            </form>
        </td>
    </tr>
</table>



<table width="100%" class="BlankTableLine" summary="OSGi filter">
    <tr>
        <td>
            <form id="listForm" method="POST" action="<portlet:actionURL><portlet:param name='action' value='list'/></portlet:actionURL>">
                Show Bundles:
                <input type="radio" name="listType" value="all" onclick="submitListForm(this)"  />All&nbsp;
                <input type="radio" name="listType" value="wab" onclick="submitListForm(this)"  />WAB&nbsp;
                <input type="radio" name="listType" value="blueprint" onclick="submitListForm(this)"  />Blueprint&nbsp;
                <input type="radio" name="listType" value="system" onclick="submitListForm(this)"  />System&nbsp;
                <input type="radio" name="listType" value="configuration" onclick="submitListForm(this)"  />Geronimo Configuration&nbsp;
            </form>
            <script language="javascript">    
                 var radiogroup = document.getElementsByName("listType");  
                 for(var i=0;i<radiogroup.length;i++){    
                     if(radiogroup[i].value == "${listTypeValue}"){    
                           radiogroup[i].checked = true;  
                     }    
                 }
             </script>
            <script language="javascript">
                function submitListForm(radiobutton){
                    if (radiobutton.value != "${listTypeValue}"){
                        document.getElementById("listForm").submit();
                    }
                }
            </script>
        </td>
        <td align="right">
            <form id="searchForm" method="POST" action="<portlet:actionURL><portlet:param name='action' value='search'/></portlet:actionURL>">
                Search by Symbolic Name:
                <input type="text" id="searchString" name="searchString" value="${searchStringValue}"/>&nbsp;
                <input type="submit" value="Go" />
                <input type="button" value="Reset" onclick="resetSearchForm()" />
            </form>
            <script language="javascript">
                function resetSearchForm(){
                    document.getElementById("searchString").value = "";
                    document.getElementById("searchForm").submit();
                }
            </script>
        </td>
    </tr>
</table>


<table width="100%" class="TableLine" summary="OSGi Manager">
    <tr class="DarkBackground">
        <th scope="col" width="35">Id</th>   
        <th scope="col">Symbolic Name</th> 
        <th scope="col" width="150">Version</th>
        <th scope="col" width="150">Type</th>
        <th scope="col" width="100">State</th>
        <c:if test="${listTypeValue == 'wab'}" >          
            <th scope="col" width="100">URL</th>
        </c:if>
        <c:if test="${listTypeValue == 'blueprint'}" >
            <th scope="col" width="100">Blueprint State</th>
        </c:if>
        <c:if test="${listTypeValue != 'system' && listTypeValue != 'configuration'}" >
            <th scope="col" width="100">Actions</th>
        </c:if>
        <th scope="col" width="100">Utilities</th>
    </tr>

    <c:set var="backgroundClass" value='MediumBackground'/>
    <c:forEach var="bundleInfo" items="${extendedBundleInfos}">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
      <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)">
        <!-- bundle id -->
        <td>&nbsp;${bundleInfo.bundleId}&nbsp;</td>
        
        <!-- bundle name -->
        <td>&nbsp;${bundleInfo.symbolicName}&nbsp;</td>

        <!-- version -->
        <td>&nbsp;${bundleInfo.bundleVersion}&nbsp;</td>

        <!-- bundle type -->
        <td>
            <c:forEach var="type" items="${bundleInfo.types}">
                ${type}&nbsp;
            </c:forEach>
        </td>

        <!-- state -->
        <td>&nbsp;${bundleInfo.state}&nbsp;</td>
        
        <!-- WAB context path -->
        <c:if test="${listTypeValue == 'wab'}" >  
            <td>
                <c:if test="${bundleInfo.state.running}">
                    <c:forEach var="contextPath" items="${bundleInfo.contextPaths}">
                        &nbsp;<a href="${contextPath}">${contextPath}</a>&nbsp;<br/>
                    </c:forEach>
                </c:if>
            </td>
        </c:if>
        
        <!-- Blueprint State -->
        <c:if test="${listTypeValue == 'blueprint'}" >
            <td>
                <c:if test="${! empty bundleInfo.blueprintState}">
                    ${bundleInfo.blueprintState}
                </c:if>
            </td>
        </c:if>
        
        <!-- actions -->
        <c:if test="${listTypeValue != 'system' && listTypeValue != 'configuration'}" >
            <td>
                &nbsp;
                <c:if test="${bundleInfo.operable}">
                    <!-- Start/Stop -->
                    <c:if test="${bundleInfo.state.running}">
                        <span> 
                            <a href="<portlet:actionURL><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/><portlet:param name='action' value='bundle'/><portlet:param name='operation' value='stop'/></portlet:actionURL>"><img border="0" src="<%=request.getContextPath()%>/images/bundle_stop.png" alt="bundle_stop.png" title="stop"/></a>&nbsp;
                        </span>
                    </c:if>
                    <c:if test="${bundleInfo.state.stopped}">
                        <span>
                            <a href="<portlet:actionURL><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/><portlet:param name='action' value='bundle'/><portlet:param name='operation' value='start'/></portlet:actionURL>"><img border="0" src="<%=request.getContextPath()%>/images/bundle_start.png" alt="bundle_start.png" title="start"/></a>&nbsp;
                        </span>
                    </c:if>
        
                    <!-- Update action -->
                    <span> 
                        <a href="<portlet:actionURL><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/><portlet:param name='action' value='bundle'/><portlet:param name='operation' value='update'/></portlet:actionURL>"><img border="0" src="<%=request.getContextPath()%>/images/bundle_update.png" alt="bundle_update.png" title="update"/></a>&nbsp;
                    </span>
                	
                	<!-- Refresh action -->
                    <span> 
                        <a href="<portlet:actionURL><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/><portlet:param name='action' value='bundle'/><portlet:param name='operation' value='refresh'/></portlet:actionURL>" onClick="return refreshPrompt(this, '${bundleInfo.bundleId}','${bundleInfo.symbolicName}');"><img border="0" src="<%=request.getContextPath()%>/images/bundle_refresh.png" alt="bundle_refresh.png" title="refresh"/></a>&nbsp;
                    </span>
                
                    <!-- Uninstall action -->
                    <span> 
                        <a href="<portlet:actionURL><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/><portlet:param name='action' value='bundle'/><portlet:param name='operation' value='uninstall'/></portlet:actionURL>" onClick="return uninstallPrompt(this, '${bundleInfo.bundleId}','${bundleInfo.symbolicName}');"><img border="0" src="<%=request.getContextPath()%>/images/bundle_delete.png" alt="bundle_delete.png" title="uninstall"/></a>&nbsp;
                    </span>
                </c:if>
            </td>
        </c:if>

        <!-- Util -->
        <td>
            &nbsp;
            <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest"/></a>&nbsp;
            <c:if test="${bundleInfo.state.running}" >
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles"/></a>&nbsp;
                <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services"/></a>&nbsp;
            </c:if>
        </td>

      </tr>
    </c:forEach>
</table>


