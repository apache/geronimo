/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable 
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.samples.daytrader.web;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.geronimo.samples.daytrader.util.*;

import java.io.IOException;
import java.math.BigDecimal;
import org.apache.geronimo.samples.daytrader.*;

public class TestServlet extends HttpServlet {


	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
	}
	
	
   /**
	* Process incoming HTTP GET requests
	*
	* @param request Object that encapsulates the request to the servlet
	* @param response Object that encapsulates the response from the servlet
	*/
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		performTask(request,response);
	}

   /**
	* Process incoming HTTP POST requests
	*
	* @param request Object that encapsulates the request to the servlet
	* @param response Object that encapsulates the response from the servlet
	*/
	public void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		performTask(request,response);
	}	

   /**
	* Main service method for TradeAppServlet
	*
	* @param request Object that encapsulates the request to the servlet
	* @param response Object that encapsulates the response from the servlet
	*/    	
	public void performTask(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException 
	{
		try {
			Log.debug("Enter TestServlet doGet");
			TradeConfig.runTimeMode = TradeConfig.DIRECT;
			for (int i=0; i<10; i++) 
			{
				new TradeAction().createQuote("s:"+i, "Company " + i, new BigDecimal(i*1.1));
			}
			/*
			
			AccountDataBean accountData = new TradeAction().register("user1", "password", "fullname", "address", 
											"email", "creditCard", new BigDecimal(123.45), false);

			OrderDataBean orderData = new TradeAction().buy("user1", "s:1", 100.0);
			orderData = new TradeAction().buy("user1", "s:2", 200.0);
			Thread.sleep(5000);
			accountData = new TradeAction().getAccountData("user1");
			Collection holdingDataBeans = new TradeAction().getHoldings("user1");
			PrintWriter out = resp.getWriter();
			resp.setContentType("text/html");
			out.write("<HEAD></HEAD><BODY><BR><BR>");
			out.write(accountData.toString());
			Log.printCollection("user1 Holdings", holdingDataBeans);
			ServletContext sc  = getServletContext();
			req.setAttribute("results", "Success");
			req.setAttribute("accountData", accountData);
			req.setAttribute("holdingDataBeans", holdingDataBeans);
			getServletContext().getRequestDispatcher("/tradehome.jsp").include(req, resp);
			out.write("<BR><BR>done.</BODY>");
			*/
		}
		catch (Exception e)
		{
			Log.error("TestServletException", e);
		}
	}
}

