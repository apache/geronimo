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
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="debugviews"/>
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

<%
    // LDAP icon
    String ldapIconURI = renderResponse.encodeURL(renderRequest.getContextPath() + "/ico_filetree_16x16.gif");
%>

<!-- DOJO Stuff -->
<script>
/* Global vars */
var _selectedNode = null; // Selected tree node
var _baseDN = null;       // Base distinguised name

/* Get selected node */
function getSelectedNode() {
    var tree = dojo.widget.byId('ldapTree');
    var selectedNode = tree.selector.selectedNode;
    return selectedNode;
}

/* Select tab */
function selectTab(tabID) {
    var mainTabContainer = dojo.widget.byId('mainTabContainer');
    var tab = dojo.widget.byId(tabID);
    mainTabContainer.selectTab(tab);   
}

/* Init stuff */
dojo.addOnLoad(
    function() {
        /* Init LDAP tree */
        LDAPHelper.getBaseDN(<portlet:namespace/>initLDAPTree);

        /* Init LDAP connection info tab */
        if (_baseDN != null) {
            LDAPHelper.getEnvironment(<portlet:namespace/>initConnectInfoTab);
        }

        /* Tree click event handler */
        var treeController = dojo.widget.manager.getWidgetById('treeController');
        dojo.event.connect(
            'before',
            treeController,
            'onTreeClick',
            {
                beforeTreeClick: function(evt) {
                    var selectedNode = evt.source;
                    if ((selectedNode.state == 'UNCHECKED') && (selectedNode.isExpanded == false)) {
                        // Add children
                        _selectedNode = selectedNode;
                        LDAPHelper.list(_selectedNode.widgetId, <portlet:namespace/>updateLDAPTree);
                    }
                }
            },
            'beforeTreeClick'
        );

        /* Tree node title click event handler */
        var tree = dojo.widget.manager.getWidgetById('ldapTree');
        dojo.event.topic.subscribe(
            tree.eventNames.titleClick,
            function(message) {
                var dn = message.source.widgetId;
                LDAPHelper.getAttributes(dn, <portlet:namespace/>updateAttributesTable);
            }
        );

        /* Tree context menu event handler: 'Refresh' */
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
                    LDAPHelper.list(_selectedNode.widgetId, <portlet:namespace/>updateLDAPTree);
                }
            }
        );

        /* Tree context menu event handler: 'Search...' */
        dojo.event.topic.subscribe(
            'treeContextMenuSearch/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();
                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }
                selectTab('searchTab');
                // Set Search DN
                document.LDAPSearchForm.searchDN.value = selectedNode.widgetId;
            }
        );

        /* Tree context menu event handler: 'View Entry' */
        dojo.event.topic.subscribe(
            'treeContextMenuViewEntry/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();
                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }
                selectTab('attributesTab');
            }
        );

        /* Tree context menu event handler: 'Connect Info' */
        dojo.event.topic.subscribe(
            'treeContextMenuConnectInfo/engage',
            function (menuItem) {
                var selectedNode = getSelectedNode();
                if (selectedNode == null) {
                    alert('Please select a tree node.');
                    return;
                }
                selectTab('connectInfoTab');
            }
        );
    }
);

/* Anonymous bind checkbox clicked even handler */
function anonBindChkboxClicked() {
    var isAnonBind = document.LDAPConnectForm.anonBind.checked;
    document.LDAPConnectForm.userDN.disabled = isAnonBind;
    document.LDAPConnectForm.password.disabled = isAnonBind;
}

/* Restore Default button clicked event handler */
function restoreDefaultBtnClicked() {
    // Restore default connection properties (Embedded Apache DS)
    document.LDAPConnectForm.host.value = 'localhost';
    document.LDAPConnectForm.port.value = '1389';
    document.LDAPConnectForm.ldapVersion[0].checked = true;
    document.LDAPConnectForm.ldapVersion[1].checked = false;
    document.LDAPConnectForm.baseDN.value = 'ou=system';
    document.LDAPConnectForm.ssl.checked = false;
    document.LDAPConnectForm.anonBind.checked = false;
    document.LDAPConnectForm.userDN.value = 'uid=admin, ou=system';
    document.LDAPConnectForm.password.value = '';
    document.LDAPConnectForm.userDN.disabled = false;
    document.LDAPConnectForm.password.disabled = false;
}

