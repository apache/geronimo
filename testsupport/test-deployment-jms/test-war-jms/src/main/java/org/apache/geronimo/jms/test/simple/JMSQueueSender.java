package org.apache.geronimo.jms.test.simple;

import java.io.IOException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class JMSQueueSender extends HttpServlet implements Servlet {

    Context initialContext = null;
    QueueConnectionFactory qcf = null;
    Queue queue = null;

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public JMSQueueSender() {
        super();
    }

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest arg0, HttpServletResponse arg1)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request,response);
    }

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest arg0, HttpServletResponse arg1)
     */
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        try {

            PrintWriter out = arg1.getWriter();
            QueueConnection connection = qcf.createQueueConnection();
            connection.start();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender queueSender = session.createSender(queue);
            TextMessage tmsg = session.createTextMessage("JMS - Test Queue Message");
            queueSender.send(tmsg);
            queueSender.close();
            session.close();
            connection.stop();
            out.println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
            out.println("<head><title>JMS Sender</title></head>");
            out.println("<body>Sent JMS Queue Message</body></html>");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    /* (non-Java-doc)
     * @see javax.servlet.Servlet#init(ServletConfig arg0)
     */
    public void init(ServletConfig arg0) throws ServletException {

        try {
            initialContext = new InitialContext();
            qcf  = (QueueConnectionFactory) initialContext.lookup("java:comp/env/jms/QCF");
            queue = (Queue) initialContext.lookup("java:comp/env/jms/TestQ");
        }
        catch ( NamingException e ) {
            e.printStackTrace();
        }

    }

}