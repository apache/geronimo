/**
 *
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
package org.apache.geronimo.console.ca;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.CertificationAuthority;

/**
 * Handler for unlock CA screen.
 *
 * @version $Rev$ $Date$
 */
public class UnlockCAHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(UnlockCAHandler.class);
    
    public UnlockCAHandler() {
        super(UNLOCKCA_MODE, "/WEB-INF/view/ca/unlockCA.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            Object value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String errorMsg = null;
        try {
            String password = request.getParameter("password");
            if(password == null) {
                throw new Exception("Password is null.");
            }
            CertificationAuthority ca = getCertificationAuthority(request);
            if(ca == null) {
                throw new Exception("CA is not running.  CA may not have been initialized.");
            }
            ca.unlock(password.toCharArray());

            // Return to CA's index page
            response.setRenderParameter(INFO_MSG, "CA has been unlocked successfully!");
            log.info("CA has been unlocked successfully!");
            return INDEX_MODE+BEFORE_ACTION;
        } catch(Exception e) {
            errorMsg = e.toString();
            log.error("Errors in unlocking CA.", e);
        }
        // An error occurred.  Set the error message and load the page again.
        response.setRenderParameter(ERROR_MSG, errorMsg);
        return getMode()+BEFORE_ACTION;
    }
}