/* Connect button clicked event handler */
function connectBtnClicked() {
    // TODO: Add validation
    var initialContextFactory = 'com.sun.jndi.ldap.LdapCtxFactory';
    var host = document.LDAPConnectForm.host.value;
    var port = document.LDAPConnectForm.port.value;
    var ldapVersion;
    if (document.LDAPConnectForm.ldapVersion[0].checked) {
        ldapVersion = '3';
    } else {
        ldapVersion = '2';
    }
    var baseDN = document.LDAPConnectForm.baseDN.value;
    var securityProtocol = '';
    if (document.LDAPConnectForm.ssl.checked) {
        securityProtocol = 'ssl';
    }
    var securityAuthentication = 'simple';
    var securityPrincipal;
    var securityCredentials;
    if (document.LDAPConnectForm.anonBind.checked) {
        securityAuthentication = 'none';
        securityPrincipal = '';
        securityCredentials = '';
    } else {
        securityAuthentication = 'simple';
        securityPrincipal = document.LDAPConnectForm.userDN.value;
        securityCredentials = document.LDAPConnectForm.password.value;
    }
    
    // DEBUG: Connect Info
    var connectInfoStr =
        'initialContextFactory:' + initialContextFactory +
        '\nhost:' + host +
        '\nport:' + port +
        '\nldapVersion:' + ldapVersion +
        '\nbaseDN:' + baseDN +
        '\nsecurityProtocol:' + securityProtocol +
        '\nsecurityAuthentication:' + securityAuthentication +
        '\nsecurityPrincipal:' + securityPrincipal +
        '\nsecurityCredentials:' + securityCredentials;
    // alert(connectInfoStr);
    
    // Connect to new LDAP server
    LDAPHelper.connect(
        initialContextFactory,
        host,
        port,
        baseDN,
        ldapVersion,
        securityProtocol,
        securityAuthentication,
        securityPrincipal,
        securityCredentials,
        function(result) {
            // TODO: Check result
            if (result == '<SUCCESS>') {
                window.location = '<portlet:actionURL />';
            } else {
                // Display error
                alert(result + '\n** Make sure LDAP server is running and/or connection properties are correct.');
            }
        }
    );
}

/* Search button clicked event handler */
function searchBtnClicked() {
    var searchDN = document.LDAPSearchForm.searchDN.value;
    var filter = document.LDAPSearchForm.filter.value;
    var scope;
    if (document.LDAPSearchForm.searchScope[0].checked) {
        scope = 'onelevel';
    } else {
        scope = 'subtree';
    }
    LDAPHelper.search(searchDN, filter, scope, <portlet:namespace/>updateSearchResultTable);
}

/* Clear result button clicked event handler */
function clearResultBtnClicked() {
    DWRUtil.removeAllRows('searchResultTableBody');
    DWRUtil.setValue('searchResultCount', '');
}
</script>

<style>
body .dojoHtmlSplitterPanePanel {
    background: white;
    overflow: auto;
}

span.invalid, span.missing, span.range {
    display: inline;
    margin-left: 1em;
    font-weight: bold;
    font-style: italic;
    font-family: Arial, Verdana, sans-serif;
    color: #f66;
    font-size: 0.9em;
}
</style>

<!-- DWR Stuff -->
<% String dwrForwarderServlet = "/console/dwr2"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/LDAPHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script>
/* Sync calls */
DWREngine.setAsync(false);

/* Generic error handler */
DWREngine.setErrorHandler(
    function (errorString) {
        alert('Error: ' + errorString + '\n** Make sure LDAP server is running and/or connection properties are correct.');
        selectTab('connectInfoTab');
    }
);

/* Table render option */
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

/* Update attributes table */
function <portlet:namespace/>updateAttributesTable(attributes) {
    DWRUtil.removeAllRows('attributesTableBody');
    DWRUtil.addRows(
        'attributesTableBody', 
        attributes,
        [
            function(attribute) { /* Attribute Name Column */
                return attribute[0];
            }, 
            function(attribute) { /* Attribute Value Column */
                return attribute[1];
            } 
        ],
        tableOption
    );
}

