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

package org.apache.geronimo.console.jmsmanager.handlers;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.jmsmanager.AbstractJMSManager;

public class StatisticsHandler extends AbstractJMSManager implements
        PortletResponseHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public void processAction(ActionRequest request, ActionResponse response)
            throws IOException, PortletException {
        String destinationName = request.getParameter(DESTINATION_NAME);
        String destinationType = request.getParameter(DESTINATION_TYPE);

        response.setRenderParameter("destinationName", destinationName);
        response.setRenderParameter("destinationType", destinationType);
        response.setRenderParameter("processAction", "statistics");
    }

}