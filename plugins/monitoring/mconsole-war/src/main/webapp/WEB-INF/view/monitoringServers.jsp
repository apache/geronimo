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
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnectorEJB" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
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
<%
 DBManager DBase = new DBManager();
 Connection con = DBase.getConnection();
 
 PreparedStatement pStmt = con.prepareStatement("SELECT * FROM servers");
 ResultSet rs = pStmt.executeQuery();
%>
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
     String username = serverInfos.get(i).username;
     String password = serverInfos.get(i).password;
     String server_id = serverInfos.get(i).server_id;
     int port = serverInfos.get(i).port;
     boolean enabled = serverInfos.get(i).enabled;
     String name = serverInfos.get(i).name;
     boolean online = false;
     boolean collecting = false;
     MRCConnectorEJB mrc = null;
     Long snapshotDuration = new Long(0);
     if (enabled) {
	     try {
	         mrc = new MRCConnectorEJB(ip, username, password, port);
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
<%
 // close connection
 con.close();
%>
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