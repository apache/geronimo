//======================================================================
//   Licensed to the Apache Software Foundation (ASF) under one or more
//   contributor license agreements.  See the NOTICE file distributed with
//   this work for additional information regarding copyright ownership.
//   The ASF licenses this file to You under the Apache License, Version 2.0
//   (the "License"); you may not use this file except in compliance with
//   the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//======================================================================

// $Rev$ $Date$

function updateEarTree(earTree) {
    var earTreeStore = new dojo.data.ItemFileReadStore({data: earTree});
    new dijit.Tree({id: 'referencesTree', store: earTreeStore, label: 'EAR'}, dojo.byId("referencesTreeHolder"));
    new dijit.Tree({id: 'securityTree', store: earTreeStore, label: 'EAR'}, dojo.byId("securityTreeHolder"));
}

function updateDependenciesTree(dependenciesJson) {
    var dependenciesStore = new dojo.data.ItemFileWriteStore({data: dependenciesJson});
    new dijit.Tree(
  	    {
            id: 'dependenciesTree', 
            store: dependenciesStore,
            label: 'Dependencies',
        }, dojo.byId("dependenciesTree"));
        
    var items = dependenciesJson.items;
     
    for(i = 0; i < items.length; i++) {
        var chkBoxElement = dojo.query("input[value='"+items[i].name[0]+"']","dependenciesDialog")[0];
        
        if(chkBoxElement) {
            var chkBoxWidget = dijit.byId(chkBoxElement.id);
            chkBoxWidget.setChecked(true);
            chkBoxWidget.setAttribute("disabled", true);
        }
    }
}

function populateEnvironment(envJson) {
    var envForm = dijit.byId("environmentForm");
    envForm.setValues(envJson);
}

function saveEnvironment(envJson) {
    EARHelper.saveEnvironmentJson(envJson);
}

function refreshGeneratedPlan() {
    EARHelper.getGeneratedPlan(function(plan) {
        dojo.byId("generatedPlanDisplayer").value = plan;
    });
}
  
function saveDependencies() {
    var treeItems = [];
    dijit.byId("dependenciesTree").store.fetch({
        query: {name: "*"},
        scope: this,
        onItem: function(item) { treeItems.push({name: item.name[0]}) },
        onComplete: function() {
            var depJson = new Object();
            depJson.identifier = 'name';
            depJson.label = 'name';
            depJson.items = treeItems;
            EARHelper.saveDependenciesJsonTree(depJson);
        }
    });
}

function addDependencyCallback(items, request) {
    value = request.query.name;
    
    if(items == null || items.length == 0) {
        var depTree = dijit.byId("dependenciesTree");
        
		depTree.store.save({scope: {'value': value, 'depTree': depTree}, onComplete: function() {
                depTree.store.newItem({name: value, attribute:'name'});
                depTree._expandNode(depTree.rootNode);
            }
        });
    }
}
  
function addNewDependency(value) {
    if(value != ' ') {
        dijit.byId("dependenciesTree").store.fetch({query:{name:value}, onComplete: addDependencyCallback})
    }
}

function addDependencies(arg) {
    var deps = arg.dependencies;
    for(i = 0; i < deps.length; i++) {
        addNewDependency(deps[i]);
    }
    var checkedElements = dojo.query("input","dependenciesDialog").filter(
        function(elem) { 
            return elem.checked; 
        }).forEach(
            function(item) {
                dijit.byId(item.id).setAttribute("disabled", true);
            });
}

function doAddDependencies() {
    dijit.byId('dependenciesDialog').show(); 
}

function deleteDependencyCallback(item) {
    if(!item) return;
    var name = item.name[0];
    var chkBoxElement = dojo.query("input","dependenciesDialog").filter(
        function(elem) {
            return elem.value == name; 
        }
    )[0];

    if(chkBoxElement) {
        var chkBoxWidget = dijit.byId(chkBoxElement.id);
        chkBoxWidget.setChecked(false);
        chkBoxWidget.setAttribute("disabled", false);

		var depTree = dijit.byId("dependenciesTree");

		depTree.store.save({
		    scope: item,
		    onComplete: function() { dijit.byId("dependenciesTree").store.deleteItem(this) }
		});
    }
}
  
