/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.connector.work.pool;

import javax.resource.spi.work.WorkException;
import EDU.oswego.cs.dl.util.concurrent.Latch;
import org.apache.geronimo.connector.work.WorkerContext;
import org.apache.geronimo.pool.GeronimoExecutor;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class StartWorkExecutor implements WorkExecutor {

    public void doExecute(WorkerContext work, GeronimoExecutor executor)
            throws WorkException, InterruptedException {
        Latch latch = work.provideStartLatch();
        executor.execute("A J2EE Connector", work);
        latch.acquire();
    }
}
