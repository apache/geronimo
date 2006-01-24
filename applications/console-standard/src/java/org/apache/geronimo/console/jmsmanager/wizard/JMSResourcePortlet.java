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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;

/**
 * A portlet that lets you configure and deploy JDBC connection pools.
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public class JMSResourcePortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(JMSResourcePortlet.class);
    private static final String MODE_KEY = "mode";
    private Map helpers = new HashMap();

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
    }

    public void destroy() {
        for (Iterator it = helpers.values().iterator(); it.hasNext();) {
            AbstractHandler handler = (AbstractHandler) it.next();
            handler.destroy();
        }
        helpers.clear();
        super.destroy();
    }

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter(MODE_KEY);
        AbstractHandler.JMSResourceData data = new AbstractHandler.JMSResourceData(actionRequest);
        while(true) {
            if(mode == null) {
                break;
            }
            int pos = mode.lastIndexOf('-');
            if(pos == -1) { // Assume it's a render request
                break;
            } else {
                String type = mode.substring(pos+1);
                mode = mode.substring(0, pos);
                AbstractHandler handler = (AbstractHandler) helpers.get(mode);
                if(handler == null) {
                    log.error("No handler for action mode '"+mode+"'");
                    break;
                }
System.out.println("Using action handler '"+handler.getClass().getName()+"'");
                if(type.equals("before")) {
                    mode = handler.actionBeforeView(actionRequest, actionResponse, data);
                } else if(type.equals("after")) {
                    mode = handler.actionAfterView(actionRequest, actionResponse, data);
                } else {
                    log.error("Unrecognized portlet action '"+mode+"'");
                    mode = null;
                }
            }
        }
        if(mode != null) {
            actionResponse.setRenderParameter(MODE_KEY, mode);
        }
        data.save(actionResponse);
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String mode = renderRequest.getParameter(MODE_KEY);
        AbstractHandler.JMSResourceData data = new AbstractHandler.JMSResourceData(renderRequest);
        if(mode == null || mode.equals("")) {
            mode = "list";
        }
        AbstractHandler handler = (AbstractHandler) helpers.get(mode);
        try {
            if(handler == null) {
                log.error("No handler for render mode '"+mode+"'");
            } else {
System.out.println("Using render handler '"+handler.getClass().getName()+"'");
                handler.renderView(renderRequest, renderResponse, data);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
        renderRequest.setAttribute("data", data);
        if(handler != null) {
            handler.getView().include(renderRequest, renderResponse);
        }
    }

    private void addHelper(AbstractHandler handler, PortletConfig config) throws PortletException {
        handler.init(config);
        helpers.put(handler.getMode(), handler);
    }
}
