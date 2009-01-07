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
package org.apache.geronimo.console.configcreator;

import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.MultiPagePortlet;

/**
 * A portlet for Create Deployment Plan Portlet.
 * 
 * @version $Rev$ $Date$
 */
public class CreatePlanPortlet extends MultiPagePortlet {
    public void init(PortletConfig config) throws PortletException {
        super.init(config);
        addHelper(new GetArchiveHandler(this), config);
        addHelper(new EnvironmentHandler(), config);
        addHelper(new ReferencesHandler(this), config);
        addHelper(new SecurityHandler(), config);
        addHelper(new DependenciesHandler(), config);
        addHelper(new DisplayPlanHandler(), config);
        addHelper(new DeployStatusHandler(this), config);
        addHelper(new EjbHandler(), config);
        addHelper(new EARHandler(), config);
    }

    protected String getModelJSPVariableName() {
        return "model";
    }

    protected MultiPageModel getModel(PortletRequest request) {
        return new AbstractHandler.TempConfigModel(request);
    }
}
