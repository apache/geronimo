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
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.RelationServiceMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.common.StringValueParser;

import org.apache.geronimo.core.util.ClassUtil;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.dependency.DependencyServiceMBean;
import org.apache.geronimo.deployment.service.MBeanDependency;
import org.apache.geronimo.deployment.service.MBeanMetadata;
import org.apache.geronimo.deployment.service.MBeanRelationship;
import org.apache.geronimo.jmx.JMXUtil;

/**
 * Creates an new MBean instance and intializes it according to the specified MBeanMetadata metadata
 *
 * @version $Revision: 1.10 $ $Date: 2003/08/24 22:40:24 $
 */
public class CreateMBeanInstance implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final MBeanServer server;
    private final MBeanMetadata metadata;
    private final URI baseURI;
    private final DependencyServiceMBean dependencyService;
    private final RelationServiceMBean relationService;
    private ObjectName actualName;

    public CreateMBeanInstance(MBeanServer server, MBeanMetadata metadata) throws DeploymentException {
        this.server = server;
        this.metadata = metadata;
        this.baseURI = metadata.getBaseURI();
        dependencyService = JMXUtil.getDependencyService(server);
        relationService = JMXUtil.getRelationService(server);
    }

    public boolean canRun() throws DeploymentException {
        boolean canRun = true;

        ObjectName loaderName = metadata.getLoaderName();
        if (loaderName != null && !server.isRegistered(loaderName)) {
            log.trace("Cannot run because class loader is not registered: loaderName=" + loaderName);
            canRun = false;
        }

        Set relationships = metadata.getRelationships();
        List relationTypeNames = relationService.getAllRelationTypeNames();
        for (Iterator i = relationships.iterator(); i.hasNext();) {
            MBeanRelationship relationship = (MBeanRelationship) i.next();

            // if there is no existing relationship...
            String relationshipName = relationship.getName();
            if (!relationService.hasRelation(relationshipName).booleanValue()) {
                // check if the relationship type has been registered
                String relationshipType = relationship.getType();
                if (!relationTypeNames.contains(relationshipType)) {
                    log.trace("Cannot run because relationship type is not registered: relationType=" + relationshipType);
                    canRun = false;
                }
            }
        }
        return canRun;
    }

    public void perform() throws DeploymentException {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCL;

        // create an MBean instance
        try {
            // Get the class loader
            try {
                newCL = server.getClassLoader(metadata.getLoaderName());
                Thread.currentThread().setContextClassLoader(newCL);
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            }

            // Create and register the MBean
            try {
                // Get the constructor arguments
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

                // Create the mbean
                if (log.isTraceEnabled()) {
                    log.trace("Creating MBean name=" + metadata.getName() + " class=" + metadata.getCode());
                }
                actualName = server.createMBean(metadata.getCode(), metadata.getName(), metadata.getLoaderName(), consValues, consTypes).getObjectName();
                if (log.isTraceEnabled() && !actualName.equals(metadata.getName())) {
                    log.trace("Actual MBean name is " + actualName);
                }
                metadata.setName(actualName);

                // Add the mbean to it's parent
                ObjectName parentName = metadata.getParentName();
                if (parentName != null) {
                    server.invoke(metadata.getParentName(), "addChild", new Object[]{actualName}, new String[]{"javax.management.ObjectName"});
                }

                // Register the dependencies with the dependecy service
                Set dependencies = new HashSet();
                for (Iterator i = metadata.getDependencies().iterator(); i.hasNext();) {
                    MBeanDependency dependency = (MBeanDependency) i.next();
                    dependencies.add(new ObjectName(dependency.getName()));
                }
                dependencyService.addStartDependencies(actualName, dependencies);
                dependencyService.addStartDependency(actualName, metadata.getParentName());
                dependencyService.addRelationships(actualName, metadata.getRelationships());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException(e);
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

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        ClassLoader newCL;
        try {
            // Get the class loader
            try {
                newCL = server.getClassLoader(metadata.getLoaderName());
                Thread.currentThread().setContextClassLoader(newCL);
            } catch (InstanceNotFoundException e) {
                log.warn("Class loader not found", e);
                return;
            }

            try {
                server.invoke(metadata.getParentName(), "removeChild", new Object[]{actualName}, new String[]{"javax.management.ObjectName"});
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
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    private static final Class[] stringArg = new Class[]{String.class};

    private Object getValue(ClassLoader cl, String typeName, String value) throws DeploymentException {
        StringValueParser parser = new StringValueParser();
        value = parser.parse(value);
        
        if("java.net.URI".equals(typeName)) {
            return baseURI.resolve(value);
        }
        if("java.net.URL".equals(typeName)) {
            try {
                return baseURI.resolve(value).toURL();
            } catch (MalformedURLException e) {
                throw new DeploymentException(e);
            }
        }
        if("java.io.File".equals(typeName)) {
            return new File(baseURI.resolve(value));
        }

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
