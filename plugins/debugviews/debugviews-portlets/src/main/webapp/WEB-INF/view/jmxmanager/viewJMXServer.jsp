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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<%@ page import="org.apache.geronimo.console.jmxmanager.JMXManagerHelper" %>
<portlet:defineObjects/>

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

<!------------------------>
<!--     DOJO Stuff     -->
<!------------------------>

<%
    // JMX icon
    String jmxIconURI = renderResponse.encodeURL(renderRequest.getContextPath() + "/ico_filetree_16x16.gif");
%>

<style type="text/css">
<!-- Splitter styles -->
body .dojoHtmlSplitterPanePanel {
    background: white;
    overflow: auto;
}

<!-- Sortable table styles -->
table {
    font-family:Lucida Grande, Verdana;
    font-size:0.8em;
    width:100%;
    cursor:default;
}

* html div.tableContainer {    /* IE only hack */
    width:95%;
    border:1px solid #ccc;
    height: 285px;
    overflow-x:hidden;
    overflow-y: auto;
}

table td, table th {
    /* border-right:1px solid #999; */
    /* padding:2px; */
    font-weight: normal;
}

table thead td, table thead th { 
    background: #2581C7; /* #94BEFF */
    color: #FFFFFF;    /* added */
}

* html div.tableContainer table thead tr td,
* html div.tableContainer table thead tr th {
    /* IE Only hacks */
    position:relative;
    top:expression(dojo.html.getFirstAncestorByTag(this,'table').parentNode.scrollTop-2);
}

html>body tbody.scrollContent {
    height: 100%; /* 262px */
    overflow-x: hidden;
    overflow-y: hidden; /* auto */
}

tbody.scrollContent td, tbody.scrollContent tr td {
    background: #FFF;
    padding: 2px;
}

tbody.scrollContent tr.alternateRow td {
    background: #F2F2F2; /* #e3edfa */
    padding: 2px;
}

tbody.scrollContent tr.selected td {
    background: yellow;
    padding: 2px;
}

tbody.scrollContent tr:hover td {
    background: #a6c2e7;
    padding: 2px;
}

tbody.scrollContent tr.selected:hover td {
    background: #ff3;
    padding: 2px;
}
</style>

