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

package org.apache.geronimo.remoting.jmx;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.proxy.ProxyInvocation;
import org.apache.geronimo.remoting.transport.TransportFactory;

/**
 * @version $Revision: 1.5 $ $Date: 2004/02/25 09:58:03 $
 */
public class NotificationRemoterInterceptor implements Interceptor, Serializable {
    private final Interceptor next;

    public NotificationRemoterInterceptor(Interceptor next) {
        this.next = next;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        Method method = ProxyInvocation.getMethod(invocation);
        Object[] args = ProxyInvocation.getArguments(invocation);

        if (
            method.getName().equals("removeNotificationListener")
                && isEquals(method.getParameterTypes(), new Class[] { ObjectName.class, NotificationListener.class })) {
            //public void removeNotificationListener(ObjectName arg0, NotificationListener arg1)
            NotificationListener nl = (NotificationListener) args[1];
            TransportFactory.unexport(nl);
        } else if (
            method.equals("removeNotificationListener")
                && isEquals(
                    method.getParameterTypes(),
                    new Class[] { ObjectName.class, NotificationListener.class, NotificationFilter.class, Object.class })) {
            //public void removeNotificationListener(ObjectName arg0, NotificationListener arg1, NotificationFilter arg2, Object arg3)
            NotificationListener nl = (NotificationListener) args[1];
            TransportFactory.unexport(nl);
        }
        return next.invoke(invocation);
    }

    /**
     * @param classes
     * @param classes2
     * @return
     */
    private boolean isEquals(Class[] classes, Class[] classes2) {
        if (classes.length != classes2.length)
            return false;
        for (int i = 0; i < classes.length; i++) {
            if (!classes[i].equals(classes2[i]))
                return false;
        }
        return true;
    }
}
