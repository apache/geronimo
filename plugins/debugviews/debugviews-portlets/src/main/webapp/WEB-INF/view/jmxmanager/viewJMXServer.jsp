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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.apache.geronimo.console.jmxmanager.JMXManagerHelper" %>
<fmt:setBundle basename="debugviews"/>
<portlet:defineObjects/>

<!----------------------->
<!--     DWR Stuff     -->
<!----------------------->

<% String dwrForwarderServlet = "/console/dwr2"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/JMXManagerHelper.js'></script>
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
    dojo.require("dojo.data.ItemFileWriteStore"); 
    dojo.require("dijit.Tree");
    dojo.require("dijit.layout.LayoutContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dijit.layout.SplitContainer");
    dojo.require("dijit.layout.TabContainer");
    dojo.require("dijit.TitlePane");
    dojo.require("dijit.form.ComboBox")
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.TextBox");
</script>


<!------------------------>
<!--     Page Stuff     -->
<!------------------------>
<script type="text/javascript">
    // set sync calls in dwr
    dwr.engine.setAsync(false);

    // globe var
    var _selectItem;    // set when open a item
    var _abstractName;  // set when click a item which type is "leaf"


    // tree var
    var treeStore;
    var treeData;
    var treeModel;
    var tree;
    
    dojo.addOnLoad(
        function() {
            JMXManagerHelper.getJMXInformation({callback:createTree,async:false});    
        }
    );

    function createTree(treeData){                 
        treeStore = new dojo.data.ItemFileWriteStore({data:treeData});
        treeModel = new dijit.tree.ForestStoreModel({
            store:treeStore, 
            childrenAttrs:["children"],
            mayHaveChildren: function(item){
                    return item.children && item.children.length > 0;
                }
            });
        tree = new dijit.Tree({
            model: treeModel,
            showRoot: false,
            openOnClick: false,
            onOpen: onOpen,
            onClick: onClick
            },
            "treeContainer");
    }

    function onOpen(item) {
        document.body.style.cursor = "wait";
        _selectItem = item;
                
        // if the node has place holder, we will try get its children by type
        if (checkPlaceholderChild(item)){
            var type = treeStore.getValue(item,"type");
            if (type == "JavaEE" || type == "Geronimo") {        //how to use static final java var?
                JMXManagerHelper.listByJ2EEType(treeStore.getValue(item,"name"), {callback:updateJMXTree});
            }
            if (type == "GeronimoService") {
                JMXManagerHelper.listBySubstring("ServiceModule="+treeStore.getValue(item,"name"), {callback:updateJMXTree});
            }
            if (type == "All") {
                // not worked
                JMXManagerHelper.listByPattern(treeStore.getValue(item,"name")+":*", {callback:updateJMXTree});
            }
            if (type == "StatsProvider") {
                // Get statistics provider MBeans
                JMXManagerHelper.getStatsProviderMBeans({callback:updateJMXTree});
            }
        } 
        
        // if item has children, del the place holder. Otherwise keep the place holder
        dealPlaceholderChild(item);

        document.body.style.cursor = "";
    }
    
    function onClick(item) {
        document.body.style.cursor = "wait";
        _selectItem = item;
        
        var type = treeStore.getValue(item, "type");
        if (type == "leaf") {
            // set abstract name
            var values = treeStore.getValues(item,"values");
            _abstractName = values[0];
            JMXManagerHelper.getAttributes(_abstractName, updateAttributesTable);
            JMXManagerHelper.getOperations(_abstractName, updateOperationsTable);
            JMXManagerHelper.getMBeanInfo(_abstractName, updateInfoTable);
            JMXManagerHelper.getMBeanStats(_abstractName, updateStatsTable);
        } else {
            // No marker means not an abstract name, clear tables
            dwr.util.removeAllRows('attributesTableBody');
            dwr.util.removeAllRows('operationsTableBody');
            dwr.util.removeAllRows('infoTableBody');
            dwr.util.removeAllRows('statsTableBody');
        }

        document.body.style.cursor = "";
    }

    /*
     * Call back: Update JMXTree
     */
    function updateJMXTree(entries) {
        for (var i = 0; i < entries.length; i++) {
            // each entry is a string pair [abstractName, objectName]
            var entry = entries[i];
            // for the leaf node, name is the objectName - entry[1], and valuse[0] is the abstractName - entry[0].
            treeStore.newItem({name:entry[1], type:"leaf", values:[entry[0]]},{parent:_selectItem, attribute:"children"});
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
    function checkPlaceholderChild(/*dojo.data.Item*/item){
        var kids = item.children;
        for (var i in kids) {
            var type = treeStore.getValue(kids[i],"type");
            if (type == "placeholder") {
                return true;
            }
        }
        return false;
    }
   
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
        },
        escapeHtml: false       /*let the table display html*/
    }
    
    /**
     * Call back: Update MBean basic info table
     */
    function updateInfoTable(info) {
        dwr.util.removeAllRows('infoTableBody');
        dwr.util.addRows(
            'infoTableBody', 
            info,
            [
                function(theinfo) { /* BasicInfoName Column */
                    var name = "<div align='right'>" + theinfo[0] + ":&nbsp;</div>";
                    return name;
                },
                function(theinfo) { /* BasicInfoValue Column */
                    var value = theinfo[1] + "<input type='hidden' id='" + theinfo[0] + "' value='" + theinfo[1] + "'>";
                    return value;
                }
            ],
            tableOption
        );
    }

    
    /**
     * Call back: Update MBean operations table 
     */
    function updateOperationsTable(operations) {
        dwr.util.removeAllRows('operationsTableBody');
        dwr.util.addRows(
            'operationsTableBody', 
            operations,
            [
                function(operation) { /* OperName  Column */
                    var abstractName = _abstractName;
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
     * Call: Invoke Operation Function 
     */
    function invokeOperFN(abstractName, opName, paramSize) {
        if (paramSize == 0) {
            // Operation without parameters
            // Invoke operator with no args        
            JMXManagerHelper.invokeOperNoArgs(
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
            JMXManagerHelper.invokeOperWithArgs(
                abstractName, opName, paramValues, paramTypes,
                function(result) { /* invokeOperWithArgs Callback */
                    alert(result[0] + ' returned: ' + result[1]);
                }
            );
        }
    }

    /**
     * Call Back: Update MBean attributes table 
     */
    function updateAttributesTable(attributes) {
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
    }
    
    /**
     * Call: Set Attribute Function 
     */
    function setAttribFN(abstractName, attribName, attribValue, attribValueID, attribType) {
        var _attribValueID = attribValueID;
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
            var _attribValue = newValue;
            // Set attribute
            JMXManagerHelper.setAttribute(
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
     * Call back: Update MBean stats table
     */
    function updateStatsTable(stats) {
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
    }


</script>

<!-- Search table -->
<div dojoType="dijit.TitlePane" title="Search">
       <fmt:message key="jmxmanager.help.objectNamePattern"/>:
       <select dojoType="dijit.form.ComboBox" id="srchPattern" jsId="srchPattern" style="width:300px">
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
        <button dojoType="dijit.form.Button"><fmt:message key="jmxmanager.help.search" />
            <script type="dojo/method" event="onClick" args="btn">
                document.body.style.cursor = "wait";
                
                var searchNode;
                var rootkids = treeModel.root.children;
                for (var i in rootkids) {
                        var type = treeStore.getValue(rootkids[i],"type");

                        if (type == "SearchNode") {

                            //get the search node
                            searchNode = rootkids[i];

                            //del the old results
                            var oldkids = treeStore.getValues(searchNode, "children");
                            for (var j in oldkids) {
                                treeStore.deleteItem(oldkids[j]);
                            }

                            //add a placeholder
                            treeStore.newItem({name:"null", type:"placeholder"},{parent:searchNode, attribute:"children"});

                            break;
                        }
                        
                }

                var jmxQuery = srchPattern.value;
                _selectItem = searchNode;
                JMXManagerHelper.listByPattern(jmxQuery, updateJMXTree);

                dealPlaceholderChild(searchNode);

                document.body.style.cursor = "";

            </script>
        </button>
</div>
<br/>

<!-- Main layout container START -->
<div dojoType="dijit.layout.LayoutContainer"  id="mainLayout" style="width: 100%; height: 700px;">

    <!-- Horizontal split container START -->
    <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="1" activeSizing="true" layoutAlign="client" style="width: 100%; height: 100%;" >
       
        <!-- left pane START -->
        <div dojoType="dijit.layout.ContentPane" sizeShare="40" layoutAlign="left" style="background-color:white; overflow: auto;">
            <div id="treeContainer"></div>
        </div>
        <!-- left pane END -->
        
        <!-- right pane START -->
        <div dojoType="dijit.layout.ContentPane" sizeShare="60" layoutAlign="right" style="background-color:white; overflow: auto;"> 
            
            <!-- Main tab container START -->
            <div dojoType="dijit.layout.TabContainer" style="width: 100%; height: 100px;">
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key="jmxmanager.help.attributes"/>" selected="true">
                    <table class="TableLine" width="100%">
                        <thead>
                            <tr class="DarkBackground">
                                <th width="10%"><fmt:message key="jmxmanager.help.name"/></th>
                                <th width="20%"><fmt:message key="jmxmanager.help.value"/></th>
                                <th width="10%"><fmt:message key="jmxmanager.help.type"/></th>
                                <th width="10%"><fmt:message key="jmxmanager.help.getter"/></th>
                                <th width="10%"><fmt:message key="jmxmanager.help.setter"/></th>
                                <th width="10%"><fmt:message key="jmxmanager.help.manageable"/></th>
                                <th width="10%"><fmt:message key="jmxmanager.help.persistent"/></th>
                                <!--
                                <th field="Readable" dataType="String" width="10%">&nbsp;Readable&nbsp;</th>
                                <th field="Writable" dataType="String" width="10%">&nbsp;Writable&nbsp;</th>
                                -->
                            </tr>
                        </thead>
                        <tbody id="attributesTableBody"></tbody>
                    </table>
                </div>
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key="jmxmanager.help.operations"/>">
                    <table class="TableLine" width="100%">
                        <thead>
                            <tr class="DarkBackground">
                                <th width="30%"><fmt:message key="jmxmanager.help.name"/></th>
                                <th width="70%"><fmt:message key="jmxmanager.help.paramList"/></th>
                            </tr>
                        </thead>
                        <tbody id="operationsTableBody"></tbody>
                    </table>
                </div>
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key="jmxmanager.help.info"/>">
                    <table class="TableLine" width="100%">
                        <thead>
                            <tr class="DarkBackground">
                                <th width="30%"><fmt:message key="jmxmanager.help.name"/></th>
                                <th width="70%"><fmt:message key="jmxmanager.help.value"/></th>
                            </tr>
                        </thead>
                        <tbody id="infoTableBody"></tbody>
                    </table>
                </div>
                <div dojoType="dijit.layout.ContentPane" title="<fmt:message key="jmxmanager.help.stats"/>">
                    <table class="TableLine" width="100%">
                        <thead>
                            <tr class="DarkBackground">
                                <th width="30%"><fmt:message key="jmxmanager.help.name"/></th>
                                <th width="70%"><fmt:message key="jmxmanager.help.value"/></th>
                            </tr>
                        </thead>
                        <tbody id="statsTableBody">
                        </tbody>
                    </table>
                    <br/>
                    <button dojoType="dijit.form.Button"><fmt:message key="jmxmanager.help.refreshStats" />
                        <script type="dojo/method" event="onClick" args="btn">
                            if (_abstractName!=null){
                                JMXManagerHelper.getMBeanStats(_abstractName, updateStatsTable);
                            }
                        </script>
                    </button>
                </div>
            </div>
            <!-- Main tab container END -->
            
        </div>
        <!-- right pane END -->
        
    </div>  
    <!-- Horizontal split container END -->
    
</div>
<!-- Main layout container END -->
