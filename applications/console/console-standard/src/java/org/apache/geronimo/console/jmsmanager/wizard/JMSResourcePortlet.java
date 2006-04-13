/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.portlet.PortletException;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.RenderResponse;
import javax.portlet.RenderRequest;
import javax.portlet.ActionRequest;
import javax.portlet.WindowState;
import javax.portlet.PortletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPagePortlet;
import org.apache.geronimo.console.MultiPageModel;

/**
 * A portlet that lets you configure and deploy JDBC connection pools.
 *
 * @version $Rev$ $Date$
 */
public class JMSResourcePortlet extends MultiPagePortlet {
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new ListScreenHandler(), config);
        addHelper(new SelectProviderHandler(), config);
        addHelper(new ConfigureRAInstanceHandler(), config);
        addHelper(new SelectConnectionFactoryTypeHandler(), config);
        addHelper(new CreateConnectionFactoryHandler(), config);
        addHelper(new SelectDestinationTypeHandler(), config);
        addHelper(new CreateDestinationHandler(), config);
        addHelper(new ShowPlanHandler(), config);
        addHelper(new DeployHandler(), config);
        addHelper(new ReviewHandler(), config);
    }

    protected MultiPageModel getModel(PortletRequest request) {
        return new AbstractHandler.JMSResourceData(request);
    }
}
