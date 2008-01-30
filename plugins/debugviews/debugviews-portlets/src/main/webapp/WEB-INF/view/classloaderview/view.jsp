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
<%@ page import="javax.portlet.RenderRequest,org.apache.geronimo.console.classloaderview.ClassLoaderViewPortlet"%>
<fmt:setBundle basename="debugviews"/>
<portlet:defineObjects />

<%
    // retrieve and set attributes
    boolean inverse = ((Boolean)renderRequest.getPortletSession().getAttribute("inverse")).booleanValue();
    
    String selectedNode = (String)renderRequest.getPortletSession().getAttribute("selectedNode");

    if (selectedNode != null) {
        renderRequest.getPortletSession().removeAttribute("selectedNode");
    }

    ClassLoaderViewPortlet cp = (ClassLoaderViewPortlet) renderRequest.getPortletSession().getAttribute("classloaderTree");
    renderRequest.getPortletSession().removeAttribute("classloaderTree");
    String childs = cp.getJSONTrees(inverse);
%>

    <script type="text/javascript" src="/dojo/0.4/dojo.js"></script>

    <script type="text/javascript">
        dojo.require("dojo.lang.*");
        dojo.require("dojo.widget.*");
        // Pane includes
        dojo.require("dojo.widget.ContentPane");
        dojo.require("dojo.widget.LayoutContainer"); // Before: LayoutPane
        dojo.require("dojo.widget.SplitContainer"); // Before: SplitPane
        // Tree includes
        dojo.require("dojo.widget.Tree");
        dojo.require("dojo.widget.TreeBasicController");
        dojo.require("dojo.widget.TreeContextMenu");
        dojo.require("dojo.widget.TreeSelector");
        // Tab includes
        dojo.require("dojo.widget.TabContainer");
        // Etc includes
        dojo.require("dojo.widget.SortableTable");
        dojo.require("dojo.widget.ComboBox");
        dojo.require("dojo.widget.Tooltip");
        dojo.require("dojo.widget.validate");
        // Includes Dojo source for debugging
        // dojo.hostenv.writeIncludes();
    </script>

