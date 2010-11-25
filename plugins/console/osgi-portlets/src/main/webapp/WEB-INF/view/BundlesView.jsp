<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet"%>
<%@ page import="javax.portlet.*" %>

<!-- set i18n properties file name -->
<fmt:setBundle basename="osgiportlets"/>
    
<portlet:defineObjects />

<!-- loading div -->
<script>
var to;
function showLoadingDIV(){
	document.getElementById("loadingdiv").style.display='block';
	to = setTimeout ("showLoadingTimeOut()",10000);
}
function showLoadingTimeOut(){
	if (document.getElementById("loadingdiv").style.display=='block'){	
		alert("timeout when process action");
		hideLoadingDIV();
	}
}
function hideLoadingDIV(){
	document.getElementById("loadingdiv").style.display='none';
	clearTimeout(to);
}
</script>
<div id="loadingdiv" style="position:absolute;width:100%;height:90%;display:none;z-index:1000;text-align:center; background-color:#ffffff;filter:alpha(opacity=70);opacity:0.7;">
<p align="center">
	<br/>
	<img border="0" src="<%=request.getContextPath()%>/images/loading128.gif" style="width:36;height:36"/>
	<br/>
	Loading...
</p>
</div>


<div class="tundra">
<script type="text/javascript"
	src="/dojo/dojo/dojo.js" djConfig="parseOnLoad: true">	
</script>

<style type="text/css">
@import "/dojo/dojo/resources/dojo.css";
@import "/dojo/dijit/themes/tundra/tundra.css";
@import "/dojo/dojox/grid/enhanced/resources/EnhancedGrid_rtl.css";
@import "/dojo/dojox/grid/enhanced/resources/tundraEnhancedGrid.css";

#grid1 {
	border: 1px solid #333;
	width: 800px;
	margin: 0px;
	height: 300px;
	font-size: 0.9em;
	font-family: Geneva, Arial, Helvetica, sans-serif;
}
</style>


<%
	//start/stop/uninstall action ajax url
    ResourceURL ajaxResourceURL = renderResponse.createResourceURL();
    ajaxResourceURL.setResourceID("bundlesAction");
%>
<%
	//install action ajax url
    ResourceURL ajaxResourceURL2 = renderResponse.createResourceURL();
    ajaxResourceURL2.setResourceID("installAction");
%>
<%
	//show single bundle
    ResourceURL ajaxResourceURL3 = renderResponse.createResourceURL();
    ajaxResourceURL3.setResourceID("showSingleBundle");
%>

<script type="text/javascript">
	dojo.require("dojo.parser");
	dojo.require("dojo.io.iframe"); 
	dojo.require("dijit.TitlePane");
	dojo.require("dojo.data.ItemFileWriteStore");
	dojo.require("dojox.grid.EnhancedGrid");
	dojo.require("dijit.form.Button");
	dojo.require("dojox.grid.enhanced.plugins.IndirectSelection");
	dojo.require("dojox.grid._Grid");
	dojo.require("dojox.grid.cells.dijit");
	dojo.require("dojox.grid.enhanced.plugins.Menu");

	dojo.addOnLoad(function() {
		dojo.connect(dijit.byId("grid1"), "onRowClick", grid1RowClick);
	});

    function actionPerform(actionName,loadHandle){
    	//get all selected lines
    	var selectedItems=dijit.byId("grid1").selection.getSelected();
    	if(selectedItems.length){
    		showLoadingDIV();

        	//construct action json string
    		var actionJsonStr='{action:'+actionName+',items:[';
    		var i;var id;
    		for(i=0;i<selectedItems.length-1;i++){
    			if(selectedItems[i]!=null){
    				id=dijit.byId("grid1").store.getValue(selectedItems[i],"id");
    				actionJsonStr=actionJsonStr+'{id:'+id+'},';
    			}
    		}
    		//The last item does not need a comma
    		id=dijit.byId("grid1").store.getValue(selectedItems[selectedItems.length-1],"id");
    		actionJsonStr=actionJsonStr+'{id:'+id+'}'+']}';	
    		
    		//alert(actionJsonStr);
    	
    		//ajax call to portlet's serveResource()
    		var postArgs={
    			url:"<%=ajaxResourceURL%>formId="+formID,
    			handleAs:"json",
    			content:{bundlesActionParam:actionJsonStr},
    			load:loadHandle
    		};
    		dojo.xhrPost(postArgs);			
    	} else {
			// need better show err msg
			alert("at least select one item");
       	}
    }

    //function for update items after action
    function  actionUpdate(response, ioArgs){
		//update the items which done an action
		var updatedItems=response.items;			
		dojo.forEach(updatedItems,function(updatedItem){
			if(updatedItem!=null){
				if (updatedItem.state == "err") {
					// need better way to show err msg
					alert(updatedItem.id + "err");
					
				}else if(updatedItem.state == "Uninstalled") {
					dijit.byId("grid1").store.fetchItemByIdentity({
						identity:updatedItem.id,
						onItem:function(item){
							dijit.byId("grid1").store.deleteItem(item);
						}
					});
				}else { // start or stop actions
					dijit.byId("grid1").store.fetchItemByIdentity({
						identity:updatedItem.id,
						onItem:function(item){
							dijit.byId("grid1").store.setValue(item,"state",updatedItem.state);
						}
					});
				}
			}
		});

		hideLoadingDIV();
	}


	function grid1RowClick(e){
        document.getElementById("singleBundleDiv").style.display = "block";
		
		var id=dijit.byId("grid1").getItem(e.rowIndex).id;
		//ajax call to portlet's serveResource()
		var postArgs={
			url:"<%=ajaxResourceURL3%>formId="+formID,
			handleAs:"text",
			content:{id:id},
			load:function(response, ioArgs){
				var headers=dojo.fromJson(response);
				var newStore = new dojo.data.ItemFileWriteStore({data:headers}); 
				dijit.byId("headersgrid").setStore(newStore);
								
			}
		};
		dojo.xhrPost(postArgs);		
	}


	
	function installBundle(){
		showLoadingDIV();
		
		dojo.io.iframe.send({
			url: "<%=ajaxResourceURL2%>formId="+formID,
			method: "post",
			handleAs: "text",
			form: dojo.byId("installForm"),
			load: function(response, ioArgs){
				var bundle=dojo.fromJson(response);
				var item=dijit.byId("grid1").store.newItem(bundle);
			}
		});
		
		hideLoadingDIV();
	}
