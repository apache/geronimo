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

package org.apache.geronimo.system;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;


/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:14 $
 */
public class ThreadPool implements GBean {

    static private final Log log = LogFactory.getLog(ThreadPool.class);

    private PooledExecutor workManager;
    private long keepAliveTime;
    private int minimumPoolSize;
    private int maximumPoolSize;
    private long maxDrainTime;
    private String poolName;

    private int nextWorkerID = 0;

    public long getMaxDrainTime() {
        return maxDrainTime;
    }

    public void setMaxDrainTime(long maxDrainTime) {
        this.maxDrainTime = maxDrainTime;
    }

    public Executor getWorkManager() {
        return workManager;
    }

    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public int getMinimumPoolSize() {
        return minimumPoolSize;
    }

    public void setMinimumPoolSize(int minimumPoolSize) {
        this.minimumPoolSize = minimumPoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    private int getNextWorkerID() {
        return nextWorkerID++;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        PooledExecutor p = new PooledExecutor();
        p.setKeepAliveTime(keepAliveTime);
        p.setMinimumPoolSize(minimumPoolSize);
        p.setMaximumPoolSize(maximumPoolSize);
        p.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable arg0) {
                return new Thread(arg0, poolName + " " + getNextWorkerID());
            }
        });

        workManager = p;

        log.info("Thread pool " + poolName + " started");
    }

    public void doStop() throws WaitingException, Exception {
        workManager.shutdownAfterProcessingCurrentlyQueuedTasks();
        workManager.awaitTerminationAfterShutdown(maxDrainTime);
        log.info("Thread pool " + poolName + " stopped");
    }

    public void doFail() {
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ThreadPool.class.getName());

        infoFactory.addAttribute("maxDrainTime", true);
        infoFactory.addAttribute("keepAliveTime", true);
        infoFactory.addAttribute("minimumPoolSize", true);
        infoFactory.addAttribute("maximumPoolSize", true);
        infoFactory.addAttribute("poolName", true);
        infoFactory.addOperation("getWorkManager");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
