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
    String listJson = generator.generateQuickLauncherJSON(pageConfigList, request.getContextPath(), "/images/ico_doc_16x16.gif");
%>

<table class="tundra" width="200px" border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td CLASS="ReallyDarkBackground"><strong>&nbsp;<fmt:message    key="Console Navigation" /></strong></td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>&nbsp;&nbsp;<input id="quickLauncher"></td>
    </tr>
    <tr>
        <td>&nbsp;&nbsp;<div id="navigationTree"></div>
        </td>
    </tr>
</table>

<div id="links" style="visibility: hidden; width: 0px; height: 0px;">
<%=generator.generateLinks(pageConfigList, request.getContextPath(), "/images/ico_doc_16x16.gif")%>
</div>

<script type="text/javascript" src="/dojo/dojo/dojo.js" djConfig="parseOnLoad: true"></script>

<script type="text/javascript">

<%-- scripts to create the navigation tree--%>

   dojo.require("dojo.data.ItemFileReadStore");
   dojo.require("dijit.form.FilteringSelect");
   dojo.require("dijit.Tree");
   
   dijit._TreeNode.prototype.setLabelNode = function (label) {
       this.labelNode.innerHTML = label;
    };

   var treeData = <%=treeJson%>;
   var listData = <%=listJson%>;
   var treeStore;
   var navigationTree;

function load() {

   var listStore= new dojo.data.ItemFileReadStore
       ({
       data: {
           identifier: 'name',
           label: 'label',
           items: listData
           }
    });

             
  treeStore = new dojo.data.ItemFileReadStore
    ({
         data: {
             identifier: 'id',
             label: 'label',
             items: treeData
             }
     });

       
   var treeModel = new dijit.tree.ForestStoreModel({
       store: treeStore
   });
       
   navigationTree = new dijit.Tree
           (
         {  model: treeModel,
            showRoot: false,
            openOnClick: true,
            onClick: function(treeNodeItem,treeNode) {
            
            var anchorNode=treeNode.labelNode.childNodes[2];

             if(anchorNode)
                {
                 displayPortlets(anchorNode);
                }                     
            }
         },
         dojo.byId("navigationTree")
       );


   var filterSelect = new dijit.form.FilteringSelect
       (
          {
           id: "quickLauncher",
           value: "Server Log",
           store: listStore,
           searchAttr: "name",
           name: "quickLauncher",
           promptMessage: "type and press enter to quick launch",
           labelAttr: "label",
           labelType: "html",
           onKeyPress: function(event){        
               if(event.charCode!=dojo.keys.ENTER) return;
                   quickLaunchPortlets(this.value);      
           },
           onChange: function(event){
               quickLaunchPortlets(this.value);
           }
         },
         dojo.byId("quickLauncher")
         );
  }

   function quickLaunchPortlets(portalPageName){

       var anchors = dojo.byId("links").getElementsByTagName("a"); 
       
       for (var i = 0; i < anchors.length; i++) { 
           anchorName = anchors[i].innerHTML; 
           if ( anchorName == portalPageName) { 
               displayPortlets(anchors[i]);
               findAndSelect(portalPageName,navigationTree.rootNode);
               return;
           }
       }

   }

    function displayPortlets(anchor){

        document.getElementById('content').style.display = 'none';
        document.getElementById("portletsFrame").src = anchor.href+"?formId="+formID;
        setTimeout('returnToTop()', 30);
     }
     
    function returnToTop(){
        window.scrollTo(0,0);
        return false;
     }
     
       

    dojo.addOnLoad(load);
   

<%-- scripts to expand  and select tree node automatically when open menu item from quick launcher --%>

   
function findAndSelect(key, rootNode)
    {
        
        var nodes = [];

        if(findRecur(rootNode.item.children, key, nodes))
        {
            select(nodes);
        } 
    }


function findRecur(items, key, nodes) 
    {
        for (var child = 0; child < items.length; child++) {

            nodes.push(items[child]);
            var label = treeStore.getLabel(items[child]);
            if (label && label.indexOf(key) != -1)
                return true;

            if (items[child].children && findRecur(items[child].children, key, nodes))
                return true;
            nodes.pop();
        }
        return false;
    }
    
function select(nodes)
    {
        var i;
        function expandParent(node)
        {
            if(node && !node.isExpanded)
            {
                expandParent(node.getParent());
                navigationTree._expandNode(node);
            }
        }
        //make sure the ancestor node expanded before
        expandParent(navigationTree._itemNodeMap[treeStore.getIdentity(nodes[0])].getParent());
        for (i = 0;;i++) {
            node  = navigationTree._itemNodeMap[treeStore.getIdentity(nodes[i])];
            if(i < nodes.length-1)
                navigationTree._expandNode(node);
            else 
            {
                navigationTree.focusNode(node);
                return node;
            }
        }
    }
 
        
</script>


