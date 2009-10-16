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

import javax.naming.NamingException;

import org.apache.geronimo.naming.ResourceSource;
import org.apache.xbean.naming.reference.SimpleReference;

/**
 * @version $Rev$ $Date$
 */
public class ResourceReference<E extends Throwable> extends SimpleReference {
    private final ResourceSource<E> source;
    private final String type;

    public ResourceReference(ResourceSource<E> source, String type) {
        this.source = source;
        this.type = type;
    }

    public Object getContent() throws NamingException {
        try {
            return source.$getResource();
        } catch (Throwable e) {
            throw (NamingException)new NamingException("Could not create resource").initCause(e);
        }
    }

    @Override
    public String getClassName() {
        return type;
    }
}
