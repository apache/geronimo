<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<%@ taglib uri="http://portals.apache.org/pluto" prefix="pluto" %>
<fmt:setLocale value="<%=request.getLocale()%>" />
<fmt:setBundle basename="org.apache.geronimo.console.i18n.ConsoleResource"/>
<%@ page import="org.apache.geronimo.pluto.impl.PageConfig"%>

<style type="text/css">
body {
    padding:0;
    margin:0;
    height:100%;
    scroll:no;
    overflow:hidden;
}
</style>

<script type="text/javascript" src="/console/dojo/dojo/dojo.js" djConfig="parseOnLoad: true"></script>
<script type="text/javascript" src="/console/dojo/dijit/dijit.js"></script>
<script type="text/javascript" src="/console/dojo/dojox/dojox.js" ></script>

<script type="text/javascript">
//we have to use dojo.hash to maintain the hash change history because browser does not 
//recogonize a hash change when users click back/forward button.

dojo.require("dojo.hash");
dojo.require("dojox.collections.Dictionary");
dojo.require("dojo.io.iframe");

var hash_iframeSrc_map= new dojox.collections.Dictionary;

dojo.subscribe("/dojo/hashchange", this, this.onHashChange);

function onHashChange(current_hash) {

    if(!current_hash||current_hash.length==0) return;
    
    var currentIframeHref=document.getElementById("portletsFrame").contentWindow.location.href;
    
    if(hash_iframeSrc_map.containsKey(current_hash)){
    
        var HrefForCurrentHash=hash_iframeSrc_map.entry(current_hash).value;
    
        if(HrefForCurrentHash==currentIframeHref) return;

        dojo.io.iframe.setSrc(document.getElementById("portletsFrame"), hash_iframeSrc_map.entry(current_hash).value, true);
    
    } else {
    
        hash_iframeSrc_map.add(current_hash,currentIframeHref);
       
    }
}

//When there's hash in current page url, redirect the page with noxxsPage hash as the query string,
//the server side will get the real redirect target page based on the value of noxxsPage
if(document.location.hash!='') {
    var href = document.location.href;
    var newHref = href.substring(0,href.lastIndexOf("#"));
    if(newHref.indexOf("&noxssPage")>0){
        newHref = newHref.substring(0,href.indexOf("&noxssPage"));
    }
    document.location.href = newHref + "&noxssPage=" +document.location.hash.substr(11,document.location.hash.length);
}

</script>

<%-- Confirm Message Definition: Start --%>
<style type="text/css">
#darkCover {
    position:fixed;
    z-index:1000; 
    width:100%; 
    height:100%; 
    top:0; 
    left:0; 
    background-color:#333333;
    filter:alpha(opacity=70);
    opacity:0.7;
}
</style>
<div id="darkCover" style="display:none"></div>
<script type="text/javascript">
    function globalConfirm(msg){
        document.getElementById("darkCover").style.display='block';
        var result = confirm(msg);
        document.getElementById("darkCover").style.display='none';
        return result;
    }
</script>
<%-- Confirm Message Definition: End --%>

<%-- Loading Message Definition: Start --%>
<style type="text/css">
#statusDiv {
    position:fixed;
    z-index:999; 
    width:100%; 
    top:0; 
    left:0; 
    
}
#statusText{
    background-color:#ffc129; 
    font-size:14px; 
    font-weight:bold;
    font-family: Verdana, Tahoma, Arial, Helvetica, sans-serif;
    text-align:center;
    white-space: nowrap; 
}
</style>
<div id="statusDiv" style="display:none; filter:alpha(opacity=100); opacity:1;" align="center">
    <table width="100px" height="24px" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td style="width:6px; background:url('/console/images/loading-left.png');"><img src="/console/images/spacer.gif" border="0" width="6px" /></td>
            <td id="statusText"></td>
            <td style="width:6px; background:url('/console/images/loading-right.png');"><img src="/console/images/spacer.gif" border="0" width="6px" /></td>
        </tr>
    </table>
