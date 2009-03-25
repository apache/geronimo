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
<%@ page import="org.apache.geronimo.monitoring.console.StatsGraph" %>
<%@ page import="org.apache.geronimo.monitoring.console.GraphsBuilder" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.lang.String" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DatabaseMetaData" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<%

String view_id = (String) request.getAttribute("view_id"); 
String message = (String) request.getAttribute("message");


DBManager DBase = new DBManager();
Connection con = DBase.getConnection();

PreparedStatement pStmt = con.prepareStatement("SELECT * FROM views WHERE view_id="+view_id);
ResultSet rs = pStmt.executeQuery();

if (message == null)
    message = new String("");

if (rs.next())
{    
    String added = rs.getString("added").substring(0,16);
    String modified = rs.getString("modified").substring(0,16);
    Integer enabled = rs.getInt("enabled");
    String name = rs.getString("name");
    String description = rs.getString("description");
    Integer graph_count = rs.getInt("graph_count");
    rs.close();
%>
<!-- <head> -->

    <script type='text/javascript' src='/dojo/dojo.js'>
    </script>
        <script type = "text/javascript">
<!--
function hide(x) {
document.getElementById(x).style.display='none';
}
function show(x) {
document.getElementById(x).style.display='';
}
function validate() {
   if (! (document.editView.name.value 
      && document.editView.minxss_description.value  ))
   {
      alert("Name and Description are required fields");
      return false;
   }
   return;
}


function openNewWindow(theURL,winName,features) {
  window.open(theURL,winName,features);
}

//-->
</script>
<!-- </head> -->
        
            <%
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
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.common.editing"/>: <%=name%>
            </font>
            </p>         
            <p>
  <form onsubmit="return validate();" name="editView" method="POST" action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveEditView"/><portlet:param name="view_id" value="<%=view_id%>"/></portlet:actionURL>">
  <table cellpadding="1" cellspacing="1">
    <tr>
      <td><fmt:message key="monitor.common.added"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><%=added%></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.common.modified"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><%=modified%></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.common.name"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input size="50" type="text" name="name" value="<%=name%>"></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.common.desc"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><textarea rows="5" cols="50" name="minxss_description"><%=description%></textarea></td>
    </tr>
    <tr>
      <td valign="top"><fmt:message key="monitor.common.graph"/>:</td>
      <td>&nbsp;</td>
      <td align="right">
      <%
      DBase = new DBManager();
      con = DBase.getConnection();
      pStmt = con.prepareStatement("SELECT * FROM graphs");
      rs = pStmt.executeQuery();
      pStmt = con.prepareStatement("SELECT * FROM views_graphs WHERE view_id="+view_id);
      ResultSet rs2 = pStmt.executeQuery();
      ArrayList<Integer> graphsList = new ArrayList<Integer>();
      while (rs2.next())
      {
          graphsList.add(rs2.getInt("graph_id"));
      }
      %>
            <table cellpadding="1" cellspacing="1">
            <tr>
            <th width="5%"></th>
            <th><fmt:message key="monitor.common.name"/></th>
            <th><fmt:message key="monitor.graph.time"/></th>
            <th width="20%"><fmt:message key="monitor.graph.server"/></th>
            <th><fmt:message key="monitor.common.edit"/></th>
            </tr>
      <%
          while (rs.next())
          {
              pStmt = con.prepareStatement("SELECT name FROM servers WHERE server_id="+rs.getString("server_id"));
              rs2 = pStmt.executeQuery();
              if (rs2.next())
              {
      %>     
            <tr>
            <td align="left" width="5%"><input type="checkbox" name="graph_ids" value="<%=rs.getString("graph_id")%>" <%if (graphsList.contains(rs.getInt("graph_id"))){%> checked<%}%>></td>
            <td align="left"><a href="javascript: void(0)" onClick="openNewWindow('/monitoring/monitoringPopUpGraph.jsp?graph_id=<%=rs.getString("graph_id")%>','graph','width=800,height=300','title=<%=rs.getString("name") %>')"><%=rs.getString("name")%></a></td>
            <td align="left"><%=rs.getString("timeframe")%> <fmt:message key="monitor.common.min"/></td>
            <td align="left"><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showServer" /><portlet:param name="server_id" value='<%=rs.getString("server_id")%>' /></portlet:actionURL>"><%=rs2.getString("name")%></a></td>
            <td align="center"><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showEditGraph" /><portlet:param name="graph_id" value='<%=rs.getString("graph_id")%>' /></portlet:actionURL>"><img border=0 src="/monitoring/images/edit-b.png"></a></td>
            </tr>
      <%

              }
              rs2.close();
          }
      rs.close();
      %>
            </table>
</td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="1" align="left"><button type="button" value='<fmt:message key="monitor.common.cancel"/>' onclick="javascript:history.go(-1)"><fmt:message key="monitor.common.cancel"/></button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="submit" value='<fmt:message key="monitor.common.save"/>' /></td>
    </tr>
  </table>
  </form>

            </p>

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
            <br>
            <br>
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.action"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showView" /><portlet:param name="view_id" value="<%=view_id%>" /></portlet:actionURL>"><fmt:message key="monitor.view.show"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>"><fmt:message key="monitor.graph.createGraph"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="deleteView" /><portlet:param name="view_id" value="<%=view_id%>" /></portlet:actionURL>"><fmt:message key="monitor.view.delete"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><fmt:message key="monitor.view.add"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            
        </td>        
    </tr>
</table>
<%
con.close();
}
    else
    {%>
<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <a HREF="javascript:history.go(-1)"><< <fmt:message key="monitor.common.back"/></a>
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.view.notExist"/>
            </font>
            </p>         

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
            <br>
            <br>
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="monitor.common.action"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddView" /></portlet:actionURL>"><fmt:message key="monitor.view.add"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>

        </td>  
    </tr>
</table>
    <%
    }%>




