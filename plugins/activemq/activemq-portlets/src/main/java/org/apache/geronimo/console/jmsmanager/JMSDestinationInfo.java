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

package org.apache.geronimo.console.jmsmanager;

import java.net.URI;

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public class JMSDestinationInfo {

    private AbstractName resourceAdapterModuleAbName;

    private String physicalName;

    private DestinationType type;

    public String getPhysicalName() {
        return physicalName;
    }

    public AbstractName getResourceAdapterModuleAbName() {
        return resourceAdapterModuleAbName;
    }

    public DestinationType getType() {
        return type;
    }

    public void setResourceAdapterModuleAbName(String resourceAdapterModuleName) {
        this.resourceAdapterModuleAbName = new AbstractName(URI.create(resourceAdapterModuleName));
    }

    public void setPhysicalName(String physicalName) {
        this.physicalName = physicalName;
    }

    public void setResourceAdapterModuleAbName(AbstractName resourceAdapterModuleAbName) {
        this.resourceAdapterModuleAbName = resourceAdapterModuleAbName;
    }

    public void setType(DestinationType type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = DestinationType.valueOf(type);
    }

    public static JMSDestinationInfo create(PortletRequest actionRequest) {
        JMSDestinationInfo jmsDestinationInfo = new JMSDestinationInfo();
        jmsDestinationInfo.setPhysicalName(actionRequest.getParameter("physicalName"));
        jmsDestinationInfo.setType(DestinationType.valueOf(actionRequest.getParameter("adminObjType")));
        jmsDestinationInfo.setResourceAdapterModuleAbName(actionRequest.getParameter("resourceAdapterModuleName"));
        return jmsDestinationInfo;
    }

    public void setRenderParameters(ActionResponse actionResponse) {
        actionResponse.setRenderParameter("physicalName", physicalName);
        actionResponse.setRenderParameter("adminObjType", type.name());
        actionResponse.setRenderParameter("resourceAdapterModuleName", resourceAdapterModuleAbName.toString());
    }
}
