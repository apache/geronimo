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
package org.apache.scout.registry;

import java.util.Collection;
import java.util.Properties;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.FederatedConnection;

/**
 * Mock implementation of default class for ConnectionFactory.newInstance()
 * 
 * @version $Revision$ $Date$
 */
public class ConnectionFactoryImpl extends ConnectionFactory {
    public Connection createConnection() throws JAXRException {
        throw new UnsupportedOperationException();
    }

    public FederatedConnection createFederatedConnection(Collection connections) throws JAXRException {
        throw new UnsupportedOperationException();
    }

    public Properties getProperties() throws JAXRException {
        throw new UnsupportedOperationException();
    }

    public void setProperties(Properties properties) throws JAXRException {
        throw new UnsupportedOperationException();
    }
}
