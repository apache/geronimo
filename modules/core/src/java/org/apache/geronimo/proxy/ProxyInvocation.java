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

package org.apache.geronimo.proxy;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.SimpleInvocation;

/**
 * @version $Revision: 1.6 $ $Date: 2004/03/10 09:58:43 $
 */
final public class ProxyInvocation extends SimpleInvocation {

    Method method;
    Object args[];
    Object proxy;

    /* (non-Javadoc)
     * @see org.apache.geronimo.core.service.SimpleInvocation#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(args);
        out.writeObject(new MarshalledMethod(method));
    }

    /**
     * @see org.apache.geronimo.core.service.SimpleInvocation#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        args = (Object[]) in.readObject();
        method = ((MarshalledMethod) in.readObject()).getMethod();
    }

    public static Method getMethod(Invocation invocation) {
        return (Method) ((ProxyInvocation) invocation).method;
    }
    public static void putMethod(Invocation invocation, Method method) {
        ((ProxyInvocation) invocation).method = method;
    }

    public static Object getProxy(Invocation invocation) {
        return ((ProxyInvocation) invocation).proxy;
    }
    public static void putProxy(Invocation invocation, Object proxy) {
        ((ProxyInvocation) invocation).proxy = proxy;
    }

    public static Object[] getArguments(Invocation invocation) {
        return ((ProxyInvocation) invocation).args;
    }

    public static void putArguments(Invocation invocation, Object[] arguments) {
        ((ProxyInvocation) invocation).args = arguments;
    }

}
