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

package org.apache.geronimo.kernel.jmx;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import net.sf.cglib.proxy.CallbackFilter;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:01 $
 */
public final class InterfaceCallbackFilter implements CallbackFilter {
    private final Set methodSet;
    public InterfaceCallbackFilter(Class iface) {
        Method[] methods = iface.getMethods();
        methodSet = new HashSet(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            methodSet.add(new MBeanOperationSignature(method));
        }
    }

    public int accept(Method method) {
        if(methodSet.contains(new MBeanOperationSignature(method))) {
            return 1;
        }
        return 0;
    }

}
