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
/**** Global Confirm ****/
function showGlobalConfirmMessage(msg){
    if (window.parent!=window.self){
        return parent.globalConfirm(msg);
    }else{
        return confirm(msg);
    }
} 

/**** Status Div ****/
function showGlobalStatus(txt){
    if (window.parent!=window.self) {
        parent.showStatus(txt);
    }
}

function hideGlobalStatus(){
    if (window.parent!=window.self) {
        setTimeout("parent.hideStatus()", 300);
    }
}

/**** highlight background-color ****/
var oldBgColor;
function highlightBgColor(target){
    oldBgColor = target.style.backgroundColor;
    target.style.backgroundColor = '#e2ebfe';
}

function recoverBgColor(target){
    target.style.backgroundColor = oldBgColor;
}

/**** show hide by Id ****/
function showHideById(id) {
    document.getElementById(id).style.display = (document.getElementById(id).style.display=='none')?'block':'none';
}