<%@ page import="java.util.Collection, java.util.Iterator, java.math.BigDecimal, org.apache.geronimo.samples.daytrader.*, org.apache.geronimo.samples.daytrader.util.*, org.apache.geronimo.samples.daytrader.soap.*" session="true" isThreadSafe="true" isErrorPage="false"%>
<% 
    String symbol = request.getParameter("symbol");
    TradeServices tAction=null;
	if(TradeConfig.getAccessMode() == TradeConfig.STANDARD)
		tAction = new TradeAction();
	else if(TradeConfig.getAccessMode() == TradeConfig.WEBSERVICES)
		tAction = new TradeWebSoapProxy();   
	try { 
		QuoteDataBean quoteData = tAction.getQuote(symbol);

 %>
	<TR align="center" bgcolor="#fafcb6">
		<TD><%= FinancialUtils.printQuoteLink(quoteData.getSymbol()) %></TD>
		<TD><%= quoteData.getCompanyName()%></TD>
		<TD><%= quoteData.getVolume()%></TD>  
		<TD><%= quoteData.getLow() + " - " + quoteData.getHigh()%></TD>                                                                      
		<TD nowrap><%= quoteData.getOpen()%></TD>                                    
		<TD>$ <%= quoteData.getPrice()%></TD>
		<TD><%= FinancialUtils.printGainHTML(new BigDecimal(quoteData.getChange())) %> <%= FinancialUtils.printGainPercentHTML( FinancialUtils.computeGainPercent(quoteData.getPrice(), quoteData.getOpen())) %></TD>
		<TD>
			<FORM><INPUT type="submit" name="action" value="buy"><INPUT type="hidden" name="symbol" value="<%= quoteData.getSymbol()%>"><INPUT size="4" type="text" name="quantity" value="100"></FORM>
		</TD>
	</TR>

<%
	}
	catch (Exception e)
	{
		Log.error("displayQuote.jsp  exception", e);
	}
%>
