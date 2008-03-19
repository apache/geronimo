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

import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.MessageListener;
import javax.jms.JMSException;
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

public class JMSQueueReceiver extends HttpServlet implements Servlet {


    Context initialContext = null;
    QueueConnectionFactory qcf = null;
    Queue queue = null;

    /* (non-Java-doc)
     * @see javax.servlet.http.HttpServlet#HttpServlet()
     */
    public JMSQueueReceiver() {
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
            QueueReceiver queueReceiver = session.createReceiver(queue);
            Message msg = queueReceiver.receiveNoWait();

            out.println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
            out.println("<head><title>JMS Receiver</title></head>");
            if ( msg instanceof TextMessage ) {
                TextMessage txtMsg = (TextMessage)msg;
                System.out.println("Message : "+txtMsg.getText());
                out.println("<body>Received JMS Queue Message</body></html>");
            }
            else {
                System.out.println("No Message");
                out.println("<body>Did Not Receive JMS Queue Message</body></html>");
            }

            queueReceiver.close();
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
            qcf  = (QueueConnectionFactory) initialContext.lookup("java:comp/env/jms/QCF");
            queue = (Queue) initialContext.lookup("java:comp/env/jms/TestQ");
        }
        catch ( NamingException e ) {
            e.printStackTrace();
        }
    }

}