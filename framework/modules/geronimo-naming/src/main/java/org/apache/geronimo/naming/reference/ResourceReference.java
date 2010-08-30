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

import org.apache.geronimo.naming.ResourceSource;
import org.apache.xbean.naming.reference.SimpleReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public class ResourceReference<E extends Throwable> extends SimpleReference implements BundleAwareReference {
    private final String type;
    private final String query;
    private transient BundleContext bundleContext;

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
            @SuppressWarnings("Unchecked")
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
}