<script>
    /**
     * Global vars 
     */
    var _selectedNode = null;  // Selected tree node
    var _attribValueID = null; // ID of the attribute to update in the Attributes table
    var _attribValue = null;   // Value of the attribute to update in the Attributes tabl

    /**
     * Get selected node 
     */
    function getSelectedNode() {
        var tree = dojo.widget.byId('jmxTree');
        var selectedNode = tree.selector.selectedNode;

        return selectedNode;
    }

    /**
    * Set the mouse pointer (NOT USED)
    */
    function setPointer(cursor) {
       if (document.all) {
            // Solution 1
            // for (var i = 0; i < document.all.length; i++) {
            //     document.all(i).style.cursor = cursor;
            // }
        
            // Solution 2
            // document.all('mainLayout').style.cursor = cursor;
            // document.getElementById('mainLayout').style.cursor = cursor;

            // $('mainLayout').style.cursor = cursor;
            // $('rootfragment').style.cursor = cursor;
            dojo.byId("mainLayout").style.cursor = cursor;
            dojo.byId("rootfragment").style.cursor = cursor;
        }
    }

    /**
     * Dojo init stuff 
     */
    dojo.addOnLoad (function() {
        var treeController = dojo.widget.byId('treeController');

        /**
         * Tree click event handler (expand & contract nodes)
         */
        dojo.event.connect(
            'before',
            treeController,
            'onTreeClick',
            {
                beforeTreeClick: function(evt) {
                    var selectedNode = evt.source;

                    if ((selectedNode.state == 'UNCHECKED') && (selectedNode.isExpanded == false)) {
                        _selectedNode = selectedNode;

                        // Check if it's 'searchMBeans'
                        if ((selectedNode.widgetId == 'searchMBeans') && (selectedNode.children.length == 0)) {
                            // skip DWR call
                            selectedNode.state = 'LOADED';

                            return;
                        } else {
                            var id = selectedNode.widgetId;

                            if (id.indexOf('<PATTERN>') != -1) {
                                // Remove pattern marker
                                var pattern = id.substring(id.indexOf('<PATTERN>') + '<PATTERN>'.length);  
                                JMXHelper.listByPattern(pattern, <portlet:namespace/>updateJMXTree);
                            } else if (selectedNode.widgetId.indexOf('<J2EETYPE>') != -1) {
                                // Remove j2ee type marker
                                var j2eeType = id.substring(id.indexOf('<J2EETYPE>') + '<J2EETYPE>'.length);  
                                JMXHelper.listByJ2EEType(j2eeType, <portlet:namespace/>updateJMXTree);
                            } else if (selectedNode.widgetId.indexOf('<SVCMODULE>') != -1) {
                                // Remove service module marker
                                var svcModule = id.substring(id.indexOf('<SVCMODULE>') + '<SVCMODULE>'.length);  
                                JMXHelper.listBySubstring(svcModule, <portlet:namespace/>updateJMXTree);
                            } else if (selectedNode.widgetId == 'statisticsProviderMBeans') {
                                // Get statistics provider MBeans
                                JMXHelper.getStatsProvidersMBeans(<portlet:namespace/>updateJMXTree);
                            } else {
                                // Marker not recognized
                            }
                        }
                    }
                }
            },
            'beforeTreeClick'
        );

        /**
         * Tree node title click event handler 
         */
        var tree = dojo.widget.byId('jmxTree');

        dojo.event.topic.subscribe(
            tree.eventNames.titleClick,
            function(message) {
                window.aName = message.source.widgetId;

                if (window.aName.indexOf('::') == -1) {
                    // No marker means not an abstract name, clear tables
                    dwr.util.removeAllRows('basicInfoTableBody');
                    dwr.util.removeAllRows('attributesTableBody');
                    dwr.util.removeAllRows('operationsTableBody');
                } else {
                    // Remove marker to get abstract name
                    window.aName = window.aName.substring(window.aName.indexOf('::') + 2);
                    JMXHelper.getMBeanInfo(window.aName, <portlet:namespace/>updateBasicInfoTable);
                    JMXHelper.getAttributes(window.aName, <portlet:namespace/>updateAttributesTable);
                    JMXHelper.getOperations(window.aName, <portlet:namespace/>updateOperationsTable);
                    JMXHelper.getMBeanStats(window.aName, <portlet:namespace/>updateStatsTable);
                }
            }
        );

        /**
         * Tree context menu event handler: 'Refresh' (NOT USED)
         */
        dojo.event.topic.subscribe(
            'treeContextMenuRefresh/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();

                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }

                if ((selectedNode.state == 'UNCHECKED') && (selectedNode.isExpanded == false)) {
                    // Unchecked tree node, do nothing
                } else {
                    // Remove children
                    var treeController = dojo.widget.byId('treeController');
                    var children = selectedNode.children;
                    while (children.length > 0) {
                        var node = children[0];
                        treeController.removeNode(node);
                        node.destroy();
                    }

                    // Add children
                    _selectedNode = selectedNode;
                    // TODO: Insert add tree node children code here
                }
            }
        );

        /**
         * Tree context menu event handler: 'Search...' 
         */
        dojo.event.topic.subscribe(
            'treeContextMenuSearch/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();

                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }

                var mainTabContainer = dojo.widget.byId('mainTabContainer');
                var searchTab = dojo.widget.byId('searchTab');

                mainTabContainer.selectTab(searchTab);
            }
        );

        /**
         * Tree context menu event handler: 'View Attributes'
         */
        dojo.event.topic.subscribe(
            'treeContextMenuViewAttribs/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();

                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }

                var mainTabContainer = dojo.widget.byId('mainTabContainer');
                var attributesTab = dojo.widget.byId('attributesTab');

                mainTabContainer.selectTab(attributesTab);
            }
        );

        /**
         * Tree context menu event handler: 'View Operations' 
         */
        dojo.event.topic.subscribe(
            'treeContextMenuViewOps/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();

                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }

                var mainTabContainer = dojo.widget.byId('mainTabContainer');
                var operationsTab = dojo.widget.byId('operationsTab');

                mainTabContainer.selectTab(operationsTab);
            }
        );

        /**
         * Tree context menu event handler: 'View Info' 
         */
        dojo.event.topic.subscribe(
            'treeContextMenuViewInfo/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();

                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }

                var mainTabContainer = dojo.widget.byId('mainTabContainer');
                var infoTab = dojo.widget.byId('infoTab');

                mainTabContainer.selectTab(infoTab);
            }
        );

        /**
         * Tree context menu event handler: 'View Stats' 
         */
        dojo.event.topic.subscribe(
            'treeContextMenuViewStats/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();

                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }

                var mainTabContainer = dojo.widget.byId('mainTabContainer');
                var statsTab = dojo.widget.byId('statsTab');

                mainTabContainer.selectTab(statsTab);
            }
        );
    }
);

/**
 * Search button clicked event handler 
 */
function searchBtnClicked() {
    var jmxQuery = dojo.widget.byId('jmxQuery').getValue();
    JMXHelper.listByPattern(jmxQuery, <portlet:namespace/>updateSearchMBeansTreeNode);
}

    /**
     * Refresh managed object stats button clicked event handler 
     */
    function refreshStatsBtnClicked() {
        var abstractName = window.aName;

        JMXHelper.getMBeanStats(abstractName, <portlet:namespace/>updateStatsTable);
    }
</script>

<!----------------------->
<!--     DWR Stuff     -->
<!----------------------->

<% String dwrForwarderServlet = "/console/dwr2"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/JMXHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script>
/**
 * Sync calls 
 */
DWREngine.setAsync(false);

/**
 * Generic error handler 
 */
DWREngine.setErrorHandler(
    function (errorString) {
        alert('Error: ' + errorString);
    }
);

/**
 * DWR table render option
 */
var tableOption = {
    rowCreator: function(options) {
        var row = document.createElement('tr');
        return row;
    },
    cellCreator: function(options) {
        var td = document.createElement('td');
        if ((options.rowIndex % 2) == 0) {
            td.style.backgroundColor = '#FFFFFF';
        } else {
            td.style.backgroundColor = '#F2F2F2';
        }
        return td;
    }
}

/**
 * Update MBean attributes table 
 */
