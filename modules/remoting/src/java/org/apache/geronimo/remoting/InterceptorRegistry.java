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

package org.apache.geronimo.remoting;

import java.util.HashMap;

import org.apache.geronimo.core.service.Interceptor;

/**
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:58:02 $
 */
public class InterceptorRegistry {
    public static final InterceptorRegistry instance = new InterceptorRegistry();

    private long nextID = System.currentTimeMillis();
    private HashMap map = new HashMap();

    private InterceptorRegistry() {
    }

    public Long register(Interceptor interceptor) {
        synchronized (map) {
            Long id = new Long(nextID++);
            map.put(id, interceptor);
            return id;
        }
    }

    public Interceptor unregister(Long id) {
        synchronized (map) {
            return (Interceptor) map.remove(id);
        }
    }

    public Interceptor lookup(Long id) {
        synchronized (map) {
            return (Interceptor) map.get(id);
        }
    }
}