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

package org.apache.geronimo.naming.jmx;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/09 18:03:11 $
 *
 * */
public class JMXObjectFactory implements ObjectFactory {

    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment) throws Exception {
        if (obj instanceof Reference) {
            Reference ref = (Reference) obj;
            RefAddr refAddr = ref.get(0);
            if (!(refAddr instanceof JMXRefAddr)) {
                throw new IllegalStateException("Invalid ref addr in a Connectionfactory ref: " + refAddr);
            }
            JMXRefAddr jmxRefAddr = (JMXRefAddr)refAddr;
            Kernel kernel;
            if (jmxRefAddr.getKernelName() == null) {
                kernel = Kernel.getSingleKernel();
            } else {
                kernel = Kernel.getKernel(jmxRefAddr.getKernelName());
            }
            Object proxy = kernel.invoke(jmxRefAddr.getTargetName(), "getProxy");
            if (!jmxRefAddr.getInterface().isAssignableFrom(proxy.getClass())) {
                throw new IllegalStateException("Proxy does not implement expected interface " + jmxRefAddr.getInterface());
            }
            return proxy;
        }
        return null;
    }
}
