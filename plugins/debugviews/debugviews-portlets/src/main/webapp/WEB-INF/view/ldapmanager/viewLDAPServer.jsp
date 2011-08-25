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

<!----------------------->
<!--     DWR Stuff     -->
<!----------------------->
<% String dwrForwarderServlet = "/console/dwr2"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/LDAPManagerHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>


<!------------------------>
<!--     DOJO Stuff     -->
<!------------------------>
<script type="text/javascript" src="/console/dojo/dojo/dojo.js" djConfig="parseOnLoad: true"></script>
<script type="text/javascript" src="/console/dojo/dijit/dijit.js"></script>

<script type="text/javascript">
    // style class
    document.body.className="claro";
    
    // dojo libs import
    dojo.require("dijit.Tree");
    dojo.require("dijit.layout.LayoutContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dijit.layout.SplitContainer");
    dojo.require("dijit.layout.TabContainer");
    dojo.require("dojo.data.ItemFileWriteStore"); 
    dojo.require("dijit.TitlePane");
    dojo.require("dijit.form.ValidationTextBox");
    dojo.require("dijit.form.TextBox");
    dojo.require("dijit.form.NumberTextBox");
    dojo.require("dijit.form.CheckBox");
    dojo.require("dijit.form.RadioButton");
    dojo.require("dijit.form.Button");
</script>


<script>
/* Sync calls */
dwr.engine.setAsync(false);

/* Generic error handler */
dwr.engine.setErrorHandler(
    function (errorString) {
        alert('Error: ' + errorString + '\n** Make sure LDAP server is running and/or connection properties are correct.');
    }
);

/*
 * create an empty tree
 */
var treeStore = new dojo.data.ItemFileWriteStore({data:{identifier:null, label:"name", items:[]}});
var treeModel = new dijit.tree.ForestStoreModel({
    store: treeStore,
    rootId: "LDAP Server",
    rootLabel: "LDAP Server",
    childrenAttrs: ["children"],
    mayHaveChildren: function(item){
        	return item.children && item.children.length > 0;
        }
});

treeModel.mayHaveChildren = function(item){
    if (item.id != treeModel.rootId) {
        if (treeStore.getValue(item, "type") == "folder") return true;
        else return false;
    } else {
        return true;
    }
};


/*
 * Global var
 */
var _selectItem;
/*
 * Call Back: Init LDAP tree. 
 * Invoked after connect successfully.
 */
function initLDAPTree(baseDN) {
    if (baseDN == null) {
        return;
    }

    LDAPManagerHelper.list(baseDN, function(baseDNKids){
        if (baseDNKids == null || baseDNKids.length==0) {
            treeStore.newItem({name:baseDN, type:"leaf", values:[baseDN]});
        }else{
            var newitem = treeStore.newItem({name:baseDN, type:"folder", values:[baseDN]});
            treeStore.newItem({name:"null", type:"placeholder"},{parent:newitem, attribute:"children"});
        }
    });
   
}

/* 
 * Call back: Update LDAP tree
 */
function updateLDAPTree(entries) {
    for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        //entry[0]:RDN, entry[1]:DN
        LDAPManagerHelper.list(entry[1], function(kids){
            if (kids == null || kids.length==0) {
                treeStore.newItem({name:entry[0], type:"leaf", values:[entry[1]]},{parent:_selectItem, attribute:"children"});
            }else{
                var newitem = treeStore.newItem({name:entry[0], type:"folder", values:[entry[1]]},{parent:_selectItem, attribute:"children"});
                treeStore.newItem({name:"null", type:"placeholder"},{parent:newitem, attribute:"children"});
            }
        });
    }

}

/*
 * utility method
 * when there are more than one child, del the placeholder child
 */
function dealPlaceholderChild(/*dojo.data.Item*/item){
    var kids = item.children;
    if (kids.length > 1){   //no need to do anything when kids.length == 1
        for (var i in kids) {
            var type = treeStore.getValue(kids[i],"type");
            if (type == "placeholder") {
                treeStore.deleteItem(kids[i]);
                break;
            }
        }
    }
}

/*
 * utility method
 */
function checkPlaceholderChild(/*dojo.data.Item*/ item){
    var kids = item.children;
    for (var i in kids) {
        var type = treeStore.getValue(kids[i],"type");
        if (type == "placeholder") {
            return true;
        }
    }
    return false;
}


/* 
 * Table render option
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

/*
 *  Update attributes table 
 */
