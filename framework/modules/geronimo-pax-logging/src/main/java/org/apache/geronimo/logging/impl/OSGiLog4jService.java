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

package org.apache.geronimo.logging.impl;

import java.util.Properties;

import org.apache.geronimo.main.ServerInfo;
import org.osgi.service.cm.Configuration;

public class OSGiLog4jService extends Log4jService {

    private Configuration configuration;

    public OSGiLog4jService(String configurationFile, int refreshPeriod, ServerInfo serverInfo, Configuration configuration) {
        super(configurationFile, refreshPeriod, serverInfo);
        this.configuration = configuration;
    }

    @Override
    protected void update(Properties properties) throws Exception {
        configuration.update(properties);
    }
 
}
