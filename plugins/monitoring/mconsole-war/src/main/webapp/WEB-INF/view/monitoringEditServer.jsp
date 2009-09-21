

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
<%@ page import="org.apache.geronimo.monitoring.console.StatsGraph" %>
<%@ page import="org.apache.geronimo.monitoring.console.GraphsBuilder" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.TreeMap" %>
<%@ page import="java.lang.String" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.DatabaseMetaData" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%@ page import="java.sql.SQLException" %>
<%@ page import="org.apache.geronimo.monitoring.console.util.*" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.crypto.EncryptionManager" %>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects/>

<%

String server_id = (String) request.getAttribute("server_id");

String name = (String) request.getAttribute("name");
String ip = (String) request.getAttribute("ip");
String username = (String) request.getAttribute("username");
String password = (String) request.getAttribute("password");
String password2 = (String) request.getAttribute("password2");
String snapshot = (String) request.getAttribute("snapshot");
String retention = (String) request.getAttribute("retention");
String port = (String)request.getAttribute("port");
String protocol = (String) request.getAttribute("protocol");
if(name == null)        name = "";
if(ip == null)          ip = "";
if(username == null)    username = "";
if(password == null)    password = "";
if(password2 == null)   password2 = "";
if(snapshot == null)    snapshot = "";
if(retention == null)   retention = "";
if(protocol == null)    protocol = "";
if(protocol.equals("1"))    
{
    if(port == null)        port = "1099";
}
else if(protocol.equals("2"))
{
    if(port == null)        port = "4201";
}
else
{
    protocol = "1";
    if(port == null)        port = "1099";
}

DBManager DBase = new DBManager();
Connection con = DBase.getConnection();

PreparedStatement pStmt = con.prepareStatement("SELECT * FROM servers WHERE server_id="+server_id);
ResultSet rs = pStmt.executeQuery();
MRCConnector mrc = null;
boolean isOnline = true;
String added = "";
String modified = "";
String last_seen = "";
boolean enabled = true;
String dbPassword = "";

if (rs.next()) {
    // name == "" when user has not submitted anything
    if(name.equals("")) {
        // store the information from db into the variables
        name = rs.getString("name");
        username = rs.getString("username");
        ip = rs.getString("ip");
        port = rs.getString("port");
        protocol = rs.getString("protocol");
        dbPassword = (String)EncryptionManager.decrypt(rs.getString("password"));
    }
    added = rs.getString("added");
    modified = rs.getString("modified");
    last_seen = rs.getString("last_seen");
    enabled = rs.getInt("enabled") == 1 ? true : false;
    try {
        // close connection before using the MRCConnector
        con.close();
        mrc = new MRCConnector(ip, username, password, Integer.parseInt(port), Integer.parseInt(protocol));
    } catch (Exception e) {
        // the password supplied by the user doesn't work
        try {
            if(retention.equals("") || snapshot.equals("")) {
                mrc = new MRCConnector(ip, username, dbPassword, Integer.parseInt(port), Integer.parseInt(protocol));
		        // get the snapshot on the first call or any subsequent valid connections
		        snapshot = snapshot == "" ?  "" + mrc.getSnapshotDuration() / 1000 / 60 : snapshot;
		        // get the retention on the first call or any subsequent valid connection
		        retention = retention == "" ? "" + mrc.getSnapshotRetention() : retention;
            }
        } catch(Exception ee) {
            // the password in the db does not work
            isOnline = false;
        }
    }
%>
<!-- <head> -->

    <style type='text/css'>
    </style>
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
   if (! (document.editServer.name.value 
      && document.editServer.ip.value 
      && document.editServer.username.value
      && document.editServer.snapshot.value 
      && document.editServer.port.value ))
   {
      alert("Name, Address, Protocol, Port, Username, and Snapshot Duration are all required fields.");
      return false;
   }
   if (document.editServer.password.value != document.editServer.password2.value)
   {
      alert("Passwords do not match");
      return false;
   }
   return true;
}

function validateTest() {
   if (! (document.editServer.name.value 
      && document.editServer.ip.value 
      && document.editServer.username.value
      && document.editServer.snapshot.value
      && document.editServer.password.value
      && document.editServer.port.value ))
   {
      alert("Name, Address, Protocol, Port, Username, and Snapshot Duration are all required fields.");
      return false;
   }
   if (document.editServer.password.value != document.editServer.password2.value)
   {
      alert("Passwords do not match");
      return false;
   }
   return true;
}

function noAlpha(obj){
    reg = /[^0-9]/g;
    obj.value =  obj.value.replace(reg,"");
 }
