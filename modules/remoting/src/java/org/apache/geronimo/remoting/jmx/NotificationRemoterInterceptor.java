/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.remoting.jmx;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.apache.geronimo.core.service.AbstractInterceptor;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.proxy.ProxyInvocation;

/**
 * @version $Revision: 1.1 $ $Date: 2003/11/16 05:27:27 $
 */
public class NotificationRemoterInterceptor extends AbstractInterceptor {

    
    HashMap exportedListners = new HashMap();

    /**
     * @see org.apache.geronimo.core.service.AbstractInterceptor#invoke(org.apache.geronimo.core.service.Invocation)
     */
    public InvocationResult invoke(Invocation invocation) throws Throwable {
        Method method = ProxyInvocation.getMethod(invocation);
        Object[] args = ProxyInvocation.getArguments(invocation);
                
        if ( method.getName().equals("addNotificationListener") && isEquals(method.getParameterTypes(), new Class[]{ObjectName.class, NotificationListener.class, NotificationFilter.class, Object.class}) ) {
            //public void addNotificationListener(ObjectName arg0, NotificationListener arg1, NotificationFilter arg2, Object arg3)
            NotificationListener local = (NotificationListener) args[1];
            NotificationListener proxy = getRemoteProxyOf( local );

            // Switch the object for a remotabable version.
            args[1] = proxy;
            try {
                return getNext().invoke(invocation);
            } finally {
                // Undo the switch..
                args[1] = local;
            }
            
            
        } else if ( method.getName().equals("removeNotificationListener") && isEquals(method.getParameterTypes(), new Class[]{ObjectName.class, NotificationListener.class}) ) {
            //public void removeNotificationListener(ObjectName arg0, NotificationListener arg1)
            
        } else if (method.equals("removeNotificationListener") && isEquals(method.getParameterTypes(), new Class[]{ObjectName.class, NotificationListener.class, NotificationFilter.class, Object.class})) {
            //public void removeNotificationListener(ObjectName arg0, NotificationListener arg1, NotificationFilter arg2, Object arg3)
            
        }
        return getNext().invoke(invocation);
    }

    /**
     * @param local
     * @return
     */
    private NotificationListener getRemoteProxyOf(NotificationListener local) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param classes
     * @param classes2
     * @return
     */
    private boolean isEquals(Class[] classes, Class[] classes2) {
        if( classes.length != classes2.length)
            return false;
        for (int i = 0; i < classes.length; i++) {
            if( !classes[i].equals(classes2[i]) )
                return false;
        }
        return true;
    }
}
