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
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JMSQueueSender extends HttpServlet implements Servlet {

    @Resource(name="MSConnectionFactory")
    QueueConnectionFactory qcf = null;
    @Resource(name="TestQueue")
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
            QueueSession sess = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueSender queueSender = sess.createSender(queue);
            TextMessage tmsg = sess.createTextMessage("JMS - Test Queue Message");
            queueSender.send(tmsg);
            queueSender.close();
            sess.close();
            connection.stop();
            out.println("<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en' lang='en'>");
            out.println("<head><title>JMS Sender</title></head>");
            out.println("<body>Sent JMS Queue Message</body></html>");
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

    }

}