function <portlet:namespace/>updateAttributesTable(attributes) {
    dwr.util.removeAllRows('attributesTableBody');
    dwr.util.addRows(
        'attributesTableBody', 
        attributes,
        [ 
            function(attribute) { /* AttribName Column */
                // TODO color code the Name for magical and nogetters atttribute 
                // var hasGetter = attribute['getterName'];
                // var hasValue = attribute['value'];
                // !hasGetter && hasValue implies magical attribute 
                return attribute['name']; 
            },
            function(attribute) { /* AttribValue  Column */
                var attribName = attribute['name'];
                var attribValue = attribute['value'];
                var attribType = attribute['type'];
                var hasGetter = attribute['getterName'];
                var isWritable = attribute['writable'];
                var attribSetterName = attribute['setterName'];
                var attribValueID = attribSetterName + '_value';
                // Special case for deployment descriptor
                if (attribName == 'deploymentDescriptor') {
                    return '<textarea cols="60" rows="15" wrap="OFF" readonly>' + attribValue + '</textarea>';
                }
                if (isWritable == 'true') {
                    // OPTION: attribValue = "<input type='text' id='" + attribValueID + "' value='" + attribValue + "' style='width: 300px;' disabled/>";
                    attribValue = "<div id='" + attribValueID + "'>" + attribValue + "</div>";       
                    return attribValue;
                }
                return attribValue;
            },
            function(attribute) { /* AttribType Column */
                return attribute['type'];
            },
            function(attribute) { /* AttribGetterName Column */
                return attribute['getterName'];
            },
            function(attribute) { /* AttribSetterName Column */
                var attribSetterName = attribute['setterName']; 
                var abstractName = window.aName;
                var attribName = attribute['name'];
                var attribType = attribute['type'];
                var attribValueID = attribSetterName + '_value';
                var isWritable = attribute['writable'];
                if (isWritable == 'true') {
                    attribSetterName = 
                        "<div align='center'>" +
                        "<input type='button' value='" + attribSetterName + "' " + 
                        "onclick='setAttribFN(\"" + abstractName + "\", \"" + attribName + "\", dwr.util.getValue(\"" + attribValueID + "\"), \"" + attribValueID + "\", \"" + attribType + "\")' " +
                        "/>&nbsp;" + 
                        "</div>";
                    return attribSetterName;
                }
                return attribSetterName; 
            },
            function(attribute) { /* AttribManageable Column */
                var isManageable = attribute['manageable'];
                if (isManageable == 'true') {
                    return 'Yes';   
                }
                return 'No';
                 
            },
            function(attribute) { /* AttribPersistent Column */
                var isPersistent = attribute['persistent']; 
                if (isPersistent == 'true') {
                    return 'Yes'; 
                }
                return 'No';
            }
            /*
            function(attribute) { AttribReadable Column
                return attribute['readable']; 
            },
            function(attribute) { AttribWritable Column
                return attribute['writable'];
            },
            */
        ],
        tableOption
    );

    // Render sortable table
    var tbl = dojo.widget.byId("attribsTable");
    if (tbl) {
        tbl.render(false);
    }
}

/**
 * Update MBean operations table 
 */
function <portlet:namespace/>updateOperationsTable(operations) {
    dwr.util.removeAllRows('operationsTableBody');
    dwr.util.addRows(
        'operationsTableBody', 
        operations,
        [
            function(operation) { /* OperName  Column */
                var abstractName = window.aName;
                var opName = operation['name'];
                var params = operation['parameterList'];
                var paramSize = params.length;
                var oper = 
                    "<div align='right'>" +
                    "<input type='button' value='" + opName + "' " + 
                    "onclick='invokeOperFN(\"" + abstractName + "\", \"" + opName + "\", " + paramSize + ")' " +
                    "/>&nbsp;" + 
                    "</div>";
                return oper;
            },
            function(operation) { /* OperParameterList Column */
                // TODO: Fix name collision problem for overloaded operations with same number of parameters
                var opName = operation['name'];
                var params = operation['parameterList'];
                var paramList = "";
                if (params.length == 0) {
                    paramList = "<b>()</b>";
                } else {
                    paramList = "<b>(&nbsp;</b>";
                    for (var i = 0; i < params.length; i++) {
                        var opParamValueID = opName + '_paramValue' + (i+1) + '.' + params.length;
                        var opParamTypeID = opName + '_paramType' + (i+1) + '.' + params.length;
                        paramList += "<span id='" + opParamTypeID + "'>" + params[i] + "</span>&nbsp;<input type='text' id='" + opParamValueID + "' style='width: 100px;'/>";
                        if ((i+1) < params.length) {
                            paramList += "<b>&nbsp;,&nbsp;</b>";
                        }
                    }
                    paramList += "<b>&nbsp;)</b>";
                }
                return paramList; 
            }
        ],
        tableOption
    );
}

/**
 * Update MBean basic info table
 */
function <portlet:namespace/>updateBasicInfoTable(basicInfo) {
    dwr.util.removeAllRows('basicInfoTableBody');
    dwr.util.addRows(
        'basicInfoTableBody', 
        basicInfo,
        [
            function(info) { /* BasicInfoName Column */
                var name = "<div align='right'>" + info[0] + ":&nbsp;</div>";
                return name;
            },
            function(info) { /* BasicInfoValue Column */
                var value = info[1] + "<input type='hidden' id='" + info[0] + "' value='" + info[1] + "'>";
                return value;
            }
        ],
        tableOption
    );
}

/**
 * Update MBean stats table
 */
