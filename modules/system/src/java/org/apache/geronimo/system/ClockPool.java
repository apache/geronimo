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

package org.apache.geronimo.system;

import EDU.oswego.cs.dl.util.concurrent.ClockDaemon;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;


/**
 * @version $Revision: 1.4 $ $Date: 2004/06/02 05:33:05 $
 */
public class ClockPool implements GBean {

    static private final Log log = LogFactory.getLog(ClockPool.class);

    private String poolName;

    /**
     * Manages the thread that can used to schedule short
     * running tasks in the future.
     */
    protected ClockDaemon clockDaemon;

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * @return
     */
    public ClockDaemon getClockDaemon() {
        return clockDaemon;
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        clockDaemon = new ClockDaemon();
        clockDaemon.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, poolName + " ");
                t.setDaemon(true);
                return t;
            }
        });
        log.info("Clock pool " + poolName + " started");
    }

    public void doStop() throws WaitingException, Exception {
        clockDaemon.shutDown();
        log.info("Clock pool " + poolName + " stopped");
    }

    public void doFail() {
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ClockPool.class);

        infoFactory.addAttribute("poolName", String.class, true);

        infoFactory.addOperation("getClockDaemon");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
