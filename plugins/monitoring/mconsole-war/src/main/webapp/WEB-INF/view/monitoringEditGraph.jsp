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
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector" %>
<portlet:defineObjects/>

<%

String graph_id = (String) request.getAttribute("graph_id");
String message = (String) request.getAttribute("message");

DBManager DBase = new DBManager();
Connection con = DBase.getConnection();


if (message == null)
    message = new String("");


PreparedStatement pStmt = con.prepareStatement("SELECT * FROM graphs WHERE graph_id="+graph_id);
ResultSet rs = pStmt.executeQuery();

if (rs.next())
{    
    String server_id = rs.getString("server_id");
    String name = rs.getString("name");
    Integer enabled = rs.getInt("enabled");
    String description = rs.getString("description");
    Integer timeframe = rs.getInt("timeframe");
    String mbean = rs.getString("mbean");
    String data1operation = rs.getString("data1operation");
    String dataname1 = rs.getString("dataname1");
    String operation = rs.getString("operation");
    String data2operation = rs.getString("data2operation");
    String dataname2 = rs.getString("dataname2");
    String xlabel = rs.getString("xlabel");
    String ylabel = rs.getString("ylabel");
    String warninglevel1 = rs.getString("warninglevel1");
    String warninglevel2 = rs.getString("warninglevel2");
    String color = rs.getString("color");
    String added = rs.getString("added").substring(0,16);
    String modified = rs.getString("modified").substring(0,16);
    String last_seen = rs.getString("last_seen").substring(0,16);
    boolean archive = rs.getInt("archive") == 1 ? true : false;
    rs.close();

    pStmt = con.prepareStatement("SELECT * FROM servers WHERE enabled=1");
    rs = pStmt.executeQuery();

    MRCConnector mrc = null;
    Long snapshotDuration = null;

    ArrayList<String> serverIds = new ArrayList<String>();
    ArrayList<String> serverNames = new ArrayList<String>();
%>
<script type = "text/javascript">
var serverBeans = new Array();
var serverPrettyBeans = new Array();
var serverBeanStatAttributes = new Array();
var server_id = "<%=server_id%>";
var mbean = "<%=mbean%>";
var dataname1 = "<%=dataname1%>";
var dataname2 = "<%=dataname2%>";

<%
while (rs.next())
{
    TreeMap <String,String> trackedBeansMap = null;
    try {
        mrc = new MRCConnector(           rs.getString("ip"), 
                                                    rs.getString("username"), 
                                                    rs.getString("password"),
                                                    rs.getInt("port"));
        trackedBeansMap = mrc.getTrackedBeansMap();;
        serverIds.add(rs.getString("server_id"));
        serverNames.add(rs.getString("name") +" - "+rs.getString("ip"));
        snapshotDuration = mrc.getSnapshotDuration();
        %>
        serverBeans[<%=rs.getString("server_id")%>] = new Array();
        serverPrettyBeans[<%=rs.getString("server_id")%>] = new Array();
        serverBeanStatAttributes[<%=rs.getString("server_id")%>] = new Array();
        <%
        int i = 0;
        for (Iterator <String> it = trackedBeansMap.keySet().iterator(); it.hasNext();)
            {
                String prettyBean = it.next().toString();
                 Set<String> statAttributes = mrc.getStatAttributesOnMBean(trackedBeansMap.get(prettyBean));
            %>
                serverBeans[<%=rs.getString("server_id")%>][<%=i%>]="<%=trackedBeansMap.get(prettyBean)%>";
                serverPrettyBeans[<%=rs.getString("server_id")%>][<%=i%>]="<%=prettyBean%>";
                serverBeanStatAttributes[<%=rs.getString("server_id")%>][<%=i%>] = new Array();
                <%
                int j = 0;
                for (Iterator <String> itt = statAttributes.iterator(); itt.hasNext();)
                {
                    %>
                    serverBeanStatAttributes[<%=rs.getString("server_id")%>][<%=i%>][<%=j%>]="<%=itt.next().toString()%>";
                    <%
                    j++;
                }
                i++;
            }       
         %>
             
        <%
    }
    catch (Exception e)
    {
        
    }
    %>
    
<%}

%>
</script>

<!-- <head> -->

    <style type='text/css'>
    </style>
    <script type = "text/javascript">
<!--

function hide(x) {
document.getElementById(x).style.display='none';
}
function show(x) {
document.getElementById(x).style.display='';
}
function validate(duration) 
{
   if (! (document.editGraph.name.value  
      && document.editGraph.dataname1.value
      && document.editGraph.mbean.value
      && document.editGraph.server_id.value
      && document.editGraph.timeframe.value ))
   {
      alert("Server, Name, Data Series, MBean and Timeframe are all required fields");
      return false;
   }
   // ensure that the timeframe is at least 2*(snapshotduration)
   if(duration * 2 > document.editGraph.timeframe.value) {
        alert("Snapshot Duration needs to be at least " + 2 * duration);
        return false;
   }
   if (document.editGraph.operation.value == 'other')
   {
       if (!document.editGraph.othermath.value)
       {
            alert("If operation is other, field must contain a math operation");
            return false;
       }
       var mathChars= /[\+\-\*\/]/;
       if (!document.editGraph.othermath.value.match(mathChars)) {
            alert("Operation must use at least one math symbol (+, -, *, /)");
            return false;
        }
        if (document.editGraph.dataname2.value != "")
        {
            if (document.editGraph.othermath.value.length > 1) 
            {
                if (!document.editGraph.othermath.value.charAt(0).match(mathChars) || !document.editGraph.othermath.value.charAt(document.editGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
                if (document.editGraph.othermath.value.length == 2) 
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
        }
        else
        {
            if (document.editGraph.othermath.value.length > 1) 
            {
                if (!document.editGraph.othermath.value.charAt(0).match(mathChars) || document.editGraph.othermath.value.charAt(document.editGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
            else if (document.editGraph.othermath.value.length == 1) 
            {
                alert("Operation must create a valid formula");
                return false;
            }
        }
        
    }
    return;
}
function noAlpha(obj){
    reg = /[^0-9]/g;
    obj.value =  obj.value.replace(reg,"");
 }
 
 function noAlphaMath(obj){
    reg = /[^0-9,\-,\+,\*,\/,\.]/g;
    obj.value =  obj.value.replace(reg,"");
 }
 
 function clearList(selectbox)
{
    var i;
    for(i=selectbox.options.length-1;i>=0;i--)
    {
        selectbox.remove(i);
    }
}


function addOption(selectbox, value, text )
{
    var optn = document.createElement("OPTION");
    optn.text = text;
    optn.value = value;
    if (selectbox == document.editGraph.mbean)
    {
        if (value==mbean)
            optn.selected = "selected";
    }
    else if (selectbox == document.editGraph.dataname2 && (document.editGraph.dataname2.disabled = ""))
    {
        if (value=="time")
            optn.selected = "selected";
    }
    else if (selectbox == document.editGraph.dataname1)
    {
        if (value==dataname1)
            optn.selected = "selected";
    }
    else if (selectbox == document.editGraph.dataname2)
    {
        if (value==dataname2)
            optn.selected = "selected";
    }
    selectbox.options.add(optn);
}
 
 
 function updateMbeanList(){
    clearList(document.editGraph.mbean);
    if (document.editGraph.server_id.value)
    {
        addOption(document.editGraph.mbean, "", "-Select MBean-");
        for (var i = 0; i < serverPrettyBeans[document.editGraph.server_id.value].length; i++)
        {
            addOption(document.editGraph.mbean, serverBeans[document.editGraph.server_id.value][i], serverPrettyBeans[document.editGraph.server_id.value][i]);
        }
    }
    else
        addOption(document.editGraph.mbean, "", "-Select Server First-");
    updateDatanameList();
 }
 
  function updateDatanameList(){
    clearList(document.editGraph.dataname1);
    if (document.editGraph.mbean.value)
    {
        addOption(document.editGraph.dataname1, "", "-Select Data Name-");
        for (var i = 0; i < serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1].length; i++)
        {
            addOption(document.editGraph.dataname1, serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i], serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i]);
        }
    }
    else
        addOption(document.editGraph.dataname1, "", "-Select MBean First-");
    updateDataname2List();
 }
 
   function updateDataname2List(){
    clearList(document.editGraph.dataname2);
    if (document.editGraph.mbean.value)
    {
        if (document.editGraph.operation.value=="other")
            addOption(document.editGraph.dataname2, "", "none");
        addOption(document.editGraph.dataname2, "time", "Time");
        for (var i = 0; i < serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1].length; i++)
        {
            addOption(document.editGraph.dataname2, serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i], serverBeanStatAttributes[document.editGraph.server_id.value][document.editGraph.mbean.selectedIndex-1][i]);
        }
    }
    else
        addOption(document.editGraph.dataname2, "", "-Select MBean First-");
        
     if (document.editGraph.operation.value=="")
     {
        document.editGraph.data2operation.selectedIndex=0;
        document.editGraph.dataname2.selectedIndex=0;
        document.editGraph.data2operation.disabled="disabled";
        document.editGraph.dataname2.disabled="disabled";
     }
 }
 
   function checkOtherMath(){
    if (document.editGraph.operation.value=="other")
    {
        document.editGraph.othermath.style.display='';
    }
    else
        document.editGraph.othermath.style.display='none';
   if (document.editGraph.operation.value=="")
   {
        document.editGraph.data2operation.selectedIndex=0;
        document.editGraph.dataname2.selectedIndex=0;
        document.editGraph.data2operation.disabled="disabled";
        document.editGraph.dataname2.disabled="disabled";
   }
   else
   {
       document.editGraph.dataname2.disabled="";
       document.editGraph.data2operation.disabled="";
       updateDataname2List();
       checkNoData2();
   }

 }
  function checkNoData2()
 {
    if (document.editGraph.dataname2.value == "")
    {
        document.editGraph.data2operation.selectedIndex = 0;
        document.editGraph.data2operation.disabled="disabled";
    }
    else
    {
        document.editGraph.data2operation.disabled="";
    }
 }
 function getObject(obj) {
  var theObj;
  if(document.all) {
    if(typeof obj=="string") {
      return document.all(obj);
    } else {
      return obj.style;
    }
  }
  if(document.getElementById) {
    if(typeof obj=="string") {
      return document.getElementById(obj);
    } else {
      return obj.style;
    }
  }
  return null;
}
 
    function updateFormula()
    {
        var exitObj=getObject('formulaData1operation');
        if (document.editGraph.dataname1.value)
        {
            if (document.editGraph.data1operation.value == 'D')
                exitObj.innerHTML = "(Delta)";
            else
                exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname1');
            exitObj.innerHTML = document.editGraph.dataname1.value;
            if (document.editGraph.operation.value)
            {
                if (document.editGraph.operation.value == "other")
                {
                    exitObj=getObject('formulaOperation');
                    exitObj.innerHTML = document.editGraph.othermath.value;
                }
                else
                {
                    exitObj=getObject('formulaOperation');
                    exitObj.innerHTML = document.editGraph.operation.value;
                }
                exitObj=getObject('formulaData2operation');
                if (document.editGraph.data2operation.value == 'D')
                    exitObj.innerHTML = "(Delta)";
                else
                    exitObj.innerHTML = "";
                exitObj=getObject('formulaDataname2');
                exitObj.innerHTML = document.editGraph.dataname2.value;
            }
            else
            {
                exitObj=getObject('formulaOperation');
                exitObj.innerHTML = "";
                exitObj=getObject('formulaData2operation');
                exitObj.innerHTML = "";
                exitObj=getObject('formulaDataname2');
                exitObj.innerHTML = "";
            }
            
        }
        else
        {
            exitObj=getObject('formulaData1operation');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname1');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaOperation');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaData2operation');
            exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname2');
            exitObj.innerHTML = "";
        }
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
            Editing: <%=name%>
            </font>
            </p>         
            <p>
   <form onsubmit="return validate(<%=snapshotDuration/1000/60%>);" name="editGraph" method="POST" action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveEditGraph"/></portlet:actionURL>">
   <table cellpadding="1" cellspacing="1">
      <tr>
      <td>Added:</td>
      <td>&nbsp;</td>
      <td align="right"><%=added%></td>
      <td></td>
    </tr>
    <tr>
      <td>Last Modified:</td>
      <td>&nbsp;</td>
      <td align="right"><%=modified%></td>
      <td></td>
    </tr>
    <tr>
      <td>Last Seen:</td>
      <td>&nbsp;</td>
      <td align="right"><%=last_seen%></td>
      <td></td>
    </tr>
   <tr>
      <td>Server:</td>
      <td>&nbsp;</td>
      <td align="right">
    <select name="server_id" onChange="updateMbeanList(); updateFormula();">
      <option value="">-Select Server-</option>
    </select>    
    <script type='text/javascript'>
    <% 
    for (int i = 1; i < serverIds.size()+1; i++)
    {
    %>
        document.editGraph.server_id.options[<%=i%>]=new Option("<%=serverNames.get(i-1)%>", "<%=serverIds.get(i-1)%>", <%if (server_id.equals(serverIds.get(i-1))){%>true<%}else{%>false<%}%>);
    <%
    }%>
    </script>
    </td>
      <td></td>
    </tr>
    <tr>
      <td>Name:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="name" value="<%=name%>"></td>
      <td></td>
    </tr>
    <tr>
      <td>Description:</td>
      <td>&nbsp;</td>
      <td align="right"><textarea rows="5" cols="50" name="description"><%=description%></textarea></td>
      <td></td>
    </tr>
    <tr>
      <td>X Axis label:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="xlabel" value="<%=xlabel%>"/></td>
      <td></td>
    </tr>
    <tr>
      <td>Y Axis label:</td>
      <td>&nbsp;</td>
      <td align="right"><input type="text" name="ylabel" value="<%=ylabel%>"/></td>
      <td></td>
    </tr>
    <tr>
        <td>Timeframe:</td>
        <td>&nbsp;</td>
        <td align="right"><input type="text" width="5" size="4" name="timeframe" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)' value="<%=timeframe%>"/></td>
        <td> minutes</td>
      </tr>
    <tr>
      <td>Mbean:</td>
      <td>&nbsp;</td>
      <td align="right">
    <select name="mbean" onChange="updateDatanameList(); updateFormula();">
      <option value="">-Select Server First-</option>
    </select>
    </td>
      <td></td>
    </tr>
    <tr>
      <td>Data series:</td>
      <td>&nbsp;</td>
      <td align="right">
      <select name="data1operation" onchange="updateFormula();">
        <option value="A" <%if (data1operation.equals("A")){  %>selected="selected"<%} %>>As-is</option>
        <option value="D" <%if (data1operation.equals("D")){  %>selected="selected"<%} %>>Change (Delta) in</option>
      </select>name
      <select name="dataname1" onchange="updateFormula();">
        <option value="">-Select MBean First-</option>
      </select>
      </td>
      <td></td>
    </tr>
    <tr>
      <td>Math operation:</td>
      <td>&nbsp;</td>
        <td align="right">
      <select name="operation" onChange="checkOtherMath(); updateFormula();">
        <option value="" <%if (operation.equals("null") || operation.equals("") || operation == null){  %>selected="selected"<%} %>>none</option>
        <option value="+" <%if (operation.equals("+")){  %>selected="selected"<%} %>>+</option>
        <option value="-" <%if (operation.equals("-")){  %>selected="selected"<%} %>>-</option>
        <option value="*" <%if (operation.equals("*")){  %>selected="selected"<%} %>>*</option>
        <option value="/" <%if (operation.equals("/")){  %>selected="selected"<%} %>>/</option>
        <option value="other" <%if (!operation.equals("null") && !operation.equals("+") && !operation.equals("-") && !operation.equals("*") && !operation.equals("/") && !operation.equals("") && operation != null){  %>selected="selected"<%} %>>Other</option>
      </select>
      </td>
      <td><input type="text" <%if (!operation.equals("null") && !operation.equals("+") && !operation.equals("-") && !operation.equals("*") && !operation.equals("/") && !operation.equals("") && operation != null){  %><%} else {%>style="display: none;"<%}%> width="6" size="8" name="othermath" onKeyUp='noAlphaMath(this); updateFormula();' onKeyPress='noAlphaMath(this); updateFormula();' value="<%=operation %>"/></td>
    </tr>
    <tr>
      <td>Data series 2:</td>
      <td>&nbsp;</td>
      <td align="right">
      <select name="data2operation" <%if (operation.equals("null") || operation.equals("") || operation == null){  %>disabled="disabled"<%} %> onchange="updateFormula();">
        <option value="A" <%if (data2operation.equals("A")){  %>selected="selected"<%} %>>As-is</option>
        <option value="D" <%if (data2operation.equals("D")){  %>selected="selected"<%} %>>Change (Delta) in</option>
      </select>
      <select name="dataname2" <%if (operation.equals("null") || operation.equals("") || operation == null){  %>disabled="disabled"<%} %> onchange=" checkNoData2(); updateFormula();">
        <option value="">-Select Operation First-</option>
      </select>
      <script type='text/javascript'>
        updateMbeanList();
      </script>
      </td>
      <td></td>
    </tr>
    <tr>
        <td></td>
        <td></td>
        <td>
            <% if(archive) { %>
                <input type="checkbox" name="showArchive" checked>Show Archive</input>
            <% } else { %>
                <input type="checkbox" name="showArchive">Show Archive</input>
            <% } %>
        </td>
        <td></td>
    </tr>
    <tr><td>Graphing: </td><td colspan="2"><strong><span id="formulaData1operation"></span> <span id="formulaDataname1"></span> <span id="formulaOperation"></span> <span id="formulaData2operation"></span> <span id="formulaDataname2"></span></strong></td></tr>
    <tr><td colspan="3"><font size="-2">&nbsp;</font></td></tr>
    <tr>
      <td colspan="1" align="left"><button type="button" value="Cancel" onclick="javascript:history.go(-1)">Cancel</button></td>
      <td>&nbsp;</td>
      <td colspan="1" align="right"><input type="submit" value="Save" /></td>
      <td></td>
    </tr>
  </table>
  <input name="graph_id" type="hidden" value="<%=graph_id%>">
  </form>
      </p>
      <script type='text/javascript'>
        updateFormula();
        checkNoData2();
      </script>
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
                            <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="deleteGraph" /><portlet:param name="graph_id" value="<%=graph_id%>" /></portlet:actionURL>">Delete this graph</a></li>
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
            <a HREF="javascript:history.go(-1)"><< Back</a>
            <p>
            <font face="Verdana" size="+1">
            Graph does not exist
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
                        <li><a href="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="showAddGraph" /></portlet:actionURL>">Add a new graph</a></li>
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


