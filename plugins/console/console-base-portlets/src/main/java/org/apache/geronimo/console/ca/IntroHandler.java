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
 * Handler for the CA home screen.
 *
 * @version $Rev$ $Date$
 */
public class IntroHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(IntroHandler.class);
    
    public IntroHandler() {
        super(INDEX_MODE, "/WEB-INF/view/ca/index.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = new String[] {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) response.setRenderParameter(params[i], value);
        }
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String[] params = {ERROR_MSG, INFO_MSG};
        for(int i = 0; i < params.length; ++i) {
            String value = request.getParameter(params[i]);
            if(value != null) request.setAttribute(params[i], value);
        }
        
        CertificationAuthority ca = getCertificationAuthority(request);
        if(ca == null) {
            // CA GBean is not running or the CA has not been initialized.
            request.setAttribute("caNotSetup", Boolean.TRUE);
        } else {
            request.setAttribute("caNotSetup", Boolean.FALSE);
            request.setAttribute("caLocked", ca.isLocked() ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        if(request.getParameter("lock") != null) {
            CertificationAuthority ca = getCertificationAuthority(request);
            if(ca == null) {
                log.warn("CA is not running or CA may not have been initialized.  Unable to lock CA.");
                response.setRenderParameter(ERROR_MSG, "CA is not running or CA may not have been initialized.  Unable to lock CA.");
            } else {
                ca.lock();
                log.info("CA is now locked.");
                response.setRenderParameter(INFO_MSG, "CA has been locked!");
            }
        } else if(request.getParameter("publish") != null) {
            CertificationAuthority ca = getCertificationAuthority(request);
            try {
                getCertificateStore(request).storeCACertificate(ca.getCertificate());
                response.setRenderParameter(INFO_MSG, "CA's certificate published to Certificate Store");
            } catch (Exception e) {
                log.error("Error while publishing CA's certificate to Certificate Store", e);
                response.setRenderParameter(ERROR_MSG, "Error while publishing CA's certificate to Certificate Store. "+e);
            }
        }
        return getMode()+BEFORE_ACTION;
    }
}
