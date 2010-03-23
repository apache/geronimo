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
package org.apache.geronimo.connector.wrapper.outbound.security;

import java.util.Map;
import java.util.HashMap;

import javax.resource.spi.ManagedConnectionFactory;

import org.apache.geronimo.connector.wrapper.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jaas.LoginModuleGBean;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = SecurityNames.LOGIN_MODULE)
public class PasswordCredentialLoginModuleWrapper extends LoginModuleGBean {
    public static final String MANAGED_CONNECTION_FACTORY_OPTION = "geronimo.managedconnectionfactory.option";

    public PasswordCredentialLoginModuleWrapper(@ParamAttribute(name = "loginModuleClass")String loginModuleClass,
                                                @ParamSpecial(type = SpecialAttributeType.objectName)String objectName,
                                                @ParamAttribute(name="wrapPrincipals")boolean wrapPrincipals,
                                                @ParamAttribute(name="options")Map<String, Object> options,
                                                @ParamAttribute(name="loginDomainName")String loginDomainName,
                                                @ParamReference(name = "ManagedConnectionFactoryWrapper", namingType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY)ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper,
                                                @ParamSpecial(type = SpecialAttributeType.classLoader)ClassLoader classLoader) {
        super(loginModuleClass, objectName, wrapPrincipals, getOptions(options, managedConnectionFactoryWrapper), loginDomainName, classLoader);
    }

    private static Map<String, Object> getOptions(Map<String, Object> options, ManagedConnectionFactoryWrapper managedConnectionFactoryWrapper) {
        if (options == null) {
            options = new HashMap<String, Object>();
        }
        ManagedConnectionFactory managedConnectionFactory = managedConnectionFactoryWrapper.getManagedConnectionFactory();
        options.put(MANAGED_CONNECTION_FACTORY_OPTION, managedConnectionFactory);
        return options;
    }

}
