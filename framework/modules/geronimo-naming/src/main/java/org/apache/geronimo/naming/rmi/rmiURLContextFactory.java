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

package org.apache.geronimo.naming.rmi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class rmiURLContextFactory implements ObjectFactory {

    private ObjectFactory rmiObjectFactory;

    public rmiURLContextFactory(Bundle bundle, String rmiClassName) {
        try {
            rmiObjectFactory = (ObjectFactory) bundle.loadClass(rmiClassName).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Fail to load the wrap rmi context factory " + rmiClassName);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Fail to load the wrap rmi context factory " + rmiClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Fail to load the wrap rmi context factory " + rmiClassName);
        }
    }

    @Override
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable<?, ?> environment) throws Exception {
        if (object == null || object instanceof String || object instanceof String[]) {
            return rmiObjectFactory.getObjectInstance(object, name, context, environment);
        }
        return null;
    }

}