</script> 




<!-- Installing a bundle -->
<div dojoType="dijit.TitlePane" title="Install a bundle" >
<form id="installForm" enctype="multipart/form-data" method="POST">
<table border="0" style="width:100%;margin:10px 0px;">
    <tr>
        <td align="right" valign="top" width="20%">Bundle:</td>
	    <td><input type="file" style="width:200px" name="bundleFile" />
	    	<br/>
	        <input type="checkbox" dojoType="dijit.form.CheckBox" name="startAfterInstalled" value="yes" align="middle" />
	        <fmt:message key="osgi.bundle.install.startAfterInstalled" />
	        <br/>
	        <input type="text" dojoType="dijit.form.TextBox" style="width: 3em;" name="startLevel" size="4" />
	        <fmt:message key="osgi.bundle.startLevel" />  
	        <br/>
	        <button dojoType="dijit.form.Button"><fmt:message key="osgi.bundle.install.install"/>
		    	<script type="dojo/method" event="onClick" args="evt">
					installBundle();
		    	</script>
	        </button> 
	    </td>
	</tr>
</table>
</form>
</div>


<br/>

<!-- bundles bar -->
<table border="0" style="width:100%;margin:0px 0px;">
    <tr>
    	<!-- actions -->
        <td>
            <label>Select all: </label>
			<div id="cbxall" name="cbxall" dojoType="dijit.form.CheckBox" value="all">
				<script type="dojo/method" event="onChange" args="newvalue">
					if (newvalue == true) {
						dijit.byId("grid1").rowSelectCell.toggleAllSelection(true);
					}else {
						dijit.byId("grid1").rowSelectCell.toggleAllSelection(false);
					}
				</script>
			</div>
			&nbsp;
			<button dojoType="dijit.form.Button">Start
				<script type="dojo/method" event="onClick" args="evt">
					actionPerform("start",actionUpdate);
				</script>
			</button>
			<button dojoType="dijit.form.Button">Stop
				<script type="dojo/method" event="onClick" args="evt">
					actionPerform("stop",actionUpdate);
				</script>
			</button>
			<button dojoType="dijit.form.Button">Uninstall
				<script type="dojo/method" event="onClick" args="evt">
					actionPerform("uninstall",actionUpdate);
				</script>
			</button>
		</td>
		
		<!-- filter -->
		<td align="right">
			<label>Filter by Symbolic Name: </label>
			<div type="text" jsid="filteredSymbolicName" id="filteredSymbolicName" dojoType="dijit.form.TextBox" style="width: 16em;">
				<script type="dojo/method" event="onKeyUp" args="evt">
				    var fitlerStr = dojo.byId("filteredSymbolicName").value;
        			dijit.byId("grid1").filter({
            			symbolicName: "*" + fitlerStr + "*"     
        			});       
					dojo.stopEvent(evt);
				</script>
			</div>
		</td>
	</tr> 
</table>

<!-- bundles grid -->
<script>
var jsonData=${GridJSONObject};
var jsonStore=new dojo.data.ItemFileWriteStore({data:jsonData});
</script>

<table jsid="grid1" id="grid1" dojoType="dojox.grid.EnhancedGrid"
	plugins="{indirectSelection: true}" store="jsonStore"
	query="{ id: '*' }" style="width:100%;margin:0px 0px;">
	<thead>
		<tr>
			<th field="id">Id</th>
			<th field="symbolicName" width="50%">Symbolic Name</th>
			<th field="version">Version</th>
			<th field="state">State</th>
		</tr>
	</thead>
</table>
<br/>


<!-- headers grid -->
<script>
var headersStore=new dojo.data.ItemFileWriteStore({data:""});
</script>
<div id="singleBundleDiv" style="display:none">
<table jsid="headersgrid" id="headersgrid" dojoType="dojox.grid.DataGrid"
	store="headersStore" autoHeight="true" escapeHTMLInData="false" style="width:100%;margin:0px 0px;">
	<thead>
		<tr>
			<th field="hkey" width="20%">Header Name</th>
			<th field="hvalue" width="80%">Header Value</th>
		</tr>
	</thead>
</table>
</div>

</div>


