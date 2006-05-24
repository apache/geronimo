/**
 * Copyright 2006 The Apache Software Foundation
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
package org.apache.geronimo.console.threads;

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;

/**
 * Base class for portlet helpers
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public abstract class AbstractThreadHandler extends MultiPageAbstractHandler {
    private final static Log log = LogFactory.getLog(AbstractThreadHandler.class);
    protected final static String ABSTRACT_NAME_PARAMETER = "abstractName";

    protected final static String LIST_MODE="list";
    protected final static String MONITOR_MODE="monitor";

    public AbstractThreadHandler(String mode, String viewName) {
        super(mode, viewName);
    }

    public static class ThreadPoolData implements MultiPageModel {
        // Used for editing an existing thread pool
        private String abstractName;

        public ThreadPoolData(PortletRequest request) {
            abstractName = request.getParameter(AbstractThreadHandler.ABSTRACT_NAME_PARAMETER);
        }

        public void save(ActionResponse response, PortletSession session) {
            if(!isEmpty(abstractName)) response.setRenderParameter(AbstractThreadHandler.ABSTRACT_NAME_PARAMETER, abstractName);
        }
    }
}
