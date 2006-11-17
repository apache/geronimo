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
package org.apache.geronimo.clustering.wadi;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.codehaus.wadi.replication.strategy.BackingStrategy;
import org.codehaus.wadi.replication.strategy.BackingStrategyFactory;
import org.codehaus.wadi.replication.strategy.RoundRobinBackingStrategyFactory;

/**
 * 
 * @version $Rev$ $Date$
 */
public class RoundRobinBackingStrategyFactoryGBean implements BackingStrategyFactory, GBeanLifecycle {
    private final int nbReplica;
    
    private BackingStrategyFactory strategyFactory;
    
    public RoundRobinBackingStrategyFactoryGBean(int nbReplica) {
        this.nbReplica = nbReplica;
    }

    public BackingStrategy factory() {
        return strategyFactory.factory();
    }

    public void doFail() {
        strategyFactory = null;
    }

    public void doStart() throws Exception {
        strategyFactory = new RoundRobinBackingStrategyFactory(nbReplica);
    }

    public void doStop() throws Exception {
        strategyFactory = null;
    }

    
    public static final GBeanInfo GBEAN_INFO;
    
    public static final String GBEAN_ATTR_NB_REPLICA = "nbReplica";
    
    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(RoundRobinBackingStrategyFactoryGBean.class, 
                NameFactory.GERONIMO_SERVICE);
        
        infoBuilder.addAttribute(GBEAN_ATTR_NB_REPLICA, int.class, true);
        
        infoBuilder.addInterface(BackingStrategyFactory.class);

        infoBuilder.setConstructor(new String[] {GBEAN_ATTR_NB_REPLICA});
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }
    
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
