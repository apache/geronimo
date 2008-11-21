/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.axis2;

import java.util.HashMap;
import java.util.Map;

public class GeronimoFactoryRegistry {

    private static ThreadLocal<GeronimoFactoryRegistry> registry = 
        new ThreadLocal<GeronimoFactoryRegistry>();

    static void setGeronimoFactoryRegistry(GeronimoFactoryRegistry manager) {
        registry.set(manager);
    }

    static GeronimoFactoryRegistry getGeronimoFactoryRegistry() {
        return registry.get();
    }   

    
    private Map<Class, Object> map = new HashMap<Class, Object>();

    public <T> void put(Class<T> key, T value) {
        map.put(key, value);
    }
    
    public <T> T get(Class<T> key) {
        return key.cast(map.get(key));
    }
    
}
