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

package org.apache.geronimo.connector;

import java.util.Timer;

import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 * @version $Revision: 1.6 $ $Date: 2004/06/02 05:33:01 $
 */
public class BootstrapContext implements javax.resource.spi.BootstrapContext {
    private WorkManager workManager;
    private XATerminator xATerminator;

    public BootstrapContext() {

    }

    public BootstrapContext(WorkManager workManager, XATerminator xaTerminator) {
        this.workManager = workManager;
        this.xATerminator = xaTerminator;
    }

    /**
     * @param workManager The workManager to set.
     */
    public void setWorkManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    /**
     * @param terminator The xATerminator to set.
     */
    public void setXATerminator(XATerminator terminator) {
        xATerminator = terminator;
    }

    /**
     * @see javax.resource.spi.BootstrapContext#getWorkManager()
     */
    public WorkManager getWorkManager() {
        return workManager;
    }

    /**
     * @see javax.resource.spi.BootstrapContext#getXATerminator()
     */
    public XATerminator getXATerminator() {
        return xATerminator;
    }

    /**
     * @see javax.resource.spi.BootstrapContext#createTimer()
     */
    public Timer createTimer() throws UnavailableException {
        return new Timer();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(BootstrapContext.class);

        infoFactory.addOperation("getWorkManager");
        infoFactory.addOperation("getXATerminator");
        infoFactory.addOperation("createTimer");

        infoFactory.addReference("WorkManager", WorkManager.class);
        infoFactory.addReference("XATerminator", XATerminator.class);

        infoFactory.setConstructor(new String[]{"WorkManager", "XATerminator"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
