package org.apache.geronimo.jms.test.simple;

import java.io.IOException;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSubscriber;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

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

public class JMSTopicSenderReceiver extends HttpServlet implements Servlet {


    Context initialContext = null;
    TopicConnectionFactory tcf = null;
    Topic topic = null;
    String msg = null;

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public JMSTopicSenderReceiver() {
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

            String type = arg0.getParameter("type");
            PrintWriter out = arg1.getWriter();

            TopicConnection connection = tcf.createTopicConnection();
            TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            TopicSubscriber topicSubscriber = session.createSubscriber(topic);
            TestListener test = new TestListener();
            topicSubscriber.setMessageListener(test);
            connection.start();
            TopicPublisher topicPublisher = session.createPublisher(topic);
            TextMessage tmsg = session.createTextMessage("JMS - Test Topic Message");
            topicPublisher.publish(tmsg);
            if ( msg != null ) {
                out.println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
                out.println("<head><title>JMS Topic Sender Receiver</title></head>");
                out.println("<body>Received JMS Topic Message</body></html>");
            }
            else {
                out.println("<body>Did Not Receive JMS Topic Message</body></html>");
            }

            topicSubscriber.close();
            session.close();
            connection.stop();

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
            tcf  = (TopicConnectionFactory) initialContext.lookup("java:comp/env/jms/TCF");
            topic = (Topic) initialContext.lookup("java:comp/env/jms/TestT");
        }
        catch ( NamingException e ) {
            e.printStackTrace();
        }
    }

    private class TestListener implements MessageListener {

        public void onMessage(Message message)
        {
            try {
                TextMessage textMessage = (TextMessage)message;
                msg = textMessage.getText( );
                System.out.println("Message : "+msg);

            }
            catch ( JMSException jmse ) {
                jmse.printStackTrace( );
            }
        }
    }
}

