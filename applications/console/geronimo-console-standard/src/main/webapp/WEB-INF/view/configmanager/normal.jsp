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
<portlet:defineObjects/>


<script>
// Check to see if a component is "safe" to stop within a running server.
// Service components with names that begin with "org.apache.geronimo.configs/", for example,
// may not be safe to stop because doing so might prevent other components
// that depend on them (like the console itself) from functioning properly.
// If the component is not safe to stop then prompt to make sure that
// the user really intends to stop the component prior to any action.
function promptIfUnsafeToStop(configId,type) {
    // if the component is a Geronimo service provide a stern warning
    if ((type == 'SERVICE') && configId.indexOf("org.apache.geronimo.configs/") == 0) {
            return confirm( configId + " is an Apache Geronimo service. " +
                           "Stopping this component may prevent the server or the "+
                           "administration console from functioning properly. " +
                           "All dependent components and subsequent dependencies will also be stopped. " +
                           "Reference the 'Child Components' list in the view for directly affected components. " +
                           "Proceed with this action?");
    }
    // if the component is the web console provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/webconsole-") == 0) {
            return confirm( configId + " provides the administration console interface " +
                           "that you are currently viewing.  Stopping it will cause the interface " +
                           "to become unavailable and manual action will be required to restore the function. " +
                           "Proceed with this action?");
    }
    // if the component is any other Geronimo provided component provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/") == 0) {
            return confirm( configId + " is provided by Apache Geronimo and may be required by other " +
                           "modules (reference the 'Child Components' listed in the view). " +
                           "All dependent components and subsequent dependencies will also be stopped. " +
                           "Proceed with this action?");
    }
    // otherwise don't challenge the stop operation
    return true;
}


// Check to see if a component is "safe" to stop within a running server.
// Service components with names that begin with "org.apache.geronimo.configs/", for example,
// may not be safe to stop because doing so might prevent other components
// that depend on them (like the console itself) from functioning properly.
// If the component is not safe to stop then prompt to make sure that
// the user really intends to stop the component prior to any action.
function promptIfUnsafeToRestart(configId,type) {
    // if the component is a Geronimo service provide a stern warning
    if ((type == 'SERVICE') && configId.indexOf("org.apache.geronimo.configs/") == 0) {
            return confirm( configId + " is an Apache Geronimo service. " +
                           "Restarting this component may prevent the server or the "+
                           "administration console from functioning properly. " +
                           "As part of the stop action, all dependent components and subsequent dependencies will also be stopped. " +
                           "Only this component will be restarted. " +
                           "Reference the 'Child Components' list in the view for directly affected components. " +
                           "Proceed with this action?");
    }
    // if the component is the web console provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/webconsole-") == 0) {
            return confirm( configId + " provides the administration console interface " +
                           "that you are currently viewing.  Restarting it will cause the interface " +
                           "to become unavailable and manual action may be necessary to restore the console function. " +
                           "Proceed with this action?");
    }
    // if the component is any other Geronimo provided component provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/") == 0) {
            return confirm( configId + " is provided by Apache Geronimo and may be required by other " +
                           "modules (reference the 'Child Components' listed in the view). " +
                           "As part of the stop action, all dependent components and subsequent dependencies will also be stopped. " +
                           "Proceed with this action?");
    }
    // otherwise don't challenge the stop operation
    return true;
}


// Uninstall is always a potentially dangerous action, so we should prompt the
// the user to ensure that they really indent to do this.  Uninistalling
// some modules is more destructive than others (such as modules which are
// dependencies of the web console or dependencies of other core server 
// modules.  In such cases. it may leave the server in a state where it 
// cannot be restarted.  These situations require more stringent warnings.
function uninstallPrompt(configId,type) {
    // if the component is a geronimo service always provide the most stern warning
    if ((type == 'SERVICE') && configId.indexOf("org.apache.geronimo.configs/") == 0) {
            return confirm( configId + " is an Apache Geronimo service. " +
                           "Uninstalling this component may have unexpected results "+
                           "such as rendering the administration web console or even the "+
                           "server itself unstable.  Reference the 'Child Components' view " + 
                           "for directly affected components. " +
                           "Are you certain you wish to proceed with this uninstall?");
    }
    // if the component is a the web console itself provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/webconsole-") == 0) {
            return confirm( configId + " provides the administration console user interface " +
                           "that you are currently viewing.  Uninstalling it will cause the interface " +
                           "to become unavailable and manual action will be required to restore the function. " +
                           "Are you certain you wish to proceed with this uninstall?");
    }
    // if the component is any other Apache Geronimo provided component than provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/") == 0) {
            return confirm( configId + " is provided by Apache Geronimo and may be required by other " +
                           "modules (reference the 'Child Components' listed in the view). " +
                           "Are you certain you wish to proceed with this uninstall?");
    }
    // if the component is none of the above provide a standard warning
    return confirm("Are you certain you wish to uninstall " + configId + " ?");
}
</script>


<br />
<br />
<table width="100%">
    <tr class="DarkBackground">
        <th align="left">&nbsp;Component Name</th>
        <c:if test="${showWebInfo}"><th>URL</th></c:if>
        <th>&nbsp;State</th>
        <th align="center" colspan="3">Commands</th>
        <th align="left">Parent Components</th>
        <th align="left">Child Components</th>
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
        <td class="${backgroundClass}">&nbsp;${moduleDetails.configId}</td>
        <c:if test="${showWebInfo}">
            <td class="${backgroundClass}">&nbsp;<c:if test="${moduleDetails.state.running}"><a href="${moduleDetails.contextPath}">${moduleDetails.contextPath}</a></c:if></td>
        </c:if>
        <td width="100" class="${backgroundClass}">&nbsp;${moduleDetails.state}</td>
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="stop"/></portlet:actionURL>" onClick="return promptIfUnsafeToStop('${moduleDetails.configId}','${moduleDetails.type.name}');");">Stop</a></c:if>
            <c:if test="${moduleDetails.state.stopped && (moduleDetails.type.name ne 'CAR')}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="start"/></portlet:actionURL>">Start</a></c:if>
            <c:if test="${moduleDetails.state.failed}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="stop"/></portlet:actionURL>" onClick="return promptIfUnsafeToStop('${moduleDetails.configId}','${moduleDetails.type.name}');");">Stop</a></c:if>
        </td>
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">&nbsp;<a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="restart"/></portlet:actionURL>" onClick="return promptIfUnsafeToRestart('${moduleDetails.configId}','${moduleDetails.type.name}');");">Restart</a></c:if>
        </td>
        <td width="75" class="${backgroundClass}">
            <a href="<portlet:actionURL><portlet:param name="configId" value="${moduleDetails.configId}"/><portlet:param name="action" value="uninstall"/></portlet:actionURL>" onClick="return uninstallPrompt('${moduleDetails.configId}','${moduleDetails.type.name}');");">Uninstall</a>
        </td>
        <td class="${backgroundClass}">
            <c:forEach var="parent" items="${moduleDetails.parents}">
                ${parent} <br>
            </c:forEach>
        </td>
        <td class="${backgroundClass}">
        <c:forEach var="child" items="${moduleDetails.children}">
            ${child} <br>
        </c:forEach>
        </td>
    </tr>
  </c:forEach>
</table>

<p>${messageInstalled} ${messageStatus}</p>
