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
package org.apache.geronimo.kernel.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.runtime.GBeanInstance;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.GBeanNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class BasicGBeanRegistry extends AbstractGBeanRegistry implements GBeanRegistry {

    public void start(Kernel kernel) {
        this.defaultDomainName = kernel.getKernelName();
    }

    public void register(GBeanInstance gbeanInstance) throws GBeanAlreadyExistsException, InternalKernelException {
        ObjectName name = gbeanInstance.getObjectNameObject();
        register(name, gbeanInstance);

    }

    public GBeanInstance getGBeanInstance(ObjectName name) throws GBeanNotFoundException {
        GBeanInstance gbeanInstance;
        synchronized (this) {
            gbeanInstance = (GBeanInstance) registry.get(name);
        }
        if (gbeanInstance == null) {
            throw new GBeanNotFoundException(name.getCanonicalName());
        }
        return gbeanInstance;
    }

}
