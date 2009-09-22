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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<CommonMsg:commonMsg/><br>

<table>
   <tr>
       <!-- Body -->
       <td width="100%" align="left" valign="top">
<table width="100%" style="border-style: solid;
border-width: 1px;">
 <thead align="center"><strong><fmt:message key="monitor.common.server"/></strong></thead>
 <tr>
  <th class="DarkBackground" width="30%"><fmt:message key="monitor.common.name"/></th>
  <th class="DarkBackground" width="10%"><fmt:message key="monitor.server.ip"/>/<fmt:message key="monitor.server.hostname"/></th>
  <th class="DarkBackground" width="15%"><fmt:message key="monitor.server.status"/></th>
  <th class="DarkBackground" width="15%"><fmt:message key="monitor.server.statQuery"/></th>
  <th class="DarkBackground" width="30%" colspan="3"><fmt:message key="monitor.common.action"/></th>
 </tr>
 <%
    List<Node> nodes = (List<Node>) request.getAttribute("nodes");
    for (Node node: nodes) {
        boolean online = false;
        Integer collecting = 0;
        MRCConnector mrc = null;
        Long snapshotDuration = new Long(0);
        if (node.isEnabled()) {
	     try {
	         mrc = new MRCConnector(node);
	         online = true;
	     } catch (Exception e) {
	         online = false;
	     }
	     try {
	         snapshotDuration = mrc.getSnapshotDuration();
	         collecting = mrc.isSnapshotRunning();
	     } catch (Exception e) {
	         collecting = 0;
	         online = false;
	     }
         if(null != mrc)
             mrc.dispose();
     }
 %>
  <c:set var="backgroundClass" value='MediumBackground'/>
  <c:choose>
      <c:when test="${backgroundClass == 'MediumBackground'}" >
          <c:set var="backgroundClass" value='LightBackground'/>
      </c:when>
      <c:otherwise>
          <c:set var="backgroundClass" value='MediumBackground'/>
      </c:otherwise>
  </c:choose>
 <tr>
 <%
if(node.isEnabled()){
 %>
  <td class="${backgroundClass}" width="30%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><%=node.getName()%></a></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><%=node.getHost()%></a></td>
<%
}
else{
    %>
    <td class="${backgroundClass}" width="30%" align="center"><%=node.getName()%></td>
    <td class="${backgroundClass}" width="10%" align="center"><%=node.getHost()%></td>
  <% 
}
if (online) {     // online
%>
  <td class="${backgroundClass}" width="15%" align="center" bgcolor="#cccccc"><fmt:message key="monitor.server.online"/></td>
<%
} else if(node.isEnabled()){         // offline
%>
  <td class="${backgroundClass}" width="15%" align="center"><font color="red"><img border=0 src="/monitoring/images/help-b.png" alt="Offline"><fmt:message key="monitor.server.offline"/></font></td>
<%
} else {         // Disabled
     %>
       <td class="${backgroundClass}" width="15%" align="center"><font color="red"><fmt:message key="monitor.server.disabled"/></font></td>
     <%
     }
if (collecting == 0) {  // not collecting statistics
%>
    <td class="${backgroundClass}" width="15%" align="center"><font color="red">(<fmt:message key="monitor.server.stopped"/>)</font></td>
<%
    if(node.isEnabled()) {   // enable the links
        if(request.isUserInRole("admin")){
%>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="startThread" /><portlet:param name="server_id" value="<%=node.getName()%>" /><portlet:param name="snapshotDuration" value="<%=java.lang.Long.toString(snapshotDuration)%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query"><fmt:message key="monitor.server.enableQuery"/></a></td>
<%        }//end admin
    } else {        // do not provide links
        if(request.isUserInRole("admin")){
%>
        <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query"><fmt:message key="monitor.server.enableQuery"/></td>
<%        }//end admin
    }
if(request.isUserInRole("admin")){
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
<%}//end admin
} 
else if (collecting == -1) {  // not collecting statistics
    %>
        <td class="${backgroundClass}" width="15%" align="center"><font color="red"><fmt:message key="monitor.server.stopping"/></font></td>
    <%
        if(node.isEnabled()) {   // enable the links
            if(request.isUserInRole("admin")){
    %>
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.disableQuery"/></td>
    <%    }//end admin
        } else {        // do not provide links
            if(request.isUserInRole("admin")){
    %>
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query"><fmt:message key="monitor.server.enableQuery"/></td>
    <%        }//end admin
        }
    if(request.isUserInRole("admin")){
    %>
      <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
    <%}//end admin
    }
else {            // collecting statistics
    if (node.isEnabled())
    {
%>
  <td class="${backgroundClass}" width="15%" align="center"><%=snapshotDuration/1000/60+" min. (running)"%></td>
  <%if(request.isUserInRole("admin")){ %>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="stopThread" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.disableQuery"/></a></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
<%}//end admin

    }
    else
    {
        %>
        <td class="${backgroundClass}" width="15%" align="center"><fmt:message key="monitor.server.stopped"/></td>
        <%if(request.isUserInRole("admin")){ %>
        <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.enableQuery"/></td>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
      <%   }//end admin     
    }
}
if(node.isEnabled()) {   // enabled server
    if(request.isUserInRole("admin")){
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="disableServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png" alt="Disable"><fmt:message key="monitor.server.disable"/></a></td>
<%}//end admin
} else {        // disabled server
    if(request.isUserInRole("admin")){
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="enableServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Enable"><fmt:message key="monitor.server.enable"/></a></td>
<%}//end admin
}
%>
 </tr>
 <%}
%>
</table>
<%if(request.isUserInRole("admin")){ %>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Add Server"><fmt:message key="monitor.server.addServer"/></a></div>
<%}
%>
        </td>
     
         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">

            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.nav"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>"><fmt:message key="monitor.common.home"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>"><fmt:message key="monitor.common.view"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>"><fmt:message key="monitor.common.server"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>"><fmt:message key="monitor.common.graph"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            
        </td>        
    </tr>
</table>
