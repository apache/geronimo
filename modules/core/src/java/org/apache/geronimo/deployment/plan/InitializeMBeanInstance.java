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
package org.apache.geronimo.deployment.plan;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.util.ClassUtil;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.MBeanMetadata;
import org.apache.geronimo.deployment.service.MBeanOperation;
import org.apache.geronimo.deployment.service.MBeanRelationship;
import org.apache.geronimo.jmx.JMXUtil;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/14 00:02:38 $
 */
public class InitializeMBeanInstance implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final Set plans;
    private final MBeanServer server;
    private final RelationServiceMBean relationService;
    private final ObjectName parent;
    private final ObjectName loaderName;
    private final MBeanMetadata metadata;
    private ObjectName objectName;
    private boolean createCalled;

    public InitializeMBeanInstance(Set plans, MBeanServer server, ObjectName objectName, ObjectName parent, MBeanMetadata metadata, ObjectName loaderName) {
        this.plans = plans;
        this.server = server;
        this.objectName = objectName;
        this.parent = parent;
        this.metadata = metadata;
        this.loaderName = loaderName;
        relationService = JMXUtil.getRelationService(server);
    }

    public boolean canRun() throws DeploymentException {
        boolean canRun = true;

        if (!server.isRegistered(objectName)) {
            log.trace("Plan can run because MBean has been unregistered.  Plan will execute but will do nothing");
            return true;
        }

        Set relationships = metadata.getRelationships();
        for (Iterator i = relationships.iterator(); i.hasNext();) {
            MBeanRelationship relationship = (MBeanRelationship) i.next();

            // if there is no existing relationship...
            String relationshipName = relationship.getName();
            if (!relationService.hasRelation(relationshipName).booleanValue()) {
                // check if the relationship type has been registered
                String relationshipType = relationship.getType();
                if (!relationService.getAllRelationTypeNames().contains(relationshipType)) {
                    log.trace("Cannot run because relationship type is not registered: relationType=" + relationshipType);
                    canRun = false;
                }

                // if we have a target, check that is is registered
                String target = relationship.getTarget();
                if (target != null && target.length() > 0) {
                    try {
                        if (!server.isRegistered(new ObjectName(target))) {
                            log.trace("Cannot run because relationship target object is not registered: target=" + target);
                            canRun = false;
                        }
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("Target is not a valid ObjectName: target=" + target);
                    }
                }
            }
        }
        return canRun;
    }

    public void perform() throws DeploymentException {
        if (!server.isRegistered(objectName)) {
            return;
        }

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCL;

        // create an MBean instance
        try {
            // Get the class loader
            try {
                newCL = server.getClassLoader(loaderName);
                Thread.currentThread().setContextClassLoader(newCL);
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            }


            // Resolve and enroll in all relationships
            Set relationships = metadata.getRelationships();
            try {
                for (Iterator i = relationships.iterator(); i.hasNext();) {
                    MBeanRelationship relationship = (MBeanRelationship) i.next();

                    // if we don't have a relationship instance create one
                    String relationshipName = relationship.getName();
                    String relationshipRole = relationship.getRole();
                    if (!relationService.hasRelation(relationshipName).booleanValue()) {
                        // if  we don't have a relationship of the
                        String relationshipType = relationship.getType();
                        if (!relationService.getAllRelationTypeNames().contains(relationshipType)) {
                            throw new DeploymentException("Relationship type is not registered: relationType=" + relationshipType);
                        }

                        RoleList roleList = new RoleList();
                        roleList.add(new Role(relationshipRole, Collections.singletonList(objectName)));

                        // if we have a target we need to add it to the role list
                        String target = relationship.getTarget();
                        if (target != null && target.length() > 0) {
                            String targetRoleName = relationship.getTargetRole();
                            if (targetRoleName == null || targetRoleName.length() == 0) {
                                List roles = relationService.getRoleInfos(relationshipType);
                                if (roles.size() < 2) {
                                    throw new DeploymentException("Relationship has less than two roles. You cannot specify a target");
                                }
                                if (roles.size() > 2) {
                                    throw new DeploymentException("Relationship has more than two roles. You must use targetRoleName");
                                }
                                if (((RoleInfo) roles.get(0)).getName().equals(relationshipRole)) {
                                    targetRoleName = ((RoleInfo) roles.get(1)).getName();
                                } else {
                                    targetRoleName = ((RoleInfo) roles.get(0)).getName();
                                }
                            }

                            roleList.add(new Role(targetRoleName, Collections.singletonList(new ObjectName(target))));
                        }
                        relationService.createRelation(relationshipName, relationshipType, roleList);
                    } else {
                        // We have an exiting relationship -- just add to the existing role
                        List members = relationService.getRole(relationshipName, relationshipRole);
                        members.add(objectName);
                        relationService.setRole(relationshipName, new Role(relationshipRole, members));
                    }
                }
            } catch (DeploymentException e) {
                throw e;
            } catch (Exception e) {
                throw new DeploymentException(e);
            }

            // Invoke the create callback method
            try {
                server.invoke(objectName, "create", null, null);
                createCalled = true;
            } catch (RuntimeException e) {
                throw new DeploymentException(e);
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (MBeanException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                if (e.getTargetException() instanceof NoSuchMethodException) {
                    // did not have a create method - ok
                } else {
                    throw new DeploymentException(e);
                }
            }

            // Add a deployment plan to start the MBean
            DeploymentPlan startPlan = new DeploymentPlan();
            startPlan.addTask(new StartMBeanInstance(server, objectName));
            List operations = metadata.getOperations();
            for (Iterator i = operations.iterator(); i.hasNext();) {
                MBeanOperation operation = (MBeanOperation) i.next();
                int argCount = operation.getTypes().size();
                String[] argTypes = (String[]) operation.getTypes().toArray(new String[argCount]);
                List values = operation.getArgs();
                Object[] args = new Object[argCount];
                for (int j = 0; j < argCount; j++) {
                    Object value = values.get(j);
                    if (value instanceof String) {
                        value = getValue(newCL, argTypes[j], (String) value);
                    }
                    args[j] = value;
                }
                startPlan.addTask(new InvokeMBeanOperation(server, objectName, operation.getOperation(), argTypes, args));
            }
            plans.add(startPlan);
        } catch (DeploymentException e) {
            undo();
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    public void undo() {
        if (objectName == null) {
            return;
        }

        try {
            if (createCalled) {
                server.invoke(objectName, "destroy", null, null);
            }
        } catch (InstanceNotFoundException e) {
            log.warn("MBean was already removed " + objectName, e);
            return;
        } catch (MBeanException e) {
            log.error("Error while destroying MBean " + objectName, e);
        } catch (ReflectionException e) {
            if (e.getTargetException() instanceof NoSuchMethodException) {
                // did not have a destroy method - ok
            } else {
                log.error("Error while destroying MBean " + objectName, e);
            }
        }

        try {
            server.invoke(parent, "removeChild", new Object[]{objectName}, new String[]{"javax.management.ObjectName"});
        } catch (InstanceNotFoundException e) {
            log.warn("Could not remove from parent", e);
        } catch (MBeanException e) {
            log.error("Error while removing MBean " + objectName + " from parent", e);
        } catch (ReflectionException e) {
            log.error("Error while removing MBean " + objectName + " from parent", e);
        }

        try {
            server.unregisterMBean(objectName);
        } catch (InstanceNotFoundException e) {
            log.warn("MBean was already removed " + objectName, e);
            return;
        } catch (MBeanRegistrationException e) {
            log.error("Error while unregistering MBean " + objectName, e);
        }
    }

    private static final Class[] stringArg = new Class[]{String.class};

    private Object getValue(ClassLoader cl, String typeName, String value) throws DeploymentException {
        Class attrType = null;
        try {
            attrType = ClassUtil.getClassForName(cl, typeName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(e);
        }

        // try a property editor
        PropertyEditor editor = PropertyEditorManager.findEditor(attrType);
        if (editor != null) {
            editor.setAsText(value);
            return editor.getValue();
        }

        // try a String constructor
        try {
            Constructor cons = attrType.getConstructor(stringArg);
            return cons.newInstance(new Object[]{value});
        } catch (Exception e) {
            throw new DeploymentException("Could not create value of type " + typeName);
        }
    }

    public String toString() {
        return "InitailizeMBeanInstance " + metadata.getName();
    }
}
