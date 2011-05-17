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

package org.apache.geronimo.axis2.feature;

import javax.xml.ws.soap.AddressingFeature.Responses;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.server.config.AddressingConfigurator;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Utils;
import org.apache.geronimo.jaxws.feature.AddressingFeatureInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class AddressingFeatureConfigurator implements WebServiceFeatureConfigurator<AddressingFeatureInfo> {

    private static Logger logger = LoggerFactory.getLogger(AddressingConfigurator.class);

    @Override
    public void configure(EndpointDescription endpointDescription, AddressingFeatureInfo webServiceFeatureInfo) {

        if (webServiceFeatureInfo == null) {
            return;
        }

        Parameter disabled = new Parameter(AddressingConstants.DISABLE_ADDRESSING_FOR_IN_MESSAGES, String.valueOf(!webServiceFeatureInfo.isEnabled()));
        Parameter required = new Parameter(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER, webServiceFeatureInfo.isRequired() ? AddressingConstants.ADDRESSING_REQUIRED
                : AddressingConstants.ADDRESSING_UNSPECIFIED);
        Parameter responses = new Parameter(AddressingConstants.WSAM_INVOCATION_PATTERN_PARAMETER_NAME, mapResponseAttributeToAddressing(webServiceFeatureInfo.getResponses()));

        try {
            AxisService service = endpointDescription.getAxisService();

            service.addParameter(disabled);
            service.addParameter(required);
            service.addParameter(responses);

            String value = Utils.getParameterValue(disabled);
            if (JavaUtils.isFalseExplicitly(value)) {
                ServiceDescription sd = endpointDescription.getServiceDescription();
                AxisConfiguration axisConfig = sd.getAxisConfigContext().getAxisConfiguration();
                if (!axisConfig.isEngaged(Constants.MODULE_ADDRESSING))
                    axisConfig.engageModule(Constants.MODULE_ADDRESSING);
            }
        } catch (Exception e) {
            logger.error("Fail to configure addressing info ", e);
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("AddressingEngagementError", e.toString()));
        }
    }

    private String mapResponseAttributeToAddressing(Responses responses) {
        String addressingType = null;
        switch (responses) {
        case ALL:
            addressingType = AddressingConstants.WSAM_INVOCATION_PATTERN_BOTH;
            break;
        case ANONYMOUS:
            addressingType = AddressingConstants.WSAM_INVOCATION_PATTERN_SYNCHRONOUS;
            break;
        case NON_ANONYMOUS:
            addressingType = AddressingConstants.WSAM_INVOCATION_PATTERN_ASYNCHRONOUS;
            break;
        }
        return addressingType;
    }
}
