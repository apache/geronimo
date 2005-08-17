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
package org.apache.geronimo.connector.outbound.security;

import java.util.Properties;
import javax.resource.spi.ManagedConnectionFactory;

import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;

/**
 * @version $Rev:  $ $Date:  $
 */
public class PasswordCredentialLoginModuleWrapper extends LoginModuleGBean {
    public static final String MANAGED_CONNECTION_FACTORY_OPTION = "geronimo.managedconnectionfactory.option";

    public void setManagedConnectionFactoryWrapper(ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper) {
        ManagedConnectionFactory managedConnectionFactory = managedConnectionFactoryWrapper.$getManagedConnectionFactory();
        Properties options = getOptions();
        options.put(MANAGED_CONNECTION_FACTORY_OPTION, managedConnectionFactory);
    }

}