function <portlet:namespace/>updateStatsTable(stats) {
    dwr.util.removeAllRows('statsTableBody');
    dwr.util.addRows(
        'statsTableBody', 
        stats,
        [
            function(stat) { /* StatisticName Column */
                var name = "<div align='right'>" + stat[0][1] + ":&nbsp;</div>";
                return name;
            },
            function(stat) { /* StatisticValue Column */
                var value = '';
                for (var i = 1; i < stat.length; i++) {
                    value += '<b>' + stat[i][0] + ':</b> ' + stat[i][1] + '<br>';
                }
                return value;
            }
        ],
        tableOption
    );
    
    // Render sortable table
    var tbl = dojo.widget.byId("statsTable");
    if (tbl) {
        tbl.render(false);
    }
}

/**
 * Update 'Search MBeans' tree node 
 */
function <portlet:namespace/>updateSearchMBeansTreeNode(searchResult) {
    // Remove nodes
    var searchMBeansNode = dojo.widget.byId('searchMBeans');
    var treeController = dojo.widget.byId('treeController');
    var children = searchMBeansNode.children;

    while (children.length > 0) {
        var node = children[0];
        treeController.removeNode(node);
        node.destroy();
    }
    treeController.removeNode(searchMBeansNode); // This fixed the layout problem
    searchMBeansNode.destroy();
    
    // Add nodes
    nodeTitle = 'Search MBeans (' + searchResult.length + ' matches)';
    searchMBeansNode = dojo.widget.createWidget(
        'TreeNode', 
        {title: nodeTitle, widgetId: 'searchMBeans', isFolder: true, childIconSrc:'<%= jmxIconURI %>', actionsDisabled: ['view']}
    );
    var tree = dojo.widget.byId('jmxTree');
    tree.addChild(searchMBeansNode); 
    for (i = 0; i < searchResult.length; i++) {
        var entry = searchResult[i];
        var id = searchMBeansNode.widgetId + '::' + entry[0]; // make it unique
        var newNode = dojo.widget.createWidget(
            'TreeNode', 
            {title: entry[1], widgetId: id, label: entry, isFolder: false, childIconSrc:'<%= jmxIconURI %>'}
        );
        searchMBeansNode.addChild(newNode);        
    }
    searchMBeansNode.state = 'LOADED';

    // Exapand node
    if (searchMBeansNode.isExpanded == false) {
        treeController.expandToLevel(searchMBeansNode, 1);
    }
    
    // Select node
    var treeSelector = dojo.widget.byId("treeSelector");
    if (getSelectedNode() != null) {
        treeSelector.deselect();
    }
    treeSelector.doSelect(searchMBeansNode);
}

/**
 * Update JMX tree
 */
function <portlet:namespace/>updateJMXTree(entries) {
    for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        var id = _selectedNode.widgetId + '::' + entry[0]; // make it unique
        var newNode = dojo.widget.createWidget(
            'TreeNode', 
            {title: entry[1], widgetId: id, label: entry[1], isFolder: false, childIconSrc:'<%= jmxIconURI %>'}
        );
        _selectedNode.addChild(newNode);
    }
    _selectedNode.state = 'LOADED';
}

/**
 * Set Attribute Function 
 */
function setAttribFN(abstractName, attribName, attribValue, attribValueID, attribType) {
    _attribValueID = attribValueID;
    var newValue = prompt("Enter new value for attribute '" + attribName + "':", attribValue);
    if (newValue == null) {
        // Do nothing.
    } else {
        // Check boolean case
        if ((attribType == 'boolean') || (attribType == 'java.lang.Boolean')) {
            if (newValue != 'true') {
                newValue = 'false';
            }
        }
        _attribValue = newValue;
        // Set attribute
        JMXHelper.setAttribute(
            abstractName, attribName, newValue, attribType, /* Arguments */
            function(result) { /* setAttribFN Callback */
                if (result[1] == '<SUCCESS>') {
                    alert("Attribute '" + result[0] + "' successfully set."); 
                    dwr.util.setValue(_attribValueID, _attribValue); // update table cell
                } else {
                    alert("Failed to set attribute '" + result[0] + "': " + result[1]);
                }
            }
        );
    }
}

/**
 * Invoke Operation Function 
 */
function invokeOperFN(abstractName, opName, paramSize) {
    if (paramSize == 0) {
        // Operation without parameters
        // Invoke operator with no args        
        JMXHelper.invokeOperNoArgs(
            abstractName, opName, /* Arguments */
            function(result) { /* invokeOperNoArgs Callback */
                alert(result[0] + ' returned: ' + result[1]);
            }
        );
    } else {
        // Operation with parameters
        var paramValues = new Array(paramSize);
        var paramTypes = new Array(paramSize);
        for (var i = 0; i < paramSize; i++) {
            var opParamValueID = opName + '_paramValue' + (i+1) + '.' + paramSize;
            var opParamTypeID = opName + '_paramType' + (i+1) + '.' + paramSize;
            paramValues[i] = dwr.util.getValue(opParamValueID);
            paramTypes[i] = dwr.util.getValue(opParamTypeID);
        }
        // Invoke operator with args
        JMXHelper.invokeOperWithArgs(
            abstractName, opName, paramValues, paramTypes,
            function(result) { /* invokeOperWithArgs Callback */
                alert(result[0] + ' returned: ' + result[1]);
            }
        );
    }
}

/**
 * Prints 'LOADING' message while waiting for DWR method calls 
 */
