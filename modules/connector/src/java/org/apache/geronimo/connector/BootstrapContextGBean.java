/**
 *
 * Copyright 2005 The Apache Software Foundation
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

import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * 
 * @version $Revision$
 */
public class BootstrapContextGBean {
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(BootstrapContext.class);
          //adding interface does not work, creates attributes for references???
//        infoFactory.addInterface(javax.resource.spi.BootstrapContext.class);

        infoFactory.addOperation("createTimer");
        infoFactory.addOperation("getWorkManager");
        infoFactory.addOperation("getXATerminator");

        infoFactory.addReference("WorkManager", WorkManager.class);
        infoFactory.addReference("XATerminator", XATerminator.class);

        infoFactory.setConstructor(new String[]{"WorkManager", "XATerminator"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
