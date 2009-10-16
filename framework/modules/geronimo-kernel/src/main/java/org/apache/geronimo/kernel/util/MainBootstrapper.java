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
package org.apache.geronimo.kernel.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 *
 * @version $Rev: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class MainBootstrapper extends MainConfigurationBootstrapper {

//    public static void main(String[] args) {
//        int status = main(new MainBootstrapper(), args, bundle);
//        System.exit(status);
//    }
    
    public void loadPersistentConfigurations() throws Exception {
        List<Artifact> configs = new ArrayList<Artifact>();

        AbstractNameQuery query = new AbstractNameQuery(PersistentConfigurationList.class.getName());
        Set configLists = kernel.listGBeans(query);
        for (Iterator i = configLists.iterator(); i.hasNext();) {
            AbstractName configListName = (AbstractName) i.next();
            configs.addAll((List<Artifact>) kernel.invoke(configListName, "restore"));
        }
        
        ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        try {
            for (Artifact config : configs) {
                configurationManager.loadConfiguration(config);
                configurationManager.startConfiguration(config);
            }
        } finally {
            ConfigurationUtil.releaseConfigurationManager(kernel, configurationManager);
        }
    }

}
