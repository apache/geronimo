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
package org.apache.geronimo.deployment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.GBeanNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class GBeanDataRegistry {
    private final Map registry = new HashMap();

    public void preregister(AbstractName name) {
        registry.put(name, null);
    }

    public void register(GBeanData gbean) {
        registry.put(gbean.getAbstractName(), gbean);
    }

    public synchronized GBeanData getGBeanInstance(AbstractName name) throws GBeanNotFoundException {
        GBeanData gbeanData = (GBeanData) registry.get(name);
        if (gbeanData == null) {
            throw new GBeanNotFoundException(name);
        }
        return gbeanData;
    }

    public Set getGBeanNames() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public GBeanData[] getGBeans() {
        return (GBeanData[])registry.values().toArray(new GBeanData[registry.size()]);
    }


    public Set listGBeans(AbstractNameQuery query) {
        Set result = new HashSet();
        for (Iterator i = registry.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            AbstractName name = (AbstractName) entry.getKey();
            if (query == null || query.matches(name)) {
                result.add(name);
            }
        }
        return result;
    }
}
