
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
<%@ page import="org.apache.geronimo.monitoring.console.data.Graph" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.View" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>
<script type = "text/javascript">
<!--
function openNewWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}
//-->
</script>
<script type = "text/javascript">
<!--
function confirm_del() {
    var msg = "All the changes you made to default servers/views/graphs will be lost.\nAre you sure you want to proceed?";
    return confirm(msg);
}
//-->
</script>
<CommonMsg:commonMsg/>
<%if (request.isUserInRole("admin")){ %>
<div align="left">
<a><strong>Note:&nbsp</strong>To reset default servers/views/graphs, please click this link:</a>&nbsp&nbsp
<a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="restoreData" /></portlet:actionURL>" onclick="return confirm_del()"><fmt:message key="monitor.common.restore"/></a></div>
<%} %>
<br>
<b><fmt:message key="monitor.common.view"/>:</b>
<table width="100%" class="TableLine" summary="Monitoring - Views">
 <tr>
  <th scope="col" class="DarkBackground" width="30%"><fmt:message key="monitor.common.name"/></th>
  <th scope="col" class="DarkBackground" width="30%"><fmt:message key="monitor.view.element"/></th>
  <%--<th scope="col" class="DarkBackground" width="10%"><fmt:message key="monitor.view.created"/></th>--%>
  <%--<th scope="col" class="DarkBackground" width="10%"><fmt:message key="monitor.view.modified"/></th>--%>
  <th scope="col" class="DarkBackground" width="40%"><fmt:message key="monitor.common.action"/></th>
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
  <td class="${backgroundClass}" width="30%" align="center"><%="" + view.getGraphs().size()%></td>
  <%--<td class="${backgroundClass}" width="15%" align="center"><%=rs.getString("added").substring(0,16)%></td>--%>
  <%--<td class="${backgroundClass}" width="15%" align="center"><%=rs.getString("modified").substring(0,16)%></td>--%>
 <%if(request.isUserInRole("admin")){ %>
  <td class="${backgroundClass}" width="40%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditView" /><portlet:param name="view_id" value='<%=view.getIdString()%>' /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
  <%} %>
 </tr>
 <%
 }
// rs.close();
 %>
</table>
<% if (request.isUserInRole("admin")) {%>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Create View"><fmt:message key="monitor.view.create"/></a></div>
<% }%>
<b><fmt:message key="monitor.common.server"/>:</b>
<table width="100%" class="TableLine" summary="Monitoring - Servers">
 <tr>
  <th scope="col" class="DarkBackground" width="30%"><fmt:message key="monitor.common.name"/></th>
  <th scope="col" class="DarkBackground" width="10%"><fmt:message key="monitor.server.ip"/>/<fmt:message key="monitor.server.hostname"/></th>
  <th scope="col" class="DarkBackground" width="15%"><fmt:message key="monitor.server.status"/></th>
  <th scope="col" class="DarkBackground" width="15%"><fmt:message key="monitor.server.statQuery"/></th>
  <th scope="col" class="DarkBackground" width="30%" colspan="3"><fmt:message key="monitor.common.action"/></th>
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
<%if (request.isUserInRole("admin")) {
    if(node.isEnabled()) {   // enable the links
%>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="startThread" /><portlet:param name="server_id" value="<%=node.getName()%>" /><portlet:param name="snapshotDuration" value="<%=java.lang.Long.toString(snapshotDuration)%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query"><fmt:message key="monitor.server.enableQuery"/></a></td>
<%     
    } else {        // do not provide links
%>
        <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/max-b.png" alt="Enable Query"><fmt:message key="monitor.server.enableQuery"/></td>
<%     
    }
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
<% } //end admin 

}
else if (collecting == -1) {  // not collecting statistics
    %>
        <td class="${backgroundClass}" width="15%" align="center"><font color="red"><fmt:message key="monitor.server.stopping"/></font></td>
<script type = "text/javascript">
<!--
    WaitForMonitorStop(1000);
    refreshForMonitorStatus();
    
    function refreshForMonitorStatus() {
       if (window.ActiveXObject) {
            window.navigate(location);
       }
       else {
            window.location.reload();
       }    
    }
    
    function WaitForMonitorStop(millis) {
       var startTime = new Date();
       var curTime = null;
       do { curTime = new Date(); }
       while(curTime-startTime < millis);
    }
//-->
</script>	
    <%if (request.isUserInRole("admin")) {   
        if(node.isEnabled()) {   // enable the links
        
    %>
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.disableQuery"/></td>
    <%   
        } else {        // do not provide links
    %>
            <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.disableQuery"/></td>
    <%  
        }
    %>
      <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
    <%
    }//end admin
    }
