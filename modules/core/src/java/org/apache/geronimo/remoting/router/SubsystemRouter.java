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
package org.apache.geronimo.remoting.router;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import javax.management.relation.RelationNotification;
import javax.management.relation.RelationServiceMBean;

import org.apache.geronimo.jmx.JMXUtil;
import org.apache.geronimo.jmx.MBeanProxyFactory;

/**
 *
 * @jmx:mbean
 *      extends="org.apache.geronimo.remoting.router.AbstractRouterRouterMBean,org.apache.geronimo.remoting.router.RouterTargetMBean"
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/01 20:38:49 $
 */
public class SubsystemRouter
    extends AbstractRouterRouter
    implements SubsystemRouterMBean
{
    String relationshipID = "Route";
    Map currentRoutingMap = new HashMap();

    /**
     * @see org.apache.geronimo.remoting.router.AbstractRouterRouter#lookupRouterFrom(java.net.URI)
     */
    protected Router lookupRouterFrom(URI to) {
        String subsystem = to.getPath();
        return (Router) currentRoutingMap.get(subsystem);
    }

    private Map calcRouteMap() {
        HashMap rc = new HashMap();

        RelationServiceMBean relationService = JMXUtil.getRelationService(server);
        Map relatedMBeans = relationService.findAssociatedMBeans(objectName, relationshipID, null);
        Iterator iter = relatedMBeans.keySet().iterator();
        while (iter.hasNext()) {
            ObjectName on = (ObjectName) iter.next();
            Collection c = (Collection) relatedMBeans.get(on);
            RouterTargetMBean  rm = (RouterTargetMBean )MBeanProxyFactory.getProxy(RouterTargetMBean .class, server, on);
            Router r = rm.getRouter();
            Iterator i = c.iterator();
            while(i.hasNext())
               rc.put(i.next(), r);
        }
        return rc;
    }

    /**
     * @see org.apache.geronimo.management.AbstractManagedObject#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification n, Object o) {
        super.handleNotification(n, o);
        if (n instanceof RelationNotification) {
            RelationNotification rn = (RelationNotification) n;
            if (!rn.getRelationTypeName().equals(relationshipID))
                return;
            try {
                // Did our routes change??
                Map map = calcRouteMap();
                if( currentRoutingMap.equals(map) )
                    return;

                    log.info("Detected a change in the active subsystems.  Restarting to reload subsystem routes.");
                restart();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * @jmx:managed-operation
     */
    public void restart() throws Exception {
        doStop();
        doStart();
    }

    /**
     * @see org.apache.geronimo.remoting.router.AbstractRouterRouter#doStart()
     */
    protected void doStart() throws Exception {
        NotificationFilter filter = null;
        server.addNotificationListener(JMXUtil.RELATION_SERVICE_NAME, objectName, filter, null);
        currentRoutingMap = calcRouteMap();
        super.doStart();
    }

    /**
     * @see org.apache.geronimo.remoting.router.AbstractRouterRouter#doStop()
     */
    protected void doStop() throws Exception {
        super.doStop();
        server.removeNotificationListener(JMXUtil.RELATION_SERVICE_NAME, objectName);
    }

    /**
     * @see org.apache.geronimo.remoting.router.RouterTargetMBean#getRouter()
     */
    public Router getRouter() {
        return this;
    }
}
