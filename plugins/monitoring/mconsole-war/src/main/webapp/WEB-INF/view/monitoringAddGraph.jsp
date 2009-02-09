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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="org.apache.geronimo.monitoring.console.StatsGraph"%>
<%@ page import="org.apache.geronimo.monitoring.console.GraphsBuilder"%>
<%@ page import="java.util.Set"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.TreeMap"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.lang.String"%>
<%@ page import="java.sql.Connection"%>
<%@ page import="java.sql.DatabaseMetaData"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>
<%@ page import="org.apache.geronimo.monitoring.console.util.*"%>
<%@ page import="org.apache.geronimo.monitoring.console.MRCConnector"%>
<fmt:setBundle basename="monitor-portlet"/>
<portlet:defineObjects />

<%
            String message = (String) request.getAttribute("message");
            String mbean = (String) request.getAttribute("mbean");
            String dataname1 = (String) request.getAttribute("dataname");
            String server_id = (String) request.getAttribute("server_id");

            DBManager DBase = new DBManager();
            Connection con = DBase.getConnection();

            PreparedStatement pStmt = con
                    .prepareStatement("SELECT * FROM servers WHERE enabled=1");
            ResultSet rs = pStmt.executeQuery();
            MRCConnector mrc = null;
            ArrayList<String> serverIds = new ArrayList<String>();
            ArrayList<String> serverNames = new ArrayList<String>();
            Long snapshotDuration = 5L;

            if (message == null)
                message = new String("");
            if (mbean == null)
                mbean = new String("");
            if (dataname1 == null)
                dataname1 = new String("");
            if (server_id == null)
                server_id = new String("");
%>
<script type="text/javascript">
var serverBeans = new Array();
var serverPrettyBeans = new Array();
var serverBeanStatAttributes = new Array();
var server_id = "<%=server_id%>";
var mbean = "<%=mbean%>";
var dataname1 = "<%=dataname1%>";


<%
while (rs.next())
{
    TreeMap <String,String> trackedBeansMap = null;
    try {
        mrc = new MRCConnector(       rs.getString("ip"), 
                                                rs.getString("username"), 
                                                rs.getString("password"),
                                                rs.getInt("port"),
                                                rs.getInt("protocol"));
        trackedBeansMap = mrc.getTrackedBeansMap();
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
        e.printStackTrace();
    }
    %>
    
<%}

%>
</script>
<!-- <head> -->

<style type='text/css'>
</style>
<script type="text/javascript">
<!--

