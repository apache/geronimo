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

import java.util.Set;
import java.util.Collections;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.registry.AbstractGBeanRegistry;

/**
 * @version $Rev:  $ $Date:  $
 */
public class GBeanDataRegistry extends AbstractGBeanRegistry {

    public void setDefaultDomain(String defaultDomain) {
        this.defaultDomainName = defaultDomain;
    }

    public void preregister(ObjectName name) {
        register(name, null);
    }

    public void register(GBeanData gbean) {
        register(gbean.getName(), gbean);
    }

    public GBeanData getGBeanInstance(ObjectName name) throws GBeanNotFoundException {
        GBeanData gbeanData;
        synchronized (this) {
            gbeanData = (GBeanData) registry.get(name);
        }
        if (gbeanData == null) {
            throw new GBeanNotFoundException(name.getCanonicalName());
        }
        return gbeanData;
    }

    public Set getGBeanNames() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    public GBeanData[] getGBeans() {
        return (GBeanData[])registry.values().toArray(new GBeanData[registry.size()]);
    }


}
