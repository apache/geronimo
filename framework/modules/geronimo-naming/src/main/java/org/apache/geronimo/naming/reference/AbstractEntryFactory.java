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

import java.util.List;
import java.util.Set;
import java.util.Collections;

import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractEntryFactory<T, S> implements EntryFactory<T> {
    private static final long serialVersionUID = 7642216668523441421L;
    private final Artifact[] configId;
    private final Set<AbstractNameQuery> abstractNameQueries;
    private final Class<S> gbeanClass;

    protected AbstractEntryFactory(Artifact[] configId, AbstractNameQuery abstractNameQuery, Class<S> gbeanClass) {
        this(configId, Collections.singleton(abstractNameQuery), gbeanClass);
    }

    protected AbstractEntryFactory(Artifact[] configId, Set<AbstractNameQuery> abstractNameQueries, Class<S> gbeanClass) {
        if (configId == null || configId.length == 0) {
            throw new NullPointerException("No configId");
        }
        this.configId = configId;
        this.abstractNameQueries = abstractNameQueries;
        this.gbeanClass = gbeanClass;
    }

    protected S getGBean(Kernel kernel) throws NamingException {
        AbstractName target;
        try {
            target = resolveTargetName(kernel);
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException) new NameNotFoundException("Could not resolve name query: " + abstractNameQueries).initCause(e);
        }

        Object proxy;
        try {
            proxy = kernel.getGBean(target);
        } catch (Exception e) {
            throw (IllegalStateException) new IllegalStateException("Could not get proxy").initCause(e);
        }
        if (proxy == null) {
            throw new IllegalStateException("Proxy not returned. Target " + target + " not started");
        }
        checkClass(proxy, gbeanClass);
        return (S)proxy;

    }

    protected void checkClass(Object proxy, Class clazz) {
        if (!clazz.isAssignableFrom(proxy.getClass())) {
            Class proxyClass = proxy.getClass();
            Class[] interfaces = proxyClass.getInterfaces();
            StringBuilder message = new StringBuilder();
            boolean namesMatch = false;
            for (Class anInterface : interfaces) {
                if (clazz.getName().equals(anInterface.getName())) {
                    namesMatch = true;
                    message.append("Proxy implements correct interface: ").append(clazz.getName()).append(", but classloaders differ\n");
                    message.append("lookup interface classloader: ").append(clazz.getClassLoader().toString()).append("\n");
                    message.append("target interface classloader: ").append(anInterface.getClassLoader().toString()).append("\n");
                    message.append("target proxy classloader: ").append(proxy.getClass().getClassLoader());
                    break;
                }
            }
            if (!namesMatch) {
                message.append("Proxy does not implement an interface named: ").append(clazz.getName());
            }
            throw new ClassCastException(message.toString());
        }
    }

    public Configuration getConfiguration(Kernel kernel) throws GBeanNotFoundException {
         ConfigurationManager configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
         Configuration configuration =  configurationManager.getConfiguration(configId[0]);
         if (configuration == null) {
             throw new IllegalStateException("No configuration found for id: " + configId[0]);
         }
         next: for (int i = 1; i < configId.length; i++) {
             List<Configuration> children = configuration.getChildren();
             for (Configuration child: children) {
                 if (child.getId().equals(configId[i])) {
                     configuration = child;
                     break next;
                 }
             }
             throw new GBeanNotFoundException("No configuration found for id: " + configId[i], null);
         }
         return configuration;
     }

     public AbstractName resolveTargetName(Kernel kernel) throws GBeanNotFoundException {
         Configuration configuration = getConfiguration(kernel);
         try {
             return configuration.findGBean(abstractNameQueries);
         } catch (GBeanNotFoundException e) {
             Set results = kernel.listGBeans(abstractNameQueries);
             if (results.size() == 1) {
                 return (AbstractName) results.iterator().next();
             }
             throw new GBeanNotFoundException("Name query " + abstractNameQueries + " not satisfied in kernel, matches: " + results, e);
         }
     }


}