</div>
<script type="text/javascript">
    var to;
    function showStatus(txt){
        var showTarget = document.getElementById("statusDiv");
        if (to) {
            clearTimeout(to);
            setOpacity(showTarget, 1);
        }
        document.getElementById("statusText").innerHTML = txt;
        showTarget.style.display='block';
    }
    function hideStatus(){
        var hideTarget = document.getElementById("statusDiv");
        if (hideTarget.style.display=='block'){
            hideGradually(hideTarget);
        }
    }
    function hideGradually(obj) {
        var i = getOpacity(obj);
        i = i - 0.05;
        if(i<=0){
            obj.style.display = "none";
            setOpacity(obj, 1);
        } else {
            setOpacity(obj, i);
            to = setTimeout(function(){hideGradually(obj)}, 50);
        }
    }
    function getOpacity(obj){
        if (obj.style.opacity) return obj.style.opacity;
        if (obj.filters) return obj.filters.alpha.Opacity/100;  //IE 8 and earlier
    }
    function setOpacity(obj, v) {
        if (obj.style.opacity) obj.style.opacity = v;
        if (obj.filters) obj.filters.alpha.Opacity = v*100;  //IE 8 and earlier
    }
    
</script>
<%-- Loading Message Definition: End --%>

<%-- Calculate Size Definition: Start --%>
<script type="text/javascript">
    function getCombinedStyle(obj,attribute){
        // IE 8 and earlier using obj.currentStyle
	    return obj.currentStyle?obj.currentStyle[attribute]:document.defaultView.getComputedStyle(obj,false)[attribute];   
    } 
    function px2num(px){
        return Number(px.replace(/[p|P][x|X]/g,""));
    }
    // the init height and width  values are in pluto.css
    function calculateSize(){
        try{
            var header = document.getElementById("headerDiv");
            
            var navigation = document.getElementById("navigationDiv");
            var panel = document.getElementById("panelDiv");
            var tree = document.getElementById("treeDiv");
            
            var content = document.getElementById("contentDiv");
            
            var contentHeight = Number(document.body.clientHeight) - px2num(getCombinedStyle(header,"height"));
            var contentWidth = Number(document.body.clientWidth) - px2num(getCombinedStyle(navigation,"width"));
            var treeHeight = contentHeight - px2num(getCombinedStyle(panel,"height"));
            
            navigation.style.height = contentHeight+ "px";
            tree.style.height = treeHeight+ "px";
            content.style.height = contentHeight+ "px";
            content.style.width = contentWidth+ "px";
            
        }catch (ex){
            window.status = ex.message;
        }
    }
    window.onresize=calculateSize;
</script>
<%-- Calculate Size Definition: End --%>

<body id="admin-console" onload="calculateSize()">

<%-- start accessibility prolog --%>
<div class="skip"><a href="#navigationDiv" accesskey="1">Skip to navigation</a></div>
<div class="skip"><a href="#contentDiv" accesskey="2">Skip to main content</a></div>
<div id="access-info">
    <p class="access">The access keys for this page are:</p>
    <ul class="access">
        <li>ALT plus 1 skips to navigation.</li>
        <li>ALT plus 2 skips to main content.</li>
    </ul>
</div>
<%-- end accessibility prolog --%>


<!-- Header -->
<div id="headerDiv">
    <jsp:include page="banner.jsp"/>
</div>

<!-- Navigation -->
<div id="navigationDiv" class="claro">
    <jsp:include page="navigation.jsp"/>
</div>

<!-- Content -->
<div id="contentDiv">
    <iframe src="" id="portletsFrame" name="portletsFrame" width="100%" height="100%" scrolling="yes" style="overflow-x:hidden;overflow-y:scroll" frameborder="0">
    </iframe>
</div>

</body>

<script type="text/javascript">
    <% 
    PageConfig pc=(PageConfig)request.getAttribute("currentPage");
    String pageID=pc.getName();
    String pageName=pageID.substring(pageID.lastIndexOf("/")+1,pageID.length());
    %>
    var pageName = "<fmt:message key="<%=pageName%>"/>";
    quickLaunchPortlets(pageName);
</script>

