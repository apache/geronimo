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

package org.apache.geronimo.test.util;

import org.apache.geronimo.kernel.jmx.JMXUtil;
//import org.apache.geronimo.kernel.deployment.service.MBeanRelationshipMetadata;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Attribute;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/10 09:58:44 $
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
        mbServer.createMBean("org.apache.geronimo.gbean.jmx.DependencyService2", DEPENDS_SERVICE2);

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
        HashSet relations = new HashSet();
        // DMB: Hacked!
        //MBeanRelationshipMetadata relMetadata = new MBeanRelationshipMetadata("/JMX", "Route", "Target", SUBSYSTEM, "Source");
        //relations.add(relMetadata);
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
