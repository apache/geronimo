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
<%@ page import="javax.portlet.RenderRequest"%>
<fmt:setBundle basename="debugviews"/>
<portlet:defineObjects/>

<% String dwrForwarderServlet = "/console/dwr2"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/JNDIViewHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script type="text/javascript" src="/console/dojo/dojo/dojo.js" djConfig="parseOnLoad:true"></script>
<script type="text/javascript" src="/console/dojo/dijit/dijit.js"></script>

<script type="text/javascript">
    //style class
    document.body.className="claro";

    //import dojo lib
    dojo.require("dojo.data.ItemFileWriteStore");
    dojo.require("dijit.tree.ForestStoreModel");
    dojo.require("dijit.Tree");
    dojo.require("dijit.TitlePane");
    dojo.require("dijit.form.Button");

</script>

<!------------------------>
<!--     Page Stuff     -->
<!------------------------>
<script type="text/javascript">
    var debugStore = null;
    var debugModel = null;
    var debugTree = null;
    
    dojo.addOnLoad(
        function(){
            JNDIViewHelper.getTrees({callback:createTree,async:false});
        }
    );
    
    function createTree(treeData){                    
        debugStore = new dojo.data.ItemFileReadStore({data:treeData});
        debugModel = new dijit.tree.ForestStoreModel({
            store:debugStore, 
            rootId:"JNDI", 
            rootLabel:"JNDI", 
            childrenAttrs:["children"],
            mayHaveChildren: function(item){
                	return item.children && item.children.length > 0;
            	}
            });
        debugTree = new dijit.Tree({
            "class": "claro",
            showRoot: false,
            model: debugModel,
            openOnClick: true,
            style: "width: 100%;overflow: auto;overflow-y: hidden;"
            },
            "treeContainer");
    }
    
    var lastFoundId = -1;
    var SearchOn = []; //For ignore the fake root it better be a array
    var caseSensitive = true;
    function findRecur(items, str, path) 
    {
        for (var child = 0; child < items.length; child++) {

            path.push(items[child]);
            var label = debugStore.getLabel(items[child]);
            if (label && debugStore.getIdentity(items[child]) > lastFoundId)
            {
                if (caseSensitive && label.indexOf(str) != -1 )
                    return true;
                if (!caseSensitive && label.toLowerCase().indexOf(str.toLowerCase()) != -1)
                    return true;
            }
            if (items[child].children && findRecur(items[child].children, str, path))
                return true;
            path.pop();
        }
        return false;
    }
    function select(path)
    {
        var i;
        function expandParent(node)
        {
            if(node && !node.isExpanded)
            {
                expandParent(node.getParent());
                debugTree._expandNode(node);
            }
        }
        //make sure the ancestor node expanded before
        expandParent(debugTree._itemNodesMap[debugStore.getIdentity(path[0])][0].getParent());
        for (i = 0;;i++) {
            node  = debugTree._itemNodesMap[debugStore.getIdentity(path[i])][0];
            if(i < path.length-1)
                debugTree._expandNode(node);
            else 
            {
                debugTree.focusNode(node);
                return node;
            }
        }
    }
    function findAndSelect(key, scope)
    {
        var path = [], node, res = false;
        document.body.style.cursor = "wait";
        if(!findRecur(scope, key, path))
        {
            lastFoundId = -1;
        } else {
            node = select(path);
            lastFoundId = debugStore.getIdentity(node.item);
            res = true;
        }
        document.body.style.cursor = '';
        return res;
    }
    function search() {
        lastFoundId = -1;
        
        var key = document.getElementById("searchText").value;
        if (key == "")
        {
            alert('<fmt:message key="debugviews.common.noText"/>');
            return;
        }
        caseSensitive = document.getElementById("caseSensitiveSelected").checked;
        if (document.getElementById("inSelected").checked) {
            node = debugTree.lastFocused;
            if (node == '' || node == debugTree.rootNode) {
                alert('<fmt:message key="debugviews.common.pleaseSelect"/>');
                return;
            }
            SearchOn = [node.item];
        } else {
            SearchOn = debugTree.rootNode.item.children;
        }
        if(!findAndSelect(key, SearchOn))
            alert('<fmt:message key="debugviews.common.notFound"/> ' + key);
        else 
            findNext.attr("disabled", false);
    }

    function searchContinue() {
        var key = document.getElementById("searchText").value;
        if(!findAndSelect(key, SearchOn))
            alert('<fmt:message key="debugviews.common.notFound"/> ' + key);
    }

    function searchReset() {
        findNext.attr("disabled", true);
        lastFoundId = -1;
        //The only way to enable "findNext" button is to search() 
    }
</script>
<div dojoType="dijit.TitlePane" title="<fmt:message key="debugviews.common.find"/>" >

    <b><label for="searchText"><fmt:message key="jndiview.view.searchText"/></label>:</b> <input type="text" id="searchText" onChange="javascript:searchReset()"/>
    <button dojoType="dijit.form.Button" jsId="findButton" ><fmt:message key="debugviews.common.find"/>
        <script type="dojo/method" event="onClick" args="btn">
            search();
        </script>
    </button>
    <button dojoType="dijit.form.Button" jsId="findNext" disabled="true"><fmt:message key="debugviews.common.findNext"/>
        <script type="dojo/method" event="onClick" args="btn">
            searchContinue();
        </script>
    </button>
    <label for="inSelected"><fmt:message key="jndiview.view.searchOnlySelected"/></label>:<input type="checkbox" id="inSelected" onChange="javascript:searchReset()"/>
    <fmt:message key="jndiview.view.caseSensitive"/>:
    <input type="checkbox" id="caseSensitiveSelected" checked="true" onChange="javascript:searchReset()"/>

</div>
<br />
<div id="treeContainer">
</div>