function setPort() {
    if (document.editServer.protocol[0].checked)
        document.editServer.port.value = "1099";
    else
        document.editServer.port.value = "4201";
}
//-->
</script>
<!-- </head> -->
        
<CommonMsg:commonMsg/><br>

<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            <fmt:message key="monitor.common.edit"/>: <%=name%> (<%=ip%>)
            </font>
            </p>         
            <p>
  <form name="editServer" method="POST" action="<portlet:actionURL/>">
  <table cellpadding="1" cellspacing="1">
    <tr>
      <td><fmt:message key="monitor.common.added"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><%=added.substring(0,16)%></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.common.modified"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><%=modified.substring(0,16)%></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.common.seen"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><%=last_seen.substring(0,16)%></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.common.name"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="name" value=<%= "\"" + name + "\"" %>></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.server.ip"/>/<fmt:message key="monitor.server.hostname"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="ip" value=<%= "\"" + ip + "\"" %>/></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.server.protocol"/></td>
      <td>&nbsp;</td>
      <td align="right"><input type="radio" name="protocol" onchange='setPort()' value="1" <%if (protocol.equals("1")){ %>checked="checked"<%} %>>JMX <input type="radio" name="protocol" onchange='setPort()' value="2" <%if (protocol.equals("2")){ %>checked="checked"<%} %>>EJB</td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.server.port"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="port" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value=<%= "\"" + port + "\"" %>/></td>
      <td></td>
    </tr>
    <%
    if (isOnline)
    {
    %>
    <tr>
      <td><fmt:message key="monitor.server.snapshotDuration"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" width="5" size="4" name="snapshot" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value="<%=snapshot%>"/></td>
      <td> <fmt:message key="monitor.common.minute"/></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.server.snapshotRetention"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" width="5" size="4" name="retention" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value="<%=retention%>"/></td>
      <td> <fmt:message key="monitor.common.day"/></td>
    </tr>

    <%
    }
    else
    {
    %>
        <tr>
            <td><fmt:message key="monitor.server.snapshotDuration"/>:</td>
            <td>&nbsp;</td>
            <td align="right"><input type="text" width="5" size="4" name="snapshot" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' disabled="disabled" value="unknown"/></td>
        <td> <fmt:message key="monitor.common.minute"/></td>
      </tr>
      <tr>
          <td><fmt:message key="monitor.server.snapshotRetention"/>:</td>
          <td>&nbsp;</td>
          <td align="right"><input type="text" width="5" size="4" name="retention" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' disabled="disabled" value="unknown"/></td>
      <td> <fmt:message key="monitor.common.day"/></td>
    </tr>

    <%
    }
    %>
    <tr>
      <td><fmt:message key="monitor.server.username"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="username" value=<%= "\"" + username + "\"" %>/></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.server.pwd"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="password" name="password" value=<%= "\"" + password + "\"" %>/></td>
      <td></td>
    </tr>
    <tr>
      <td><fmt:message key="monitor.server.pwd2"/>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="password" name="password2" value=<%= "\"" + password2 + "\"" %>/></td>
      <td></td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <input type="hidden" name="mode" value="" />
      <input type="hidden" name="action" value="" />
      <input type="hidden" name="server_id" value=<%= "\"" + server_id + "\"" %> />
      <td colspan="1" align="left"><button type="button" value='<fmt:message key="monitor.common.cancel"/>' onclick="javascript:history.go(-1)"><fmt:message key="monitor.common.cancel"/></button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="button" value='<fmt:message key="monitor.common.save"/>' onclick="document.editServer.action.value='saveEditServer'; document.editServer.mode.value='edit'; if(validate()) document.editServer.submit();" /></td>
      <td></td>
    </tr>
  </table>

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
                        <li><a onclick="document.editServer.action.value='testEditServerConnection'; document.editServer.mode.value='edit'; if(validateTest()) document.editServer.submit();" href="#"><fmt:message key="monitor.server.testSetting"/></a></li>
                        <% 
                        if(enabled) {
                        %>
                            <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="disableEditServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><fmt:message key="monitor.server.disableServer"/></a></li>
                        <%
                        } else {
                        %>
                            <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="enableEditServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><fmt:message key="monitor.server.enableServer"/></a></li>
                        <%
                        }
                        %>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="deleteServer" /><portlet:param name="server_id" value="<%=server_id%>" /></portlet:actionURL>"><fmt:message key="monitor.server.deleteServer"/></a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><fmt:message key="monitor.server.addServer"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            
        </td>
        </form>   
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
            <fmt:message key="monitor.server.notExist"/>
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
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>"><fmt:message key="monitor.server.addServer"/></a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>

        </td>  
    </tr>
</table>
<%
}
%>
