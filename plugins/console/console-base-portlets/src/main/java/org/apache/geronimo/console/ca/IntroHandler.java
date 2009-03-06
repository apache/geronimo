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
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.CertificationAuthority;

/**
 * Handler for the CA home screen.
 *
 * @version $Rev$ $Date$
 */
public class IntroHandler extends BaseCAHandler {
    private static final Logger log = LoggerFactory.getLogger(IntroHandler.class);
    
    public IntroHandler(BasePortlet portlet) {
        super(INDEX_MODE, "/WEB-INF/view/ca/index.jsp", portlet);
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {        
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
                portlet.addWarningMessage(request, portlet.getLocalizedString(request, "consolebase.warnMsg05"));
            } else {
                ca.lock();
                log.info("CA is now locked.");
                portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg12"));
            }
        } else if(request.getParameter("publish") != null) {
            CertificationAuthority ca = getCertificationAuthority(request);
            try {
                getCertificateStore(request).storeCACertificate(ca.getCertificate());
                portlet.addInfoMessage(request, portlet.getLocalizedString(request, "consolebase.infoMsg13"));
            } catch (Exception e) {
                log.error("Error while publishing CA's certificate to Certificate Store", e);
                portlet.addErrorMessage(request, portlet.getLocalizedString(request, "consolebase.errorMsg13"), e.getMessage());
            }
        }
        return getMode()+BEFORE_ACTION;
    }
}
