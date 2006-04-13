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
package org.apache.geronimo.console;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletConfig;
import javax.portlet.PortletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;

/**
 * A base class for porlets consisting on multiple JSPs with before and after
 * actions (e.g. for load and validation/save) and the ability for an "after"
 * action to set the next page to load.
 *
 * @version $Rev$ $Date$
 */
public abstract class MultiPagePortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(MultiPagePortlet.class);
    protected static final String MODE_KEY = "mode";
    protected Map helpers = new HashMap();

    public void destroy() {
        for (Iterator it = helpers.values().iterator(); it.hasNext();) {
            MultiPageAbstractHandler handler = (MultiPageAbstractHandler) it.next();
            handler.destroy();
        }
        helpers.clear();
        super.destroy();
    }

    public void processAction(ActionRequest actionRequest,
                              ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter(MODE_KEY);
        MultiPageModel model = getModel(actionRequest);
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
                MultiPageAbstractHandler handler = (MultiPageAbstractHandler) helpers.get(mode);
                if(handler == null) {
                    log.error("No handler for action mode '"+mode+"'");
                    break;
                }
                log.debug("Using action handler '"+handler.getClass().getName()+"'");
                if(type.equals("before")) {
                    mode = handler.actionBeforeView(actionRequest, actionResponse, model);
                } else if(type.equals("after")) {
                    mode = handler.actionAfterView(actionRequest, actionResponse, model);
                } else {
                    log.error("Unrecognized portlet action '"+mode+"'");
                    mode = null;
                }
            }
        }
        if(mode != null) {
            actionResponse.setRenderParameter(MODE_KEY, mode);
        }
        model.save(actionResponse);
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        String mode = renderRequest.getParameter(MODE_KEY);
        MultiPageModel model = getModel(renderRequest);
        if(mode == null || mode.equals("")) {
            mode = getDefaultMode();
        }
        MultiPageAbstractHandler handler = (MultiPageAbstractHandler) helpers.get(mode);
        try {
            if(handler == null) {
                log.error("No handler for render mode '"+mode+"'");
            } else {
                log.debug("Using render handler '"+handler.getClass().getName()+"'");
                handler.renderView(renderRequest, renderResponse, model);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
        renderRequest.setAttribute("data", model);
        if(handler != null) {
            handler.getView().include(renderRequest, renderResponse);
        }
    }

    protected void addHelper(MultiPageAbstractHandler handler, PortletConfig config) throws PortletException {
        handler.init(config);
        helpers.put(handler.getMode(), handler);
    }

    protected String getDefaultMode() {
        if(helpers.containsKey("list")) return "list";
        if(helpers.containsKey("index")) return "index";
        return null;
    }

    protected abstract MultiPageModel getModel(PortletRequest request);
}
