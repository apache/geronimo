/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.pool;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;

/**
 * @version $Rev$ $Date$
 */
public class ThreadPool implements Executor, GBeanLifecycle {

    private PooledExecutor executor;

    private int nextWorkerID = 0;

    public ThreadPool(final int poolSize, final String poolName, final long keepAliveTime, final ClassLoader classLoader) {
        PooledExecutor p = new PooledExecutor(poolSize);
        p.abortWhenBlocked();
        p.setKeepAliveTime(keepAliveTime);
        p.setMinimumPoolSize(poolSize);
        p.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable arg0) {
                Thread thread = new Thread(arg0, poolName + " " + getNextWorkerID());
                thread.setContextClassLoader(classLoader);
                return thread;
            }
        });

        executor = p;
    }


    public void execute(Runnable command) throws InterruptedException {
        executor.execute(command);
    }

    private synchronized int getNextWorkerID() {
        return nextWorkerID++;
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        executor.shutdownNow();
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ThreadPool.class);

        infoFactory.addAttribute("poolSize", int.class, true);
        infoFactory.addAttribute("poolName", String.class, true);
        infoFactory.addAttribute("keepAliveTime", long.class, true);

        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addInterface(Executor.class);

        infoFactory.setConstructor(new String[] {"poolSize", "poolName", "keepAliveTime", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
