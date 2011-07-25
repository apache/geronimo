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
function addErrorMessage(namespace, message){
    var msg = {};
    msg.type = "error";
    msg.abbr = message;
    addCommonMessage(namespace, msg);
}

function addWarningMessage(namespace, message){
    var msg = {};
    msg.type="warn";
    msg.abbr = message;
    addCommonMessage(namespace, msg);
}

function addInfoMessage(namespace, message){
    var msg = {};
    msg.type="info";
    msg.abbr = message;
    addCommonMessage(namespace, msg);
}

function addCommonMessage(namespace, msg){
    var container = document.getElementById(namespace+"CommonMsgContainer");
    var pre = container.previousSibling;
    while (pre) {
        if (pre.className=="messagePortlet") {
            container.parentNode.removeChild(pre);
            break;
        }
        pre = pre.previousSibling;
    }
    while(container.firstChild) {
        container.removeChild(container.firstChild);
    }
    var table = document.createElement("table");
    container.appendChild(table);
    table.align="center"; table.vAlign="top"; table.width="100%"; table.border="0"; table.cellSpacing="0"; table.cellPadding="0"; table.className="messagePortlet"; table.summary="Inline Messages";
    var tbody =  document.createElement("tbody");
    table.appendChild(tbody);
    var tr = document.createElement("tr");
    tbody.appendChild(tr);    
    tr.vAlign="top";
    var td1 = document.createElement("td");
    tr.appendChild(td1);    
    td1.style.width="20px";
    if (msg.detail) {
        var a = document.createElement("a");
        td1.appendChild(a);
        a.className="expand-task"; a.href="javascript:showHideSection('"+namespace+"org_apache_geronimo_abbreviateMessages');showHideSection('"+namespace+"org_apache_geronimo_detailedMessages');"; a.tabIndex="1";
        var img = document.createElement("img");
        a.appendChild(img);
        img.id=namespace+"org_apache_geronimo_abbreviateMessagesImg"; img.border="0"; img.align="absmiddle"; img.alt="show/hide"; img.src="/console/images/arrow_collapsed.gif"; img.title="show/hide";
    }
    var td2 = document.createElement("td");
    tr.appendChild(td2);    
    td2.style.width="20px";
    var img2 = document.createElement("img");
    td2.appendChild(img2);
    img2.height="16"; img2.width="16"; img2.align="baseline";
    switch (msg.type)
    {
        case "error":
            img2.src="/console/images/msg_error.gif"; img2.alt="Error"; img2.title="Error";
            break;
        case "warn":
            img2.src="/console/images/msg_warn.gif"; img2.alt="Warn"; img2.title="Warning";
            break;
        case "info":
        default:
            img2.src="/console/images/msg_info.gif"; img2.alt="Info"; img2.title="Infomation";
    }
    var td3 = document.createElement("td");
    tr.appendChild(td3);
    var span1 = document.createElement("span");
    td3.appendChild(span1);
    span1.id=namespace+"org_apache_geronimo_abbreviateMessages"; span1.style.display="inline";
    switch (msg.type)
    {
        case "error":
            span1.className="validation-error";
            break;
        case "warn":
            span1.className="validation-warn";
            break;
        case "info":
        default:
            span1.className="validation-info";
    }
    span1.appendChild(document.createTextNode(msg.abbr));
    if (msg.detail) {
        var span2 = document.createElement("span");
        td3.appendChild(span2);
        span2.id=namespace+"org_apache_geronimo_detailedMessages"; span2.style.display="none";
        switch (msg.type)
        {
            case "error":
                span2.className="validation-error";
                break;
            case "warn":
                span2.className="validation-warn";
                break;
            case "info":
            default:
                span2.className="validation-info";
        }
        //var pre = document.createElement("pre");
        //span2.appendChild(pre);
        span2.appendChild(document.createTextNode(msg.abbr));
        span2.appendChild(document.createElement("br"));
        span2.appendChild(document.createTextNode(convertLineBreakToBR(msg.detail)));
        span2.appendChild(document.createElement("br"));
    }
}

function convertLineBreakToBR(str) {
    str = str.replace(/(\r\n|[\r\n])/g, "<br />");
    return str;
}

function convertBRtoLineBreak (str) {
    str = str.replace(/<br \/>/g, "\r\n");
    return str;
}

function showHideSection(id){
    if(document.getElementById(id)!=null){
        if(document.getElementById(id).style.display=="none"){
            document.getElementById(id).style.display="inline";
            if(document.getElementById(id+"Img")){
                document.getElementById(id+"Img").src="/console/images/arrow_collapsed.gif";
            }
        }
        else{
            document.getElementById(id).style.display="none";
            if(document.getElementById(id+"Img")){
                document.getElementById(id+"Img").src="/console/images/arrow_expanded.gif";
            }
        }
    }
}    
