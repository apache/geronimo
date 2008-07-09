<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://portals.apache.org/pluto/portlet-el" prefix="portlet-el" %>
<fmt:setBundle basename="portaldriver"/>
<portlet:defineObjects/>

<portlet:actionURL var="formActionUrl"/>
<form name="adminForm" action="<c:out value="${formActionUrl}"/>" method="POST">
<script type="text/javascript">


</script>

<div>
  <h2>Portal Pages</h2>
  <!-- h4>Namespace: <portlet:namespace/> </h4 -->
  <p>
      <script type="text/javascript">
          var <portlet:namespace/>placedPortlets = new Array();
          <c:forEach items="${availablePages}" var="page">
              <portlet:namespace/>placedPortlets['<c:out value="${page.id}"/>'] = new Array();
              var <portlet:namespace/>i = 0;
              <c:forEach items="${page.portlets}" var="portlet">
              	<portlet:namespace/>placedPortlets['<c:out value="${page.id}"/>'][<portlet:namespace/>i] = new Array();
              	<portlet:namespace/>placedPortlets['<c:out value="${page.id}"/>'][<portlet:namespace/>i][0] = '<c:out value="${portlet.id}"/>';
              	<portlet:namespace/>placedPortlets['<c:out value="${page.id}"/>'][<portlet:namespace/>i++][1] = '<c:out value="${portlet.portletName}"/>';
              </c:forEach>
          </c:forEach>

          function <portlet:namespace/>doSwitchPage(select) {
              var placePortletsSelect = document.forms['adminForm'].elements['placedPortlets'];
              for(var i=0; i < placePortletsSelect.options.length;i++) {
                  placePortletsSelect.options[i] = null;
              }

              var disabled = select.value == 'Select. . .'
              document.forms['adminForm'].elements['command'][0].disabled = disabled;

              if(disabled) {
                  return;
              }

              for(var i=0; i < <portlet:namespace/>placedPortlets[select.value].length;i++) {
                  placePortletsSelect[i] = new Option(<portlet:namespace/>placedPortlets[select.value][i][1], <portlet:namespace/>placedPortlets[select.value][i][0]);
              }

          }
      </script>





    <select name="page" title='<fmt:message key="console.common.pages" />' onChange="<portlet:namespace/>doSwitchPage(this)">
      <option value="Select. . .">Select. . .</option>
    <c:forEach items="${driverConfig.pages}" var="page">
      <option value="<c:out value="${page.name}"/>"><c:out value="${page.name}"/></option>
    </c:forEach>
    </select>

    <select name="placedPortlets" title='<fmt:message key="console.common.pages" />' size="5">

    </select>

    <button name="command" disabled="true" value="remove">
      Remove
    </button>
  </p>
</div>

<div>
  <h2>Portlet Applications</h2>
  <p>

    <script type="text/javascript">
        var <portlet:namespace/>portlets = new Array();
        <c:forEach items="${portletContainer.optionalContainerServices.portletRegistryService.registeredPortletApplications}" var="app">
            var <portlet:namespace/>i = 0;
            <portlet:namespace/>portlets['<c:out value="${app.applicationId}"/>'] = new Array();
            <portlet:namespace/>portlets['<c:out value="${app.applicationId}"/>'][<portlet:namespace/>i++] = 'Select. . .';
          <c:forEach items="${app.portletApplicationDefinition.portlets}" var="portlet">
            <portlet:namespace/>portlets['<c:out value="${app.applicationId}"/>'][<portlet:namespace/>i++] = '<c:out value="${portlet.portletName}"/>';
          </c:forEach>
        </c:forEach>

        function <portlet:namespace/>doSwitch(select) {
            var portletsSelectBox = document.forms['adminForm'].elements['availablePortlets'];
            for(var i = 0; i < portletsSelectBox.options.length;i++) {
                portletsSelectBox.options[i] = null;
            }
            if (select.value == '-') {
                document.forms['adminForm'].elements['availablePortlets'].disabled = true;
            } else {
                portletsSelectBox.disabled = false;
                var pList = <portlet:namespace/>portlets[select.value];
                for (i = 0; i < pList.length; i++) {
                    portletsSelectBox.options[i] = new Option(pList[i], pList[i]);
                }
            }
            <portlet:namespace/>doSwitchButton(portletsSelectBox);
        }

        function <portlet:namespace/>doSwitchButton(select) {
            document.forms['adminForm'].elements['command'][1].disabled = (select.value == 'Select. . .' || select.disabled);
        }
    </script>

    <select name="applications" title='<fmt:message key="console.common.applications" />' onChange="<portlet:namespace/>doSwitch(this)">
      <option value='-'>Select. . .</option>
      <c:forEach items="${portletContainer.optionalContainerServices.portletRegistryService.registeredPortletApplications}" var="app">
      <option value="<c:out value="${app.applicationId}"/>"><c:out value="${app.applicationName}"/></option>
      </c:forEach>
    </select>

    <select name="availablePortlets" title='<fmt:message key="console.common.applications" />' disabled="true" onChange='<portlet:namespace/>doSwitchButton(this)'>

    </select>

    <button name="command" disabled="true" value="add">
        Add Portlet
    </button>
  </p>
</div>
</form>
<%-- Properties for link to app server deployer and help mode file --%>
<fmt:bundle basename="AdminPortlet">
	<fmt:message key="appserver.deployer.url" var="deployerURL"/>
	<fmt:message key="appserver.deployer.help.page" var="deployerHelp"/>
</fmt:bundle> 

<portlet:renderURL portletMode="help" var="deployerhelpURL">
	<%-- needed el taglib to be able to use fmt:message value above --%>
	<portlet-el:param name="helpPage" value="${deployerHelp}"/>
</portlet:renderURL>

<div>
<a href='<c:out value="${deployerURL}"/>' target="_blank">Upload and deploy a new portlet war</a> 
<a href='<c:out value="${deployerhelpURL}"/>'>Help</a>
</div>
