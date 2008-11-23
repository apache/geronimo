

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
<%@ page import="org.apache.geronimo.monitoring.console.Constants" %>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<%@ page import="org.apache.geronimo.monitoring.console.data.Node" %>

<portlet:defineObjects/>

<%

    Node node = (Node) request.getAttribute("node");

String message = (String) request.getAttribute("message");
String snapshot = (String) request.getAttribute("snapshot");
String retention = (String) request.getAttribute("retention");
if(message == null)     message = "";
if(snapshot == null)    snapshot = "";
if(retention == null)   retention = "";
if(node.getProtocol() == null || node.getProtocol().equals("EJB"))
{
    node.setProtocol("EJB");
    if(node.getPort() == 0)        node.setPort(4201);
}
else
{
    if(node.getPort() == 0)        node.setPort(1099);
}
MRCConnector mrc = null;
boolean isOnline = true;

if (node != null) {
    try {
        mrc = new MRCConnector(node);
    } catch (Exception e) {
        // the password supplied by the user doesn't work
        try {
            if(retention.equals("") || snapshot.equals("")) {
                mrc = new MRCConnector(node);
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
    <script type='text/javascript' src='<%=Constants.DOJO_JS%>'>
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
        document.editServer.port.value = "4201";
    else
        document.editServer.port.value = "1099";
}
//-->
</script>
<!-- </head> -->
        
            <%
 if (!message.equals(""))
 {
 %>
<div align="left" style="width: 500px">
<%=message %><br>
</div>
<%} %>
<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p>
            <font face="Verdana" size="+1">
            Editing: <%=node.getName()%> (<%=node.getHost()%>)
            </font>
            </p>         
            <p>
  <form name="editServer" method="POST" action="<portlet:actionURL/>">
  <table cellpadding="1" cellspacing="1">
    <%--<tr>--%>
      <%--<td>Added:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=added.substring(0,16)%></td>--%>
      <%--<td></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
      <%--<td>Last Modified:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=modified.substring(0,16)%></td>--%>
      <%--<td></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
      <%--<td>Last Seen:</td>--%>
      <%--<td>&nbsp;</td>--%>
      <%--<td align="right"><%=last_seen.substring(0,16)%></td>--%>
      <%--<td></td>--%>
    <%--</tr>--%>
    <tr>
      <td><label for="<portlet:namespace/>name">Name</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="name" id="<portlet:namespace/>name" value=<%= "\"" + node.getName() + "\"" %>></td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>ip">IP/Hostname</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="ip" id="<portlet:namespace/>ip" value=<%= "\"" + node.getHost() + "\"" %>/></td>
      <td></td>
    </tr>
    <tr>
      <td>Protocol</td>
      <td>&nbsp;</td>
      <td align="right">
      		<input type="radio" name="protocol" id="<portlet:namespace/>protocol1" onchange='setPort()' value="EJB" <%if (node.getProtocol().equals("EJB")){ %>checked="checked"<%} %>><label for="<portlet:namespace/>protocol1">EJB</label>
      		<input type="radio" name="protocol" id="<portlet:namespace/>protocol2" onchange='setPort()' value="JMX" <%if (node.getProtocol().equals("JMX")){ %>checked="checked"<%} %>><label for="<portlet:namespace/>protocol2">JMX</label>
      </td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>port">Port</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="port" id="<portlet:namespace/>port" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value=<%= "\"" + node.getPort() + "\"" %>/></td>
      <td></td>
    </tr>
    <%
    if (isOnline)
    {
    %>
    <tr>
      <td><label for="<portlet:namespace/>snapshot">Snapshot Duration</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" width="5" size="4" name="snapshot" id="<portlet:namespace/>snapshot" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value="<%=snapshot%>"/></td>
      <td> minutes</td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>retention">Snapshot Retention</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" width="5" size="4" name="retention" id="<portlet:namespace/>retention" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value="<%=retention%>"/></td>
      <td> days</td>
    </tr>

    <%
    }
    else
    {
    %>
        <tr>
            <td><label for="<portlet:namespace/>snapshot">Snapshot Duration</label>:</td>
            <td>&nbsp;</td>
            <td align="right"><input type="text" width="5" size="4" name="snapshot" id="<portlet:namespace/>snapshot" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' disabled="disabled" value="unknown"/></td>
        <td> minutes</td>
      </tr>
      <tr>
          <td><label for="<portlet:namespace/>retention">Snapshot Retention</label>:</td>
          <td>&nbsp;</td>
          <td align="right"><input type="text" width="5" size="4" name="retention" id="<portlet:namespace/>retention" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' disabled="disabled" value="unknown"/></td>
      <td> days</td>
    </tr>

    <%
    }
    %>
    <tr>
      <td><label for="<portlet:namespace/>username">Username</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="username" id="<portlet:namespace/>username" value=<%= "\"" + node.getUserName() + "\"" %>/></td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>password">Password</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="password" name="password" id="<portlet:namespace/>password" value=<%= "\"" + node.getPassword() + "\"" %>/></td>
      <td></td>
    </tr>
    <tr>
      <td><label for="<portlet:namespace/>password2">Password (verify)</label>:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="password" name="password2" id="<portlet:namespace/>password2" value=<%= "\"" + node.getPassword() + "\"" %>/></td>
      <td></td>
    </tr>
    <tr><td colspan="2"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <input type="hidden" name="mode" value="" />
      <input type="hidden" name="action" value="" />
      <input type="hidden" name="server_id" value=<%= "\"" + node.getName() + "\"" %> />
      <td colspan="1" align="left"><button type="button" value="Cancel" onclick="javascript:history.go(-1)">Cancel</button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="button" value="Save" onclick="document.editServer.action.value='saveEditServer'; document.editServer.mode.value='edit'; if(validate()) document.editServer.submit();" /></td>
      <td></td>
    </tr>
  </table>
  </form>
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
            <br>
            <br>
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Actions</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a onclick="document.editServer.action.value='testEditServerConnection'; document.editServer.mode.value='edit'; if(validateTest()) document.editServer.submit();" href="#">Test these settings</a></li>
                        <%
                        if(node.isEnabled()) {
                        %>
                            <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="disableEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>">Disable this server</a></li>
                        <%
                        } else {
                        %>
                            <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="enableEditServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>">Enable this server</a></li>
                        <%
                        }
                        %>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="deleteServer" /><portlet:param name="server_id" value="<%=node.getName()%>" /></portlet:actionURL>">Delete this server</a></li>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>">Add a new server</a></li>
                        </ul>
                        &nbsp;<br />
                    </td>   
                </tr>
            </table>
            
        </td>
    </tr>
</table>
<%
} else {
%>
<table>
    <tr>
        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <a HREF="javascript:history.go(-1)"><< Back</a>
            <p>
            <font face="Verdana" size="+1">
            Server does not exist
            </font>
            </p>         

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
            <br>
            <br>
            <table width="100%" style="border-bottom: 1px solid #2581c7;" cellspacing="1" cellpadding="1">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1">Actions</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <ul>
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddServer" /></portlet:actionURL>">Add a new server</a></li>
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
