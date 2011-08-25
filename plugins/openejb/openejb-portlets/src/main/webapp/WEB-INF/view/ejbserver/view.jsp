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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.apache.geronimo.console.util.PortletManager,
                 javax.portlet.RenderRequest"%>
<fmt:setBundle basename="openejb-portlet"/>
<portlet:defineObjects/>

<% String dwrForwarderServlet = "/console/dwr6"; %>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/interface/EjbHelper.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/engine.js'></script>
<script type='text/javascript' src='<%= dwrForwarderServlet %>/util.js'></script>

<script type="text/javascript" src="/console/dojo/dojo/dojo.js" djConfig="parseOnLoad: true"></script>
<script type="text/javascript" src="/console/dojo/dijit/dijit.js"></script>
    
<script>    

    dojo.require("dijit.Tree");
    dojo.require("dijit.layout.LayoutContainer");
    dojo.require("dijit.layout.ContentPane");
    dojo.require("dijit.layout.SplitContainer");
    dojo.require("dijit.form.Button");
    dojo.require("dijit.form.TextBox");
    dojo.require("dojo.data.ItemFileReadStore"); 
    
    var lastFound = '';
    var doCheck = false;
    var serverStore = null;
    var storeModel = null;
    var tree = null;
    var data = '';
    var containerPropertyValue = ''; 

    dojo.addOnLoad(
        function () {
            EjbHelper.getEjbInformation({callback:createTree,async:false});
        }
    );

    function check(curr,last) {
        var cr = curr.split(".");
        var ls = last.split(".");
        for (var i =0; i < cr.length; i++)
            if(parseInt(cr[i]) < parseInt(ls[i]))
                return false;
        return true;
    }
       
    function createTree(treeDat){
        serverStore = new dojo.data.ItemFileReadStore({data: treeDat});
        storeModel = new dijit.tree.ForestStoreModel({
            store: serverStore, 
            rootId: "Ejb Containers", 
            rootLabel: "Ejb Containers", 
            childrenAttrs: ["children"],
            mayHaveChildren: function(item){
                    return item.children && item.children.length > 0;
                }
            });
        tree = new dijit.Tree({
            "class": "claro",
            model: storeModel,
            openOnClick: false,
            onClick: onClick
            },
            "treeContainer");
    }
    
    function onClick(item) {
        if (item.id!="Ejb Containers") {
            var ids = serverStore.getValues(item, "values");
            if (ids[1] != null) {
                EjbHelper.getDeploymentInfo(ids[0],ids[1],updateEjbInfoTable);
            } else {
                EjbHelper.getContainerInfo(ids[0],updateEjbInfoTable);
            }
        } else {
            dwr.util.removeAllRows('ejbInfoTableBody');
        }
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
       escapeHtml:false
   }

   function updateEjbInfoTable(ejbInfos){
       var containerId = "";
       var showDialogFlag = false;
       dwr.util.removeAllRows('ejbInfoTableBody');
       dwr.util.addRows(
           'ejbInfoTableBody', 
           ejbInfos,
           [ 
               function(info) {            
                   if(info.editable == "true" && info.dirty == "true"){                    
                       showDialogFlag = true;                   
                       return "<div style='width:100%;display:inline;'><p id='" + info.id + "para' style='color: red'>" + info.name + "</p></div>";
                   } else { 
                       return "<div style='width:100%;display:inline;'><p id='" + info.id + "para'>" + info.name + "</p></div>";
                   }        
               },
               function(info) {
                   var value = info.value;
                   var name = info.name;
                   if(info.id == "ContainerId"){
                       containerId = info.value;  
                   }         
                   if(info.editable == "true"){                                        
                       return "<div style='width:100%;display:inline;'><input type='text' id="+info.id + " name="+info.name + " value="+info.value+" widgetId="+info.id+" dojoType='Textbox' /><button dojoType='Button'  onclick=updateValues('"+escape(containerId)+"','"+info.id+"','"+info.value+"')><fmt:message key="portlet.openejb.view.update"/></button></div> "
                   } else {
                       return "<div style='width:100%'>" + info.value + "</div>";        
                   } 
               }       
           ],
           tableOption
       );
       if(showDialogFlag)
           addWarningMessage("<portlet:namespace/>", "<fmt:message key='portlet.openejb.view.unrestarted'/>");         
   }

   function getCurrentValue(value){
       containerPropertyValue = value; 
   }
  
   function updateValues(containerId,  propertyKey, originalValue){       
       var propertyValue = document.getElementById(propertyKey).value;       
       var para = propertyKey+"para";
       EjbHelper.getCurrentContainerProperty(containerId,  propertyKey, {callback:getCurrentValue,async:false}); 
       if(containerPropertyValue != propertyValue ){
           EjbHelper.setContainerProperty(containerId,  propertyKey,  propertyValue, updateResult);
           document.getElementById(para).style.color="red";
       } else {
           EjbHelper.setContainerProperty(containerId,  propertyKey,  propertyValue,{async:false} );
           document.getElementById(para).style.color=document.body.style.color;
       }
   }
   
   function updateResult(msg){
       addCommonMessage("<portlet:namespace/>", msg);
   }
     
   document.body.className="claro";
   
  </script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>
    
<div dojoType="dijit.layout.LayoutContainer" id="mainLayout" style="width: 100%; height: 700px;">
    <!-- Horizontal split container -->
    <div dojoType="dijit.layout.SplitContainer" orientation="horizontal" sizerWidth="1" activeSizing="true" layoutAlign="client" style="width: 100%; height: 100%;" >
        <div dojoType="dijit.layout.ContentPane" layoutAlign="left" style="background-color:white; overflow: auto;" preload="true" widgetId="ejbcontainerTree" sizeShare="40">
            <div id="treeContainer"></div>
        </div> 

        <div id="infoTab" dojoType="dijit.layout.ContentPane" title="Ejb Info" label="Info" sizeShare="60" style="background-color:white; overflow: auto;" layoutAlign="right" class="claro" >
            <table id="ejbsTable" class="TableLine" width="100%">
                <thead>
                    <tr class="DarkBackground">
                        <th width="40%"><fmt:message key="portlet.openejb.view.name" />&nbsp;</th>
                        <th width="60%"><fmt:message key="portlet.openejb.view.value" />&nbsp;</th>
                    </tr>
                </thead>
                <tbody id="ejbInfoTableBody">
                </tbody>
            </table>
        </div>
    </div>
</div>   

