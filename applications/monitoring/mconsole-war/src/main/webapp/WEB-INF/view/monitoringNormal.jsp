
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
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DatabaseMetaData" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
<portlet:defineObjects/>
<%
    String message = (String) request.getAttribute("message"); 
    if (message != null)
    {
%>
        <p><%= message %></p>
<%  } %>
<br>
<table width="100%" style="border-style: solid;
border-width: 1px;">
 <thead align="center"><strong>Views</strong></thead>
 <tr>
  <th class="DarkBackground" width="30%">Name</th>
  <th class="DarkBackground" width="10%">Elements</th>
  <th class="DarkBackground" width="10%">Created</th>
  <th class="DarkBackground" width="10%">Modified</th>
  <th class="DarkBackground" width="30%" colspan="2">Actions</th>
 </tr>
 <%
 DBManager DBase = new DBManager();
 Connection con = DBase.getConnection();
 
 PreparedStatement pStmt = con.prepareStatement("SELECT view_id, name, description, graph_count, added, modified FROM views");
 ResultSet rs = pStmt.executeQuery();
 while (rs.next())
 {
     String view_id = rs.getString("view_id");
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
  <td class="${backgroundClass}" width="30%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showView" /><portlet:param name="view_id" value="<%=rs.getString("view_id")%>" /></portlet:actionURL>"><%=rs.getString("name")%></a></td>
  <td class="${backgroundClass}" width="10%" align="center"><%=rs.getString("graph_count")%></td>
  <td class="${backgroundClass}" width="15%" align="center"><%=rs.getString("added").substring(0,16)%></td>
  <td class="${backgroundClass}" width="15%" align="center"><%=rs.getString("modified").substring(0,16)%></td>
  <td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditView" /><portlet:param name="view_id" value="<%=rs.getString("view_id")%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png">Edit</a></td>
 </tr>
 <%
 }
 rs.close();
 %>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png">Create View</a></div>
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
 
 pStmt = con.prepareStatement("SELECT * FROM servers");
 rs = pStmt.executeQuery();
 
 // data structure to store the server's info
 class ServerInfo {
     public String ip;
     public int port;
     public String username;
     public String password;
     public String server_id;
     public boolean enabled;
     public String name;
 }
 
 ArrayList<ServerInfo> serverInfos = new ArrayList<ServerInfo>();
 // for each server, save the information locally
 while(rs.next()) {
     ServerInfo s = new ServerInfo();
     s.ip = rs.getString("ip");
     s.username = rs.getString("username");
     s.password = rs.getString("password");
     s.server_id = rs.getString("server_id");
     s.name = rs.getString("name");
     s.enabled = rs.getInt("enabled") == 1 ? true : false;
     s.port = rs.getInt("port");
     serverInfos.add( s );
 }
 // for each server, draw it
 for(int i = 0 ; i < serverInfos.size(); i++) {
     String ip = serverInfos.get(i).ip;
     int port = serverInfos.get(i).port;
     String username = serverInfos.get(i).username;
     String password = serverInfos.get(i).password;
     String server_id = serverInfos.get(i).server_id;
     boolean enabled = serverInfos.get(i).enabled;
     String name = serverInfos.get(i).name;
     boolean online = false;
     boolean collecting = false;
     MRCConnector mrc = null;
     Long snapshotDuration = new Long(0);
     if (enabled) {
	     try {
	         mrc = new MRCConnector(ip, username, password, port);
	         online = true;
	     } catch (Exception e) {
	         online = false;
	     }
	     try {
	         snapshotDuration = mrc.getSnapshotDuration();
	         if (mrc.isSnapshotRunning())
	             collecting = true;
	         else
	             collecting = false;
	     } catch (Exception e) {
	         collecting = false;
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
if(enabled){
 %>
  <td class="${backgroundClass}" width="30%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><%=name%></a></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><%=ip%></a></td>
<%
}
else{
    %>
    <td class="${backgroundClass}" width="30%" align="center"><%=name%></td>
    <td class="${backgroundClass}" width="10%" align="center"><%=ip%></td>
  <% 
}
if (online) {     // online
%>
  <td class="${backgroundClass}" width="15%" align="center" bgcolor="#cccccc">Online</td>
<%
} else if(enabled){         // offline
%>
  <td class="${backgroundClass}" width="15%" align="center"><font color="red"><img border=0 src="/monitoring/images/help-b.png">Offline</font></td>
<%
} else {         // Disabled
     %>
       <td class="${backgroundClass}" width="15%" align="center"><font color="red">Disabled</font></td>
     <%
     }
if (!collecting) {  // not collecting statistics
%>
    <td class="${backgroundClass}" width="15%" align="center"><font color="red">(stopped)</font></td>
<%
    if(enabled) {   // enable the links
%>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="startThread" /><portlet:param name="server_id" value="<%=server_id%>" /><portlet:param name="snapshotDuration" value="<%=java.lang.Long.toString(snapshotDuration)%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png">Enable Query</a></td>
<%
    } else {        // do not provide links
%>
        <td class="${backgroundClass}" width="10%" align="center">Enable Query</td>
<%
    }
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png">Edit</a></td>
<%
} else {            // collecting statistics
    if (enabled)
    {
%>
  <td class="${backgroundClass}" width="15%" align="center"><%=snapshotDuration/1000/60+" min. (running)"%></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="stopThread" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png">Disable Query</a></td>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png">Edit</a></td>
<%
    }
    else
    {
        %>
        <td class="${backgroundClass}" width="15%" align="center">Stopped</td>
        <td class="${backgroundClass}" width="10%" align="center">Disable Query</td>
        <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png">Edit</a></td>
      <%        
    }
}
if(enabled) {   // enabled server
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="disableServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/close-b.png">Disable</a></td>
<%
} else {        // disabled server
%>
  <td class="${backgroundClass}" width="10%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="enableServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png">Enable</a></td>
<%
}
%>
 </tr>
 <%}
%>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png">Add Server</a></div>
<table width="100%" style="border-style: solid;
border-width: 1px;">
 <thead align="center"><strong>Graphs</strong></thead>
 <tr>
  <th class="DarkBackground" width="30%">Name</th>
  <th class="DarkBackground" width="20%">Server</th>
  <th class="DarkBackground" width="15%">Timeframe</th>
  <th class="DarkBackground" width="20%">Data Series</th>
  <th class="DarkBackground" width="15%">Actions</th>
 </tr>
 <%
 
 pStmt = con.prepareStatement("SELECT * FROM graphs");
 rs = pStmt.executeQuery();
 
 // data structure to store the graph's info
 class GraphInfo {
     public String name;
     public String server_name;
     public String server_id;
     public String graph_id;
     public String timeframe;
     public String dataname1;
     public String operation;
     public String dataname2;
     public boolean enabled;
 }
 
 ArrayList<GraphInfo> graphInfo = new ArrayList<GraphInfo>();
 // for each graph, save the information locally
 while(rs.next()) {
	 pStmt = con.prepareStatement("SELECT name FROM servers WHERE server_id="+rs.getInt("server_id"));
     ResultSet rs2 = pStmt.executeQuery();
     if (rs2.next())
     {
    	 GraphInfo s = new GraphInfo();
    	 s.server_name = rs2.getString("name");
         s.name = rs.getString("name");
         s.timeframe = rs.getString("timeframe");
         s.dataname1 = rs.getString("dataname1");
         s.operation = rs.getString("operation");
         s.dataname2 = rs.getString("dataname2");
         s.server_id = rs.getString("server_id");
         s.graph_id = rs.getString("graph_id");
         s.enabled = rs.getInt("enabled") == 1 ? true : false;
         graphInfo.add( s );
     }
     rs2.close();
 }
 // close connection
 con.close();
 // for each graph, draw it
 for(int i = 0 ; i < graphInfo.size(); i++) {
     String name = graphInfo.get(i).name;
     String server_name = graphInfo.get(i).server_name;
     String timeframe = graphInfo.get(i).timeframe;
     String dataname1 = graphInfo.get(i).dataname1;
     String operation = graphInfo.get(i).operation;
     String dataname2 = graphInfo.get(i).dataname2;
     String server_id = graphInfo.get(i).server_id;
     String graph_id = graphInfo.get(i).graph_id;
     boolean enabled = graphInfo.get(i).enabled;
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
 if (enabled)
 {
 	%>
  	<td class="${backgroundClass}" width="30%" align="center">
  	    <a href="/monitoring/monitoringPopUpGraph.jsp?graph_id=<%=graph_id%>" target="window.open('','','width=670,height=220,resizable=1')"><%=name %></a>
  	</td>
  	<td class="${backgroundClass}" width="20%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><%=server_name%></a></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=timeframe%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=dataname1%><%if (operation != null && !operation.equals("null")){%><%=operation%><%}%><%if (dataname2 != null && !dataname2.equals("null")){%><%=dataname2%><%}%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value="<%=graph_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png">Edit</a></td>
<%} 
else
{
	%>
	<td class="${backgroundClass}" width="30%" align="center"><%=name%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=server_name%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=timeframe%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=dataname1%><%if (operation != null && !operation.equals("null")){%><%=operation%><%}%><%if (dataname2 != null && !dataname2.equals("null")){%><%=dataname2%><%}%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><img border=0 src="/monitoring/images/edit-b.png">Edit</td>
	<%
}%>
 </tr>
 <%}
%>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png">Add Graph</a></div>
