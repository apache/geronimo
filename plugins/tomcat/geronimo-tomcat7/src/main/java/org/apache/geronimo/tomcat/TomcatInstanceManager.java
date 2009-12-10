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


package org.apache.geronimo.tomcat;


import java.lang.reflect.InvocationTargetException;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;
import org.apache.geronimo.j2ee.annotation.Holder;

/**
 * @version $Rev$ $Date$
 */
public class TomcatInstanceManager implements InstanceManager {

    private final Holder holder;
    private final ClassLoader classLoader;
    private final Context context;


    public TomcatInstanceManager(Holder holder, ClassLoader classLoader, Context context) {
        this.holder = holder;
        this.classLoader = classLoader;
        this.context = context;
    }

    public Object newInstance(String fqcn, ClassLoader classLoader) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return holder.newInstance(fqcn, classLoader, context);
    }
	
    public Object newInstance(String className) throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException {
        return holder.newInstance(className, classLoader, context);
    }

    public void destroyInstance(Object o) throws IllegalAccessException, InvocationTargetException {
        try {
            holder.destroyInstance(o);
        } catch (Exception e) {
            throw new InvocationTargetException(e, "Attempted to destroy instance");
        }
    }
    
    public void newInstance(Object o) throws IllegalAccessException,  InvocationTargetException, NamingException {
	throw new UnsupportedOperationException("separate instantiation and injection is not supported");
    }
}
