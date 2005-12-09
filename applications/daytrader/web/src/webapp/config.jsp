<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
	<META http-equiv="Content-Style-Type" content="text/css">
	<TITLE>Welcome to Trade</TITLE>
</HEAD>
<BODY bgcolor="#ffffff" link="#000099">
<%@ page import="org.apache.geronimo.samples.daytrader.TradeConfig" session="false" isThreadSafe="true" isErrorPage="false" %>

<TABLE style="font-size: smaller">
	<TBODY>
		<TR>
			<TD bgcolor="#8080c0" align="left" width="500" height="10" colspan="5"><FONT color="#ffffff"><B>Trade Configuration</B></FONT></TD>
			<TD align="center" bgcolor="#000000" width="100" height="10"><FONT color="#ffffff"><B>Trade</B></FONT></TD>
		</TR>
		<TR>
			<TD colspan="6">
				<HR>
			</TD>
		</TR>
		<TR>
			<TD colspan="6">
			</TD>
		</TR>
	</TBODY>
</TABLE>

<%
String status;
status = (String) request.getAttribute("status");
if (status != null) {
%>
<TABLE width="100%" height="30">
	<TBODY>
		<TR>
			<TD></TD>
			<TD><FONT color="#ff0033"><% out.print(status); %>
	</FONT></TD>
			<TD></TD>
		</TR>
	</TBODY>
</TABLE>
<%
}
%>

<FORM action="config" method="POST">
	<INPUT type="hidden" name="action" value="updateConfig">
	
<TABLE border="1" width="614">
	<TBODY>
		<TR>
			<TD colspan="2">
			The Current Trade runtime configuration is detailed
			below. View and optionally update run-time parameters. &nbsp;<BR>
			<BR>
			<B>NOTE: </B>Parameters settings will return to default
			on&nbsp;server restart. To make configuration settings persistent
			across application server stop/starts, edit the servlet init
			parameters for each Trade servlet. This is described in the <A
				href="docs/tradeFAQ.html">Trade FAQ</A>.<BR>
			<HR>
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<B>Run-Time Mode </B>
			<P align="left"><%String configParm = "RunTimeMode";
String names[] = TradeConfig.runTimeModeNames;
int index = TradeConfig.runTimeMode;
for (int i = 0; i < names.length; i++) {
	out.print(
		"<INPUT type=\"radio\" name=\""
			+ configParm
			+ "\" value=\""
			+ i
			+ "\" ");
	if (index == i)
		out.print("checked");
	out.print("> " + names[i] + "<BR>");
}
%></P>
			</TD>
			<TD>
			<BR>
			Run Time Mode determines server implementation of the TradeServices
			to use in the Trade application Enterprise Java Beans including
			Session, Entity and Message beans or Direct mode which uses direct
			database and JMS access. See <A href="docs/tradeFAQ.html">Trade FAQ</A>
			for details.<BR>
			</TD>
		</TR>


		<TR>
			<TD align="left">
			<B>Order-Processing Mode </B>
			<P align="left"><%configParm = "OrderProcessingMode";
names = TradeConfig.orderProcessingModeNames;
index = TradeConfig.orderProcessingMode;
for (int i = 0; i < names.length; i++) {
	out.print(
		"<INPUT type=\"radio\" name=\""
			+ configParm
			+ "\" value=\""
			+ i
			+ "\" ");
	if (index == i)
		out.print("checked");
	out.print("> " + names[i] + "<BR>");
}
%></P>
			</TD>
			<TD>
			<BR>
			Order Processing Mode determines the mode for completing stock
			purchase and sell operations. Synchronous mode completes the order
			immediately. Asynchronous_1-phase mode uses MDB/JMS to queue the
			order to a Trade broker agent to complete the order.
			Asychronous_2-Phase performs a 2-phase commit over the EJB Entity/DB
			and MDB/JMS transactions. See <A href="docs/tradeFAQ.html">Trade FAQ</A>
			for details. <B></B><BR>
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<B>Access Mode </B>
			<P align="left"><%configParm = "AcessMode";
names = TradeConfig.accessModeNames;
index = TradeConfig.getAccessMode();
for (int i = 0; i < names.length; i++) {
	out.print(
		"<INPUT type=\"radio\" name=\""
			+ configParm
			+ "\" value=\""
			+ i
			+ "\" ");
	if (index == i)
		out.print("checked");
	out.print("> " + names[i] + "<BR>");
}
%></P>
			<P><B> Web Services Endpoint <BR>
			<INPUT name="SOAP_URL" size="30" type="text"
				value='<%=(TradeConfig.getSoapURL() == null) ? "" : TradeConfig.getSoapURL()%>'>
			<BR>
			</B></P>
			</TD>
			<TD>
			<BR>
			Access Mode determines the protocol used by the DayTrader Web application
			to access server side services. The Standard mode uses the default
			Java RMI protocol. The Web Services mode uses the Axis 
			implementation of Web Services including SOAP, WSDL and UDDI. <BR>
			For the Web Services Access mode, set the Web Services Endpoint URL
			to point to the host and port which is running the Trade Application
			Web Services module. <BR>
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<B>Scenario Workload Mix</B>
			<P align="left"><%configParm = "WorkloadMix";
