/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis2;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

import org.apache.axis2.jaxws.description.impl.DescriptionFactoryImpl;

public class ClearCacheGBean implements GBeanLifecycle {

    private static final Log LOG = LogFactory.getLog(ClearCacheGBean.class);

    private Timer timer;
    private long period;

    public ClearCacheGBean(long period) {
        this.period = period;
    }
    
    public void doStart() throws Exception {
        if (timer == null) {
            timer = new Timer(true);
        }
        timer.schedule(new ClearCacheTask(), 0, this.period);
    }
    
    public void doStop() throws Exception {  
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void doFail() {
    }

    private static class ClearCacheTask extends TimerTask {
        public void run() {
            LOG.debug("Clearing Axis2 cache");
            DescriptionFactoryImpl.clearServiceDescriptionCache();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ClearCacheGBean.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addAttribute("period", long.class, true, true);

        infoBuilder.setConstructor(new String[]{
                "period"
        });
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
