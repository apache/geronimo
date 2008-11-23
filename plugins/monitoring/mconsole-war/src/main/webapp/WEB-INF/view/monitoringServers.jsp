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
<%@ page import="java.util.List" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<portlet:defineObjects/>
<%
    String message = (String) request.getAttribute("message"); 
if (!message.equals(""))
{
%>
<div align="left" style="width: 650px">
<%=message %><br>
</div>
<%} %>
<table>
   <tr>
       <!-- Body -->
       <td width="100%" align="left" valign="top">
<table width="100%" style="border-style: solid;
border-width: 1px;">
 <thead align="center"><strong>Servers</strong></thead>
 <tr>
  <th class="DarkBackground" width="30%">Name</th>
  <th class="DarkBackground" width="10%">IP/Hostname</th>
  <th class="DarkBackground" width="15%">Status</th>
  <th class="DarkBackground" width="15%">Stat. Query</th>
  <th class="DarkBackground" width="30%" colspan="3">Actions</th>
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
  <td class="${backgroundClass}" width="15%" align="center" bgcolor="#cccccc">Online</td>
<%
} else if(node.isEnabled()){         // offline
%>
  <td class="${backgroundClass}" width="15%" align="center"><font color="red"><img border=0 src="/monitoring/images/help-b.png" alt="Offline">Offline</font></td>
<%
} else {         // Disabled
     %>
       <td class="${backgroundClass}" width="15%" align="center"><font color="red">Disabled</font></td>
     <%
     }
if (collecting == 0) {  // not collecting statistics
%>
    <td class="${backgroundClass}" width="15%" align="center"><font color="red">(stopped)</font></td>
<%
    if(node.isEnabled()) {   // enable the links
%>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="startThread" /><portlet:param name="server_id" value="<%=node.getName()%>" /><portlet:param name="snapshotDuration" value="<%=java.lang.Long.toString(snapshotDuration)%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query">Enable Query</a></td>
<%
    } else {        // do not provide links
%>
        <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query">Enable Query</td>
<%
    }
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</a></td>
<%
} 
else if (collecting == -1) {  // not collecting statistics
    %>
        <td class="${backgroundClass}" width="15%" align="center"><font color="red">Stopping...</font></td>
    <%
        if(node.isEnabled()) {   // enable the links
    %>
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query">Disable Query</td>
    <%
        } else {        // do not provide links
    %>
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query">Enable Query</td>
    <%
        }
    %>
      <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</a></td>
    <%
    }
else {            // collecting statistics
    if (node.isEnabled())
    {
%>
  <td class="${backgroundClass}" width="15%" align="center"><%=snapshotDuration/1000/60+" min. (running)"%></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="stopThread" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query">Disable Query</a></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</a></td>
<%
    }
    else
    {
        %>
        <td class="${backgroundClass}" width="15%" align="center">Stopped</td>
        <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query">Disable Query</td>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</a></td>
      <%        
    }
}
if(node.isEnabled()) {   // enabled server
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="disableServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png" alt="Disable">Disable</a></td>
<%
} else {        // disabled server
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="enableServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Enable">Enable</a></td>
<%
}
%>
 </tr>
 <%}
%>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Add Server">Add Server</a></div>
        </td>
     
         <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">

            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Navigation</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>">Home</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>">Views</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>">Servers</a></li>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>">Graphs</a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            
        </td>        
    </tr>
</table>
