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
<%@ taglib uri="http://portals.apache.org/pluto" prefix="pluto" %>

<body id="admin-console" marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" rightmargin="0">

<script language="JavaScript">

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

                <!-- Content block: portlets are divided into two columns/groups -->
                <!--<div id="body-block" style="height:100%">-->
                <div id="content">
                    <pluto:isMaximized var="isMax" />

                    <c:forEach var="portlet" varStatus="status"	items="${currentPage.portletIds}">
                        <c:set var="portlet" value="${portlet}" scope="request" />
                        <jsp:include page="portlet-skin.jsp" />
                    </c:forEach>

                </div>
                
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

