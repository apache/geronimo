
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
<%@ page import="org.apache.geronimo.monitoring.console.data.Graph" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.View" %>
<portlet:defineObjects/>
<script type = "text/javascript">
<!--
function openNewWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}
//-->
</script>
<%
    String message = (String) request.getAttribute("message"); 
    if (message != null)
    {
%>
        <p><%= message %></p>
<%  } %>
<br>
<b>Views:</b>
<table width="100%" class="TableLine" summary="Monitoring - Views">
 <tr>
  <th scope="col" class="DarkBackground" width="30%">Name</th>
  <th scope="col" class="DarkBackground" width="10%">Elements</th>
  <th scope="col" class="DarkBackground" width="10%">Created</th>
  <th scope="col" class="DarkBackground" width="10%">Modified</th>
  <th scope="col" class="DarkBackground" width="30%" colspan="2">Actions</th>
 </tr>
 <%

     List<View> views = (List<View>) request.getAttribute("views");
 for (View view: views) {
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
  <td class="${backgroundClass}" width="30%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showView" /><portlet:param name="view_id" value='<%=view.getIdString()%>' /></portlet:actionURL>"><%=view.getName()%></a></td>
  <td class="${backgroundClass}" width="10%" align="center"><%="" + view.getGraphs().size()%></td>
  <%--<td class="${backgroundClass}" width="15%" align="center"><%=rs.getString("added").substring(0,16)%></td>--%>
  <%--<td class="${backgroundClass}" width="15%" align="center"><%=rs.getString("modified").substring(0,16)%></td>--%>
  <td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditView" /><portlet:param name="view_id" value='<%=view.getIdString()%>' /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</a></td>
 </tr>
 <%
 }
// rs.close();
 %>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Create View">Create View</a></div>
<b>Servers:</b>
<table width="100%" class="TableLine" summary="Monitoring - Servers">
 <tr>
  <th scope="col" class="DarkBackground" width="30%">Name</th>
  <th scope="col" class="DarkBackground" width="10%">IP/Hostname</th>
  <th scope="col" class="DarkBackground" width="15%">Status</th>
  <th scope="col" class="DarkBackground" width="15%">Stat. Query</th>
  <th scope="col" class="DarkBackground" width="30%" colspan="3">Actions</th>
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
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query">Disable Query</td>
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
<b>Graphs:</b>
<table width="100%" class="TableLine" summary="Monitoring - Graphs">
 <tr>
  <th scope="col" class="DarkBackground" width="30%">Name</th>
  <th scope="col" class="DarkBackground" width="20%">Server</th>
  <th scope="col" class="DarkBackground" width="15%">Timeframe</th>
  <th scope="col" class="DarkBackground" width="20%">Data Series</th>
  <th scope="col" class="DarkBackground" width="15%">Actions</th>
 </tr>
 <%
 
    List<Graph> graphs = (List<Graph>) request.getAttribute("graphs");
     for (Graph graph: graphs) {
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
     if (graph.getNode().isEnabled())
 {
 	%>
  	<td class="${backgroundClass}" width="30%" align="center">
  	    <a href="javascript: void(0)" onClick="openNewWindow('/monitoring/popUpGraph?graph_id=<%=graph.getId()%>','graph','width=800,height=300','title=<%=graph.getGraphName1() %>')"><%=graph.getGraphName1() %></a>
  	</td>
  	<td class="${backgroundClass}" width="20%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=graph.getNode().getName()%>" /></portlet:actionURL>"><%=graph.getNode().getName()%></a></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=graph.getTimeFrame()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getDataName1()%><%if (graph.getOperation() != null && !graph.getOperation().equals("null")){%><%=graph.getOperation()%><%}%><%if (graph.getDataName2() != null && !graph.getDataName2().equals("null")){%><%=graph.getDataName2()%><%}%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value="<%=graph.getIdString()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</a></td>
<%} 
else
{
	%>
	<td class="${backgroundClass}" width="30%" align="center"><%=graph.getGraphName1()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getNode().getName()%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=graph.getTimeFrame()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getDataName1()%><%if (graph.getOperation() != null && !graph.getOperation().equals("null")){%><%=graph.getOperation()%><%}%><%if (graph.getDataName2() != null && !graph.getDataName2().equals("null")){%><%=graph.getDataName2()%><%}%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit">Edit</td>
	<%
}%>
 </tr>
 <%}
%>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Add Graph">Add Graph</a></div>
