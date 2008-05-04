/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.RequestResponseTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.webservices.WebServiceContainer.Response;

public class Axis2RequestResponseTransport implements RequestResponseTransport
{
    private static final Logger LOG = LoggerFactory.getLogger(Axis2RequestResponseTransport.class);
    
    private Response response;

    private CountDownLatch responseReadySignal = new CountDownLatch(1);

    private RequestResponseTransportStatus status = RequestResponseTransportStatus.INITIAL;

    private AxisFault faultToBeThrownOut = null;

    private boolean responseWritten;

    Axis2RequestResponseTransport(Response response) {
        this.response = response;
    }

    public void acknowledgeMessage(MessageContext msgContext) throws AxisFault {
        LOG.debug("acknowledgeMessage");
        LOG.debug("Acking one-way request");

        response.setContentType("text/xml; charset="
                                + msgContext.getProperty("message.character-set-encoding"));

        response.setStatusCode(HttpURLConnection.HTTP_ACCEPTED);
        try {
            response.flushBuffer();
        } catch (IOException e) {
            throw new AxisFault("Error sending acknowledgement", e);
        }

        signalResponseReady();
    }

    public void awaitResponse() throws InterruptedException, AxisFault {
        LOG.debug("Blocking servlet thread -- awaiting response");
        status = RequestResponseTransportStatus.WAITING;
        responseReadySignal.await();
        if (faultToBeThrownOut != null) {
            throw faultToBeThrownOut;
        }
    }

    public void signalFaultReady(AxisFault fault) {
        faultToBeThrownOut = fault;
        signalResponseReady();
    }

    public void signalResponseReady() {
        LOG.debug("Signalling response available");
        status = RequestResponseTransportStatus.SIGNALLED;
        responseReadySignal.countDown();
    }

    public RequestResponseTransportStatus getStatus() {
        return status;
    }

    public boolean isResponseWritten() {
        return responseWritten;
    }
    
    public void setResponseWritten(boolean responseWritten) {
        this.responseWritten = responseWritten;
    }
}