else {            // collecting statistics
    if (node.isEnabled())
    {
%>
  <td class="${backgroundClass}" width="15%" align="center"><%=snapshotDuration/1000/60+" min. "%>(<fmt:message key="monitor.server.run"/>)</td>
  <% if (request.isUserInRole("admin")) {%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="stopThread" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.disableQuery"/></a></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
<%   }//end admin
    }
    else
    {
        %>
        <td class="${backgroundClass}" width="15%" align="center"><fmt:message key="monitor.server.stopped"/></td>
        <% if (request.isUserInRole("admin")) {%>
        <td class="${backgroundClass}" width="10%" align="center"><img border=0 src="/monitoring/images/close-b.png" alt="Disable Query"><fmt:message key="monitor.server.disableQuery"/></td>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
      <% }       
    }
}
if (request.isUserInRole("admin")) {
if(node.isEnabled()) {   // enabled server

%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="disableServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png" alt="Disable"><fmt:message key="monitor.server.disable"/></a></td>
<%
} else {        // disabled server
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="enableServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Enable"><fmt:message key="monitor.server.enable"/></a></td>
<%
}
}//end admin
%>
 </tr>
 <%}
%>
</table>
<%if (request.isUserInRole("admin")) {%>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Add Server"><fmt:message key="monitor.server.addServer"/></a></div>
<% } %>
<b><fmt:message key="monitor.common.graph"/>:</b>
<table width="100%" class="TableLine" summary="Monitoring - Graphs">
 <tr>
  <th scope="col" class="DarkBackground" width="30%"><fmt:message key="monitor.common.name"/></th>
  <th scope="col" class="DarkBackground" width="20%"><fmt:message key="monitor.graph.server"/></th>
  <th scope="col" class="DarkBackground" width="15%"><fmt:message key="monitor.graph.time"/></th>
  <th scope="col" class="DarkBackground" width="20%"><fmt:message key="monitor.graph.data"/></th>
  <th scope="col" class="DarkBackground" width="15%"><fmt:message key="monitor.common.action"/></th>
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
      <%if(request.isUserInRole("admin")){ %>
  	<td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value="<%=graph.getIdString()%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></a></td>
<%} }
else
{
	%>
	<td class="${backgroundClass}" width="30%" align="center"><%=graph.getGraphName1()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getNode().getName()%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=graph.getTimeFrame()%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=graph.getDataName1()%><%if (graph.getOperation() != null && !graph.getOperation().equals("null")){%><%=graph.getOperation()%><%}%><%if (graph.getDataName2() != null && !graph.getDataName2().equals("null")){%><%=graph.getDataName2()%><%}%></td>
      <%if(request.isUserInRole("admin")){ %>
  	<td class="${backgroundClass}" width="15%" align="center"><img border=0 src="/monitoring/images/edit-b.png" alt="Edit"><fmt:message key="monitor.common.edit"/></td>
    <% } %><%
}%>
 </tr>
 <%}
%>
</table>
<% if (request.isUserInRole("admin")) {%>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png" alt="Add Graph"><fmt:message key="monitor.graph.addGraph"/></a></div>
<%}%>