function init() {
    // dwr.util.useLoadingMessage();
    dwr.util.setEscapeHtml(false);
}

/**
 * Call on DWR load
 */
function callOnLoad(load) {
    if (window.addEventListener) {
        window.addEventListener('load', load, false);
    } else if (window.attachEvent) {
        window.attachEvent('onload', load);
    } else {
        window.onload = load;
    }
}

/**
 * Call init function
 */
callOnLoad(init);
</script>

<div dojoType="TreeContextMenu" toggle="explode" contextMenuForWindow="false" widgetId="treeContextMenu">
    <!-- <div dojoType="TreeMenuItem" treeActions="refreshNode" widgetId="treeContextMenuRefresh" caption="Refresh"></div> -->
    <div dojoType="TreeMenuItem" treeActions="searchNode" widgetId="treeContextMenuSearch" caption="Search..."></div>
    <div dojoType="TreeMenuItem" treeActions="view" widgetId="treeContextMenuViewAttribs" caption="View Attributes"></div>
    <div dojoType="TreeMenuItem" treeActions="view" widgetId="treeContextMenuViewOps" caption="View Operations"></div>
    <div dojoType="TreeMenuItem" treeActions="view" widgetId="treeContextMenuViewInfo" caption="View Info"></div>
    <div dojoType="TreeMenuItem" treeActions="view" widgetId="treeContextMenuViewStats" caption="View Stats"></div>
</div>

<div dojoType="TreeSelector" widgetId="treeSelector"></div>
<div dojoType="TreeBasicController" widgetId="treeController"></div>

