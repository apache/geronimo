/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.messaging.util;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:43 $
 */
public class ProxyFactory {

    /**
     * Interfaces to be proxied.
     */
    private final Class[] interfaces;
    
    private final Callback[] callbacks;

    private final Class[] callbackTypes;
    
    private final CallbackFilter filter;
    
    public ProxyFactory(Class[] anInterfaces, Callback[] aCallbacks,
        Class[] aCallbackTypes, CallbackFilter aFilter) {
        if (null == anInterfaces) {
            throw new IllegalArgumentException("Interfaces is required.");
        } else if ( null == aCallbacks ) {
            throw new IllegalArgumentException("CallBacks is required.");
        }
        interfaces = anInterfaces;
        callbacks = aCallbacks;
        callbackTypes = aCallbackTypes;
        filter = aFilter;
    }

    public Object getProxy() {
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(interfaces);
        enhancer.setCallbackTypes(callbackTypes);
        enhancer.setUseFactory(false);
        enhancer.setCallbacks(callbacks);
        enhancer.setCallbackFilter(filter);
        return enhancer.create();
    }
    
}
