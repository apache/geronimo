/**
 *
 *  Copyright 2004 Jeremy Boynes
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
package javax.xml.registry;

import java.util.Collection;
import java.util.Properties;

import junit.framework.TestCase;
import org.apache.ws.scout.registry.ConnectionFactoryImpl;

/**
 * @version $Revision$ $Date$
 */
public class ConnectionFactoryTest extends TestCase {
    private static final String CONNECTIONFACTORYCLASS_PROPERTY = "javax.xml.registry.ConnectionFactoryClass";

    private Properties originalProperties;
    private Properties props;

    public void testNewInstanceWithDefault() throws JAXRException {
        ConnectionFactory factory = ConnectionFactory.newInstance();
        assertEquals(ConnectionFactoryImpl.class, factory.getClass());
    }

    public void testNewInstanceWithClass() throws JAXRException {
        System.setProperty(CONNECTIONFACTORYCLASS_PROPERTY, MockFactory.class.getName());
        ConnectionFactory factory = ConnectionFactory.newInstance();
        assertEquals(MockFactory.class, factory.getClass());
    }

    protected void setUp() throws Exception {
        super.setUp();
        originalProperties = System.getProperties();
        props = new Properties(originalProperties);
        props.remove(CONNECTIONFACTORYCLASS_PROPERTY);
        System.setProperties(props);
    }

    protected void tearDown() throws Exception {
        System.setProperties(originalProperties);
        super.tearDown();
    }

    public static class MockFactory extends ConnectionFactory {
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
}