<script>
    dojo.require("dojo.widget.*");
    dojo.require("dojo.widget.TreeV3");
    dojo.require("dojo.widget.TreeNodeV3");
    dojo.require("dojo.widget.TreeBasicControllerV3");
    dojo.require("dojo.widget.TreeSelectorV3");
    dojo.require("dojo.widget.TreeEmphasizeOnSelect");
    dojo.require("dojo.widget.TreeToggleOnSelect");
        
    dojo.hostenv.writeIncludes();

    dojo.addOnLoad(function() {
        var tree = dojo.widget.manager.getWidgetById('tree');

        var treeNodes = <%= childs %>;          

        tree.setChildren(treeNodes);

        dojo.event.topic.subscribe('tree/afterExpand',
            function(source) { 
                for (i in source['source'].children)
                    if (source['source'].children[i].title.indexOf('link::') == 0) {
                        var node = source['source'].children[i].title.substring(6);

                        load(node);

                        var curr = dojo.widget.byId(node);
                        var chil = replace(curr.children, curr.widgetId, source['source'].children[i].widgetId);

                        source['source'].children[i].setTitle(curr.title);
                        source['source'].children[i].setChildren(chil);  
                    }
            }
        );
<%
    if (selectedNode != null && ! selectedNode.equals("")) {
%>
        findPrevious('<%= selectedNode %>');
<%
    }
%>
    }); // end of addOnLoad

    var lastFound = '';
    var doCheck = false;

    function replace(children, from, to) {
        var child = [];

        for (name in children) {
            var chds = [];
            var newname = '';
            var currTitle = children[name].title;

            if (children[name].widgetId.indexOf(from) == 0)
                newname = to+children[name].widgetId.substring(from.length);

            if (children[name].children)
                chds = replace(children[name].children, from , to);

            child.push({title:currTitle,widgetId:newname,children:chds});
        }

        return child;
    }

    function check(curr,last) {
        var cr = curr.split(".");
        var ls = last.split(".");

        for (var i = 0; i < cr.length; i++)
            if(parseInt(cr[i]) < parseInt(ls[i]))
                return false;

        return true;
    }

    function findNext(children, str) {
        for (var child = 0; child < children.length; child++) {
            if (doCheck) {
                if (! check(children[child].widgetId, lastFound))
                    continue;
                else
                    if(children[child].widgetId == lastFound)doCheck = false;

            }

            if (children[child].title.indexOf("link::") == 0) {
                var node = children[child].title.substring(6);

                load(node);

                var curr = dojo.widget.byId(node);
                var ret = findNext([curr],str)

                if (ret) {
                    var chil = replace(curr.children, curr.widgetId, children[child].widgetId); 

                    children[child].setTitle(curr.title);
                    children[child].setChildren(chil);
                    ret = findNext([children[child]],str);

                    if (ret)
                        return ret;
                }
            } else {
                if (children[child].title.indexOf(str) != -1 && children[child].widgetId != lastFound && !doCheck) {
                    return children[child].widgetId;
                }

                if (children[child].children) {
                    var ret = findNext(children[child].children, str);

                    if (ret) {
                        return ret;
                    }
                }
            }
        }

        return ;
    }

    function load(node) {
        var nodes = node.split(".");
        var curr = "";

        for (nod in nodes) {
            if (curr == "")
                curr = curr + nodes[nod];
            else
                curr = curr + "." + nodes[nod];

            if (dojo.widget.byId(curr).state != "LOADED" && dojo.widget.byId(curr).children.length != 0)
                dojo.widget.byId(curr).setChildren(dojo.widget.byId(curr).children);
        }
    }

    function select(node) {
        var nodes = node.split(".");
        var curr = "";

        for (nod in nodes) {
            if (curr == "")
                curr = curr + nodes[nod];
            else
                curr = curr + "." + nodes[nod];

            if (dojo.widget.byId(curr).state != "LOADED" && dojo.widget.byId(curr).children.length != 0)
                dojo.widget.byId(curr).setChildren(dojo.widget.byId(curr).children);

            dojo.widget.byId('controller').expandToLevel(dojo.widget.byId(curr), 1);
        }

        dojo.widget.byId('selector').select(dojo.widget.byId(node));
    }

    function findInSelected(selectedNodes) {
        var search = document.getElementById("searchText").value;

        if (search == "") {
            debug.innerHTML = "<br/>Failure in search: No text to search";
            alert('Failure in search: No text to search');
            return;
        }

        var v= findNext(selectedNodes, search);

        if (v) {
            dojo.widget.byId('selector').deselectAll();
            select(v);
            document.getElementById("findNext").disabled = false;
            lastFound = v;
            doCheck = true;
        } else {
            debug.innerHTML = "<br/>Failure in search: No more matching result found";
            alert('Failure in search: No more matching result found');
            lastFound = '';
            doCheck = false;
        }
    }

    function textChange() {
        document.getElementById("findNext").disabled=true;
        lastFound = '';
        doCheck = false;
    }

    var lastSearchOn = '';

    function search() {
        debug.innerHTML = "";
        document.body.style.cursor = "wait";
        lastFound = '';
        doCheck = false;
        lastSearchOn = [];

        var nodes;

        if (document.getElementById("inSelected").checked) {
            nodes = dojo.widget.byId('selector').selectedNodes;

            if (nodes == '') {
                debug.innerHTML = "<br/>Failure in search: No node selected to search.";
                alert('Failure in search: No node selected to search');
            } else {
                for (obj in nodes)
                    lastSearchOn.push(nodes[obj]);  

                findInSelected(nodes);
            }
        } else {
            nodes = dojo.widget.byId('tree').children;
            lastSearchOn = dojo.widget.byId('tree').children;
            findInSelected(nodes);
        }

        document.body.style.cursor = '';
    }

    function findPrevious(search) {
        var selectNodes = dojo.widget.byId('tree').children;
        var v = findNext(selectNodes,search);

        if (v) {
            dojo.widget.byId('selector').deselectAll();
            select(v);
            lastFound = '';
            doCheck = false;
        }
    }

    function searchContinue() {
        debug.innerHTML = "";
        document.body.style.cursor = "wait";
        findInSelected(lastSearchOn);
        document.body.style.cursor = '';
    }

    function getAction() {
        var nodes = dojo.widget.manager.getWidgetById('selector').selectedNodes[0];

        if (nodes != undefined) {
            if (nodes.title == 'Classes' || nodes.title == 'Interfaces') {
                document.clform.snNode.value = nodes.parent.title;
            } else {
                document.clform.snNode.value = nodes.title;
            }
        }

        document.clform.action = '<portlet:actionURL><portlet:param name="action" value="invert"/></portlet:actionURL>';

        return true;
    }
</script>

<form name="clform" onSubmit="return getAction()">
<input type="hidden" name="snNode" value=""/>
<input type="hidden" name="inverse" value="${inverse}"/>
<TABLE cellpadding="1" cellspacing="1" border="1">
 <tr>
  <td><b><fmt:message key="classloaderview.view.searchText"/>:</b> <input type="text" name="searchText" id="searchText"
   onChange="javascript:textChange()"/> <input type="button"
   value='<fmt:message key="debugviews.common.find"/>' onClick="javascript:search()" /> <input type="button"
   id="findNext" value='<fmt:message key="debugviews.common.findNext"/>' onClick="javascript:searchContinue()"
   disabled=true /> <fmt:message key="classloaderview.view.searchOnlySelected"/>:<input type="checkbox"
   id="inSelected" onChange="javascript:textChange()" /></td>
 </tr>
</table>
<input type="submit" value='<fmt:message key="classloaderview.view.invertTree"/>' />
<br />

<div dojoType="TreeBasicControllerV3" widgetId="controller"></div>
<div dojoType="TreeSelectorV3" widgetId="selector"></div>
<div dojoType="TreeEmphasizeOnSelect" selector="selector"></div>
<div dojoType="TreeToggleOnSelect" selector="selector"
 controller="controller"></div>
<div dojoType="TreeDocIconExtension" widgetId="iconcontroller"
 templateCssPath="<%= renderResponse.encodeURL(renderRequest.getContextPath() + "/TreeDocIcon.css") %>"></div>
<div dojoType="TreeV3" listeners="controller;selector;iconcontroller"
 widgetId='tree' allowedMulti='false'></div>

<div id="debug"></div>
</form>
