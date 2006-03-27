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
package org.apache.geronimo.console.keystores;

import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.security.keystore.KeystoreManager;
import org.apache.geronimo.security.keystore.KeystoreIsLocked;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * Handler for the keystore list screen.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ListHandler extends BaseKeystoreHandler {
    public ListHandler() {
        super(LIST_MODE, "/WEB-INF/view/keystore/index.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        KeystoreManager manager = PortletManager.getKeystoreManager(request);
        String[] names = manager.listKeystores();
        PortletSession session = request.getPortletSession(true);
        KeystoreData[] keystores = new KeystoreData[names.length];
        Map keys = new HashMap();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            KeystoreData data = (KeystoreData) session.getAttribute(KEYSTORE_DATA_PREFIX+name);
            if(data == null) {
                data = new KeystoreData();
                data.setInstance(manager.getKeystore(name));
                session.setAttribute(KEYSTORE_DATA_PREFIX+name, data);
            }
            keystores[i] = data;
            if(!data.getInstance().isKeystoreLocked()) {
                try {
                    String[] all = data.getInstance().getUnlockedKeys();
                    if(all.length > 0) {
                        keys.put(data.getInstance().getKeystoreName(), all.length+" key"+(all.length > 1 ? "s" : "")+" ready");
                    } else {
                        keys.put(data.getInstance().getKeystoreName(), "NO KEYS READY");
                    }
                } catch (KeystoreIsLocked locked) {}
            }
        }
        request.setAttribute("keystores", keystores);
        request.setAttribute("keys", keys);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode()+BEFORE_ACTION;
    }

}
