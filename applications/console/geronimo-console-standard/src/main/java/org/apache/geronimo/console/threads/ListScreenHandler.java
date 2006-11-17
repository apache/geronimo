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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.threads.ThreadPool;

/**
 * A handles for the front page that lists available thread pools.
 *
 * @version $Rev$ $Date$
 */
public class ListScreenHandler extends AbstractThreadHandler {
    public ListScreenHandler() {
        super(LIST_MODE, "/WEB-INF/view/threads/list.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        populateExistingList(request);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    private void populateExistingList(PortletRequest renderRequest) {
        ThreadPool[] pools = PortletManager.getCurrentServer(renderRequest).getThreadPools();
        ThreadPoolSummary[] result = new ThreadPoolSummary[pools.length];
        for (int i = 0; i < pools.length; i++) {
            result[i] = new ThreadPoolSummary(PortletManager.getNameFor(renderRequest, pools[i]), pools[i].getPoolSize());
        }
        Arrays.sort(result);
        renderRequest.setAttribute("pools", result);
    }

    public static class ThreadPoolSummary implements Serializable, Comparable {
        private static final long serialVersionUID = -7515061254194067140L;
        private final String abstractName;
        private final int maxSize;
        private final String name;

        public ThreadPoolSummary(AbstractName abstractName, int maxSize) {
            this.abstractName = abstractName.toString();
            name = (String) abstractName.getName().get(NameFactory.J2EE_NAME);
            this.maxSize = maxSize;
        }

        public String getAbstractName() {
            return abstractName;
        }

        public int getPoolSize() {
            return maxSize;
        }

        public String getName() {
            return name;
        }

        public int compareTo(Object o) {
            ThreadPoolSummary other = (ThreadPoolSummary) o;
            return name.compareTo(other.name);
        }
    }
}