names = TradeConfig.workloadMixNames;
index = TradeConfig.workloadMix;
for (int i = 0; i < names.length; i++) {
	out.print(
		"<INPUT type=\"radio\" name=\""
			+ configParm
			+ "\" value=\""
			+ i
			+ "\" ");
	if (index == i)
		out.print("checked");
	out.print("> " + names[i] + "<BR>");
}
%></P>
			</TD>
			<TD>
			This setting determines the runtime
			workload mix of Trade operations when driving the benchmark through
			TradeScenarioServlet. See <A href="docs/tradeFAQ.html">Trade FAQ</A>
			for details.
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<B>WebInterface</B>
			<P align="left"><%configParm = "WebInterface";
names = TradeConfig.webInterfaceNames;
index = TradeConfig.webInterface;
for (int i = 0; i < names.length; i++) {
	out.print(
		"<INPUT type=\"radio\" name=\""
			+ configParm
			+ "\" value=\""
			+ i
			+ "\" ");
	if (index == i)
		out.print("checked");
	out.print("> " + names[i] + "<BR>");
}
%></P>
			</TD>
			<TD>
			This setting determines the Web interface
			technology used, JSPs or JSPs with static images and GIFs.
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<B>Caching Type</B>
			<P align="left"><%configParm = "CachingType";
names = TradeConfig.cachingTypeNames;
index = TradeConfig.cachingType;
for (int i = 0; i < names.length; i++) {
	out.print(
		"<INPUT type=\"radio\" name=\""
			+ configParm
			+ "\" value=\""
			+ i
			+ "\" ");
	if (index == i)
		out.print("checked");
	out.print("> " + names[i] + "<BR>");
}
%></P>
			</TD>
			<TD>
			This setting determines the caching technology used for data caching
			, DistributedMap, Command Caching or No Caching.
			</TD>
		</TR>	
		<TR>
			<TD colspan="2" align="center">
				<B>Miscellaneous Settings</B>
			</TD>
		</TR>
		<TR>
			<TD align="left">
				<B>Trade Max Users </B><BR>
			<INPUT size="25" type="text" name="MaxUsers"
				value=<%=TradeConfig.getMAX_USERS()%>><BR>
			<B>Trade Max Quotes</B><BR>
			<INPUT size="25" type="text" name="MaxQuotes"
				value=<%=TradeConfig.getMAX_QUOTES()%>>
			</TD>
			<TD>
			By default the Trade database is
			populated with 500 users (uid:0 - uid:499) and 1000 quotes (s:0 -
			s:999). <BR>
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<B>Primitive Iteration</B><BR>
			<INPUT size="25" type="text" name="primIterations"
				value="<%=TradeConfig.getPrimIterations()%>">
			</TD>
			<TD>
			By default the Trade primitives are
			execute one operation per web request. Change this value to repeat
			operations multiple times per web request.
			</TD>
		</TR>
		<TR>
			<TD align="left">
			<INPUT type="checkbox"
				<%=TradeConfig.getActionTrace() ? "checked" : ""%>
				name="EnableActionTrace"> <B><FONT size="-1">Enable operation trace</FONT></B><BR>
			<INPUT type="checkbox" <%=TradeConfig.getTrace() ? "checked" : ""%>
				name="EnableTrace"> <B><FONT size="-1">Enable full trace</FONT></B>
			</TD>
			<TD>
				Enable Trade processing trace messages<BR>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
				<INPUT type="submit" value="Update Config">
			</TD>
		</TR>
	</TBODY>
</TABLE>

<TABLE height="54" style="font-size: smaller">
	<TBODY>
		<TR>
			<TD colspan="2">
			<HR>
			</TD>
		</TR>
		<TR>
			<TD colspan="2"></TD>
		</TR>
		<TR>
			<TD bgcolor="#8080c0" align="left" width="500" height="10"><B><FONT
				color="#ffffff">Trade Configuration</FONT></B></TD>
			<TD align="center" bgcolor="#000000" width="100" height="10"><FONT
				color="#ffffff"><B>Trade</B></FONT></TD>
		</TR>
		<TR>
            <TD colspan="2" align="center">Apache Geronimo Performance Benchmark Sample DayTrader<BR>
Copyright 2005, Apache Software Foundation</TD>
		</TR>
	</TBODY>
</TABLE>
</FORM>
</BODY>
</HTML>
