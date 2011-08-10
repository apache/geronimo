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

<%@ page import="java.util.List,java.util.ArrayList,java.util.Map,
                org.apache.geronimo.pluto.impl.PageConfig,
                org.apache.geronimo.console.navigation.TreeNode,
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
    List<PageConfig> filteredPageConfigList = NavigationJsonGenerator.filterPagesByRole(pageConfigList,request);
    NavigationJsonGenerator generator = new NavigationJsonGenerator(request.getLocale());

    Map<String, TreeNode> treeBasic = generator.getNavigationTree(filteredPageConfigList, "basic");
    String treeJsonBasic = generator.generateTreeJSON(treeBasic, request.getContextPath(), "/images/ico_doc_16x16.gif", "basic", 8);
    String listJsonBasic = generator.generateQuickLauncherJSON(treeBasic, request.getContextPath(), "/images/ico_doc_16x16.gif", "basic");
    boolean isBasicTreeHasValidItem=generator.isTreeHasValidItem(treeBasic, "basic");
        
    Map<String, TreeNode> treeAll = generator.getNavigationTree(filteredPageConfigList, "all");
    String treeJson = generator.generateTreeJSON(treeAll, request.getContextPath(), "/images/ico_doc_16x16.gif", "all", 8);
    String listJson = generator.generateQuickLauncherJSON(treeAll, request.getContextPath(), "/images/ico_doc_16x16.gif", "all");
%>

<!-- panel div -->
<div id="panelDiv">
    <div style="height:10px"></div>
    
    <!-- quick launcher div -->
    <div id="quickLauncherDiv">
        <strong>&nbsp;Quick Launch:</strong>
        <input id="quickLauncher" />
    </div>
    
    <div style="height:5px"></div>
    
    <!-- mode div -->
    <div id="modeSwitcherDiv" class="<%=isBasicTreeHasValidItem?"":"Hidden"%>">
        <strong>&nbsp;Navigator:</strong>
        <input type="radio" name="mode" id="mode" checked="checked" onclick="changeMode()"/>&nbsp;<fmt:bundle basename="portaldriver"><fmt:message key="console.mode.basic"/></fmt:bundle>&nbsp;
        <input type="radio" name="mode" id="mode" onclick="changeMode()"/>&nbsp;<fmt:bundle basename="portaldriver"><fmt:message key="console.mode.advanced"/></fmt:bundle>
    </div>
    
    <div style="height:5px"></div>
</div>            
           
<!-- tree div -->
<div id="treeDiv">
    <div id="navigationTreeBasic"></div>
    <div id="navigationTreeAdvanced"></div>
</div>
            

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
     
    <% if(isBasicTreeHasValidItem) {%>
   
        dojo.addOnLoad(function() { createNavigationTree(treeStoreBasic,listStoreBasic,"basic"); });
    
    <%} else {%>
    
        dojo.addOnLoad(function() { createNavigationTree(treeStore,listStore,"advanced"); });
   
    <%}%>
    
    var oldSelectedValue;
    if(dijit.byId("quickLauncher")!=null){
        dijit.byId("quickLauncher").store=listStore;
    }else{
        filterSelect = new dijit.form.FilteringSelect(
                {
                    store: listStore,
                    searchAttr: "name",
                    labelAttr: "label",
                    labelType: "html",
                    onChange: function(event){
                        if(this.isValid(true) && this.value != oldSelectedValue) {
                            oldSelectedValue = this.value;
                            quickLaunchPortlets(this.value);
                        }
                    },
                    onKeyPress: function(event){        
                        if(event.keyCode!=dojo.keys.ENTER) return;
                        if(this.isValid(true) && this.value == oldSelectedValue)
                            quickLaunchPortlets(this.value);      
                    }
                    
                },
                dojo.byId("quickLauncher")
         );
    }
</script>
