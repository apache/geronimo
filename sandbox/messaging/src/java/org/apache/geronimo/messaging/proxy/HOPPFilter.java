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

package org.apache.geronimo.messaging.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.CallbackFilter;


/**
 * Maps the methods defined by the specified interfaces to the first Callback
 * of an Enhancer.
 * <BR>
 * The other methods are mapped to the second one.
 * 
 * @version $Rev$ $Date$
 */
public class HOPPFilter
    implements CallbackFilter
{
    
    private final Class[] interfaces;

    /**
     * @param anInterfaces Interfaces whose methods should be handled by the
     * first Callback of an Enhancer.
     */
    public HOPPFilter(Class[] anInterfaces) {
        interfaces = anInterfaces;
    }
    
    public int accept(Method arg0) {
        Class declaringClass = arg0.getDeclaringClass(); 
        for (int i = 0; i < interfaces.length; i++) {
            if ( interfaces[i].equals(declaringClass) ) {
                return 0;
            }
        }
        return 1;  
    }
    
}