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

package org.apache.geronimo.axis2;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.geronimo.webservices.WebServiceContainer;


public class Axis2WebServiceContainer implements WebServiceContainer {

    public static final String REQUEST = Axis2WebServiceContainer.class.getName() + "@Request";
    public static final String RESPONSE = Axis2WebServiceContainer.class.getName() + "@Response";

    private transient final ClassLoader classLoader;
    private final String endpointClassName;
    private final PortInfo portInfo;

    public Axis2WebServiceContainer(PortInfo portInfo, String endpointClassName, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.endpointClassName = endpointClassName;
        this.portInfo = portInfo;
    }

    public void getWsdl(Request request, Response response) throws Exception {
        //TODO: Implement the logic
    }

    public void invoke(Request req, Response res) throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.createEmptyConfigurationContext();

        //TODO: Change the Message Reciever to JAXWSMessageReciever
        AxisService service = AxisService.createService(endpointClassName, configContext.getAxisConfiguration(), RPCMessageReceiver.class);
        configContext.getAxisConfiguration().addService(service);

        // TODO: Lot's more to be done here.
    }
}
