/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
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
 * @version $Revision: 1.1 $ $Date: 2004/02/24 04:38:26 $
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
