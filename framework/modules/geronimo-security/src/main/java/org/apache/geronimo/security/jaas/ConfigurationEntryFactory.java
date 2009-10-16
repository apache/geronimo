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
package org.apache.geronimo.security.jaas;

import javax.security.auth.login.AppConfigurationEntry;


/**
 * A factory interface used by <code>GeronimoLoginConfiguration</code> to obtain
 * <code>AppConfigurationEntry[]</code>s from GBean configuration entries.
 *
 * @version $Rev$ $Date$
 * @see GeronimoLoginConfiguration
 * @see DirectConfigurationEntry
 */
public interface ConfigurationEntryFactory {

    /**
     * Used to obtain the configuration name to be associated with the generated
     * <code>AppConfigurationEntry</code> array.
     *
     * @return the configuration name
     */
    public String getConfigurationName();

    /**
     * Generate the <code>AppConfigurationEntry</code> array for the login modules in this configuration.
     *
     * @return a <code>AppConfigurationEntry[]</code>
     */
    AppConfigurationEntry[] getAppConfigurationEntries();

    /**
     * return false to exclude from global GeronimoLoginConfiguration
     *
     * @return whether to include in GeronimoLoginConfiguration
     */
    boolean isGlobal();

    /**
     * delegate from Configuration
     */
    void refresh();
}
