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
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;


/**
 * @version $Revision: 1.2 $ $Date: 2004/07/08 22:07:54 $
 */
public class ThreadPool implements Executor, ExecutorFactory, GBeanLifecycle {

    static private final Log log = LogFactory.getLog(ThreadPool.class);

    private PooledExecutor executor;
    private long keepAliveTime;
    private int poolSize;
    private int maximumPoolSize;
    private String poolName;

    private int nextWorkerID = 0;

    public Executor getExecutor() {
        return new ExecutorWrapper(executor);
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getPoolName() {
        return poolName;
    }

    public void execute(Runnable command) throws InterruptedException {
        executor.execute(command);
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    private int getNextWorkerID() {
        return nextWorkerID++;
    }

    public void doStart() throws WaitingException, Exception {
        PooledExecutor p = new PooledExecutor(new LinkedQueue(), poolSize);
        p.setKeepAliveTime(keepAliveTime);
        //I think this does nothing with a LinkedQueue present
        p.setMaximumPoolSize(poolSize);
        p.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable arg0) {
                return new Thread(arg0, poolName + " " + getNextWorkerID());
            }
        });
        //I think this does nothing with a LinkedQueue present
        p.waitWhenBlocked();

        executor = p;

        log.info("Thread pool " + poolName + " started");
    }

    public void doStop() throws WaitingException, Exception {
        executor.shutdownNow();
        log.info("Thread pool " + poolName + " stopped");
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            log.error("Failed to shutdown", e);
        }
    }

    private static class ExecutorWrapper implements Executor {
        private final Executor delegate;

        public ExecutorWrapper(Executor delegate) {
            this.delegate = delegate;
        }

        public void execute(Runnable command) throws InterruptedException {
            delegate.execute(command);
        }
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ThreadPool.class);

        infoFactory.addAttribute("keepAliveTime", long.class, true);
        infoFactory.addAttribute("poolSize", int.class, true);
        infoFactory.addAttribute("poolName", String.class, true);
        infoFactory.addOperation("getExecutor");

        infoFactory.addInterface(Executor.class);
        infoFactory.addInterface(ExecutorFactory.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
