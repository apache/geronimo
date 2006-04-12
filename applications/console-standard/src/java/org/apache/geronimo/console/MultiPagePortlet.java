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
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

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
import java.util.List;
import java.io.IOException;

/**
 * A base class for porlets consisting on multiple JSPs with before and after
 * actions (e.g. for load and validation/save) and the ability for an "after"
 * action to set the next page to load.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
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
        String mode = null;
        Map files = null;
        Map fields = null;
        if(actionRequest.getContentType() != null && actionRequest.getContentType().startsWith("multipart/form-data")) {
            files = new HashMap();
            fields = new HashMap();
            PortletFileUpload request = new PortletFileUpload(new DiskFileItemFactory());
            try {
                List items = request.parseRequest(actionRequest);
                for (int i = 0; i < items.size(); i++) {
                    FileItem item = (FileItem) items.get(i);
                    if(item.isFormField()) {
                        if(item.getFieldName().equals(MODE_KEY)) {
                            mode = item.getString();
                        }
                        fields.put(item.getFieldName(), item.getString());
                    } else {
                        files.put(item.getFieldName(), item);
                    }
                }
            } catch (FileUploadException e) {
                log.error("Unable to process form including a file upload", e);
            }
        } else {
            mode = actionRequest.getParameter(MODE_KEY);
        }
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
                if(files == null) {
                    handler.getUploadFields().clear();
                    handler.getUploadFiles().clear();
                } else {
                    handler.getUploadFields().putAll(fields);
                    handler.getUploadFiles().putAll(files);
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
        renderRequest.setAttribute(getModelJSPVariableName(), model);
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

    protected abstract String getModelJSPVariableName();

    protected abstract MultiPageModel getModel(PortletRequest request);
}