/* Update search result table */
function <portlet:namespace/>updateSearchResultTable(searchResult) {
    DWRUtil.removeAllRows('searchResultTableBody');
    DWRUtil.addRows(
        'searchResultTableBody',
        searchResult,
        [
            function(dn) { /* Distinguished Name Column */ 
                return dn;
            }
        ],
        tableOption
    );
    DWRUtil.setValue('searchResultCount', searchResult.length + ' entries returned...');
}

/* Update LDAP tree */
function <portlet:namespace/>updateLDAPTree(entries) {
    for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        var newNode = dojo.widget.createWidget(
            'TreeNode', 
            {title: entry[0], widgetId: entry[1], isFolder: true, childIconSrc:'<%= ldapIconURI %>'}
        );
        _selectedNode.addChild(newNode);
    }
    _selectedNode.state = 'LOADED';
}

/* Init LDAP tree */
function <portlet:namespace/>initLDAPTree(baseDN) {
    if (baseDN == null) {
        selectTab('connectInfoTab');
        return;
    }
    _baseDN = baseDN;

    var tree = dojo.widget.byId('ldapTree');
    var rootNode = dojo.widget.createWidget(
        'TreeNode', 
        {title: baseDN, widgetId: baseDN, isFolder: true, childIconSrc:'<%= ldapIconURI %>'}
    );
    tree.addChild(rootNode);
    var controller = dojo.widget.byId('treeController');
    controller.expand(rootNode);
    _selectedNode = rootNode;
    LDAPHelper.list(_selectedNode.widgetId, <portlet:namespace/>updateLDAPTree);

    // Select node
    var treeSelector = dojo.widget.byId("treeSelector");
    if (getSelectedNode() != null) {
        treeSelector.deselect();
    }
    treeSelector.doSelect(rootNode);
    
    // Select attributes tab
    selectTab('attributesTab');
    
    // Update attributes table
    var dn = rootNode.widgetId;
    LDAPHelper.getAttributes(dn, <portlet:namespace/>updateAttributesTable);
    
    // Update Search tab's 'Search DN' field
    document.LDAPSearchForm.searchDN.value = baseDN;
}

/* Init LDAP connection info tab */
function <portlet:namespace/>initConnectInfoTab(env) {
    var host = env['host'];
    document.LDAPConnectForm.host.value = host;
    var port = env['port'];
    document.LDAPConnectForm.port.value = port;
    var version = env['ldapVersion'];
    if (version == '3') {
        document.LDAPConnectForm.ldapVersion[0].checked = true;
    } else if (version == '2') {
        document.LDAPConnectForm.ldapVersion[1].checked = true;
    }
    var baseDN = env['baseDN'];
    document.LDAPConnectForm.baseDN.value = baseDN;
    var securityProtocol = env['securityProtocol'];
    if ((securityProtocol != null) && (securityProtocol == 'ssl')) {
        // SSL
        document.LDAPConnectForm.ssl.checked = true;
    }
    var securityAuthentication = env['securityAuthentication'];
    if ((securityAuthentication != null) && (securityAuthentication == 'none')) {
        // Anonymous bind
        document.LDAPConnectForm.anonBind.checked = true;
        document.LDAPConnectForm.userDN.value = '';
    } else {
        var securityPrincipal = env['securityPrincipal'];
        document.LDAPConnectForm.userDN.value = securityPrincipal;
    }
}

/* Prints 'LOADING' message while waiting for DWR method calls */
function init() {
    DWRUtil.useLoadingMessage();
}

function callOnLoad(load) {
    if (window.addEventListener) {
        window.addEventListener('load', load, false);
    } else if (window.attachEvent) {
        window.attachEvent('onload', load);
    } else {
        window.onload = load;
    }
}

callOnLoad(init);
</script>

<div dojoType="TreeContextMenu" toggle="explode" contextMenuForWindow="false" widgetId="treeContextMenu">
    <div dojoType="TreeMenuItem" treeActions="refreshNode" widgetId="treeContextMenuRefresh" caption="Refresh" ></div>
    <div dojoType="TreeMenuItem" treeActions="searchNode" widgetId="treeContextMenuSearch" caption="Search..."></div>
    <div dojoType="TreeMenuItem" treeActions="viewEntry" widgetId="treeContextMenuViewEntry" caption="View Entry"></div>
    <div dojoType="TreeMenuItem" treeActions="viewConnectInfo" widgetId="treeContextMenuConnectInfo" caption="Connect Info"></div>
