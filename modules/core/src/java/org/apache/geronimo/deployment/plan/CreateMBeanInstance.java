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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import javax.management.relation.RoleList;
import javax.management.relation.RoleInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.util.ClassUtil;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.service.MBeanMetadata;
import org.apache.geronimo.deployment.service.MBeanRelationship;
import org.apache.geronimo.deployment.service.MBeanOperation;
import org.apache.geronimo.jmx.JMXUtil;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/13 04:15:43 $
 */
public class CreateMBeanInstance extends DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final Set plans;
    private final MBeanServer server;
    private final RelationServiceMBean relationService;
    private final ObjectName parent;
    private final ObjectName loaderName;
    private final MBeanMetadata metadata;
    private ObjectName actualName;
    private boolean createCalled;

    public CreateMBeanInstance(Set plans, MBeanServer server, ObjectName parent, MBeanMetadata metadata, ObjectName loaderName) {
        this.plans = plans;
        this.server = server;
        this.parent = parent;
        this.metadata = metadata;
        this.loaderName = loaderName;
        relationService = JMXUtil.getRelationService(server);
    }

    public void perform() throws DeploymentException {
        boolean trace = log.isTraceEnabled();
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCL;

        // create an MBean instance
        try {
            try {
                newCL = server.getClassLoader(loaderName);
                Thread.currentThread().setContextClassLoader(newCL);
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            }

            try {

                Object[] consValues = metadata.getConstructorArgs().toArray();
                List constructorTypes = metadata.getConstructorTypes();
                String[] consTypes = (String[]) constructorTypes.toArray(new String[constructorTypes.size()]);
                for (int i = 0; i < consTypes.length; i++) {
                    String consType = consTypes[i];
                    Object value = consValues[i];
                    if (value instanceof String) {
                        value = getValue(newCL, consType, (String) value);
                        consValues[i] = value;
                    }
                }
                if (trace) {
                    log.trace("Creating MBean name=" + metadata.getName() + " class=" + metadata.getCode());
                }
                actualName = server.createMBean(metadata.getCode(), metadata.getName(), loaderName, consValues, consTypes).getObjectName();
                if (trace && !actualName.equals(metadata.getName())) {
                    log.trace("Actual MBean name is " + actualName);
                }
                server.invoke(parent, "addChild", new Object[]{actualName}, new String[]{"javax.management.ObjectName"});
            } catch (RuntimeException e) {
                throw new DeploymentException(e);
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            } catch (InstanceAlreadyExistsException e) {
                throw new DeploymentException(e);
            } catch (MBeanException e) {
                throw new DeploymentException(e);
            } catch (NotCompliantMBeanException e) {
                throw new DeploymentException(e);
            }

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
                        roleList.add(new Role(relationshipRole, Collections.singletonList(actualName)));

                        // if we have a target we need to add it to the role list
                        String target = relationship.getTarget();
                        if (target != null && target.length() > 0) {
                            List roles = relationService.getRoleInfos(relationshipType);

                            String targetRoleName;
                            if (((RoleInfo) roles.get(0)).getName().equals(relationshipRole)) {
                                targetRoleName = ((RoleInfo) roles.get(1)).getName();
                            } else {
                                targetRoleName = ((RoleInfo) roles.get(0)).getName();
                            }
                            roleList.add(new Role(targetRoleName, Collections.singletonList(new ObjectName(target))));
                        }
                        relationService.createRelation(relationshipName, relationshipType, roleList);

                    } else {
                        // We have an exiting relationship -- just add to the existing role
                        List members = relationService.getRole(relationshipName, relationshipRole);
                        members.add(actualName);
                        relationService.setRole(relationshipName, new Role(relationshipRole, members));
                    }
                }
            } catch (DeploymentException e) {
                throw e;
            } catch (Exception e) {
                throw new DeploymentException(e);
            }

            // set its attributes
            MBeanInfo mbInfo;
            try {
                mbInfo = server.getMBeanInfo(actualName);
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IntrospectionException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            }
            MBeanAttributeInfo[] attrInfo = mbInfo.getAttributes();
            Map attributeValues = metadata.getAttributeValues();
            AttributeList attrs = new AttributeList(attributeValues.size());
            for (int i = 0; i < attrInfo.length; i++) {
                MBeanAttributeInfo mBeanAttributeInfo = attrInfo[i];
                String attrName = mBeanAttributeInfo.getName();
                if (!attributeValues.containsKey(attrName)) {
                    continue;
                }
                Object value = attributeValues.get(attrName);
                if (value instanceof String) {
                    value = getValue(newCL, mBeanAttributeInfo.getType(), (String) value);
                }

                attrs.add(new Attribute(attrName, value));
            }

            if (trace) {
                for (Iterator i = attrs.iterator(); i.hasNext();) {
                    Attribute attr = (Attribute) i.next();
                    log.trace("Attribute " + attr.getName() + " will be set to " + attr.getValue());
                }
            }
            try {
                AttributeList attrsSet = server.setAttributes(actualName, attrs);
                if (attrsSet.size() != attrs.size()) {
                    throw new DeploymentException("Unable to set all supplied attributes");
                }
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            }

            // invoke its create method
            try {
                server.invoke(actualName, "create", null, null);
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

            DeploymentPlan startPlan = new DeploymentPlan();
            startPlan.addTask(new StartMBeanInstance(server, actualName));
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
                startPlan.addTask(new InvokeMBeanOperation(server, actualName, operation.getOperation(), argTypes, args));
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
        if (actualName == null) {
            return;
        }

        try {
            if (createCalled) {
                server.invoke(actualName, "destroy", null, null);
            }
        } catch (InstanceNotFoundException e) {
            log.warn("MBean was already removed " + actualName, e);
            return;
        } catch (MBeanException e) {
            log.error("Error while destroying MBean " + actualName, e);
        } catch (ReflectionException e) {
            if (e.getTargetException() instanceof NoSuchMethodException) {
                // did not have a destroy method - ok
            } else {
                log.error("Error while destroying MBean " + actualName, e);
            }
        }

        try {
            server.invoke(parent, "removeChild", new Object[]{actualName}, new String[]{"javax.management.ObjectName"});
        } catch (InstanceNotFoundException e) {
            log.warn("Could not remove from parent", e);
        } catch (MBeanException e) {
            log.error("Error while removing MBean " + actualName + " from parent", e);
        } catch (ReflectionException e) {
            log.error("Error while removing MBean " + actualName + " from parent", e);
        }
        try {
            server.unregisterMBean(actualName);
        } catch (InstanceNotFoundException e) {
            log.warn("MBean was already removed " + actualName, e);
            return;
        } catch (MBeanRegistrationException e) {
            log.error("Error while unregistering MBean " + actualName, e);
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
        return "CreateMBeanInstance " + metadata.getName();
    }
}
