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
package org.apache.geronimo.axis.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.naming.RefAddr;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.reflect.FastConstructor;
import net.sf.cglib.reflect.FastClass;
import org.apache.axis.client.Service;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ServiceRefAddr extends RefAddr {

    private final static String TYPE = "org.apache.geronimo.axis.ServiceRefType";
    private final static Class[] CONSTRUCTOR_TYPES = new Class[] {Map.class};

    private final Class serviceClass;
    private final Callback[] methodInterceptors;
    private final Map ports;
    //THIS IS NOT SERIALIZABLE!
    private final FastConstructor constructor;

    public ServiceRefAddr(Class serviceClass, MethodInterceptor methodInterceptor, Map ports) {
        super(TYPE);
        this.serviceClass = serviceClass;
        this.methodInterceptors = new Callback[] {SerializableNoOp.INSTANCE,  methodInterceptor};
        this.ports = ports;
        this.constructor = FastClass.create(serviceClass).getConstructor(CONSTRUCTOR_TYPES);
    }

    public Object getContent() {
        try {
            Enhancer.registerCallbacks(serviceClass, methodInterceptors);
            Object serviceInstance =  constructor.newInstance(new Object[] {ports});
            return serviceInstance;
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Could not create instance", e);
        }
    }


}
