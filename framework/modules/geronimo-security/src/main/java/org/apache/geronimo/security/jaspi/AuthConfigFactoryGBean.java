/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.security.jaspi;

import javax.security.auth.message.config.AuthConfigFactory;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;

/**
 * Installs the specified AuthConfigFactory
 *
 * @version $Rev$ $Date$
 */
@GBean
public class AuthConfigFactoryGBean {

    public AuthConfigFactoryGBean(@ParamAttribute(name = "authConfigFactoryClassName") final String authConfigFactoryClassName,
                                  @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) {

        Thread currentThread = Thread.currentThread();
        ClassLoader oldClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(classLoader);
        try {
            java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {
                        public Object run() {
                            java.security.Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, authConfigFactoryClassName);
                            return null;
                        }
                    });

            AuthConfigFactory.getFactory();

        } finally {
            currentThread.setContextClassLoader(oldClassLoader);
        }

    }
}
