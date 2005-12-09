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

package org.apache.geronimo.samples.daytrader.web.prims;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.*;
import javax.jms.*;

import org.apache.geronimo.samples.daytrader.ejb.*;
import org.apache.geronimo.samples.daytrader.util.*;

import org.apache.geronimo.samples.daytrader.*;

/**
 *
 * This primitive is designed to run inside the TradeApplication and relies upon
 * the {@link trade_client.TradeConfig} class to set configuration parameters.
 * PingServlet2MDBQueue tests key functionality of a servlet call to a 
 * post a message to an MDB Topic. The TradeStreamerMDB (and any other subscribers)
 * receives the message
 * This servlet makes use of the MDB EJB {@link org.apache.geronimo.samples.daytrader.ejb.TradeStreamerMDB} 
 * by posting a message to the MDB Topic
 *
 */
public class PingServlet2MDBTopic extends HttpServlet {

	private static String initTime;
	private static int hitCount;
	private static Connection conn;
	private static ConnectionFactory connFactory;
	private static Topic topic;

/**
 * forwards post requests to the doGet method
 * Creation date: (11/6/2000 10:52:39 AM)
 * @param res javax.servlet.http.HttpServletRequest
 * @param res2 javax.servlet.http.HttpServletResponse
 */
public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
	doGet(req, res);
}
 
 
/**
* this is the main method of the servlet that will service all get requests.
* @param request HttpServletRequest
* @param responce HttpServletResponce
**/

public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {

	res.setContentType("text/html");
	java.io.PrintWriter out = res.getWriter();
	Trade trade = null;
	//use a stringbuffer to avoid concatenation of Strings
	StringBuffer output = new StringBuffer(100);
	output.append(
		"<html><head><title>PingServlet2MDBTopic</title></head>"
			+ "<body><HR><FONT size=\"+2\" color=\"#000066\">PingServlet2MDBTopic<BR></FONT>"
			+ "<FONT size=\"-1\" color=\"#000066\">"
			+ "Tests the basic operation of a servlet posting a message to an EJB MDB (and other subscribers) through a JMS Topic."); 

	//we only want to look up the JMS resources once	
	try
	{
		
		if (conn == null)
		{
		
			synchronized (lock)
			{
				if (conn == null)
				{
			
					try 
					{
						output.append("<HR><B>Performing JNDI lookups and creating JMS Resources</B>");						
						InitialContext context = new InitialContext();
						connFactory = (ConnectionFactory) context.lookup("java:comp/env/jms/TopicConnectionFactory");
						topic = (Topic) context.lookup("java:comp/env/jms/TradeStreamerTopic");
						// TODO: Is this wrong as it only creates one shared connecton?
						conn = connFactory.createConnection();		            			
					}
					catch (Exception e)
					{
						Log.error("PingServlet2MDBTopic:doGet() -- error on intialization of JMS factories, topics", e);
						throw e;
					}		
				}
			}
		}
		
		try
		{
			TextMessage message = null;
			int iter = TradeConfig.getPrimIterations();
			for (int ii = 0; ii < iter; ii++) {
				Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
				MessageProducer producer = sess.createProducer(topic);
				message = sess.createTextMessage();
	
				String command= "ping";
				message.setStringProperty("command", command);
				message.setLongProperty("publishTime", System.currentTimeMillis());
				message.setText("Ping message for topic java:comp/env/jms/TradeStreamerTopic sent from PingServlet2MDBTopic at " + new java.util.Date());
	
				producer.send(message);
				sess.close();
			}					

			//write out the output
			output.append("<HR>initTime: " + initTime);
			output.append("<BR>Hit Count: " + hitCount++);
			output.append("<HR>Posted Text message to java:comp/env/jms/TradeStreamerTopic topic");
			output.append("<BR>Message: " + message);
			output.append("<BR><BR>Message text: " + message.getText());			
			output.append(
				"<BR><HR></FONT></BODY></HTML>"); 
			out.println(output.toString());

		}
		catch (Exception e)
		{
			Log.error("PingServlet2MDBTopic.doGet(...):exception posting message to TradeStreamerTopic topic"); 
			throw e;
		}
	} //this is where I actually handle the exceptions
	catch (Exception e)
	{
		Log.error(e, "PingServlet2MDBTopic.doGet(...): error"); 
		res.sendError(500, "PingServlet2MDBTopic.doGet(...): error, " + e.toString());

	}
}                           




/** 
 * returns a string of information about the servlet
 * @return info String: contains info about the servlet
 **/

public String getServletInfo()
{
	return "web primitive, configured with trade runtime configs, tests Servlet to Session EJB path"; 

}      
/**
* called when the class is loaded to initialize the servlet
* @param config ServletConfig:
**/
public void init(ServletConfig config) throws ServletException {
	super.init(config);
	hitCount = 0;
	initTime = new java.util.Date().toString();
	//for synchronization
	lock = new Integer(99);
}      


	private java.lang.Integer lock;}