</div>

<div dojoType="TreeSelector" widgetId="treeSelector"></div>
<div dojoType="TreeBasicController" widgetId="treeController"></div>

<!-- Main layout container -->
<div dojoType="LayoutContainer"
    layoutChildPriority='left-right'
    id="mainLayout"
    style="height: 500px;">

    <!-- Horizontal split container -->
    <div dojoType="SplitContainer"
        orientation="horizontal"
        sizerWidth="5"
        activeSizing="1"
        layoutAlign="client">
        
        <!-- LDAP tree -->
        <div dojoType="Tree"
            toggle="fade"
            layoutAlign="flood"
            sizeMin="60"
            sizeShare="40"
            widgetId="ldapTree"
            selector="treeSelector"
            controller="treeController"
            expandLevel="0"
            menu="treeContextMenu"
            strictFolders="false">
            <!-- Nodes will be added programmatically -->
        </div>

        <!-- Main tab container -->
        <div id="mainTabContainer" 
            dojoType="TabContainer" 
            selectedTab="attributesTab" 
            style="overflow: hidden" 
            sizeShare="60">
            
            <!-- Attributes tab -->
            <div id="attributesTab" dojoType="ContentPane" title="LDAP Entry Attributes" label="Attributes" style="overflow: auto">
                <br>
                <table width="100%">
                    <tr>
                        <td class="DarkBackground" align="center" width="40%"><fmt:message key="ldapmanager.viewLDAPServer.name"/></td>
                        <td class="DarkBackground" align="center" width="60%"><fmt:message key="ldapmanager.viewLDAPServer.value"/></td>
                    </tr>
                    <tbody id="attributesTableBody">
                    </tbody>
                </table>
            </div> <!-- Attributes tab -->

            <!-- Search tab -->
            <div id="searchTab" dojoType="ContentPane" title = "" label='<fmt:message key="ldapmanager.viewLDAPServer.search" />' style="overflow: auto">
                <br>
                <form NAME="LDAPSearchForm">
                    <table>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>searchDN"><fmt:message key="ldapmanager.viewLDAPServer.searchDN" /></label>:</td>
                            <td><input type="text" name="searchDN" id="<portlet:namespace/>searchDN" value="" size="45"/></td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>filter"><fmt:message key="ldapmanager.viewLDAPServer.filter" /></label>:</td>
                            <td><input type="text" name="filter" id="<portlet:namespace/>filter" value="(objectclass=*)" size="45"/></td>
                        </tr>
                        <tr>
                            <td nowrap align="right">&nbsp;<fmt:message key="ldapmanager.viewLDAPServer.searchScope" />:</td>
                            <td>
                              <INPUT type="radio" name="searchScope" value="onelevel" id="<portlet:namespace/>onelevel" checked> <label for="<portlet:namespace/>onelevel"><fmt:message key="ldapmanager.viewLDAPServer.oneLevel" /></label>
                                <INPUT type="radio" name="searchScope" value="subtree" id="<portlet:namespace/>subtree"> <label for="<portlet:namespace/>subtree"><fmt:message key="ldapmanager.viewLDAPServer.subTreeLevel" /></label>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td align="left" colspan="2">
                                &nbsp;<input type="button" value='<fmt:message key="ldapmanager.viewLDAPServer.search" />' name="ldapSearch" onClick="searchBtnClicked()"/>
                                &nbsp;<input type="button" value='<fmt:message key="ldapmanager.viewLDAPServer.clearResult" />' name="clearResult" onClick="clearResultBtnClicked()"/>
                                &nbsp;&nbsp;<span id='searchResultCount'></span>
                            </td>
                        </tr>
                    </table>
                </form>
                <hr>
                <table width="100%">
                  <tr>
                    <td class="DarkBackground" align="center"><fmt:message key="ldapmanager.viewLDAPServer.DN" /></td>
                  </tr>
                  <tbody id="searchResultTableBody">
                  </tbody>
                </table>
            </div> <!-- Search tab -->

            <!-- Connection Info tab -->
            <div id="connectInfoTab" dojoType="ContentPane" title = "" label="Connect Info" style="overflow: auto">
                <br>
                <form NAME="LDAPConnectForm">
                    <table>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>host"><fmt:message key="ldapmanager.viewLDAPServer.host"/></label>:</td>
                            <td>
                                <input type="text" name="host" id="<portlet:namespace/>host" value="localhost" size="40"
                                    dojoType="ValidationTextbox"
                                    required="true"
                                    trim="true"
                                    uppercase: false,
                                    lowercase: false,
                                    ucFirst: false,
                                    digit: false,
                                    missingMessage="<br>* Host is required." />
                            </td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>port"><fmt:message key="ldapmanager.viewLDAPServer.port"/></label>:</td>
                            <td>
                                <input type="text" name="port" id="<portlet:namespace/>port" value="1389" size="40"
                                    dojoType="IntegerTextbox"
                                    required="true"
                                    trim="true"
                                    digit="true"
                                    min="0"
                                    max="65535"
                                    missingMessage="<br>* Port is required." 
                                    invalidMessage="<br>* The value entered is not valid."
                                    rangeMessage="<br>* This value is out of range use 0 - 65535." />
                            </td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><fmt:message key="ldapmanager.viewLDAPServer.version"/>:</td>
                            <td>
                                <INPUT type="radio" name="ldapVersion" value="3" title="3" checked> 3
                                <INPUT type="radio" name="ldapVersion" value="2" title="2"> 2
                            </td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>baseDN"><fmt:message key="ldapmanager.viewLDAPServer.baseDN" /></label>:</td>
                            <td>
                                <input type="text" name="baseDN" id="<portlet:namespace/>baseDN" value="ou=system" size="40"
                                    dojoType="ValidationTextbox"
                                    required="true"
                                    trim="true"
                                    missingMessage="<br>* Base DN is required." />
                            </td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>ssl"><fmt:message key="ldapmanager.viewLDAPServer.SSL"/></label>:</td>
                            <td><input type="checkbox" name="ssl" id="<portlet:namespace/>ssl" value="" size="40"></td>
                        </tr>
                        <tr>
                            <td nowrap align="right">&nbsp;<label for="<portlet:namespace/>anonBind"><fmt:message key="ldapmanager.viewLDAPServer.anonymousBind" /></label>:</td>
                            <td><input type="checkbox" name="anonBind" id="<portlet:namespace/>anonBind" value="" size="40" onclick="javascript:anonBindChkboxClicked()"></td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>userDN"><fmt:message key="ldapmanager.viewLDAPServer.userDN" /></label>:</td>
                            <td>
                                <input type="text" name="userDN" id="<portlet:namespace/>userDN" value="uid=admin, ou=system" size="40"
                                    dojoType="ValidationTextbox"
                                    required="true"
                                    trim="true"
                                    missingMessage="<br>* User DN is required." />
                            </td>
                        </tr>
                        <tr>
                            <td nowrap align="right"><label for="<portlet:namespace/>password"><fmt:message key="ldapmanager.viewLDAPServer.password"/></label>:</td>
                            <td><input type="password" name="password" id="<portlet:namespace/>password" value="" size="40"></td>
                        </tr>
                        <tr>
                            <td align="right" colspan="2">
                                &nbsp;<input type="button" value='<fmt:message key="ldapmanager.viewLDAPServer.restoreDefault" />' name="defaultLDAP" onClick="restoreDefaultBtnClicked()"/>
                                &nbsp;<input type="button" value='<fmt:message key="ldapmanager.viewLDAPServer.connect" />' name="connectLDAP" onClick="connectBtnClicked()"/>
                            </td>
                        </tr>
                    </table>
                </form>
            </div> <!-- Connection Info tab -->

            <!-- Help tab -->
            <!--
            <div id="helpTab" dojoType="ContentPane" title="Help Information" label="Help" style="overflow: auto">
                <br>
                <p>The LDAP viewer portlet can be used to do the following:
                <ul>
                    <li>Connect to any LDAP server and explore its contents (default is the Embedded LDAP server - Apache DS)
                    <li>View the attributes of an entry
                    <li>Do an LDAP search on a particular entry
                    <li>Refresh any entry to get the latest data from the directory server
                    <li>View the LDAP connection environment data
                </ul>
                <p>Note: Right-click to any tree node to view the context menu for performing different actions.
            </div>
            --> 
            <!-- Help tab -->

        </div> <!-- Main tab container -->
    </div>  <!-- Horizontal split container -->
</div> <!-- Main layout container -->
