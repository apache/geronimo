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

function populateEnvironment(envJson) {
    var envForm = dijit.byId("environmentForm");
    envForm.setValues(envJson);
}

function saveEnvironment() {
    EARHelper.saveEnvironmentJson(dijit.byId("environmentForm").getValues());
    
    var treeItems = [];
    dijit.byId("dependenciesTree").store.fetch({
        query: {name: "*"},
        scope: this,
        onItem: function(item) {treeItems.push({name: item.name[0]})},
        onComplete: function() {
            var depJson = new Object();
            depJson.identifier = 'name';
            depJson.label = 'name';
            depJson.items = treeItems;
            EARHelper.saveDependenciesJsonTree(depJson);
        }
    });
}

function _formatElements(planHtml, expr, className, prefix, suffix, beg, end) {
    var values = planHtml.match(expr);
    if(values != null) {
        beg = prefix.length;
        end = suffix.length;
        dojo.forEach(values, function(v) {
            planHtml = planHtml.replace(v, prefix + '<span class="' + className + '">' + v.substring(beg,v.length-end) + '</span>' + suffix);
        }, this);
    }
    return planHtml;
}

function _formatPlan(planHtml) {
    planHTML = planHtml.replace(/\r/g, '');
    planHTML = planHtml.replace(/\n/g, '<br/>');

    // strings
    planHtml = _formatElements(planHtml, /(=(\")(\S)+(\"))+/g, 'string', '=', '');
    planHtml = _formatElements(planHtml, /(=(\')(\S)+(\'))+/g, 'string', "=", "");

    // values
    planHtml = _formatElements(planHtml, /&gt;(.)*?&lt;/g, 'value', "&gt;", "&lt;");    
    
    // opening tags
    values = planHtml.match(/&lt;(\S)+/g);
    if(values) {
        dojo.forEach(values, function(v) {
            var end = v.indexOf('&gt;');
            if(end != -1)
                v = v.substring(0,end);
            planHtml = planHtml.replace(v, '&lt;<span class="tag">' + v.substring(4) + '</span>');
        }, this);
    }

    // closing tags
    planHtml = _formatElements(planHtml, /&lt;\/(\S)+/g, 'tag', "&lt;", "&gt;");
    return planHtml;
}

function refreshGeneratedPlan() {
    EARHelper.getGeneratedPlan(function(plan) {
        var elem = dojo.byId('generatedPlanDisplayer');
        if(elem.textContent) {
            elem.textContent = plan;
        } else {
            elem.innerText = plan;
        }
        elem.innerHTML = _formatPlan(elem.innerHTML);
    });
}

// Dependencies related methods:
function updateDependenciesTree(dependenciesJson) {
    var dependenciesStore = new dojo.data.ItemFileWriteStore({data: dependenciesJson});
    new dijit.Tree(
        {
            id: 'dependenciesTree',
            store: dependenciesStore,
            label: 'Dependencies'
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

function addDependencyCallback(items, request) {
    value = request.query.name;
    
    if(items == null || items.length == 0) {
        var depTree = dijit.byId("dependenciesTree");
        
        depTree.store.save({scope: {'value': value, 'depTree': depTree}, onComplete: function() {
                depTree.store.newItem({name: value});
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
        depTree.store.newItem({name: newName});
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

/*
**
** Security related data and methods:
**
*/
function updateSecurity(securityJson) {
    var webModules = securityJson.webModules;

    for(module in webModules) {
        var moduleConfig = webModules[module];
        var treeJson = moduleConfig.roleMappings;

        var runAsSubjectsData = {identifier: 'name', label: 'name', items:[]};
        for(i = 0; i < treeJson.items.length; i++) {
            var item = treeJson.items[i];
            var runAsSubject = moduleConfig.runAsSubjects[item.roleName];
            if(runAsSubject) {
                runAsSubjectsData.items.push({name: item.roleName, realm: runAsSubject.realm, id: runAsSubject.id});
            }

            if(item.children == null) delete item.children;
        }

        var securityStore = new dojo.data.ItemFileWriteStore({data: treeJson});
        new dijit.Tree(
            {   id: module+'.form.securityTree',
                store: securityStore,
                label: 'Role Mappings',
                onClick: checkSecurityTreeFocusedNode,
                getLabel: getSecurityTreeLabels,
                getIconClass: getSecurityTreeIcons
            }, dojo.byId(module+'.form.securityTree'));


        var runAsSubjectsStore = new dojo.data.ItemFileWriteStore({data: runAsSubjectsData});
        var treeId = module+'.form.runAsSubjectsTree';
        new dijit.Tree(
            {   id: treeId,
                store: runAsSubjectsStore,
                label: 'Run-as-subjects',
                onClick: checkRunAsSubjectsTreeFocusedNode,
                getLabel: getRunAsSubjectsTreeLabels
            }, dojo.byId(treeId));

        var rolesJson = {identifier: 'name', label: 'name', items: []};
        for(i = 0; i < treeJson.items.length; i++) {
            var item = treeJson.items[i];
            if(!moduleConfig.runAsSubjects[item.roleName])
                rolesJson.items.push({name: item.roleName.toString()});
        }

        dijit.byId(treeId).rolesJson = rolesJson;

        if(moduleConfig.doasCurrentCaller) moduleConfig.doasCurrentCaller = ["true"];
        if(moduleConfig.useContextHandler) moduleConfig.useContextHandler = ["true"];

        for(p in moduleConfig)
            if(moduleConfig[p] == null) delete moduleConfig[p];

        var form = dijit.byId(module+'.form');
        form.reset();
        if(!moduleConfig.securityRealmName) {
            moduleConfig.securityRealmName = dijit.byId(module+'.form.txtSecurityRealmName').getDisplayedValue();
        }
        if(!moduleConfig.credentialStoreRef) {
            moduleConfig.credentialStoreRef = '';
        }
        form.setValues(moduleConfig);
    }
}

function _constructModuleJson(moduleName, securityTree, runAsSubjectsTree) {
    if(dijit.byId(moduleName + ".form").isValid()) {        
        var module = dijit.byId(moduleName + ".form").getValues();
        module.doasCurrentCaller = (dojo.indexOf(module.doasCurrentCaller, "true") != -1);
        module.useContextHandler = (dojo.indexOf(module.useContextHandler, "true") != -1);
        for(val in module)
            if(module[val] == "") delete module[val];

        module.roleMappings = {identifier: 'name', label: 'name', items: []};
        module.runAsSubjects = null;

        var roles = securityTree.rootNode.item.children;
        for(i = 0; i < roles.length; i++) {
            var role = {roleName: roles[i].roleName.toString(), children: []};
            var mappings = roles[i].children;
            if(mappings) {
                for(j = 0; j < mappings.length; j++) {
                    var mapping = {};
                    for(p in mappings[j])
                        if(p[0]!='_') mapping[p] = mappings[j][p].toString();
                    role.children.push(mapping);
                }
            }
            module.roleMappings.items.push(role);
        }

        var runAsSubjects = runAsSubjectsTree.rootNode.item.children;
        for(i = 0; i < runAsSubjects.length; i++) {
            var item = runAsSubjects[i];
            if(item.realm && item.id) {
                module.runAsSubjects = (module.runAsSubjects) ? module.runAsSubjects : {};
                module.runAsSubjects[item.name] = {'realm': item.realm.toString(), 'id': item.id.toString()};
            }
        }
        return module;
    }
    return null;
}

function saveSecurity() {
    var webModules = null;

    for(i = 0; i < dojo.global._moduleNames.length; i++) {
        var moduleName = dojo.global._moduleNames[i];
        var securityTree = dijit.byId(module + ".form.securityTree");
        var runAsSubjectsTree = dijit.byId(module + ".form.runAsSubjectsTree");
        var moduleJson = _constructModuleJson(moduleName, securityTree, runAsSubjectsTree);
        if(moduleJson) {
            webModules = webModules ? webModules : {}; 
            webModules[module] = moduleJson;
        }
    }

    if(webModules)
        EARHelper.saveSecurityJson({'webModules': webModules});
}

function checkRunAsSubjectsTreeFocusedNode(item, node) {
    var currentModule = dijit.byId("securityAccordionContainer").selectedChildWidget.id;
    var btnAdd = dijit.byId(currentModule+".form.btnAddRunAsSubject");
    btnAdd.setAttribute('disabled', true);
    if(node == node.tree.rootNode) {
        if(dijit.byId(currentModule+".form.runAsSubjectsTree").rolesJson.items.length != 0)
            btnAdd.setAttribute('disabled', false);
        dijit.byId(currentModule+".form.btnEditRunAsSubject").setAttribute('disabled', true);
        dijit.byId(currentModule+".form.btnDeleteRunAsSubject").setAttribute('disabled', true);
    } else {
        dijit.byId(currentModule+".form.btnEditRunAsSubject").setAttribute('disabled', false);
        dijit.byId(currentModule+".form.btnDeleteRunAsSubject").setAttribute('disabled', false);
    }
}

function getRunAsSubjectsTreeLabels(item) {
    var name = item.name;
    if(item.realm && item.id)
        name = name + ': realm="' + item.realm + '", id="' + item.id + '"';
    return name;
}

function doAddOrEditRunAsSubject(treeId, isAdding) {
    var dialog = dijit.byId('runAsSubjectDialog');
    dialog.runAsSubjectsTree = dijit.byId(treeId);
    dialog.isAdding = isAdding;

    var select = dijit.byId('selRunAsSubjectRole');
    dialog.reset();
    if(isAdding) {
        dojo.byId('selRunAsSubjectRoleRow').style.display = "";
        select.store = new dojo.data.ItemFileReadStore({data: dialog.runAsSubjectsTree.rolesJson});
        select.setDisplayedValue(dialog.runAsSubjectsTree.rolesJson.items[0].name.toString());
    } else {
        var item = dialog.runAsSubjectsTree.lastFocused.item;
        dialog.setValues({name: item.name.toString(), realm: item.realm.toString(), id: item.id.toString()});
        select.setDisplayedValue(item.name.toString());
        dojo.byId('selRunAsSubjectRoleRow').style.display = 'none';
    }
    dialog.show();
}

function doDeleteRunAsSubject(treeId) {
    var runAsSubjectsTree = dijit.byId(treeId);
    var item = runAsSubjectsTree.lastFocused.item;
    runAsSubjectsTree.store.save({scope: this, onComplete: function() {
            runAsSubjectsTree.rolesJson.items.push({name: item.name.toString()});
            runAsSubjectsTree.store.deleteItem(item);
        }
    });
}

function addOrEditRunAsSubject(runAsSubject) {
    var runAsSubjectDialog = dijit.byId("runAsSubjectDialog");
    var runAsSubjectsTree = runAsSubjectDialog.runAsSubjectsTree;
    var runAsSubject = runAsSubject;
    if(runAsSubjectDialog.isAdding) {
        runAsSubjectsTree.store.save({scope: this, onComplete: function() {
                runAsSubjectsTree.store.newItem(runAsSubject);
                var items = runAsSubjectsTree.rolesJson.items;
                var newItems = [];
                for(var i = 0; i < items.length; i++)
                    if(items[i].name.toString() != runAsSubject.name)
                        newItems.push(items[i]);
                runAsSubjectsTree.rolesJson.items = newItems;
            }
        });
    } else {
        var item = runAsSubjectsTree.lastFocused.item;
        runAsSubjectsTree.store.setValue(item, 'realm', runAsSubject.realm);
        runAsSubjectsTree.store.setValue(item, 'id', runAsSubject.id);
    }
}
function checkRunAsSubjectFields() {
    var dialog = dijit.byId("runAsSubjectDialog");
    var values = dialog.getValues();
    var valid = (values.realm != "") && (values.id != "");
    if(dialog.isAdding) valid = valid && (dijit.byId("selRunAsSubjectRole").isValid());
    dijit.byId("btnRunAsSubjectOK").setAttribute('disabled', !valid);
}

function getSecurityTreeLabels(item) {
    if(item.roleName) return item.roleName;

    var label = "Name: " + item.principalName[0];

    if(item.className && item.className[0]) {
        var className = item.className[0];
        className = className.substring(className.lastIndexOf('.')+1, className.length);
        label = label + ", Class: " + className;
        if(item.domainName && item.domainName[0]) {
            label = label + ", Domain: " + item.domainName[0];
            if(item.realmName && item.realmName[0]) {
                label = label + ", Realm: " + item.realmName[0];
            }
        }
    }
    return label;
}

function getSecurityTreeIcons(item, opened) {
    if(!item || item.root || item.roleName) {
        return (opened ? "dijitFolderOpened" : "dijitFolderClosed")
    } else {
        return "dijit" + item.type[0] + "Icon";
    }
}

function checkSecurityTreeFocusedNode(item, node) {
    var currentModule = dijit.byId("securityAccordionContainer").selectedChildWidget.id;
    dijit.byId(currentModule+".form.btnAdd").setAttribute('disabled', (item.roleName==null));
    var disable = (item.roleName!=null) || (node == node.tree.rootNode);
    dijit.byId(currentModule+".form.btnEdit").setAttribute('disabled', disable);
    dijit.byId(currentModule+".form.btnDelete").setAttribute('disabled', disable);
}

function validatePrincipalName(value) {
        var valid = false;
        if(value != "") {
            valid = true;

            var dialog = dijit.byId("roleMappingDialog");
            if(dialog.roleMappingTree) {
                if(dialog.isAdding) {
                    var existingPrincipals = dialog.roleMappingTree.lastFocused.item.children;
                    if(existingPrincipals) {
                        for(i = 0; i < existingPrincipals.length; i++)
                            if(existingPrincipals[i].principalName == value) {
                                valid = false;
                                break;
                            }
                    }
                } else {
                    var selectedNode = dialog.roleMappingTree.lastFocused;
                    var existingPrincipals = selectedNode.getParent().item.children;
                    if(existingPrincipals) {
                        for(i = 0; i < existingPrincipals.length; i++)
                            if((existingPrincipals[i] != selectedNode.item) && (existingPrincipals[i].principalName == value)) {
                                valid = false;
                                break;
                            }
                    }
                }
            }
        }
        if(!valid) {
            var btnOk = dijit.byId("btnRoleMappingOK");
            if(btnOk)
                btnOk.setAttribute("disabled", true);
        }
        return valid;
}
function addRoleMappingCallback(item) {
    if(!item) {
        var securityTree = this.securityTree;
        var principal = this.principal;
        switch(principal.type) {
            case "Distinguished Name":
                delete principal.className;
            case "Principal":
                delete principal.domainName;
            case "Login Domain Principal":
                delete principal.realmName;
                break;
        }
        securityTree.store.save({scope: this, onComplete: function() {
                securityTree.store.newItem(principal, {parent: securityTree.lastFocused.item, attribute: 'children'});
            }
        });
    }
}

function editRoleMappingCallback(item) {
    var store = this.store;
    var principal = this.principal;

    switch(this.principal.type) {
        case "Distinguished Name":
            delete principal.className;
        case "Principal":
            delete principal.domainName;
        case "Login Domain Principal":
            delete principal.realmName;
            break;
    }

    store.unsetAttribute(item, "className");
    store.unsetAttribute(item, "domainName");
    store.unsetAttribute(item, "realmName");

    for(p in principal) {
        store.setValue(item, p, principal[p]);
    }
    store.save();
}

function addOrEditRoleMapping(principal) {   
    this.securityTree = dijit.byId("roleMappingDialog").roleMappingTree;
    this.store = this.securityTree.store;
    this.principal = principal;  

    if(dijit.byId("roleMappingDialog").isAdding) {
        var roleNode = securityTree.lastFocused;
        roleNode.pCount = (roleNode.pCount) ? (roleNode.pCount+1) : 1;
        principal.name = roleNode.item.roleName + ".principal" + roleNode.pCount;
        this.store.fetchItemByIdentity({identity: principal.name, scope: this, onItem: addRoleMappingCallback});
    } else {
        this.store.fetchItemByIdentity({identity: this.securityTree.lastFocused.item.name, scope: this, onItem: editRoleMappingCallback});
    }
}

function doAddOrEditRoleMapping(treeId, isAdding) {
    checkRoleMappingFields();
    var dialog = dijit.byId("roleMappingDialog");
    dialog.roleMappingTree = dijit.byId(treeId); 
    dialog.isAdding = isAdding;

    if(!isAdding) {
        var principal = dialog.roleMappingTree.lastFocused.item;
        var values = {};

        for(p in principal) {
            if(p[0] != "_") {        
                var value = principal[p].toString();
                if(value != "")
                    values[p] = value;
            }
        }
        dialog.setValues(values);
    }
    checkRoleMappingFields();
    dialog.validate();
    dialog.show();
}

function deleteRoleMappingCallback(item) {
    if(!item) return;
    this.securityTree.store.save({
        scope: {'securityTree': this.securityTree, 'item': item},
        onComplete: function() { this.securityTree.store.deleteItem(this.item) }
    });
}

function doDeleteRoleMapping(treeId) {
    var securityTree = dijit.byId(treeId);
    if(securityTree.lastFocused != null) {
        securityTree.store.fetchItemByIdentity({
            identity: securityTree.lastFocused.item.name,
            onItem: deleteRoleMappingCallback,
            scope: {'securityTree': securityTree}
        });
    }
}

function _setRoleMappingFieldVisibility(fieldName, hide) {
    var field = dijit.byId(fieldName);
    field.setAttribute("disabled", hide);
    field.domNode.parentNode.parentNode.style.display = (hide ? "none" : "");
}

function modifyRoleMappingForm(mappingType) {
    var isPrincipal = (mappingType == "Principal");
    var isLoginDomain = (mappingType == "LoginDomainPrincipal");
    var isRealm = (mappingType == "RealmPrincipal");
    var isDistinguished = (mappingType == "DistinguishedName");

    _setRoleMappingFieldVisibility("selRoleMappingClass", isDistinguished);
    _setRoleMappingFieldVisibility("txtRoleMappingDomainName", isPrincipal || isDistinguished);
    _setRoleMappingFieldVisibility("selRoleMappingRealmName", isPrincipal || isDistinguished || isLoginDomain);

    checkRoleMappingFields();
}

function checkRoleMappingFields() {
    var domainNameField = dijit.byId("txtRoleMappingDomainName");
    if(!dijit.byId("txtRoleMappingName").isValid() || (!domainNameField.disabled && !domainNameField.isValid()))
        dijit.byId("btnRoleMappingOK").setAttribute("disabled", true);
    else
        dijit.byId("btnRoleMappingOK").setAttribute("disabled", false);
}

function doPrevious() {
    var tabContainer = dijit.byId("mainTabContainer");
    if(tabContainer.selectedChildWidget.title != "Environment") 
        tabContainer.back();
}

function doNext() {
    var tabContainer = dijit.byId("mainTabContainer");
    if(tabContainer.selectedChildWidget.title != "Generated Plan")
        tabContainer.forward();
}

function saveGeneratedPlan() {
    var elem = dojo.byId('generatedPlanDisplayer');
    var plan = (elem.textContent ? elem.textContent : elem.innerText);
    EARHelper.saveGeneratedPlan(plan);
}

function onTabSwitch(page) {
    switch(this.currentTab) {
        case 'environment': saveEnvironment(); break;
        case 'references': /* saveReferences() */; break;
        case 'security': saveSecurity(); break;
        case 'generatedPlan': /* saveGeneratedPlan(); */ break;
    }
    this.currentTab = page.id;
    switch(this.currentTab) {
        case 'generatedPlan': refreshGeneratedPlan(); break;
    }
}

dojo.addOnLoad(function(){
    EARHelper.getEnvironmentJson(populateEnvironment);
    EARHelper.getDependenciesJsonTree(updateDependenciesTree);
    EARHelper.getSecurityJson(updateSecurity);

    var dlgDep = dijit.byId("dependenciesDialog");
    dlgDep._getFocusItems = function(node) {}
    dlgDep._firstFocusItem = dojo.byId("depChkBox_1");
    dlgDep._lastFocusItem = dojo.byId("btnAdd");

    var nodeCount = 0;
    dojo.query("tr", "dependenciesDialog").filter(
        function(row) {
            return (nodeCount++) % 2 == 0;
        }).style("backgroundColor","rgb(240,250,255)");

    dojo.subscribe('mainTabContainer-selectChild', {currentTab: 'environment'}, onTabSwitch);

    var moduleWidgets = dijit.byId('securityAccordionContainer').getChildren();
    dojo.global._moduleNames = [];
    for(i = 0; i < moduleWidgets.length; i++)
        dojo.global._moduleNames.push(moduleWidgets[i].id);
});
