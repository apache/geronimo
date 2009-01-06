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
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<script>
var EXPERT_COOKIE = "org.apache.geronimo.configmanager.expertmode";
var SHOW_DEPENDENCIES_COOKIE = "org.apache.geronimo.configmanager.showDependencies";

// Check to see if a component is "safe" to stop within a running server.
// Service components with names that begin with "org.apache.geronimo.configs/", for example,
// may not be safe to stop because doing so might prevent other components
// that depend on them (like the console itself) from functioning properly.
// If the component is not safe to stop then prompt to make sure that
// the user really intends to stop the component prior to any action.
function promptIfUnsafeToStop(configId,expertConfig, type) {
    // if the component is a Geronimo "expert" service then provide a stern warning
    if ((type == 'SERVICE') && (expertConfig == 'true')) {
        return confirm( configId + " is an Apache Geronimo service.\r\n \r\n" +
                       "Stopping this component may prevent the server or the "+
                       "administration console from functioning properly. " +
                       "All dependent components and subsequent dependencies will also be stopped. " +
                       "Reference the 'Child Components' list in the view for directly affected components.\r\n \r\n" +
                       "Proceed with this action?");
    }
    // if the component is the web console provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/webconsole-") == 0) {
        return confirm( configId + " provides the administration console interface " +
                       "that you are currently viewing.\r\n \r\n Stopping it will cause the interface " +
                       "to become unavailable and manual action will be required to restore the function.\r\n \r\n" +
                       "Proceed with this action?");
    }
    // if the component is any other Geronimo "expert" component provide an appropriate warning
    if (expertConfig == 'true') {
        return confirm( configId + " is provided by Apache Geronimo and may be required by other " +
                       "modules (reference the 'Child Components' listed in the view).\r\n \r\n " +
                       "All dependent components and subsequent dependencies will also be stopped. \r\n \r\n" +
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
function promptIfUnsafeToRestart(configId,expertConfig, type) {
    // if the component is a Geronimo "expert" service then provide a stern warning
    if ((type == 'SERVICE') && (expertConfig == 'true')) {
        return confirm( configId + " is an Apache Geronimo service.\r\n \r\n " +
                       "Restarting this component may prevent the server or the "+
                       "administration console from functioning properly. " +
                       "As part of the stop action, all dependent components and subsequent dependencies will also be stopped. " +
                       "Only this component will be restarted. " +
                       "Reference the 'Child Components' list in the view for directly affected components.\r\n \r\n " +
                       "Proceed with this action?");
    }
    // if the component is the web console provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/webconsole-") == 0) {
        return confirm( configId + " provides the administration console interface " +
                       "that you are currently viewing.\r\n \r\n  Restarting it will cause the interface " +
                       "to become unavailable and manual action may be necessary to restore the console function.\r\n \r\n" +
                       "Proceed with this action?");
    }
    // if the component is a Geronimo "expert" component then provide an appropriate warning
    if (expertConfig == 'true') {
        return confirm( configId + " is provided by Apache Geronimo and may be required by other " +
                       "modules (reference the 'Child Components' listed in the view).\r\n \r\n " +
                       "As part of the stop action, all dependent components and subsequent dependencies will also be stopped. \r\n \r\n" +
                       "Proceed with this action?");
    }
    // otherwise don't challenge the restart operation
    return true;
}


// Uninstall is always a potentially dangerous action, so we should prompt the
// the user to ensure that they really indent to do this.  Uninistalling
// some modules is more destructive than others (such as modules which are
// dependencies of the web console or dependencies of other core server 
// modules.  In such cases. it may leave the server in a state where it 
// cannot be restarted.  These situations require more stringent warnings.
function uninstallPrompt(configId,expertConfig, type) {
    // if the component is a geronimo "expert" service always provide the most stern warning
    if ((type == 'SERVICE') && (expertConfig == 'true')) {
        return confirm( configId + " is an Apache Geronimo service.\r\n \r\n" +
                       "Uninstalling this component may have unexpected results "+
                       "such as rendering the administration web console or even the "+
                       "server itself unstable.  Reference the 'Child Components' view " + 
                       "for directly affected components. \r\n \r\n" +
                       "Are you certain you wish to proceed with this uninstall?");
    }
    // if the component is a the web console itself then provide an appropriate warning
    if (configId.indexOf("org.apache.geronimo.configs/webconsole-") == 0) {
        return confirm( configId + " provides the administration console user interface " +
                       "that you are currently viewing.\r\n \r\n  Uninstalling it will cause the interface " +
                       "to become unavailable and manual action will be required to restore the function.\r\n \r\n " +
                       "Are you certain you wish to proceed with this uninstall?");
    }
    // if the component is any other Apache Geronimo "expert" component then provide an appropriate warning
    if (expertConfig == 'true') {
        return confirm( configId + " is provided by Apache Geronimo and may be required by other " +
                       "modules (reference the 'Child Components' listed in the view). \r\n \r\n" +
                       "Are you certain you wish to proceed with this uninstall?");
    }
    // if the component is none of the above provide a standard warning
    return confirm("Are you certain you wish to uninstall " + configId + " ?");
}

// Toggle expert mode on and off with onClick
function toggleExpertMode() {
    if (document.checkExpert.expertMode.checked) {
        //  Set attribute/parameter to indicated expertMode is checked
        document.cookie=EXPERT_COOKIE+"=true";
        var expertActions = getSpanElementsByName('expert');
        for( var i = 0; i < expertActions.length; ++i ) {
            expertActions[i].style.display='block' ;
        }
        var nonexpertActions = getSpanElementsByName('nonexpert');
        for( var i = 0; i < nonexpertActions.length; ++i ) {
            nonexpertActions[i].style.display='none' ;
        }
    }
    else {
        //  Set attribute/parameter to indicated expertMode is not checked
        document.cookie=EXPERT_COOKIE+"=false";
        var expertActions = getSpanElementsByName('expert');
        for( var i = 0; i < expertActions.length; ++i ) {
            expertActions[i].style.display='none' ;
        }
        var nonexpertActions = getSpanElementsByName('nonexpert');
        for( var i = 0; i < nonexpertActions.length; ++i ) {
            nonexpertActions[i].style.display='block' ;
        }
    }
}


// work around since IE doesn't support document.getElementsByName
function getSpanElementsByName(name) {
    var results = new Array();
    var spans = document.getElementsByTagName("span");
    for(i = 0,j = 0; i < spans.length; i++) {
        nameValue = spans[i].getAttribute("name");
        if(nameValue == name) {
          results[j] = spans[i];
          j++;
        }
    }
    return results;
}

// get cookie utility routine
function getCookie(name) {
    var result = "";
    var key = name + "=";
    if (document.cookie.length > 0) {
        start = document.cookie.indexOf(key);
        if (start != -1) { 
            start += key.length;
            end = document.cookie.indexOf(";", start);
            if (end == -1) end = document.cookie.length;
            result=document.cookie.substring(start, end);
        }
    }
    return result;
}

// initialization routine to set the initial display state for expert mode correctly
function init() {
    if (getCookie(EXPERT_COOKIE) == 'true') {
        document.checkExpert.expertMode.checked = true;
    } else {
        document.checkExpert.expertMode.checked = false;
    }
    toggleExpertMode();
    
    if (getCookie(SHOW_DEPENDENCIES_COOKIE) == 'true') {
        document.showDependenciesForm.showDependenciesMode.checked = true;
    } else {
        document.showDependenciesForm.showDependenciesMode.checked = false;
    }
}

function toggleShowDependenciesMode() {
    if (document.showDependenciesForm.showDependenciesMode.checked) {
        document.cookie=SHOW_DEPENDENCIES_COOKIE+"=true";
    } else {
        document.cookie=SHOW_DEPENDENCIES_COOKIE+"=false";
    }
    window.location.reload();
}

</script>

<br/>
<form name="checkExpert">
<input type="checkbox" name="expertMode" id="<portlet:namespace/>expertMode" onClick="toggleExpertMode();" />&nbsp;<label for="<portlet:namespace/>expertMode"><fmt:message key="configmanager.normal.expertMode" /></label>  
</form>

<form name="showDependenciesForm">
<input type="checkbox" name="showDependenciesMode" id="<portlet:namespace/>showDependenciesMode" onClick="toggleShowDependenciesMode();" />&nbsp;<label for="<portlet:namespace/>showDependenciesMode"><fmt:message key="configmanager.normal.showDependencyMode" /></label>
</form>
<br/>

<table width="100%" class="TableLine" summary="Config Manager">
    <tr class="DarkBackground">
        <th scope="col" align="left">&nbsp;<fmt:message key="configmanager.normal.componentName" /></th>
        <c:if test="${showWebInfo}">
          <th scope="col">URL</th>
        </c:if>
        <th scope="col">&nbsp;<fmt:message key="consolebase.common.state"/></th>
        <th scope="col" align="center" colspan="3"><fmt:message key="consolebase.common.commands"/></th>
        <c:if test="${showDependencies}">
          <th scope="col" align="left"><fmt:message key="configmanager.normal.parentComponents" /></th>
          <th scope="col" align="left"><fmt:message key="configmanager.normal.childComponents" /></th>
        </c:if>
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
        <!-- module id -->
        <c:choose>
            <c:when test="${moduleDetails.componentName != null}">
                <td class="${backgroundClass}">&nbsp;${moduleDetails.componentName}&nbsp;</td>
            </c:when>
            <c:otherwise>
               <td class="${backgroundClass}">&nbsp;${moduleDetails.configId}&nbsp;</td>
            </c:otherwise>   
        </c:choose>
        
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
            <c:if test="${moduleDetails.state.running || moduleDetails.state.failed}">
                <span <c:if test="${moduleDetails.expertConfig}"> name=expert </c:if>> 
                    &nbsp;<a href="<portlet:actionURL><portlet:param name='configId' value='${moduleDetails.configId}'/><portlet:param name='action' value='stop'/></portlet:actionURL>" onClick="return promptIfUnsafeToStop('${moduleDetails.configId}','${moduleDetails.expertConfig}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.stop"/></a>&nbsp;
                </span>
            </c:if>
            <c:if test="${moduleDetails.expertConfig && (moduleDetails.state.running || moduleDetails.state.failed)}">
                <span name=nonexpert> 
                    &nbsp;<a><fmt:message key="consolebase.common.stop"/></a>&nbsp;
                </span>
            </c:if>
            <c:if test="${moduleDetails.state.stopped && (moduleDetails.type.name ne 'CAR')}">
                &nbsp;<a href="<portlet:actionURL><portlet:param name='configId' value='${moduleDetails.configId}'/><portlet:param name='action' value='start'/></portlet:actionURL>"><fmt:message key="consolebase.common.start"/></a>&nbsp;
            </c:if>
        </td>

        <!-- Restart action -->
        <td width="75" class="${backgroundClass}">
            <c:if test="${moduleDetails.state.running}">
                <span <c:if test="${moduleDetails.expertConfig}"> name=expert </c:if>> 
                    &nbsp;<a href="<portlet:actionURL><portlet:param name='configId' value='${moduleDetails.configId}'/><portlet:param name='action' value='restart'/></portlet:actionURL>" onClick="return promptIfUnsafeToRestart('${moduleDetails.configId}','${moduleDetails.expertConfig}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.restart"/></a>&nbsp;
                </span>
            </c:if>
            <c:if test="${moduleDetails.expertConfig && moduleDetails.state.running}">
                <span name=nonexpert> 
                    &nbsp;<a><fmt:message key="consolebase.common.restart"/></a>&nbsp;
                </span>
            </c:if>
        </td>

        <!-- Uninstall action -->
        <td width="75" class="${backgroundClass}">
            <span <c:if test="${moduleDetails.expertConfig}"> name=expert </c:if>> 
                &nbsp;<a href="<portlet:actionURL><portlet:param name='configId' value='${moduleDetails.configId}'/><portlet:param name='action' value='uninstall'/></portlet:actionURL>" onClick="return uninstallPrompt('${moduleDetails.configId}','${moduleDetails.expertConfig}','${moduleDetails.type.name}');"><fmt:message key="consolebase.common.uninstall"/></a>&nbsp;
            </span>
            <c:if test="${moduleDetails.expertConfig}">
                <span name=nonexpert> 
                    &nbsp;<a><fmt:message key="consolebase.common.uninstall"/></a>&nbsp;
                </span>
            </c:if>
        </td>

        <c:if test="${showDependencies}">
           <!-- Parents -->
           <td class="${backgroundClass}">
               <c:forEach var="parent" items="${moduleDetails.parents}">
                  ${parent} <br>
               </c:forEach>
           </td>

           <!-- Children -->
           <td class="${backgroundClass}">
               <c:forEach var="child" items="${moduleDetails.children}">
                  ${child} <br>
               </c:forEach>
           </td>
        </c:if>
    </tr>
  </c:forEach>
</table>

<br />
<p>${messageInstalled} ${messageStatus}</p>


<script>
// Call to set initial expert mode actions correctly 
init();
</script>
