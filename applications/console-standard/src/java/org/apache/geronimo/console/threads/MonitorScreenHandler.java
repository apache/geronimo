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

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.management.StatisticsProvider;
import org.apache.geronimo.management.geronimo.stats.ThreadPoolStats;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * A handles for the page that gives statistics about a particular thread pool.
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public class MonitorScreenHandler extends AbstractThreadHandler {
    public MonitorScreenHandler() {
        super(MONITOR_MODE, "/WEB-INF/view/threads/monitor.jsp");
    }

    public String actionBeforeView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        response.setRenderParameter(ABSTRACT_NAME_PARAMETER, request.getParameter(ABSTRACT_NAME_PARAMETER));
        return getMode();
    }

    public void renderView(RenderRequest request, RenderResponse response, MultiPageModel model) throws PortletException, IOException {
        AbstractName name = new AbstractName(URI.create(request.getParameter(ABSTRACT_NAME_PARAMETER)));
        StatisticsProvider pool = (StatisticsProvider) PortletManager.getManagedBean(request, name);
        ThreadPoolStats stats = (ThreadPoolStats) pool.getStats();
        String[] consumers = stats.getThreadConsumers();
        ClientStats[] result = new ClientStats[consumers.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new ClientStats(consumers[i], (int)stats.getCountForConsumer(consumers[i]).getCount());
        }
        request.setAttribute("poolName", name.getName().get(NameFactory.J2EE_NAME));
        request.setAttribute("stats", stats);
        request.setAttribute("consumers", result);
    }

    public String actionAfterView(ActionRequest request, ActionResponse response, MultiPageModel model) throws PortletException, IOException {
        return getMode();
    }

    public static class ClientStats implements Serializable, Comparable {
        private final String name;
        private final int threadCount;

        public ClientStats(String name, int threadCount) {
            this.name = name;
            this.threadCount = threadCount;
        }

        public String getName() {
            return name;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public int compareTo(Object o) {
            ClientStats other = (ClientStats) o;
            return name.compareTo(other.name);
        }
    }
}
