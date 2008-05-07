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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.configcreator.configData.WARConfigData;

/**
 * A handler for ...
 * 
 * @version $Rev$ $Date$
 */
public class DependenciesHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(DependenciesHandler.class);

    public DependenciesHandler() {
        super(DEPENDENCIES_MODE, "/WEB-INF/view/configcreator/dependencies.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getWARSessionData(request);
        data.consolidateDependencies();
        request.setAttribute(DATA_PARAMETER, data);
        List<String> commonLibs = JSR77_Util.getCommonLibs(request);
        List<String> addedDependencies = data.getEnvironmentConfig().getDependencies();
        //addedDependencies will be a subset of commonLibs
        //sort commonLibs so that addedDependencies show up towards the beginning
        commonLibs.removeAll(addedDependencies);
        Collections.sort(commonLibs);
        Collections.sort(addedDependencies);
        commonLibs.addAll(0, addedDependencies);
        request.setAttribute(COMMON_LIBS_PARAMETER, commonLibs);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model)
            throws PortletException, IOException {
        WARConfigData data = getWARSessionData(request);
        data.getEnvironmentConfig().getDependenciesSet().clear();
        String[] selectedJars = request.getParameterValues(SELECTED_LIBS_PARAMETER);
        for (int i = 0; selectedJars != null && i < selectedJars.length; i++) {
            data.getEnvironmentConfig().getDependenciesSet().add(selectedJars[i]);
        }
        return DISPLAY_PLAN_MODE + "-before";
    }
}
