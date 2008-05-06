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
package org.apache.geronimo.console.threads;

import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;

/**
 * Base class for portlet helpers
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractThreadHandler extends MultiPageAbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractThreadHandler.class);
    
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
