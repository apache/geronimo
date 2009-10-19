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

package org.apache.geronimo.connector.wrapper.work;

import java.util.Collection;
import java.util.concurrent.Executor;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.connector.work.WorkContextHandler;

/**
 * 
 * @version $Revision$
 */
@GBean(j2eeType = NameFactory.JCA_WORK_MANAGER)
public class GeronimoWorkManagerGBean extends GeronimoWorkManager implements GBeanLifecycle {

    public GeronimoWorkManagerGBean() {
    }

    public GeronimoWorkManagerGBean(@ParamReference(name="SyncPool") Executor sync,
                                    @ParamReference(name="StartPool")Executor start,
                                    @ParamReference(name="ScheduledPool") Executor sched,
                                    @ParamReference(name="WorkContextHandler")Collection<WorkContextHandler> workContextHandlers) {
        super(sync, start, sched, workContextHandlers);
    }

}
