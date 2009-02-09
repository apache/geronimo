
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
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
<%@page import="org.apache.geronimo.monitoring.console.GraphsBuilder"%>
<%@page import="org.apache.geronimo.monitoring.console.StatsGraph"%>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>
<script language="JavaScript" type="text/javascript">
<!--
function openNewWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}
//-->
</script>
<%
    String message = (String) request.getAttribute("message"); 
    if (message != null) {
%>
        <p><%= message %></p>
<%  }  %>
<table>
   <tr>
       <!-- Body -->
       <td width="100%" align="left" valign="top">
<table width="100%" style="border-style: solid;
border-width: 1px;">
 <thead align="center"><strong><fmt:message key="monitor.common.graph"/></strong></thead>
 <tr>
  <th class="DarkBackground" width="30%"><fmt:message key="monitor.common.name"/></th>
  <th class="DarkBackground" width="20%"><fmt:message key="monitor.graph.server"/></th>
  <th class="DarkBackground" width="15%"><fmt:message key="monitor.graph.time"/></th>
  <th class="DarkBackground" width="20%"><fmt:message key="monitor.graph.data"/></th>
  <th class="DarkBackground" width="15%"><fmt:message key="monitor.common.action"/></th>
 </tr>
 <% 
 DBManager DBase = new DBManager();
 Connection con = DBase.getConnection();
 
 PreparedStatement pStmt = con.prepareStatement("SELECT * FROM graphs");
 ResultSet rs = pStmt.executeQuery();
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
    <a href="javascript: void(0)" onClick="openNewWindow('/monitoring/monitoringPopUpGraph.jsp?graph_id=<%=graph_id%>','graph','width=800,height=300','title=<%=name %>')"><%=name %></a>
    </td>
  	<td class="${backgroundClass}" width="20%" align="center"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><%=server_name%></a></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=timeframe%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=dataname1%><%if (operation != null && !operation.equals("null")){%><%=operation%><%}%><%if (dataname2 != null && !dataname2.equals("null")){%><%=dataname2%><%}%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value="<%=graph_id%>" /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png"><fmt:message key="monitor.common.edit"/></a></td>
<%} 
else
{
	%>
	<td class="${backgroundClass}" width="30%" align="center"><%=name%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=server_name%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><%=timeframe%></td>
  	<td class="${backgroundClass}" width="20%" align="center"><%=dataname1%><%if (operation != null && !operation.equals("null")){%><%=operation%><%}%><%if (dataname2 != null && !dataname2.equals("null")){%><%=dataname2%><%}%></td>
  	<td class="${backgroundClass}" width="15%" align="center"><img
					border=0 src="/monitoring/images/edit-b.png"><fmt:message key="monitor.common.edit"/></td>
	<%
}%>
 </tr>
 <%}
%>
</table>
<div align="right"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><img border=0 src="/monitoring/images/max-b.png"><fmt:message key="monitor.graph.addGraph"/></a></div>
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