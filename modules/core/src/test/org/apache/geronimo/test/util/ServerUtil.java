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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.test.util;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.deployment.service.MBeanRelationshipMetadata;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Attribute;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/19 02:07:01 $
 */

public class ServerUtil {
    private static final ObjectName LOADER = JMXUtil.getObjectName("geronimo.remoting:transport=async");
    private static final ObjectName SUBSYSTEM = JMXUtil.getObjectName("geronimo.remoting:router=SubsystemRouter");
    private static final ObjectName JMX_ROUTER = JMXUtil.getObjectName("geronimo.remoting:router=JMXRouter");
    private static final ObjectName RELATIONSHIP = JMXUtil.getObjectName("geronimo.remoting:role=Relationship,name=Route");
    private static final Object[] REL_ARGS = {"name=Route\nleft.name=Source\nright.name=Target\nright.class=org.apache.geronimo.remoting.router.RouterTargetMBean"};
    private static final ObjectName RELATION_SERVICE = JMXUtil.getObjectName("geronimo.boot:role=RelationService");
    private static final ObjectName DEPENDS_SERVICE = JMXUtil.getObjectName("geronimo.boot:role=DependencyService");
    private static final ObjectName DEPENDS_SERVICE2 = JMXUtil.getObjectName("geronimo.boot:role=DependencyService2");

    public static MBeanServer newLocalServer() throws Exception {
        MBeanServer mbServer = MBeanServerFactory.createMBeanServer("LocalTestServer");
        mbServer.createMBean("javax.management.relation.RelationService", RELATION_SERVICE, new Object[]{Boolean.TRUE}, new String[]{"boolean"});
        mbServer.createMBean("org.apache.geronimo.kernel.deployment.DependencyService", DEPENDS_SERVICE);
        mbServer.createMBean("org.apache.geronimo.kernel.service.DependencyService2", DEPENDS_SERVICE2);

        return mbServer;
    }

    public static MBeanServer newRemoteServer() throws Exception {
        MBeanServer mbServer = newLocalServer();
        mbServer.createMBean("org.apache.geronimo.remoting.router.SubsystemRouter", SUBSYSTEM);

        mbServer.createMBean("org.apache.geronimo.common.jmx.Relationship", RELATIONSHIP, REL_ARGS, new String[]{"java.lang.String"});

        mbServer.createMBean("org.apache.geronimo.remoting.transport.TransportLoader", LOADER);
        mbServer.setAttribute(LOADER, new Attribute("BindURI", new URI("async://0.0.0.0:3434")));
        mbServer.setAttribute(LOADER, new Attribute("RouterTarget", SUBSYSTEM.toString()));

        mbServer.createMBean("org.apache.geronimo.remoting.router.JMXRouter", JMX_ROUTER);
        MBeanRelationshipMetadata relMetadata = new MBeanRelationshipMetadata("/JMX", "Route", "Target", SUBSYSTEM, "Source");
        HashSet relations = new HashSet();
        relations.add(relMetadata);
        mbServer.invoke(DEPENDS_SERVICE, "addRelationships", new Object[]{JMX_ROUTER, relations}, new String[]{ObjectName.class.getName(), Set.class.getName()});

        mbServer.invoke(SUBSYSTEM, "start", null, null);
        mbServer.invoke(LOADER, "start", null, null);
        mbServer.invoke(JMX_ROUTER, "start", null, null);
        return mbServer;
    }

    public static void stopRemoteServer(MBeanServer mbServer) throws Exception {
        mbServer.invoke(JMX_ROUTER, "stop", null, null);
        mbServer.invoke(LOADER, "stop", null, null);
        mbServer.invoke(SUBSYSTEM, "stop", null, null);
        stopLocalServer(mbServer);
    }

    public static void stopLocalServer(MBeanServer mbServer) throws Exception {
        MBeanServerFactory.releaseMBeanServer(mbServer);
    }
}
