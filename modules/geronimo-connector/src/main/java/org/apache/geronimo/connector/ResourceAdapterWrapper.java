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

package org.apache.geronimo.connector;

import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.geronimo.transaction.manager.WrapperNamedXAResource;

/**
 * Dynamic GBean wrapper around a ResourceAdapter object, exposing the config-properties as
 * GBean attributes.
 *
 * @version $Rev$ $Date$
 */
public class ResourceAdapterWrapper implements ResourceAdapter {

    private final String name;

    private final String resourceAdapterClass;

    private final BootstrapContext bootstrapContext;

    protected final ResourceAdapter resourceAdapter;

    private final Map<String,String> messageListenerToActivationSpecMap;

    private final RecoverableTransactionManager transactionManager;


    /**
     *  default constructor for enhancement proxy endpoint
     */
    public ResourceAdapterWrapper() {
        this.name = null;
        this.resourceAdapterClass = null;
        this.bootstrapContext = null;
        this.resourceAdapter = null;
        this.messageListenerToActivationSpecMap = null;
        this.transactionManager = null;
    }

    public ResourceAdapterWrapper(String name,
            String resourceAdapterClass,
            Map<String, String> messageListenerToActivationSpecMap,
            BootstrapContext bootstrapContext,
            RecoverableTransactionManager transactionManager,
            ClassLoader cl) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this.name = name;
        this.resourceAdapterClass = resourceAdapterClass;
        this.bootstrapContext = bootstrapContext;
        Class clazz = cl.loadClass(resourceAdapterClass);
        resourceAdapter = (ResourceAdapter) clazz.newInstance();
        this.messageListenerToActivationSpecMap = messageListenerToActivationSpecMap;
        this.transactionManager = transactionManager;
    }
    
    public ResourceAdapterWrapper(String name, ResourceAdapter resourceAdapter, Map<String, String> messageListenerToActivationSpecMap, BootstrapContext bootstrapContext, RecoverableTransactionManager transactionManager) {
        this.name = name;
        this.resourceAdapterClass = resourceAdapter.getClass().getName();
        this.bootstrapContext = bootstrapContext;
        this.resourceAdapter = resourceAdapter;
        this.messageListenerToActivationSpecMap = messageListenerToActivationSpecMap;
        this.transactionManager = transactionManager;
    }

    public String getName() {
        return name;
    }

    public String getResourceAdapterClass() {
        return resourceAdapterClass;
    }

    public Map<String,String> getMessageListenerToActivationSpecMap() {
        return messageListenerToActivationSpecMap;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void registerResourceAdapterAssociation(final ResourceAdapterAssociation resourceAdapterAssociation) throws ResourceException {
        resourceAdapterAssociation.setResourceAdapter(resourceAdapter);
    }

    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        throw new IllegalStateException("Don't call this");
    }

    public void stop() {
        throw new IllegalStateException("Don't call this");
    }

    //endpoint handling
    public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) throws ResourceException {
        resourceAdapter.endpointActivation(messageEndpointFactory, activationSpec);
    }

    public void doRecovery(ActivationSpec activationSpec, String containerId) {
        try {
            XAResource[] xaResources = getXAResources(new ActivationSpec[]{activationSpec});
            if (xaResources == null || xaResources.length == 0) {
                return;
            }
            NamedXAResource xaResource = new WrapperNamedXAResource(xaResources[0], containerId);
            transactionManager.recoverResourceManager(xaResource);
        } catch (ResourceException e) {
            transactionManager.recoveryError((SystemException) new SystemException("Could not get XAResource for recovery for mdb: " + containerId).initCause(e));
        }

    }

    public void endpointDeactivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec) {
        resourceAdapter.endpointDeactivation(messageEndpointFactory, activationSpec);
    }

    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return resourceAdapter.getXAResources(specs);
    }

    public void doStart() throws Exception {
        resourceAdapter.start(bootstrapContext);
    }

    public void doStop() {
        resourceAdapter.stop();
    }

    public void doFail() {
        resourceAdapter.stop();
    }

}
