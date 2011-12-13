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

package org.apache.geronimo.openwebbeans;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.openejb.cdi.ThreadSingletonServiceAdapter;
import org.apache.openejb.cdi.ThreadSingletonService;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Rev$ $Date$
 */
@GBean(name = "OpenWebBeansSystemInitializer")
public class OpenWebBeansSystemInitializer implements GBeanLifecycle {

    @Override
    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    @Override
    public void doStart() throws Exception {
        ThreadSingletonServiceAdapter adapter = getThreadSingletonServiceAdapter();
        if (adapter != null) {
            adapter.getGeronimoSingletonService().setWebApplicationSingletonService(new WebApplicationNameBasedSingletonService());
        }
    }

    @Override
    public void doStop() throws Exception {
        ThreadSingletonServiceAdapter adapter = getThreadSingletonServiceAdapter();
        if (adapter != null) {
            adapter.getGeronimoSingletonService().setWebApplicationSingletonService(null);
        }
    }

    private ThreadSingletonServiceAdapter getThreadSingletonServiceAdapter() {
        ThreadSingletonService threadSingletonService = SystemInstance.get().getComponent(ThreadSingletonService.class);
        return (threadSingletonService instanceof ThreadSingletonServiceAdapter) ? (ThreadSingletonServiceAdapter) threadSingletonService : null;
    }
}
