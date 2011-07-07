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


package org.apache.geronimo.naming.reference;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.xbean.naming.reference.SimpleReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class ResourceReference<E extends Throwable> extends SimpleReference implements BundleAwareReference, KernelAwareReference {

    private static final Logger logger = LoggerFactory.getLogger(ResourceReference.class);
    private final String type;
    private String query;
    private AbstractName gbeanName;
    private transient BundleContext bundleContext;

    public ResourceReference(AbstractName gbeanName, String type) {
        this.gbeanName = gbeanName;
        this.type = type;
    }

    public ResourceReference(String query, String type) {
        this.query = query;
        this.type = type;
    }

    @Override
    public Object getContent() throws NamingException {
        ServiceReference ref = null;
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(ResourceSource.class.getName(), query);
            if (refs == null || refs.length == 0) {
                throw new NameNotFoundException("could not locate osgi service matching " + query);
            }
            ref = refs[0];
            @SuppressWarnings("unchecked")
            ResourceSource<E> source = (ResourceSource<E>) bundleContext.getService(ref);
            return source.$getResource();
        } catch (Throwable e) {
            throw (NamingException) new NamingException("Could not create resource").initCause(e);
        } finally {
            if (ref != null) {
                bundleContext.ungetService(ref);
            }
        }
    }

    @Override
    public String getClassName() {
        return type;
    }

    @Override
    public void setBundle(Bundle bundle) {
        this.bundleContext = bundle.getBundleContext();
    }

    @Override
    public void setKernel(Kernel kernel) {
        if(query == null) {
            try {
                ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
                AbstractName mappedAbstractName = new AbstractName(configurationManager.getArtifactResolver().resolveInClassLoader(gbeanName.getArtifact()), gbeanName.getName());
                String osgiJndiName = kernel.getNaming().toOsgiJndiName(mappedAbstractName);
                query = "(osgi.jndi.service.name=" + osgiJndiName + ')';
            } catch (GBeanNotFoundException e) {
                logger.error("Fail to build the jndi name for " + gbeanName, e);
            } catch (MissingDependencyException e) {
                logger.error("Fail to build the jndi name for " + gbeanName, e);
            }
        }
    }
}