function doDeleteDependency() {
    depTree = dijit.byId("dependenciesTree");
    if(depTree.lastFocused != null) {
        if(depTree.lastFocused == depTree.rootNode) {
            if(confirm("Delete all dependencies ?")) {
                depTree.store.fetch({query: {name: "*"}, onItem: deleteDependencyCallback});
            }
        }
        depTree.store.fetchItemByIdentity({
            identity: depTree.lastFocused.label,
            onItem: deleteDependencyCallback 
        });
    }
}
  
function editDependencyTo(dialogInputs) {
    var name = dojo.byId("depEditPrevName").value;
    var newName = dialogInputs.groupId + "/" + dialogInputs.artifactId + "/" + dialogInputs.version + "/" + dialogInputs.type;
    if(name == newName) return;

    var chkBoxElement = dojo.query("input","dependenciesDialog").filter(
        function(elem) {
            return elem.value == name;
        }
    )[0];

    if(chkBoxElement) {
        dijit.byId(chkBoxElement.id).setValue(newName);
        chkBoxLabel = dojo.query("label","dependenciesDialog").filter(
            function(elem) {
                return elem.innerHTML == name;
            }
        )[0];
        chkBoxLabel.innerHTML = newName;
        var depTree = dijit.byId("dependenciesTree");
        depTree.store.fetchItemByIdentity({
            identity: name, 
            onItem: function(item) { 
                dijit.byId("dependenciesTree").store.deleteItem(item)
            }
        });
        depTree.store.newItem({name: newName, attribute: 'name'});
    }
}

function editDependencyCallback(item) {
    if(!item)
        return;
    var name = item.name[0];
    var elems = name.split('/');
    dijit.byId("txtGroupId").setValue(elems[0]);
    dijit.byId("txtArtifactId").setValue(elems[1]);
    dijit.byId("txtVersion").setValue(elems[2]);
    dijit.byId("txtType").setValue(elems[3]);
    dojo.byId("depEditPrevName").value = name;
    dijit.byId("editDependencyDialog").show();
}

function doEditDependency() {
    depTree = dijit.byId("dependenciesTree");
    if(depTree.lastFocused != null) {
        depTree.store.fetchItemByIdentity({
            identity: depTree.lastFocused.label,
            onItem: editDependencyCallback
        });
    }
}

function checkDepEditFields() {
    if(dijit.byId("txtArtifactId").getValue() == "") {
        dijit.byId("btnDepEditOK").setAttribute("disabled", true);
        dojo.byId("depEditStatus").innerHTML = "<img src='/dojo/dijit/themes/tundra/images/warning.png'/> Invalid artifact id";
    } else {
        dijit.byId("btnDepEditOK").setAttribute("disabled", false);
        dojo.byId("depEditStatus").innerHTML = "";
    }
}

dojo.addOnLoad(function(){
    EARHelper.getEnvironmentJson(populateEnvironment);
    EARHelper.getDependenciesJsonTree(updateDependenciesTree);
    EARHelper.getEarTree(updateEarTree);

    var dlgDep = dijit.byId("dependenciesDialog");
    dlgDep._getFocusItems = function(node) {}
    dlgDep._firstFocusItem = dojo.byId("depChkBox_1");
    dlgDep._lastFocusItem = dojo.byId("btnAdd");
    
    var nodeCount = 0;
    dojo.query("tr", "dependenciesDialog").filter(
        function(row) {
            return (nodeCount++) % 2 == 0;
        }).style("backgroundColor","rgb(240,250,255)");

    dojo.connect(dijit.byId('generatedPlan').controlButton, 'onClick', refreshGeneratedPlan);
});