function updateAttributesTable(attributes) {
    dwr.util.removeAllRows('attributesTableBody');
    dwr.util.addRows(
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
function updateSearchResultTable(searchResult) {
    dwr.util.removeAllRows('searchResultTableBody');
    dwr.util.addRows(
        'searchResultTableBody',
        searchResult,
        [
            function(dn) { /* Distinguished Name Column */ 
                return dn;
            }
        ],
        tableOption
    );
    dwr.util.setValue('searchResultCount', '(' + searchResult.length + ' entries returned..' + ')');
}

/* Prints 'LOADING' message while waiting for DWR method calls */
function init() {
    dwr.util.useLoadingMessage();
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

<!-- Connect Pane START -->
<div dojoType="dijit.TitlePane" title="Connect">
       
    <table style="border:0px">
        <thead>
            <tr>
                <th>Network Parameters</th>
                <th>Authentication Parameters</th>            
                <th>Options</th>  
            </tr>          
        </thead>
        <tbody>
            <tr>
                <td valign="top">
                    <table style="border:0px">
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.host" />:</td>
                            <td><input type="text" name="host" id="host" jsId="connHost" value="localhost" dojoType="dijit.form.ValidationTextBox" required="true" invalidMessage="Pleas input the host name"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.port" />:</td>
                            <td><input type="text" name="port" id="port" jsId="connPort" value="10389" dojoType="dijit.form.ValidationTextBox" required="true" invalidMessage="Pleas input the port number"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.SSL" />:</td>
                            <td><input type="checkbox" id="ssl" name="ssl" jsId="connSSL" dojoType="dijit.form.CheckBox"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.version" />:</td>
                            <td>
                                <input type="radio" dojoType="dijit.form.RadioButton" name="version" id="version3" jsId="connVersion3" value="3" checked="true" />3
                                <input type="radio" dojoType="dijit.form.RadioButton" name="version" id="version2" jsId="connVersion2" value="2" />2
                            </td>
                        </tr>
                    </table>                    
                </td>
                <td valign="top">
                    <table style="border:0px">
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.anonymousBind" />:</td>
                            <td><input type="checkbox" id="anonymous" name="anonymous" jsId="connAnonymous" dojoType="dijit.form.CheckBox"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.userDN" />:</td>
                            <td><input type="text" name="userdn" id="userdn" jsId="connUserDN" value="uid=admin, ou=system" dojoType="dijit.form.ValidationTextBox" required="true" invalidMessage="Pleas input the user name"/></td>
                        </tr>
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.password" />:</td>
                            <td><input type="password" name="password" id="password" jsId="connPassword" value="" dojoType="dijit.form.ValidationTextBox" required="true" invalidMessage="Pleas input the password"/></td>
                        </tr>
                    </table>                    
                </td>
                <td valign="top">
                    <table style="border:0px">
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.baseDN" />:</td>
                            <td><input type="text" name="basedn" id="basedn" jsId="connBaseDN" value="ou=system" dojoType="dijit.form.ValidationTextBox" required="true" invalidMessage="BaseDN required"/></td>
                        </tr>
                    </table>                    
                </td>
            </tr>
        </tbody>
    </table>

    <button dojoType="dijit.form.Button"><fmt:message key="ldapmanager.viewLDAPServer.connect" />
        <script type="dojo/method" event="onClick" args="btn">
            document.body.style.cursor = "wait";

            // TODO: Add validation
            // Context Factory name
            var initialContextFactory = 'com.sun.jndi.ldap.LdapCtxFactory';
            
            // network param
            var host = connHost.value;
            var port = connPort.value;
            var securityProtocol = '';
            if (connSSL.checked) {
                securityProtocol = 'ssl';
            }
            var ldapVersion;
            if (connVersion3.checked) {
                ldapVersion = '3';
            } else {
                ldapVersion = '2';
            }
            
            // authentication param
            var securityAuthentication;
            var securityPrincipal;
            var securityCredentials;
            if (connAnonymous.checked) {
                securityAuthentication = 'none';
                securityPrincipal = '';
                securityCredentials = '';
            } else {
                securityAuthentication = 'simple';
                securityPrincipal = connUserDN.value;
                securityCredentials = connPassword.value;
            }
            
            // options
            var baseDN = connBaseDN.value;
            
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
            //alert(connectInfoStr);
            
            // Connect to new LDAP server
            LDAPManagerHelper.connect(
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
                        /* Init LDAP tree */
                        LDAPManagerHelper.getBaseDN(initLDAPTree);

                        /* Init LDAP connection info tab */
                        //if (_baseDN != null) {
                        //    LDAPManagerHelper.getEnvironment(<portlet:namespace/>initConnectInfoTab);
                        //}
                        
                    } else {
                        // Display error
                        alert(result + '\n Make sure LDAP server is running and/or connection properties are correct.');
                    }
                }
            );
            
            
            document.body.style.cursor = "";
        </script>
    </button>
</div>
<!-- Connect Pane END -->
<br/>
<!-- Main layout container START -->
<div dojoType="dijit.layout.LayoutContainer"  id="mainLayout" style="width: 100%; height: 500px;">

    <!-- Horizontal split container START -->
    <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="1" activeSizing="true" layoutAlign="client" style="width: 100%; height: 100%;" >
           
        <!-- left pane START -->
        <div dojoType="dijit.layout.ContentPane" sizeShare="40" layoutAlign="left" style="background-color:white; overflow: auto;">       
            
            <!-- JMX tree START -->
            <div dojoType="dijit.Tree" model="treeModel" openOnClick="false">
                <script type="dojo/method" event="onOpen" args="item">
                    document.body.style.cursor = "wait";
                    _selectItem = item;

                    if (item.id != treeModel.rootLabel){
                        // if the node has place holder, we will try get its children
                        if (checkPlaceholderChild(item)){
                            var values = treeStore.getValues(item, "values");  
                            LDAPManagerHelper.list(values[0], {callback: updateLDAPTree}); //values[0] is DN
                           } 
                        // if item has children, del the place holder(actually always has children here)
                           dealPlaceholderChild(item);
                    }

                    document.body.style.cursor = "";
                </script>
                <script type="dojo/method" event="onClick" args="item">
                    document.body.style.cursor = "wait";
                    _selectItem = item;

                    if (item.id != treeModel.rootLabel){
                        var values = treeStore.getValues(item, "values");  
                        LDAPManagerHelper.getAttributes(values[0], {callback: updateAttributesTable});
                    } else {
                        dwr.util.removeAllRows('attributesTableBody');
                    }

                    document.body.style.cursor = "";
                </script>
            </div>
            <!-- JMX tree END -->
            
        </div> 
        <!-- left pane END -->

        <!-- right pane START -->
        <div dojoType="dijit.layout.ContentPane" sizeShare="60" layoutAlign="right" style="background-color:white; overflow: auto;"> 
            
            <!-- Main tab container START -->
            <div dojoType="dijit.layout.TabContainer" style="width: 100%; height: 100px;">
            
                <!-- Attributes Tab START -->
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key='ldapmanager.viewLDAPServer.attributes'/>" selected="true">
                    <table class="TableLine" width="100%">
                        <thead>
                            <tr class="DarkBackground">
                                <th width="40%"><fmt:message key="ldapmanager.viewLDAPServer.name"/></th>
                                <th width="60%"><fmt:message key="ldapmanager.viewLDAPServer.value"/></th>
                            </tr>
                        </thead>
                        <tbody id="attributesTableBody">
                        </tbody>
                    </table>
                </div>
                <!-- Attributes Tab END -->
                
                <!-- Search Tab START -->
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key='ldapmanager.viewLDAPServer.search' />">
                    <table style="border:0px">
                        <tr>
                            <td><label for="searchDN"><fmt:message key="ldapmanager.viewLDAPServer.searchDN" /></label>:</td>
                            <td>
                                <input type="text" name="searchDN" id="searchDN" jsId="srchSearchDN" value="" dojoType="dijit.form.ValidationTextBox" required="true" invalidMessage="Pleas input the DN" />
                            </td>
                        </tr>
                        <tr>
                            <td><label for="filter"><fmt:message key="ldapmanager.viewLDAPServer.filter" /></label>:</td>
                            <td>
                                <input type="text" name="filter" id="filter" jsId="srchFilter" value="(objectclass=*)" dojoType="dijit.form.TextBox" />
                            </td>
                        </tr>
                        <tr>
                            <td><fmt:message key="ldapmanager.viewLDAPServer.searchScope" />:</td>
                            <td>
                                <input type="radio" dojoType="dijit.form.RadioButton" name="searchScope" id="onelevel" jsId="srchOneLevel" value="onelevel" checked="true" /><label for="onelevel"><fmt:message key="ldapmanager.viewLDAPServer.oneLevel" /></label>
                                <input type="radio" dojoType="dijit.form.RadioButton" name="searchScope" id="subtree" jsId="srchSubTree" value="subtree" /><label for="subtree"><fmt:message key="ldapmanager.viewLDAPServer.subTreeLevel" /></label>
                            </td>
                        </tr>
                        <tr>
                            <td align="left" colspan="2">
                                <button dojoType="dijit.form.Button"><fmt:message key="ldapmanager.viewLDAPServer.search" />
                                    <script type="dojo/method" event="onClick" args="btn">
                                        var scope;
                                        if (srchOneLevel.checked.value == true) {
                                            scope = 'onelevel';
                                        } else {
                                            scope = 'subtree';
                                        }
                                        LDAPManagerHelper.search(srchSearchDN.value, srchFilter.value, scope, updateSearchResultTable);
                                    </script>
                                </button>
                                <button dojoType="dijit.form.Button"><fmt:message key="ldapmanager.viewLDAPServer.clearResult" />
                                    <script type="dojo/method" event="onClick" args="btn">
                                        dwr.util.removeAllRows('searchResultTableBody');
                                        dwr.util.setValue('searchResultCount', '');
                                    </script>
                                </button>
                            </td>
                        </tr>
                    </table>
                    <table class="TableLine" width="100%">
                        <thead>
                            <tr class="DarkBackground">
                                <th><fmt:message key="ldapmanager.viewLDAPServer.DN" />&nbsp;<span id='searchResultCount'></span></th>
                            </tr>
                        </thead>
                        <tbody id="searchResultTableBody"></tbody>
                    </table>
                </div>
                <!-- Search Tab END -->
                
            </div>
            <!-- Main tab container END -->
            
        </div>
        <!-- right pane END -->
        
    </div>  
    <!-- Horizontal split container END -->
    
</div>
<!-- Main layout container END -->
