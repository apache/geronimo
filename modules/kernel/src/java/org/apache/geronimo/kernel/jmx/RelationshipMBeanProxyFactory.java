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
package org.apache.geronimo.kernel.jmx;

import java.lang.reflect.Proxy;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleInfoNotFoundException;
import javax.management.relation.RoleNotFoundException;

import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.jmx.AbstractMBeanProxyHandler;


/**
 * RelationshipMBeanProxyFactory creates a dynamic proxy to an MBean based on a relationship end point.
 * The interface type, relationship existience, and object existance is not enforced during construction.
 * Instead, if a method is invoked on the proxy and there is no relationship registered with the specified id
 * or if the target role does not conatin any objects, an InvocationTargetException is thrown, which
 * contains an IllegalStateException.  If an interface method that is not implemented by the MBean is invoked,
 * an InvocationTargetException is thrown, which contains an NoSuchMethodException.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:34 $
 */
public final class RelationshipMBeanProxyFactory {
    private RelationshipMBeanProxyFactory() {
    }

    /**
     * Creates a dynamic proxy to the specified relationship and targetRole.  The interface of the dynamic proxy
     * id determined by inspecing the RoleInfo of the target role.
     *
     * @param server the mbean server in which the relationship is registered
     * @param relationshipID the id (name) of the relationship
     * @param targetRole the role which contains the object we will be proxying
     * @return a dynamic proxy to the relationship which implements the interface defined by the relationship\
     * @throws java.lang.IllegalArgumentException if the target role class type is not an interface or if the target
     * roll class could not be loaded
     * @throws java.lang.IllegalStateException if the relationship does not exist
     */
    public static Object getProxy(MBeanServer server, String relationshipID, String targetRole) {
        RelationServiceMBean relationService = JMXUtil.getRelationService(server);
        Class iface = null;
        try {
            String relationType = relationService.getRelationTypeName(relationshipID);
            RoleInfo roleInfo = relationService.getRoleInfo(relationType, targetRole);
            iface = Thread.currentThread().getContextClassLoader().loadClass(roleInfo.getRefMBeanClassName());
            if (!iface.isInterface()) {
                throw new IllegalArgumentException("Target Role type is not an interface:" +
                        " relationshipID=" + relationshipID +
                        " targetRole=" + targetRole +
                        " targetRoleClass=" + iface.getName());
            }
        } catch (RelationNotFoundException e) {
            IllegalStateException illegalStateException = new IllegalStateException("MBean relationshipID is not registered: relationshipID=" + relationshipID);
            illegalStateException.initCause(e);
            throw illegalStateException;
        } catch (RelationTypeNotFoundException e) {
            // this is highly unlikely because we got the relation type from the MBeanServer, but the type could
            // be unregistered between getting it and accessing the role
            IllegalStateException illegalStateException = new IllegalStateException("MBean relationshipID type is not registered: relationshipID=" + relationshipID);
            illegalStateException.initCause(e);
            throw illegalStateException;
        } catch (RoleInfoNotFoundException e) {
            IllegalStateException illegalStateException = new IllegalStateException("MBean relationshipID does not have target role:" +
                    " relationshipID=" + relationshipID +
                    " target=" + targetRole);
            illegalStateException.initCause(e);
            throw illegalStateException;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Could not load Target Role class:" +
                    " relationshipID=" + relationshipID +
                    " targetRole=" + targetRole +
                    " targetRoleClass=" + iface.getName());
        }
        return getProxy(iface, server, relationshipID, targetRole);
    }

    /**
     * Creates a dynamic proxy to the specified relationship and targetRole, which implements the specified
     * interface.
     *
     * @param iface the interface of proxy
     * @param server the mbean server in which the relationship is registered
     * @param relationshipID the id (name) of the relationship
     * @param targetRole the role which contains the object we will be proxying
     * @return a dynamic proxy to the relationship which implements the interface defined by the relationship\
     */
    public static Object getProxy(Class iface, MBeanServer server, String relationshipID, String targetRole) {
        assert iface != null;
        assert iface.isInterface();
        assert server != null;

        ClassLoader cl = iface.getClassLoader();
        return Proxy.newProxyInstance(cl, new Class[]{iface}, new LocalHandler(iface, server, relationshipID, targetRole));
    }

    private static final class LocalHandler extends AbstractMBeanProxyHandler {
        private final String relationshipID;
        private final String targetRole;

        public LocalHandler(Class iface, MBeanServer server, String relationshipID, String targetRole) {
            super(iface, server);
            this.relationshipID = relationshipID;
            this.targetRole = targetRole;
        }

        public ObjectName getObjectName() {
            // @todo this should cache the related set and listen for change notifications from relationService
            RelationServiceMBean relationService = JMXUtil.getRelationService(server);
            List relatedMBeans = null;
            try {
                relatedMBeans = relationService.getRole(relationshipID, targetRole);
            } catch (RelationServiceNotRegisteredException e) {
                throw new IllegalStateException("MBean relation service is not deployed");
            } catch (RelationNotFoundException e) {
                throw new IllegalStateException("MBean relationshipID is not registered: relationshipID=" + relationshipID);
            } catch (RoleNotFoundException e) {
                throw new IllegalStateException("MBean relationshipID does not have target role:" +
                        " relationshipID=" + relationshipID +
                        " target=" + targetRole);
            }
            if (relatedMBeans.size() > 1) {
                throw new IllegalStateException("MBean is related to more the one MBean");
            }
            if (relatedMBeans.isEmpty()) {
                throw new IllegalStateException("MBean does not have a realated MBean");
            }
            return (ObjectName) relatedMBeans.iterator().next();
        }
    }
}
