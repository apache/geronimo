/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.naming.java;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.OperationNotSupportedException;
import javax.naming.spi.ObjectFactory;

/**
 * URLContextFactory for the java: JNDI namespace.
 *
 * @version $Rev$ $Date$
 */
public class javaURLContextFactory implements ObjectFactory {
    /**
     * Return a Context that is able to resolve names in the java: namespace.
     * The root context, "java:" is always returned. This is a specific
     * implementation of a URLContextFactory and not a general ObjectFactory.
     * @param obj must be null
     * @param name ignored
     * @param nameCtx ignored
     * @param environment ignored
     * @return the Context for "java:"
     * @throws javax.naming.OperationNotSupportedException if obj is not null
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
        if (obj == null) {
            return new RootContext(environment);
        } else {
            throw new OperationNotSupportedException();
        }
    }
}