function hide(x) {
document.getElementById(x).style.display='none';
}
function show(x) {
document.getElementById(x).style.display='';
}
function validate(duration) 
{
   if (! (document.addGraph.name.value  
      && document.addGraph.dataname1.value
      && document.addGraph.mbean.value
      && document.addGraph.server_id.value
      && document.addGraph.timeframe.value ))
   {
      alert("Server, Name, Data Series, MBean and Timeframe are all required fields");
      return false;
   }
   // ensure that the timeframe is at least 2*(snapshotduration)
   if(duration * 2 > document.addGraph.timeframe.value) {
       alert("Timeframe needs to be at least " + 2 * duration);
       return false;
   }
   if (document.addGraph.operation.value == 'other')
   {
       if (!document.addGraph.othermath.value)
       {
            alert("If operation is other, field must contain a math operation");
            return false;
       }
       var mathChars= /[\+\-\*\/]/;
       if (!document.addGraph.othermath.value.match(mathChars)) {
            alert("Operation must use at least one math symbol (+, -, *, /)");
            return false;
        }
        if (document.addGraph.dataname2.value != "")
        {
            if (document.addGraph.othermath.value.length > 1) 
            {
                if (!document.addGraph.othermath.value.charAt(0).match(mathChars) || !document.addGraph.othermath.value.charAt(document.addGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
                if (document.addGraph.othermath.value.length == 2) 
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
        }
        else
        {
            if (document.addGraph.othermath.value.length > 1) 
            {
                if (!document.addGraph.othermath.value.charAt(0).match(mathChars) || document.addGraph.othermath.value.charAt(document.addGraph.othermath.value.length - 1).match(mathChars))
                {
                    alert("Operation must create a valid formula");
                    return false;
                }
            }
            else if (document.addGraph.othermath.value.length == 1) 
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
    if (selectbox == document.addGraph.mbean)
    {
        if (value==mbean)
            optn.selected = "selected";
    }
    else if (selectbox == document.addGraph.dataname2 && (document.addGraph.dataname2.disabled = ""))
    {
        if (value=="time")
            optn.selected = "selected";
    }
    else if (selectbox == document.addGraph.dataname1)
    {
        if (value==dataname1)
            optn.selected = "selected";
    }
    selectbox.options.add(optn);
}
 
 
 function updateMbeanList(){
    clearList(document.addGraph.mbean);
    if (document.addGraph.server_id.value)
    {
        addOption(document.addGraph.mbean, "", "-Select MBean-");
        for (var i = 0; i < serverPrettyBeans[document.addGraph.server_id.value].length; i++)
        {
            addOption(document.addGraph.mbean, serverBeans[document.addGraph.server_id.value][i], serverPrettyBeans[document.addGraph.server_id.value][i]);
        }
    }
    else
        addOption(document.addGraph.mbean, "", "-Select Server First-");
    updateDatanameList();
 }
 
  function updateDatanameList(){
    clearList(document.addGraph.dataname1);
    if (document.addGraph.mbean.value)
    {
        addOption(document.addGraph.dataname1, "", "-Select Data Name-");
        for (var i = 0; i < serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex-1].length; i++)
        {
            addOption(document.addGraph.dataname1, serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex-1][i], serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex-1][i]);
        }
    }
    else
        addOption(document.addGraph.dataname1, "", "-Select MBean First-");
    updateDataname2List();
 }
 
   function updateDataname2List(){
    clearList(document.addGraph.dataname2);
    if (document.addGraph.mbean.value)
    {
        if (document.addGraph.operation.value=="other")
            addOption(document.addGraph.dataname2, "", "none");
        addOption(document.addGraph.dataname2, "time", "Time");
        for (var i = 0; i < serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex-1].length; i++)
        {
            addOption(document.addGraph.dataname2, serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex-1][i], serverBeanStatAttributes[document.addGraph.server_id.value][document.addGraph.mbean.selectedIndex-1][i]);
        }
    }
    else
        addOption(document.addGraph.dataname2, "", "-Select MBean First-");
        
     if (document.addGraph.operation.value=="")
     {
        document.addGraph.data2operation.selectedIndex=0;
        document.addGraph.dataname2.selectedIndex=0;
        document.addGraph.data2operation.disabled="disabled";
        document.addGraph.dataname2.disabled="disabled";
     }
 }
 
   function checkOtherMath(){
    if (document.addGraph.operation.value=="other")
    {
        document.addGraph.othermath.style.display='';
    }
    else
        document.addGraph.othermath.style.display='none';
   if (document.addGraph.operation.value=="")
   {
        document.addGraph.data2operation.selectedIndex=0;
        document.addGraph.dataname2.selectedIndex=0;
        document.addGraph.data2operation.disabled="disabled";
        document.addGraph.dataname2.disabled="disabled";
   }
   else
   {
       document.addGraph.dataname2.disabled="";
       document.addGraph.data2operation.disabled="";
       updateDataname2List();
       checkNoData2();
   }

 }
 
 function checkNoData2()
 {
    if (document.addGraph.dataname2.value == "")
    {
        document.addGraph.data2operation.selectedIndex = 0;
        document.addGraph.data2operation.disabled="disabled";
    }
    else
    {
        document.addGraph.data2operation.disabled="";
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
        if (document.addGraph.dataname1.value)
        {
            if (document.addGraph.data1operation.value == 'D')
                exitObj.innerHTML = "(Delta)";
            else
                exitObj.innerHTML = "";
            exitObj=getObject('formulaDataname1');
            exitObj.innerHTML = document.addGraph.dataname1.value;
            if (document.addGraph.operation.value)
            {
                if (document.addGraph.operation.value == "other")
                {
                    exitObj=getObject('formulaOperation');
                    exitObj.innerHTML = document.addGraph.othermath.value;
                }
                else
                {
                    exitObj=getObject('formulaOperation');
                    exitObj.innerHTML = document.addGraph.operation.value;
                }
                exitObj=getObject('formulaData2operation');
                if (document.addGraph.data2operation.value == 'D')
                    exitObj.innerHTML = "(Delta)";
                else
                    exitObj.innerHTML = "";
                exitObj=getObject('formulaDataname2');
                exitObj.innerHTML = document.addGraph.dataname2.value;
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
if (!message.equals("")) {
%>
<div align="left" style="width: 500px"><%=message%><br>
</div>
<%
}
%>
<table>
	<tr>
		<!-- Body -->
		<td width="90%" align="left" valign="top">
		<p><font face="Verdana" size="+1"> <fmt:message key="monitor.graph.addGraph"/> </font></p>
		<p>
		<form onsubmit="return validate(<%=snapshotDuration/1000/60%>);"
			name="addGraph" method="POST"
			action="<portlet:actionURL portletMode="edit"><portlet:param name="action" value="saveAddGraph"/></portlet:actionURL>">
		<table cellpadding="1" cellspacing="1">
			<tr>
				<td><fmt:message key="monitor.graph.server"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><select name="server_id"
					onChange="updateMbeanList(); updateFormula();">
					<option value="">-<fmt:message key="monitor.graph.selectServer"/>-</option>
				</select> <script type='text/javascript'>
    <% 
    for (int i = 1; i < serverIds.size()+1; i++)
    {
    %>
        document.addGraph.server_id.options[<%=i%>]=new Option("<%=serverNames.get(i-1)%>", "<%=serverIds.get(i-1)%>", <%if (server_id.equals(serverIds.get(i-1))){%>true<%}else{%>false<%}%>);
    <%
    }%>
    </script></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.common.name"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><input type="text" name="name" value=""></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.common.desc"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><textarea rows="5" cols="50"
					name="description"></textarea></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.x"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><input type="text" name="xlabel" value="" /></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.y"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><input type="text" name="ylabel" value="" /></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.time"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><input type="text" width="5" size="4"
					name="timeframe" onKeyUp='noAlpha(this)' onKeyPress='noAlpha(this)'
					value="60" /></td>
				<td><fmt:message key="monitor.common.minute"/></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.mbean"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><select name="mbean"
					onChange="updateDatanameList(); updateFormula();">
					<option value="">-<fmt:message key="monitor.graph.selectServerFirst"/>-</option>
				</select></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.data"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><select name="data1operation"
					onchange="updateFormula();">
					<option value="A" selected="selected"><fmt:message key="monitor.graph.asis"/></option>
					<option value="D"><fmt:message key="monitor.graph.change"/></option>
				</select> <select name="dataname1" onchange="updateFormula();">
					<option value="">-<fmt:message key="monitor.graph.selectMbeanFirst"/>-</option>
				</select></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.math"/>:</td>
				<td>&nbsp;</td>
				<td align="right"><select name="operation"
					onChange="checkOtherMath(); updateFormula();">
					<option value="" selected="selected"><fmt:message key="monitor.common.none"/></option>
					<option value="+">+</option>
					<option value="-">-</option>
					<option value="*">*</option>
					<option value="/">/</option>
					<option value="other"><fmt:message key="monitor.common.other"/></option>
				</select></td>
				<td><input type="text" style="display: none;" width="6"
					size="8" name="othermath"
					onKeyUp='noAlphaMath(this); updateFormula();'
					onKeyPress='noAlphaMath(this); updateFormula();' value="" /></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.graph.data"/> 2:</td>
				<td>&nbsp;</td>
				<td align="right"><select name="data2operation"
					disabled="disabled" onchange="updateFormula();">
					<option value="A" selected="selected"><fmt:message key="monitor.graph.asis"/></option>
					<option value="D"><fmt:message key="monitor.graph.change"/></option>
				</select> <select name="dataname2" disabled="disabled"
					onchange="updateFormula(); checkNoData2();">
					<option value="">-<fmt:message key="monitor.graph.selectOpFirst"/>-</option>
				</select> <script type='text/javascript'>
        updateMbeanList();
      </script></td>
				<td></td>
			</tr>
			<tr>
				<td></td>
				<td>&nbsp;</td>
				<td align="right"><input type="checkbox" name="showArchive"><fmt:message key="monitor.graph.showArchive"/></td>
				<td></td>
			</tr>
			<tr>
				<td><fmt:message key="monitor.common.graphing"/>:</td>
				<td colspan="2"><strong><span
					id="formulaData1operation"></span> <span id="formulaDataname1"></span>
				<span id="formulaOperation"></span> <span id="formulaData2operation"></span>
				<span id="formulaDataname2"></span></strong></td>
			</tr>
			<tr>
				<td colspan="3"><font size="-2">&nbsp;</font></td>
			</tr>
			<tr>
				<td colspan="1" align="left">
				<button type="button" value='<fmt:message key="monitor.common.cancel"/>'
					onclick="javascript:history.go(-1)"><fmt:message key="monitor.common.cancel"/></button>
				</td>
				<td>&nbsp;</td>
				<td colspan="1" align="right"><input type="submit" value='<fmt:message key="monitor.common.add"/>' /></td>
				<td></td>
			</tr>
		</table>
		</form>
		</p>
		<script type='text/javascript'>
        updateFormula();
        checkNoData2();
      </script></td>

		<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

		<!-- Geronimo Links -->
		<td valign="top">

		<table width="100%" style="border-bottom: 1px solid #2581c7;"
			cellspacing="1" cellpadding="1">
			<tr>
				<td class="DarkBackground" align="left" nowrap><font
					face="Verdana" size="+1"><fmt:message key="monitor.common.nav"/></font></td>
			</tr>
			<tr>
				<td bgcolor="#FFFFFF" nowrap>&nbsp;<br />
				<ul>
					<li><a
						href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showHome" /></portlet:actionURL>"><fmt:message key="monitor.common.home"/></a></li>
					<li><a
						href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllViews" /></portlet:actionURL>"><fmt:message key="monitor.common.view"/></a></li>
					<li><a
						href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllServers" /></portlet:actionURL>"><fmt:message key="monitor.common.server"/></a></li>
					<li><a
						href="<portlet:actionURL portletMode="view"><portlet:param name="action" value="showAllGraphs" /></portlet:actionURL>"><fmt:message key="monitor.common.graph"/></a></li>
				</ul>
				&nbsp;<br />
				</td>
			</tr>
		</table>

		</td>
	</tr>
</table>




