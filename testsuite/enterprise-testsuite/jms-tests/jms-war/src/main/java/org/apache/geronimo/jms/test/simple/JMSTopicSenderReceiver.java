/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jms.test.simple;

import java.io.IOException;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;

import javax.jms.TextMessage;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JMSTopicSenderReceiver extends HttpServlet implements Servlet {


    @Resource(name="MSConnectionFactory")
    ConnectionFactory tcf = null;
    
    @Resource(name="TestTopic")
    Topic topic = null;

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
        PrintWriter out = arg1.getWriter();
        CountDownLatch latch = new CountDownLatch(1);
        try {

            String type = arg0.getParameter("type");

            Connection connection = tcf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer topicSubscriber = session.createConsumer(topic);
            TestListener test = new TestListener(latch);
            topicSubscriber.setMessageListener(test);
            connection.start();
            MessageProducer topicPublisher = session.createProducer(topic);
            TextMessage tmsg = session.createTextMessage("JMS - Test Topic Message");
            topicPublisher.send(tmsg);
            latch.await(1, TimeUnit.SECONDS);
            out.println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
            out.println("<head><title>JMS Topic Sender Receiver</title></head>");
            if ( test.getMsg() != null ) {
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

    private class TestListener implements MessageListener {

        private final CountDownLatch latch;
        private volatile String msg = null;

        private TestListener(CountDownLatch latch) {
            this.latch = latch;
        }

        public String getMsg() {
            return msg;
        }

        public void onMessage(Message message)
        {
            try {
                TextMessage textMessage = (TextMessage)message;
                msg = textMessage.getText( );
                latch.countDown();
                System.out.println("Message : "+msg);

            }
            catch ( JMSException jmse ) {
                jmse.printStackTrace( );
            }
        }
    }
}

