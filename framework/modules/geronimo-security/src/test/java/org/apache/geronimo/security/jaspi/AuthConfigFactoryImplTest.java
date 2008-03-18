/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.security.jaspi;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.AuthConfigFactory.RegistrationContext;

import junit.framework.TestCase;

import org.apache.geronimo.security.jaspi.providers.BadConstructorProvider;
import org.apache.geronimo.security.jaspi.providers.BadImplementProvider;
import org.apache.geronimo.security.jaspi.providers.DummyProvider;

public class AuthConfigFactoryImplTest extends TestCase {

    protected void setUp() throws Exception {
        AuthConfigFactory.setFactory(null);
    }
    
    public void testFactory() throws Exception {
        AuthConfigFactory factory1 = AuthConfigFactory.getFactory();
        assertNotNull(factory1);
        AuthConfigFactory factory2 = AuthConfigFactory.getFactory();
        assertNotNull(factory2);
        assertSame(factory1, factory2);
    }
    
    public void testBadConstructorProvider() throws Exception {
        try {
            AuthConfigFactory factory = AuthConfigFactory.getFactory();
            factory.registerConfigProvider(BadConstructorProvider.class.getName(), null, "layer", "appContext", "description");
            fail("An exception should have been thrown");
        } catch (AuthException e) {
            //e.printStackTrace();
        }
    }
    
    public void testBadImplementProvider() throws Exception {
        try {
            AuthConfigFactory factory = AuthConfigFactory.getFactory();
            factory.registerConfigProvider(BadImplementProvider.class.getName(), null, "layer", "appContext", "description");
            fail("An exception should have been thrown");
        } catch (AuthException e) {
            //e.printStackTrace();
        }
    }
    
    public void testRegisterUnregister() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        String regId = factory.registerConfigProvider(DummyProvider.class.getName(), null, "layer", "appContext", "description");
        assertNotNull(regId);
        RegistrationContext regContext = factory.getRegistrationContext(regId);
        assertNotNull(regContext);
        assertEquals("layer", regContext.getMessageLayer());
        assertEquals("appContext", regContext.getAppContext());
        assertEquals("description", regContext.getDescription());

        assertTrue(factory.removeRegistration(regId));
        
        regContext = factory.getRegistrationContext(regId);
        assertNull(regContext);
    }
    
    public void testProviderWithLayerAndContext() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        factory.registerConfigProvider(DummyProvider.class.getName(), null, "layer", "appContext", "description");

        assertNotNull(factory.getConfigProvider("layer", "appContext", null));
        assertNull(factory.getConfigProvider("layer", null, null));
        assertNull(factory.getConfigProvider("layer", "bad", null));
        assertNull(factory.getConfigProvider("bad", "appContext", null));
        assertNull(factory.getConfigProvider(null, null, null));
    }
    
    public void testProviderWithLayer() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        factory.registerConfigProvider(DummyProvider.class.getName(), null, "layer", null, "description");

        assertNotNull(factory.getConfigProvider("layer", "appContext", null));
        assertNotNull(factory.getConfigProvider("layer", null, null));
        assertNotNull(factory.getConfigProvider("layer", "bad", null));
        assertNull(factory.getConfigProvider("bad", "appContext", null));
        assertNull(factory.getConfigProvider(null, null, null));
    }
    
    public void testProviderContextLayer() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        factory.registerConfigProvider(DummyProvider.class.getName(), null, null, "appContext", "description");

        assertNotNull(factory.getConfigProvider("layer", "appContext", null));
        assertNull(factory.getConfigProvider("layer", null, null));
        assertNull(factory.getConfigProvider("layer", "bad", null));
        assertNotNull(factory.getConfigProvider("bad", "appContext", null));
        assertNull(factory.getConfigProvider(null, null, null));
    }
    
    public void testProviderDefault() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        factory.registerConfigProvider(DummyProvider.class.getName(), null, null, null, "description");

        assertNotNull(factory.getConfigProvider("layer", "appContext", null));
        assertNotNull(factory.getConfigProvider("layer", null, null));
        assertNotNull(factory.getConfigProvider("layer", "bad", null));
        assertNotNull(factory.getConfigProvider("bad", "appContext", null));
        assertNotNull(factory.getConfigProvider(null, null, null));
    }
    
    public void testListenerOnRegister() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        factory.registerConfigProvider(DummyProvider.class.getName(), null, null, null, "description");
        DummyListener listener = new DummyListener();
        assertNotNull(factory.getConfigProvider(null, null, listener));
        factory.registerConfigProvider(DummyProvider.class.getName(), null, null, null, "description");
        assertTrue(listener.notified);
    }
    
    public void testListenerOnUnregister() throws Exception {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        String regId = factory.registerConfigProvider(DummyProvider.class.getName(), null, null, null, "description");
        DummyListener listener = new DummyListener();
        assertNotNull(factory.getConfigProvider(null, null, listener));
        factory.removeRegistration(regId);
        assertTrue(listener.notified);
    }
    
    public static class DummyListener implements RegistrationListener {
        public boolean notified = true;
        public void notify(String layer, String appContext) {
            notified = true;
        }
    }
    
}
