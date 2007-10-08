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
package org.apache.geronimo.console.keystores;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.management.geronimo.KeystoreException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * Handler for deleting a trusted certificate or private key from keystore
 *
 * @version $Rev$ $Date$
 */
public class DeleteEntryHandler extends BaseKeystoreHandler {
    public DeleteEntryHandler() {
        super(DELETE_ENTRY, "/WEB-INF/view/keystore/viewKeystore.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        String id = request.getParameter("id");
        String alias = request.getParameter("alias");
        if(id != null) {
            response.setRenderParameter("id", id);
            if(alias != null) {
            	KeystoreData data = (KeystoreData) request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + id);
            	try {
                    data.deleteEntry(alias);
                } catch (KeystoreException e) {
                    throw new PortletException(e);
                }
            }
        } // else we hope this is after a failure and the actionAfterView took care of it below!
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        String id = request.getParameter("id");
        request.setAttribute("id", id);
        request.setAttribute("keystore", request.getPortletSession(true).getAttribute(KEYSTORE_DATA_PREFIX + id));
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
    	String id = request.getParameter("id");
    	response.setRenderParameter("id", id);
        return VIEW_KEYSTORE+BEFORE_ACTION;
    }
}
