package org.apache.geronimo.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.geronimo.test.local.TestLocal;
import org.apache.geronimo.test.local.TestLocalHome;
import org.apache.geronimo.test.remote.Test;
import org.apache.geronimo.test.remote.TestHome;
import org.apache.geronimo.test.ws.HelloWorld;
import org.apache.geronimo.test.ws.HelloWorldService;


/**
 * Servlet implementation class for Servlet: TomcatTestServlet
 *
 */
 public class TomcatTestServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public TomcatTestServlet() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		out.println("<html><head><title>Sample application to check references</title></head></html>");
		out.println("<body>");

		try
		{
			Context ctx = new InitialContext();
			TestHome result = (TestHome)ctx.lookup("java:comp/env/ejb/TestBean");
			//TestHome testHome = (TestHome) PortableRemoteObject.narrow(result, TestHome.class);
			Test test = result.create();
			String echo = test.echo("Test");
			out.println("<font align=Center face=\"Garamond\"> Check EJB Reference : Call to bean method returned ->"+echo+" </font><br>");
			
			DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/MyDataSource");
	        Connection con = ds.getConnection();
	        out.println("<font align=Center face=\"Garamond\"> Check Resource Reference : Got Connection ->"+con+" </font><br>");
	        
	        QueueConnectionFactory qcf = (QueueConnectionFactory)ctx.lookup("java:comp/env/jms/DefaultActiveMQConnectionFactory");
	        QueueConnection qCon = qcf.createQueueConnection();
	        out.println("<font align=Center face=\"Garamond\"> Check JMS Resource Reference : Got Queue Connection ->"+qCon+" </font><br>");
	        
	        Queue q = (Queue) ctx.lookup("java:comp/env/jms/SendReceiveQueue");
	        out.println("<font align=Center face=\"Garamond\"> Check JMS Resource Env Reference : Got Queue ->"+q.getQueueName()+" </font><br>");
	        
	        HelloWorldService hello = (HelloWorldService) ctx.lookup("java:comp/env/service/HelloWorldService");
	        HelloWorld port = hello.getHelloWorld();
	        out.println("<font align=Center face=\"Garamond\"> Check Service Reference : Called Web Service ->"+port.getHelloWorld("Test")+" </font><br>");
	     
			TestLocalHome resultLocal = (TestLocalHome)ctx.lookup("java:comp/env/ejb/TestLocalBean");
			TestLocal testLocal = resultLocal.create();
			String echoLocal = testLocal.echoLocal("Test");
			out.println("<font align=Center face=\"Garamond\"> Check EJB Local Reference : Call to bean method returned ->"+echoLocal+" </font><br>");
	        
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
		out.println("</body></html>");

	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
	}   	  	    
}