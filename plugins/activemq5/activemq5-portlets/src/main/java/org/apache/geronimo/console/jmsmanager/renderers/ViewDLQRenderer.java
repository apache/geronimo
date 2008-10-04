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

package org.apache.geronimo.console.jmsmanager.renderers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.management.ObjectName;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

//import org.activemq.service.DeadLetterPolicy;
import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewDLQRenderer extends AbstractJMSManager implements PortletRenderer {

    private static final Logger log = LoggerFactory.getLogger(ViewDLQRenderer.class);

    private Destination dlq = null;

    private QueueBrowser dlqBrowser = null;

    private Connection connection = null;

    private Session session = null;

    private String dlqName;

    public ViewDLQRenderer() {
    }

    public void setup(RenderRequest request, RenderResponse response) {
    	/*
        String destinationApplicationName = request
                .getParameter("destinationApplicationName");
        String destinationModuleName = request
                .getParameter("destinationModuleName");
        String destinationName = request.getParameter("destinationName");

        try {
            //TODO configid disabled
            AbstractName adminObjectName = null;//NameFactory.getComponentName(null,
//                    null, destinationApplicationName, NameFactory.JCA_RESOURCE,
//                    destinationModuleName, destinationName, null, baseContext);
            Destination destination = (Destination) kernel.invoke(adminObjectName,
                    "$getResource");
            ConnectionFactory connectionFactory = (ConnectionFactory) kernel
                    .invoke(JCA_MANAGED_CONNECTION_FACTORY_NAME,
                            "$getResource");
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            DeadLetterPolicy dlp = new DeadLetterPolicy();

            //dlqName =
            // dlp.getDeadLetterNameFromDestination((ActiveMQDestination)
            // destination);
            // This is a hack to get around the fact that the code commented
            // above throws a ClassCastException due to ClassLoader weirdness.
            Field f = dlp.getClass().getDeclaredField(
                    "deadLetterPerDestinationName");
            f.setAccessible(true);
            boolean deadLetterPerDestinationName = f.getBoolean(dlp);
            f = dlp.getClass().getDeclaredField("deadLetterPrefix");
            f.setAccessible(true);
            String deadLetterPrefix = "" + f.get(dlp);
            if (deadLetterPerDestinationName) {
                dlqName = deadLetterPrefix
                        + destination.getClass().getMethod("getPhysicalName",
                                null).invoke(destination, null);
            } else {
                dlqName = deadLetterPrefix + deadLetterPrefix;
            }

            dlq = session.createQueue(dlqName);
            dlqBrowser = session.createBrowser((Queue) dlq);

            connection.start();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        */
    }

    public List getDLQContents(QueueBrowser qb) {

        List list = new ArrayList();

        try {
            for (Enumeration e = qb.getEnumeration(); e.hasMoreElements();) {
                Object o = e.nextElement();
                list.add(o);
            }

            connection.stop();
            dlqBrowser.close();
            session.close();
            connection.close();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return list;
    }

    public String render(RenderRequest request, RenderResponse response)
            throws PortletException, IOException {

        setup(request, response);
        List dlqContents = getDLQContents(dlqBrowser);
        request.setAttribute("dlqcontents", dlqContents);
        request.setAttribute("dlqname", dlqName);

        return "/WEB-INF/view/jmsmanager/viewDLQ.jsp";
    }

}
