<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
<head>
	<title>PingJspEL</title>
</head>
<body>
<%@ page import="org.apache.geronimo.samples.daytrader.*" session="false" %>

<%!
int hitCount = 0;
String initTime = new java.util.Date().toString();
%>
 
<%
// setup some variables to work with later
int someint1 = TradeConfig.rndInt(100) + 1;
pageContext.setAttribute("someint1", new Integer(someint1));
int someint2 = TradeConfig.rndInt(100) + 1;
pageContext.setAttribute("someint2", new Integer(someint2));
float somefloat1 = TradeConfig.rndFloat(100) + 1.0f;
pageContext.setAttribute("somefloat1", new Float(somefloat1));
float somefloat2 = TradeConfig.rndFloat(100) + 1.0f;
pageContext.setAttribute("somefloat2", new Float(somefloat2));
 
QuoteDataBean quoteData1 = QuoteDataBean.getRandomInstance();
pageContext.setAttribute("quoteData1", quoteData1);
QuoteDataBean quoteData2 = QuoteDataBean.getRandomInstance();
pageContext.setAttribute("quoteData2", quoteData2);
QuoteDataBean quoteData3 = QuoteDataBean.getRandomInstance();
pageContext.setAttribute("quoteData3", quoteData3);
QuoteDataBean quoteData4 = QuoteDataBean.getRandomInstance();
pageContext.setAttribute("quoteData4", quoteData4);

QuoteDataBean quoteData[] = new QuoteDataBean[4];
quoteData[0] = quoteData1;
quoteData[1] = quoteData2;
quoteData[2] = quoteData3;
quoteData[3] = quoteData4;
pageContext.setAttribute("quoteData", quoteData);
%>
  
<HR>
<BR>
  <FONT size="+2" color="#000066">PING JSP EL:<BR></FONT><FONT size="+1" color="#000066">Init time: <%= initTime %></FONT>
  <P>
    <B>Hit Count: <%= hitCount++ %></B>
   </P>
<HR>

<P>

someint1 = <%= someint1 %><br/>
someint2 = <%= someint2 %><br/>
somefloat1 = <%= somefloat1 %><br/>
somefloat2 = <%= somefloat2 %><br/>

<P>

<HR>

<table border="1">
	<thead>
		<td><b>EL Type</b></td>
		<td><b>EL Expressions</b></td>
		<td><b>Result</b></td>
	</thead>
	<tr>
		<td>Integer Arithmetic</td>
		<td>\${someint1 + someint2 - someint1 * someint2 mod someint1}</td>
		<td>${someint1 + someint2 - someint1 * someint2 mod someint1}</td>
	</tr>
	<tr>
		<td>Floating Point Arithmetic</td>
		<td>\${somefloat1 + somefloat2 - somefloat1 * somefloat2 / somefloat1}</td>
		<td>${somefloat1 + somefloat2 - somefloat1 * somefloat2 / somefloat1}</td>
	</tr>
	<tr>
		<td>Logical Operations</td>
		<td>\${(someint1 < someint2) && (someint1 <= someint2) || (someint1 == someint2) && !Boolean.FALSE}</td>
		<td>${(someint1 < someint2) && (someint1 <= someint2) || (someint1 == someint2) && !Boolean.FALSE}</td>
	</tr>
	<tr>
		<td>Indexing Operations</td>
		<td>
			\${quoteData3.symbol}<br/>
			\${quoteData[2].symbol}<br/>
			\${quoteData4["symbol"]}<br/>
			\${header["host"]}<br/>
			\${header.host}<br/>
		</td>
		<td>
			${quoteData3.symbol}<br/>
			${quoteData[1].symbol}<br/>
			${quoteData4["symbol"]}<br/>
			${header["host"]}<br/>
			${header.host}
		</td>
	</tr>
	<tr>
		<td>Variable Scope Tests</td>
		<td>
			\${(quoteData3 == null) ? "null" : quoteData3}<br/>
			\${(noSuchVariableAtAnyScope == null) ? "null" : noSuchVariableAtAnyScope}
		</td>
		<td>
			${(quoteData3 == null) ? "null" : quoteData3}<br/>
			${(noSuchVariableAtAnyScope == null) ? "null" : noSuchVariableAtAnyScope}
		</td>
	</tr>
</table>
</body>
</html>
