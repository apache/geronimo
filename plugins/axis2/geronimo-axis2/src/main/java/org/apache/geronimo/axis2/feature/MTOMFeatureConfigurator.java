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

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.geronimo.jaxws.feature.MTOMFeatureInfo;

/**
 * @version $Rev$ $Date$
 */
public class MTOMFeatureConfigurator implements WebServiceFeatureConfigurator<MTOMFeatureInfo> {

    @Override
    public void configure(EndpointDescription endpointDescription, MTOMFeatureInfo webServiceFeatureInfo) {
        AxisService service = endpointDescription.getAxisService();

        Parameter enableMTOM = new Parameter(Constants.Configuration.ENABLE_MTOM, webServiceFeatureInfo.isEnabled());
        Parameter threshold = new Parameter(Constants.Configuration.MTOM_THRESHOLD, webServiceFeatureInfo.getThreshold());

        try {
            service.addParameter(enableMTOM);
            service.addParameter(threshold);
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("mtomEnableErr"), e);
        }
    }

}
