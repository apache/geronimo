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

<body id="admin-console" marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" rightmargin="0">


<script type="text/javascript" src="/dojo/dojo/dojo.js" djConfig="parseOnLoad: true">

</script>


<script>
<%-- we have to use dojo.hash to maintain the hash change history because browser does not 
recogonize a hash change when users click back/forward button.--%>

dojo.require("dojo.hash");
dojo.require("dojox.collections.Dictionary");

var hash_iframeSrc_map= new dojox.collections.Dictionary;


function onHashChange(current_hash) {

    if(current_hash&&!hash_iframeSrc_map.containsKey(current_hash)){

        hash_iframeSrc_map.add(current_hash,document.getElementById("portletsFrame").src);

    } else {
        document.getElementById("portletsFrame").src=""+hash_iframeSrc_map.entry(current_hash).value;
    }
}

dojo.subscribe("/dojo/hashchange", this, this.onHashChange);


</script>


<script language="JavaScript">

<%-- When there's hash in current page url
redirect the page with noxxsPage hash as the query string,
the server side will get the real redirect target page based on the value of noxxsPage--%>

 if(document.location.hash!='') {
       var href = document.location.href;
       var newHref = href.substring(0,href.lastIndexOf("#"));
       if(newHref.indexOf("&noxssPage")>0){
            newHref = newHref.substring(0,href.indexOf("&noxssPage"));
       }
       document.location.href =  newHref + "&noxssPage=" +document.location.hash.substr(11,document.location.hash.length);
    }

var iframeId;

function delayResize(id){
    iframeId=id;
    <%--delay the resize so that the ajax content get loaded before the resizing.--%>
    setTimeout('autoResize()',300); 
}

function autoResize(){

  try{
    frame = document.getElementById(iframeId);
    frame_document = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
    objToResize = (frame.style) ? frame.style : frame;
    objToResize.height = frame_document.body.scrollHeight + 10;
  }
  catch(err){
    window.status = err.message;
  }

}


</script>

<!-- start accessibility prolog -->
<div class="skip"><a href="#left-nav" accesskey="1">Skip to navigation</a></div>
<div class="skip"><a href="#content" accesskey="2">Skip to main content</a></div>
<div id="access-info">
    <p class="access" >The access keys for this page are:</p>
    <ul class="access">
        <li>ALT plus 1 skips to navigation.</li>
        <li>ALT plus 2 skips to main content.</li>
    </ul>
</div>
<!-- end accessibility prolog -->
<table width="100%" cellpadding="0" cellspacing="0" border="0" id="rootfragment">

    <!-- Header -->
    <%@ include file="./banner.jsp" %>

    <tr>
        <td>
            <table width="100%"  border="0" cellpadding="0" cellspacing="0">
                <!-- Spacer -->
                <tr> 
                    <td class="Gutter">&nbsp;</td> 
                    <td>&nbsp;</td> 
                    <td class="Gutter">&nbsp;</td> 
                    <td>&nbsp;</td> 
                    <td class="Gutter">&nbsp;</td> 
                </tr> 
                
                <!-- Start of Body -->
                <tr>
                    <!-- Navigation Column -->
                    <td class="Gutter">&nbsp;</td> <!-- Spacer -->
                    <td width="200px" class="Selection" valign="top"> 
                        <div id="left-nav"> 
                            <table width="100%"  border="0" cellpadding="0" cellspacing="0"> 
                                <tr> 
                                    <td >
                                        <!-- Include Navigation.jsp here -->
                                        <jsp:include page="navigation.jsp"/>
                                    </td>
                                </tr>
                            </table>
                        </div>
                    </td>

                    <!-- Portlet Section -->
                    <td class="Gutter">&nbsp;</td> <!-- Spacer -->
                    <td valign="top">
                
                    <iframe  src="" id="portletsFrame" width="100%" height="100%" scrolling="no" frameborder="0" onload="if (window.parent && window.parent.delayResize) {window.parent.delayResize('portletsFrame');}">
                    
                    </iframe>
                    </td>

                    <td class="Gutter">&nbsp;</td> <!-- Spacer -->
                    <td class="Gutter">&nbsp;</td> <!-- Spacer -->
                </tr>
                <!-- End of Body -->
            </table>
        </td>
    </tr>
</table>
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
