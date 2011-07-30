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

// $Rev: 753156 $ $Date: 2009-03-13 16:21:04 +0800 (Fri, 13 Mar 2009) $
    function createNavigationTree(treeStore1,listStore1,showMode) {
        treeModel = new dijit.tree.ForestStoreModel({store: treeStore1});  
        if(showMode=="basic"){
            dojo.byId("navigationTreeAdvanced").style.display="none"; 
            dojo.byId("navigationTreeBasic").style.display="block"; 
            if(dijit.byId("navigationTreeBasic")==null){
                navigationTreeBasic = new dijit.Tree(
                    {  
                        model: treeModel,
                        showRoot: false,
                        openOnClick: true,
                        autoExpand: true,
                        onClick: function(treeNodeItem,treeNode) {
                            //var anchorNode=treeNode.labelNode.childNodes[2];
                            var iframeHref = treeStore1.getValue(treeNodeItem, "href");
                            if(iframeHref){
                                displayPortlets(iframeHref,"navigationTreeBasic");
                            }                     
                        },
                        _createTreeNode: function(args) {
                            var tnode = new dijit._TreeNode(args);
                            tnode.labelNode.innerHTML = args.label;
                            return tnode;
                        }
                    },
                    dojo.byId("navigationTreeBasic")
                );
            }
        }else{
            dojo.byId("navigationTreeBasic").style.display="none"; 
            dojo.byId("navigationTreeAdvanced").style.display="block"; 
            if(dijit.byId("navigationTreeAdvanced")==null){
                navigationTreeAdvanced = new dijit.Tree(
                    {  
                        model: treeModel,
                        showRoot: false,
                        openOnClick: true,
                        autoExpand: false,
                        onClick: function(treeNodeItem,treeNode) {
                            //var anchorNode=treeNode.labelNode.childNodes[2];
                            var iframeHref = treeStore1.getValue(treeNodeItem, "href");
                            if(iframeHref){
                                displayPortlets(iframeHref,"navigationTreeAdvanced");
                            }                     
                        },
                        _createTreeNode: function(args) {
                            var tnode = new dijit._TreeNode(args);
                            tnode.labelNode.innerHTML = args.label;
                            return tnode;
                        }
                    },
                    dojo.byId("navigationTreeAdvanced")
                );
            }
        }
    }

    function quickLaunchPortlets(portalPageName,navigationTree){
        listStore.fetchItemByIdentity({identity:portalPageName,
            onItem:function(item){
                var iframeHref = listStore.getValue(item,"href");
                displayPortlets(iframeHref);
            }
        });
        if(dijit.byId(navigationTree)){
            findAndSelect(portalPageName,navigationTree);
        }
    }

    function displayPortlets(iframeHref){
        showStatus("Loading...");
        if(document.location.href.indexOf(iframeHref)==0){      
            iframeHref=document.location.href.substring(0,document.location.href.indexOf("?"));
        }
        dojo.io.iframe.setSrc(document.getElementById("portletsFrame"), iframeHref+"?formId="+formID, true);
        
    }

    function changeMode(){
        var rmode=document.getElementsByName("mode");
        if(rmode[0].checked){
             createNavigationTree(treeStoreBasic,listStoreBasic,"basic");
             return;
        }else{
             createNavigationTree(treeStore,listStore,"advanced");
             return;
        }
          
    }
    //scripts to expand and select tree node automatically when open menu item from quick launcher

   
    function findAndSelect(key,navigationTree){
        rootNode=dijit.byId(navigationTree).rootNode
        var pathToExpandItems = [];

        if(findRecur(rootNode.item.children, key, pathToExpandItems)){
            select(pathToExpandItems,navigationTree);
        } 
    }


    function findRecur(items, key, pathToExpandItems) {
        for (var child = 0; child < items.length; child++) {

            pathToExpandItems.push(items[child]);
            var label = treeStore.getLabel(items[child]);
            if (label && label.indexOf(key) != -1)
                return true;

            if (items[child].children && findRecur(items[child].children, key, pathToExpandItems))
                return true;
            pathToExpandItems.pop();
        }
        return false;
    }
    
    function select(pathToExpandItems,navigationTree){
        var navigationTree=dijit.byId(navigationTree);
        var i;
        function expandParent(node) {
            if(node && !node.isExpanded) {
                expandParent(node.getParent());
                navigationTree._expandNode(node);
            }
        }
        //make sure the ancestor node expanded before
        var firstNode = getTreeNode(navigationTree,pathToExpandItems[0],treeStore);     
        expandParent(firstNode.getParent());
        
        for (i = 0;;i++) {
            node  = getTreeNode(navigationTree,pathToExpandItems[i],treeStore);
            if(i < pathToExpandItems.length-1) {
                navigationTree._expandNode(node);
            } else {
                navigationTree.focusNode(node);
                return node;
            }
        }
    }
    
    function getTreeNode(tree,item,treeStore){
        var wrapperNode =tree._itemNodesMap[treeStore.getIdentity(item)];
        return wrapperNode[0];
    } 
   

