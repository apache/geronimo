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
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://portals.apache.org/pluto" prefix="pluto"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<fmt:setLocale value="<%=request.getLocale()%>" />
<fmt:setBundle basename="org.apache.geronimo.console.i18n.ConsoleResource"/>
<%response.setContentType("text/html; charset=UTF-8");%>

<%@ page import="java.util.List,java.util.ArrayList,java.util.HashMap,
                org.apache.geronimo.pluto.impl.PageConfig,
                org.apache.geronimo.console.navigation.NavigationJsonGenerator"%>

<%
    List<PageConfig> pageConfigList=new ArrayList<PageConfig>();
%>

<c:forEach var="page" items="${driverConfig.pages}">
    <%
        pageConfigList.add((PageConfig) (pageContext.getAttribute("page")));
    %>
</c:forEach>

<%
    NavigationJsonGenerator generator = new NavigationJsonGenerator(request.getLocale());
    String treeJson = generator.generateTreeJSON(pageConfigList, request.getContextPath(), "/images/ico_doc_16x16.gif");
    String treeJsonBasic = generator.generateTreeJSON(pageConfigList, request.getContextPath(), "/images/ico_doc_16x16.gif","basic");
    String listJson = generator.generateQuickLauncherJSON(pageConfigList, request.getContextPath(), "/images/ico_doc_16x16.gif");
    String listJsonBasic = generator.generateQuickLauncherJSON(pageConfigList, request.getContextPath(), "/images/ico_doc_16x16.gif","basic");
%>

<table class="tundra" width="200px" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td CLASS="ReallyDarkBackground"><strong>&nbsp;<fmt:message    key="Console Navigation"/></strong></td>
    </tr>
    <tr>
        <td>&nbsp;<input type="radio" name="mode" id ="mode" checked="checked" onclick="changeMode()"/><fmt:bundle basename="portaldriver"><fmt:message key="console.mode.basic"/></fmt:bundle>
                  <input type="radio"  name="mode" id ="mode" onclick="changeMode()"/><fmt:bundle basename="portaldriver"><fmt:message key="console.mode.advanced"/></fmt:bundle>
        </td>
    </tr>
    <tr><td>&nbsp;&nbsp;</td></tr>
    <tr id="tquickLauncher" style="display:none;">
        <td>&nbsp;&nbsp;<input id="quickLauncher"></td>
    </tr>
    <tr>
        <td>&nbsp;&nbsp;<div id="navigationTreeBasic"></div><div id="navigationTreeAdvanced"></div>
        </td>
    </tr>
</table>
<script language="Javascript" src="<%=request.getContextPath()%>/js/navigation.js" type="text/javascript"></script>
<script language="Javascript">
   dojo.require("dojo.data.ItemFileReadStore");
   dojo.require("dijit.form.FilteringSelect");
   dojo.require("dijit.Tree");
   var treeData = <%=treeJson%>;
   var treeDataBasic =<%=treeJsonBasic%>;
   var listData = <%=listJson%>;
   var listDataBasic = <%=listJsonBasic%>;
   var treeModel="";
   var navigationTreeBasic="";
   var navigationTreeAdvanced="";
   var filterSelect="";
   
   var treeStore = new dojo.data.ItemFileReadStore
    ({
         data: {
             identifier: 'id',
             label: 'label',
             items: treeData
             }
     });
   var treeStoreBasic = new dojo.data.ItemFileReadStore
    ({
         data: {
             identifier: 'id',
             label: 'label',
             items: treeDataBasic
             }
     });
    var listStore = new dojo.data.ItemFileReadStore({
       data: {
           identifier: 'name',
           label: 'label',
           items: listData
           }
    });
    var listStoreBasic = new dojo.data.ItemFileReadStore({
        data: {
            identifier: 'name',
            label: 'label',
            items: listDataBasic
            }
     });
    dojo.addOnLoad(function() { createNavigationTree(treeStoreBasic,listStoreBasic,"basic"); });
</script>
