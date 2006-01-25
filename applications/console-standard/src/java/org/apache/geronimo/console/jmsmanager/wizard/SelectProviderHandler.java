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
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.net.URI;
import java.net.URISyntaxException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.console.util.PortletManager;

/**
 * Handler for the screen where you select a JMS provider (because
 * you didn't want one of the ones we know about).
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public class SelectProviderHandler extends AbstractHandler {
    private final static String[] SKIP_RARS_CONTAINING = new String[]{"tranql"};

    public SelectProviderHandler() {
        super(SELECT_PROVIDER_MODE, "/WEB-INF/view/jmswizard/provider.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, JMSResourceData data) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, JMSResourceData data) throws PortletException, IOException {
        loadRARList(request);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, JMSResourceData data) throws PortletException, IOException {
        String rar = request.getParameter(RAR_FILE_PARAMETER);
        if(isEmpty(rar)) {
            return SELECT_PROVIDER_MODE+BEFORE_ACTION;
        }
        data.setRarURI(rar);
        return CONFIGURE_RA_MODE+BEFORE_ACTION;
    }

    private void loadRARList(RenderRequest renderRequest) {
        // List the available RARs
        List list = new ArrayList();
        ListableRepository[] repos = PortletManager.getListableRepositories(renderRequest);
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];
            try {
                final URI[] uris = repo.listURIs();
                outer:
                for (int j = 0; j < uris.length; j++) {
                    if(uris[j] == null) {
                        continue; // probably a JAR lacks a version number in the name, etc.
                    }
                    String test = uris[j].toString();
                    if(!test.endsWith("/rar")) { //todo: may need to change this logic if configId format changes
                        continue;
                    }
                    for (int k = 0; k < SKIP_RARS_CONTAINING.length; k++) {
                        String skip = SKIP_RARS_CONTAINING[k];
                        if(test.indexOf(skip) > -1) {
                            continue outer;
                        }
                    }
                    list.add(test);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(list);
        renderRequest.setAttribute("rars", list);
    }
}
