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

package org.apache.geronimo.messaging.reference;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;

/**
 *
 * @version $Rev$ $Date$
 */
public class ReferenceableEnhancer
{

    /**
     * Injects the Referenceable interface to the set of interfaces of anOpaque.
     * 
     * @param anOpaque Object to be enhanced.
     * @return An object implementing all the interfaces of anOpaque plus the
     * Referenceable interface.
     */
    public static Object enhance(final Object anOpaque) {
        // Injects the Referenceable interface.
        Set interfaces = new HashSet();
        Class current = anOpaque.getClass();
        while ( null != current ) {
            Class[] intfs = current.getInterfaces();
            for (int i = 0; i < intfs.length; i++) {
                interfaces.add(intfs[i]);
            }
            current = current.getSuperclass();
        }
        Class[] newInterfaces = new Class[interfaces.size() + 1];
        int i = 1;
        newInterfaces[0] = Referenceable.class;
        for (Iterator iter = interfaces.iterator(); iter.hasNext();) {
            newInterfaces[i++] = (Class) iter.next();
        }
        
        Enhancer enhancer = new Enhancer();
        enhancer.setInterfaces(newInterfaces);
        enhancer.setCallbackType(LazyLoader.class);
        enhancer.setCallback(new LazyLoader() {
            public Object loadObject() throws Exception {
                return anOpaque;
            }
        });
        // Gets rid of the Factory interface.
        enhancer.setUseFactory(false);
        return enhancer.create();
    }
    
}
