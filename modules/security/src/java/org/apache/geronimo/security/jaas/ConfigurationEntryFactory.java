/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.security.jaas;

/**
 * A factory interface used by <code>GeronimoLoginConfiguration</code> to obtain
 * <code>JaasLoginModuleConfiguration</code>s from GBean configuration entries.
 *
 * @version $Rev: $ $Date: $
 * @see GeronimoLoginConfiguration
 * @see DirectConfigurationEntry
 * @see ServerRealmConfigurationEntry
 */
public interface ConfigurationEntryFactory {

    /**
     * Used to obtain the configuration name to be associated with the generated
     * <code>JaasLoginModuleConfiguration</code>.
     *
     * @return the configuration name
     */
    public String getConfigurationName();

    /**
     * Generate a <code>JaasLoginModuleConfiguration</code>
     *
     * @return a <code>JaasLoginModuleConfiguration</code>
     */
    public JaasLoginModuleConfiguration generateConfiguration();

}