<!-- Main layout container -->
<div dojoType="LayoutContainer"
    layoutChildPriority='left-right'
    id="mainLayout"
    style="height: 700px;">

    <!-- Horizontal split container -->
    <div dojoType="SplitContainer"
        orientation="horizontal"
        sizerWidth="5"
        activeSizing="1"
        layoutAlign="client">

        <!-- JMX tree -->
        <div dojoType="Tree"
            toggle="fade"
            layoutAlign="flood"
            sizeMin="60"
            sizeShare="40"
            widgetId="jmxTree"
            selector="treeSelector"
            controller="treeController"
            expandLevel="0"
            menu="treeContextMenu"
            strictFolders="false">

            <!-- All MBeans -->
            <div dojoType="TreeNode" title="All MBeans" widgetId="allMBeans" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view">
                 <div dojoType="TreeNode" title="geronimo" widgetId="<PATTERN>geronimo:*" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="geronimo.config" widgetId="<PATTERN>geronimo.config:*" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
            </div>

            <!-- J2EE MBeans -->
             <div dojoType="TreeNode" title="J2EE Managed Objects" widgetId="j2eeMBeans" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view">
                 <div dojoType="TreeNode" title="AppClientModule" widgetId="<J2EETYPE>AppClientModule" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="EJBModule" widgetId="<J2EETYPE>EJBModule" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="EntityBean" widgetId="<J2EETYPE>EntityBean" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="J2EEApplication" widgetId="<J2EETYPE>J2EEApplication" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="J2EEDomain" widgetId="<J2EETYPE>J2EEDomain" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="J2EEServer" widgetId="<J2EETYPE>J2EEServer" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JavaMailResource" widgetId="<J2EETYPE>JavaMailResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAConnectionFactory" widgetId="<J2EETYPE>JCAConnectionFactory" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAManagedConnectionFactory" widgetId="<J2EETYPE>JCAManagedConnectionFactory" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAResource" widgetId="<J2EETYPE>JCAResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JDBCDataSource" widgetId="<J2EETYPE>JDBCDataSource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JDBCDriver" widgetId="<J2EETYPE>JDBCDriver" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JDBCResource" widgetId="<J2EETYPE>JDBCResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JMSResource" widgetId="<J2EETYPE>JMSResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JNDIResource" widgetId="<J2EETYPE>JNDIResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JTAResource" widgetId="<J2EETYPE>JTAResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JVM" widgetId="<J2EETYPE>JVM" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="MessageDrivenBean" widgetId="<J2EETYPE>MessageDrivenBean" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
              <div dojoType="TreeNode" title="PersistenceUnit" widgetId="<J2EETYPE>PersistenceUnit" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ResourceAdapter" widgetId="<J2EETYPE>ResourceAdapter" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ResourceAdapterModule" widgetId="<J2EETYPE>ResourceAdapterModule" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="RMI_IIOPResource" widgetId="<J2EETYPE>RMI_IIOPResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="Servlet" widgetId="<J2EETYPE>Servlet" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="StatefulSessionBean" widgetId="<J2EETYPE>StatefulSessionBean" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="StatelessSessionBean" widgetId="<J2EETYPE>StatelessSessionBean" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="URLResource" widgetId="<J2EETYPE>URLResource" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="WebModule" widgetId="<J2EETYPE>WebModule" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
             </div>
             
             <!-- Geronimo MBeans -->
             <div dojoType="TreeNode" title="Geronimo MBeans" widgetId="geronimoMBeans" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view">
                 <div dojoType="TreeNode" title="AppClient" widgetId="<J2EETYPE>AppClient" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ArtifactManager" widgetId="<J2EETYPE>ArtifactManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ArtifactResolver" widgetId="<J2EETYPE>ArtifactResolver" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="AttributeStore" widgetId="<J2EETYPE>AttributeStore" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ConfigBuilder" widgetId="<J2EETYPE>ConfigBuilder" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ConfigurationEntry" widgetId="<J2EETYPE>ConfigurationEntry" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ConfigurationManager" widgetId="<J2EETYPE>ConfigurationManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ConfigurationStore" widgetId="<J2EETYPE>ConfigurationStore" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="CORBABean" widgetId="<J2EETYPE>CORBABean" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="CORBACSS" widgetId="<J2EETYPE>CORBACSS" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="CORBATSS" widgetId="<J2EETYPE>CORBATSS" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="Deployer" widgetId="<J2EETYPE>Deployer" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="DeploymentConfigurer" widgetId="<J2EETYPE>DeploymentConfigurer" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="GBean" widgetId="<J2EETYPE>GBean" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="Host" widgetId="<J2EETYPE>Host" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JaasLoginService" widgetId="<J2EETYPE>JaasLoginService" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JACCManager" widgetId="<J2EETYPE>JACCManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JAXRConnectionFactory" widgetId="<J2EETYPE>JAXRConnectionFactory" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAActivationSpec" widgetId="<J2EETYPE>JCAActivationSpec" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAAdminObject" widgetId="<J2EETYPE>JCAAdminObject" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAConnectionManager" widgetId="<J2EETYPE>JCAConnectionManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAConnectionTracker" widgetId="<J2EETYPE>JCAConnectionTracker" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAResourceAdapter" widgetId="<J2EETYPE>JCAResourceAdapter" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JCAWorkManager" widgetId="<J2EETYPE>JCAWorkManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JMSConnector" widgetId="<J2EETYPE>JMSConnector" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JMSPersistence" widgetId="<J2EETYPE>JMSPersistence" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="JMSServer" widgetId="<J2EETYPE>JMSServer" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="KeyGenerator" widgetId="<J2EETYPE>KeyGenerator" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="Keystore" widgetId="<J2EETYPE>Keystore" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="LoginModule" widgetId="<J2EETYPE>LoginModule" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="LoginModuleUse" widgetId="<J2EETYPE>LoginModuleUse" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="MEJB" widgetId="<J2EETYPE>MEJB" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ModuleBuilder" widgetId="<J2EETYPE>ModuleBuilder" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="PersistentConfigurationList" widgetId="<J2EETYPE>PersistentConfigurationList" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="RealmBridge" widgetId="<J2EETYPE>RealmBridge" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="Repository" widgetId="<J2EETYPE>Repository" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="RoleMapper" widgetId="<J2EETYPE>RoleMapper" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="SecurityRealm" widgetId="<J2EETYPE>SecurityRealm" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ServiceModule" widgetId="<J2EETYPE>ServiceModule" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ServletTemplate" widgetId="<J2EETYPE>ServletTemplate" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ServletWebFilterMapping" widgetId="<J2EETYPE>ServletWebFilterMapping" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="ServletWebServiceTemplate" widgetId="<J2EETYPE>ServletWebServiceTemplate" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="SystemLog" widgetId="<J2EETYPE>SystemLog" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="TomcatValve" widgetId="<J2EETYPE>TomcatValve" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="TransactionContextManager" widgetId="<J2EETYPE>TransactionContextManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="TransactionLog" widgetId="<J2EETYPE>TransactionLog" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="TransactionManager" widgetId="<J2EETYPE>TransactionManager" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="URLPattern" widgetId="<J2EETYPE>URLPattern" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="URLWebFilterMapping" widgetId="<J2EETYPE>URLWebFilterMapping" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="WebFilter" widgetId="<J2EETYPE>WebFilter" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="WSLink" widgetId="<J2EETYPE>WSLink" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="XIDFactory" widgetId="<J2EETYPE>XIDFactory" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="XIDImporter" widgetId="<J2EETYPE>XIDImporter" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="XmlAttributeBuilder" widgetId="<J2EETYPE>XmlAttributeBuilder" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 <div dojoType="TreeNode" title="XmlReferenceBuilder" widgetId="<J2EETYPE>XmlReferenceBuilder" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
            </div>

             <!-- ServiceModule MBeans -->
             <div dojoType="TreeNode" title="ServiceModule MBeans" widgetId="serviceModuleMBeans" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view">
                <% pageContext.setAttribute("serviceModules", new JMXManagerHelper().getServiceModules()); %>
                <c:forEach var="serviceModule" items="${serviceModules}">
                 <div dojoType="TreeNode" title="${serviceModule}" widgetId="<SVCMODULE>ServiceModule=${serviceModule}" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view"></div>
                 </c:forEach>
            </div>

             <!-- Statistics Provider MBeans -->
             <div dojoType="TreeNode" title="Statistics Provider MBeans" widgetId="statisticsProviderMBeans" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view">
            </div>

            <!-- Search MBeans -->
             <div dojoType="TreeNode" title="Search MBeans" widgetId="searchMBeans" isFolder="true" childIconSrc="<%= jmxIconURI %>" actionsDisabled="view">
             </div>
        </div> <!-- JMX tree -->

        <!-- Main tab container -->
        <div id="mainTabContainer" 
            dojoType="TabContainer" 
            selectedTab="attributesTab" 
            style="overflow: hidden" 
            sizeShare="60">
            
            <!-- Attributes tab -->
            <div id="attributesTab" dojoType="ContentPane" title="MBean Attributes" label="Attributes" style="overflow: auto">
                <br>
                <table dojoType="SortableTable" 
                    widgetId="attribsTable" 
                    tbodyClass="scrollContent" 
                    enableMultipleSelect="true" 
                    enableAlternateRows="true" 
                    rowAlternateClass="alternateRow" 
                    cellpadding="0" 
                    cellspacing="2" 
                    border="0"
                    width="100%">
                    <thead>
                        <tr>
                            <th field="Name" dataType="String" width="10%">&nbsp;Name&nbsp;</th>
                            <th dataType="html" width="20%">&nbsp;Value&nbsp;</th>
                            <th field="Type" dataType="String" width="10%">&nbsp;Type&nbsp;</th>
                            <th field="Getter" dataType="String" width="10%">&nbsp;Getter&nbsp;</th>
                            <th dataType="html" width="10%">&nbsp;Setter&nbsp;</th>
                            <th field="Manageable" dataType="String" width="10%" align="center">&nbsp;Manageable&nbsp;</th>
                            <th field="Persistent" dataType="String" width="10%" align="center">&nbsp;Persistent&nbsp;</th>
                            <!--
                            <th field="Readable" dataType="String" width="10%">&nbsp;Readable&nbsp;</th>
                            <th field="Writable" dataType="String" width="10%">&nbsp;Writable&nbsp;</th>
                            -->
                        </tr>
                    </thead>
                    <tbody id="attributesTableBody">
                    </tbody>
                </table>
            </div> <!-- Attributes tab -->

            <!-- Operations tab -->
            <div id="operationsTab" dojoType="ContentPane" title="MBean Operations" label="Operations" style="overflow: auto">
                <br>
                <table width="100%">
                    <tr>
                        <!--
                        <td class="DarkBackground" align="center" width="30%">Name</td>
                        <td class="DarkBackground" align="center" width="70%">Paremeter List</td>
                        -->
                        <th style="background: #2581C7; color: #FFFFFF; font-weight: bold;" align="center" width="30%">Name</td>
                        <th style="background: #2581C7; color: #FFFFFF; font-weight: bold;" align="center" width="70%">Paremeter List</td>
                    </tr>
                    <tbody id="operationsTableBody">
                    </tbody>
                </table>
            </div> <!-- Operations tab -->

            <!-- Info tab -->
            <div id="infoTab" dojoType="ContentPane" title="MBean Info" label="Info" style="overflow: auto">
                <br>
                <table width="100%">
                    <tr>
                        <!--
                        <td class="DarkBackground" align="center" width="30%">Name</td>
                        <td class="DarkBackground" align="center" width="70%">Value</td>
                        -->
                        <th style="background: #2581C7; color: #FFFFFF; font-weight: bold;" align="center" width="30%">Name</td>
                        <th style="background: #2581C7; color: #FFFFFF; font-weight: bold;" align="center" width="70%">Value</td>
                    </tr>
                    <tbody id="basicInfoTableBody">
                    </tbody>
                </table>
            </div> <!-- Info tab -->

            <!-- Stats tab -->
            <div id="statsTab" dojoType="ContentPane" title="MBean Stats" label="Stats" style="overflow: auto">
                <br>
                <table dojoType="SortableTable" 
                    widgetId="statsTable" 
                    tbodyClass="scrollContent" 
                    enableMultipleSelect="true" 
                    enableAlternateRows="true" 
                    rowAlternateClass="alternateRow" 
                    cellpadding="0" 
                    cellspacing="2" 
                    border="0"
                    width="100%">
                    <thead>
                        <tr>
                            <th field="Name" dataType="String" width="30%">&nbsp;Name&nbsp;</th>
                            <th dataType="html" width="70%">&nbsp;Value&nbsp;</th>
                        </tr>
                    </thead>
                    <tbody id="statsTableBody">
                    </tbody>
                </table>
                <br>
                &nbsp;<input type='button' value='Refresh Stats' onclick='refreshStatsBtnClicked()' /> 
            </div> <!-- Stats tab -->

            <!-- Search tab -->
            <div id="searchTab" dojoType="ContentPane" title="Search" label="Search" style="overflow: auto">
                <!-- JMXSearch Form -->
                <form name="JMXSearchForm" onsubmit="return false;">
                    <br>
                    <table>
                        <tr>
                            <td width="15%"><label for="jmxQuery">&nbsp;Object&nbsp;Name&nbsp;Pattern</label>:</td>
                            <td width="70%">
                                <select dojoType="combobox" id="jmxQuery" searchType="SUBSTRING" style="width: 100%;">
                                    <!-- Domains -->
                                    <option>*:*</option>
                                    <option>geronimo:*</option>
                                    <option>geronimo.config:*</option>
                                    <!-- J2EE MBeans -->
                                    <option>*:j2eeType=AppClientModule,*</option>
                                    <option>*:j2eeType=EJBModule,*</option>
                                    <option>*:j2eeType=EntityBean,*</option>
                                    <option>*:j2eeType=J2EEApplication,*</option>
                                    <option>*:j2eeType=J2EEDomain,*</option>
                                    <option>*:j2eeType=J2EEServer,*</option>
                                    <option>*:j2eeType=JavaMailResource,*</option>
                                    <option>*:j2eeType=JCAConnectionFactory,*</option>
                                    <option>*:j2eeType=JCAManagedConnectionFactory,*</option>
                                    <option>*:j2eeType=JCAResource,*</option>
                                    <option>*:j2eeType=JDBCDataSource,*</option>
                                    <option>*:j2eeType=JDBCDriver,*</option>
                                    <option>*:j2eeType=JDBCResource,*</option>
                                    <option>*:j2eeType=JMSResource,*</option>
                                    <option>*:j2eeType=JNDIResource,*</option>
                                    <option>*:j2eeType=JTAResource,*</option>
                                    <option>*:j2eeType=JVM,*</option>
                                    <option>*:j2eeType=MessageDrivenBean,*</option>
                                    <option>*:j2eeType=ResourceAdapter,*</option>
                                    <option>*:j2eeType=ResourceAdapterModule,*</option>
                                    <option>*:j2eeType=RMI_IIOPResource,*</option>
                                    <option>*:j2eeType=Servlet,*</option>
                                    <option>*:j2eeType=StatefulSessionBean,*</option>
                                    <option>*:j2eeType=StatelessSessionBean,*</option>
                                    <option>*:j2eeType=URLResource,*</option>
                                    <option>*:j2eeType=WebModule,*</option>
                                    <!-- Geronimo MBeans -->
                                    <option>*:j2eeType=AppClient,*</option>
                                    <option>*:j2eeType=ArtifactManager,*</option>
                                    <option>*:j2eeType=ArtifactResolver,*</option>
                                    <option>*:j2eeType=AttributeStore,*</option>
                                    <option>*:j2eeType=ConfigBuilder,*</option>
                                    <option>*:j2eeType=ConfigurationEntry,*</option>
                                    <option>*:j2eeType=ConfigurationManager,*</option>
                                    <option>*:j2eeType=ConfigurationStore,*</option>
                                    <option>*:j2eeType=CORBABean,*</option>
                                    <option>*:j2eeType=CORBACSS,*</option>
                                    <option>*:j2eeType=CORBATSS,*</option>
                                    <option>*:j2eeType=Deployer,*</option>
                                    <option>*:j2eeType=DeploymentConfigurer,*</option>
                                    <option>*:j2eeType=GBean,*</option>
                                    <option>*:j2eeType=Host,*</option>
                                    <option>*:j2eeType=JaasLoginService,*</option>
                                    <option>*:j2eeType=JACCManager,*</option>
                                    <option>*:j2eeType=JAXRConnectionFactory,*</option>
                                    <option>*:j2eeType=JCAActivationSpec,*</option>
                                    <option>*:j2eeType=JCAAdminObject,*</option>
                                    <option>*:j2eeType=JCAConnectionManager,*</option>
                                    <option>*:j2eeType=JCAConnectionTracker,*</option>
                                    <option>*:j2eeType=JCAResourceAdapter,*</option>
                                    <option>*:j2eeType=JCAWorkManager,*</option>
                                    <option>*:j2eeType=JMSConnector,*</option>
                                    <option>*:j2eeType=JMSPersistence,*</option>
                                    <option>*:j2eeType=JMSServer,*</option>
                                    <option>*:j2eeType=KeyGenerator,*</option>
                                    <option>*:j2eeType=Keystore,*</option>
                                    <option>*:j2eeType=LoginModule,*</option>
                                    <option>*:j2eeType=LoginModuleUse,*</option>
                                    <option>*:j2eeType=MEJB,*</option>
                                    <option>*:j2eeType=ModuleBuilder,*</option>
                                    <option>*:j2eeType=PersistentConfigurationList,*</option>
                                    <option>*:j2eeType=RealmBridge,*</option>
                                    <option>*:j2eeType=Repository,*</option>
                                    <option>*:j2eeType=RoleMapper,*</option>
                                    <option>*:j2eeType=SecurityRealm,*</option>
                                    <option>*:j2eeType=ServiceModule,*</option>
                                    <option>*:j2eeType=ServletTemplate,*</option>
                                    <option>*:j2eeType=ServletWebFilterMapping,*</option>
                                    <option>*:j2eeType=ServletWebServiceTemplate,*</option>
                                    <option>*:j2eeType=SystemLog,*</option>
                                    <option>*:j2eeType=TomcatValve,*</option>
                                    <option>*:j2eeType=TransactionContextManager,*</option>
                                    <option>*:j2eeType=TransactionLog,*</option>
                                    <option>*:j2eeType=TransactionManager,*</option>
                                    <option>*:j2eeType=URLPattern,*</option>
                                    <option>*:j2eeType=URLWebFilterMapping,*</option>
                                    <option>*:j2eeType=WebFilter,*</option>
                                    <option>*:j2eeType=WSLink,*</option>
                                    <option>*:j2eeType=XIDFactory,*</option>
                                    <option>*:j2eeType=XIDImporter,*</option>
                                    <option>*:j2eeType=XmlAttributeBuilder,*</option>
                                    <option>*:j2eeType=XmlReferenceBuilder,*</option>
                                </select>                            
                            </td>
                            <td width="15%"><input type="button" value="Search" id="jmxSearch" onClick="searchBtnClicked()" style="width: 100%;" /></td>
                        </tr>
                    </table>
                </form> <!-- JMXSearch Form -->
            </div> <!-- Search tab -->
            
        </div> <!-- Main tab container -->
        
    </div>  <!-- Horizontal split container -->
    
</div> <!-- Main layout container -->
