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
package org.apache.geronimo.corba.security.config.tss;

import java.io.IOException;

import org.omg.CSIIOP.SCS_GeneralNames;
import org.omg.CSIIOP.ServiceConfiguration;

import org.apache.geronimo.corba.security.config.ConfigException;
import org.apache.geronimo.corba.util.Util;


/**
 * @version $Revision: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class TSSGeneralNameConfig extends TSSServiceConfigurationConfig {

    private String name;

    public TSSGeneralNameConfig(byte[] name) throws Exception {
        this.name = Util.decodeGeneralName(name);
    }

    public TSSGeneralNameConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServiceConfiguration generateServiceConfiguration() throws ConfigException {
        try {
            ServiceConfiguration config = new ServiceConfiguration();

            config.syntax = SCS_GeneralNames.value;
            config.name = Util.encodeGeneralName(name);

            return config;
        } catch (IOException e) {
            throw new ConfigException("Unable to encode GeneralName", e);
        }
    }

    void toString(String spaces, StringBuffer buf) {
        String moreSpaces = spaces + "  ";
        buf.append(spaces).append("TSSGeneralNameConfig: [\n");
        buf.append(moreSpaces).append("name: ").append(name).append("\n");
        buf.append(spaces).append("]\n");
    }

}
