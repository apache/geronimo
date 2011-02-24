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

<CommonMsg:confirmMsg/>

<script>

// Check to see if a component is "safe" to stop within a running server.
// Service components with names that begin with "org.apache.geronimo.configs/", for example,
// may not be safe to stop because doing so might prevent other components
// that depend on them (like the console itself) from functioning properly.
// If the component is not safe to stop then prompt to make sure that
// the user really intends to stop the component prior to any action.
function promptIfUnsafeToStop(target, bundleId, bundleName, type) {
    // otherwise don't challenge the stop operation
    return true;
}


// Check to see if a component is "safe" to stop within a running server.
// Service components with names that begin with "org.apache.geronimo.configs/", for example,
// may not be safe to stop because doing so might prevent other components
// that depend on them (like the console itself) from functioning properly.
// If the component is not safe to stop then prompt to make sure that
// the user really intends to stop the component prior to any action.
function promptIfUnsafeToRestart(target, bundleId, bundleName, type) {
    // otherwise don't challenge the restart operation
    return true;
}


// Uninstall is always a potentially dangerous action, so we should prompt the
// the user to ensure that they really indent to do this.  Uninistalling
// some modules is more destructive than others (such as modules which are
// dependencies of the web console or dependencies of other core server 
// modules.  In such cases. it may leave the server in a state where it 
// cannot be restarted.  These situations require more stringent warnings.
function uninstallPrompt(target, bundleId, bundleName, type) {
    // if the component is none of the above provide a standard warning
    return showConfirmMessage(target, '<fmt:message key="configmanager.normal.confirmMsg10"/> ' + bundleName + '?', '<fmt:message key="configmanager.normal.ok"/>', '<fmt:message key="configmanager.normal.cancel"/>');
}

function updatePrompt(target, bundleId, bundleName, type) {
    return true;
}
</script>

<CommonMsg:commonMsg/>

<table width="100%" class="TableLine" summary="Config Manager">
    <tr class="DarkBackground">
        <th scope="col" align="left">&nbsp;<fmt:message key="bundlemanager.normal.bundleId" /></th>   
        <th scope="col"><fmt:message key="bundlemanager.normal.symbolicName" /></th> 
        <c:if test="${showWebInfo}">          
          <th scope="col">URL</th>
        </c:if>
        <th scope="col">&nbsp;<fmt:message key="consolebase.common.state"/></th>
        <th scope="col" align="center" colspan="4"><fmt:message key="consolebase.common.commands"/></th>
    </tr>
  <c:set var="backgroundClass" value='MediumBackground'/>
  <c:forEach var="moduleDetails" items="${configurations}">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
      <tr>
        <!-- bundle id -->
        <td class="${backgroundClass}">&nbsp;${moduleDetails.bundleId}&nbsp;</td>
        
        <!-- bundle name -->
        <td class="${backgroundClass}">&nbsp;${moduleDetails.symbolicName}&nbsp;</td>
                
        <!-- context path -->
        <c:if test="${showWebInfo}">
            <td class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">
            	<c:forEach var="contextPath" items="${moduleDetails.contextPaths}">
            		&nbsp;<a href="${contextPath}">${contextPath}</a>&nbsp;<br/>
            	</c:forEach>
            </c:if>
            </td>
        </c:if>

        <!-- state -->
        <td width="100" class="${backgroundClass}">&nbsp;${moduleDetails.state}&nbsp;</td>

        <!-- Start/Stop actions -->
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">
                <span> 
                    &nbsp;<a href="<portlet:actionURL><portlet:param name='bundleId' value='${moduleDetails.bundleId}'/><portlet:param name='action' value='stop'/></portlet:actionURL>" onClick="return promptIfUnsafeToStop(this, '${moduleDetails.bundleId}','${moduleDetails.symbolicName}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.stop"/></a>&nbsp;
                </span>
            </c:if>
            <c:if test="${moduleDetails.state.stopped}">
                &nbsp;<a href="<portlet:actionURL><portlet:param name='bundleId' value='${moduleDetails.bundleId}'/><portlet:param name='action' value='start'/></portlet:actionURL>"><fmt:message key="consolebase.common.start"/></a>&nbsp;
            </c:if>
        </td>

        <!-- Restart action -->
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">
                <span> 
                    &nbsp;<a href="<portlet:actionURL><portlet:param name='bundleId' value='${moduleDetails.bundleId}'/><portlet:param name='action' value='restart'/></portlet:actionURL>" onClick="return promptIfUnsafeToRestart(this, '${moduleDetails.bundleId}','${moduleDetails.symbolicName}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.restart"/></a>&nbsp;
                </span>
            </c:if>
        </td>

        <!-- Update action -->
        <td width="75" class="${backgroundClass}">
            <span> 
                &nbsp;<a href="<portlet:actionURL><portlet:param name='bundleId' value='${moduleDetails.bundleId}'/><portlet:param name='action' value='update'/></portlet:actionURL>" onClick="return updatePrompt(this, '${moduleDetails.bundleId}','${moduleDetails.symbolicName}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.update"/></a>&nbsp;
            </span>
        </td>
        
        <!-- Uninstall action -->
        <td width="75" class="${backgroundClass}">
            <span> 
                &nbsp;<a href="<portlet:actionURL><portlet:param name='bundleId' value='${moduleDetails.bundleId}'/><portlet:param name='action' value='uninstall'/></portlet:actionURL>" onClick="return uninstallPrompt(this, '${moduleDetails.bundleId}','${moduleDetails.symbolicName}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.uninstall"/></a>&nbsp;
            </span>
        </td>

      </tr>
  </c:forEach>
</table>

<script>
// Call to set initial expert mode actions correctly 
init();
</script>
