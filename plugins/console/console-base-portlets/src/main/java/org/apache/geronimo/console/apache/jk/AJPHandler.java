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
package org.apache.geronimo.console.apache.jk;

import java.io.IOException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.NetworkConnector;
import org.apache.geronimo.management.geronimo.WebManager;

/**
 * ReplaceMe
 *
 * @version $Rev$ $Date$
 */
public class AJPHandler extends BaseApacheHandler {
    private static final Logger log = LoggerFactory.getLogger(AJPHandler.class);

    public AJPHandler() {
        super(AJP_MODE, "/WEB-INF/view/apache/jk/ajpPort.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        WebManager[] managers = PortletManager.getWebManagers(request);
        // See if any AJP listeners are defined
        for (int i = 0; i < managers.length; i++) {
            WebManager manager = managers[i];
            NetworkConnector[] connectors = manager.getConnectors(WebManager.PROTOCOL_AJP);
            if(connectors.length > 0 && connectors[0]!=null) {
                log.warn("Found AJP listener on port "+connectors[0].getPort());
                return BASIC_CONFIG_MODE+BEFORE_ACTION;
            }
        }
        // Find an unused port
        int port = 8009;
        outer:
        while(true) {
            for (int i = 0; i < managers.length; i++) {
                WebManager manager = managers[i];
                NetworkConnector[] cons = manager.getConnectors();
                for (int j = 0; j < cons.length; j++) {
                    NetworkConnector con = cons[j];
                    if(con.getPort() == port) {
                        port += 10;
                        continue outer;
                    }
                }
            }
            break;
        }

        ((ApacheModel)model).setAddAjpPort(Integer.valueOf(port));

        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel amodel) throws PortletException, IOException {
        ApacheModel model = (ApacheModel) amodel;
        // Make sure port is set and unused
        if(model.getAddAjpPort() == null) {
            return getMode()+BEFORE_ACTION; //todo: some sort of error message
        }
        WebManager[] managers = PortletManager.getWebManagers(request);
        for (int i = 0; i < managers.length; i++) {
            WebManager manager = managers[i];
            NetworkConnector[] cons =  manager.getConnectors();
            for (int j = 0; j < cons.length; j++) {
                NetworkConnector con = cons[j];
                if(con.getPort() == model.getAddAjpPort().intValue()) {
                    return getMode()+BEFORE_ACTION; //todo: some sort of error message
                }
            }
        }

        return BASIC_CONFIG_MODE+BEFORE_ACTION;
    }
}
