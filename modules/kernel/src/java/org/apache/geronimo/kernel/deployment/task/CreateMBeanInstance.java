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
package org.apache.geronimo.kernel.deployment.task;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.RelationServiceMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.classspace.ClassSpaceException;
import org.apache.geronimo.kernel.classspace.ClassSpaceUtil;
import org.apache.geronimo.kernel.deployment.DependencyServiceMBean;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.service.MBeanDependencyMetadata;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.service.MBeanRelationshipMetadata;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.ParserUtil;

/**
 * Creates an new MBean instance and intializes it according to the specified MBeanMetadata metadata
 *
 * @version $Revision: 1.3 $ $Date: 2003/11/11 04:39:37 $
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
            log.trace("Cannot run because class space is not registered: loaderName=" + loaderName);
            canRun = false;
        }

        Set relationships = metadata.getRelationships();
        List relationTypeNames = relationService.getAllRelationTypeNames();
        for (Iterator i = relationships.iterator(); i.hasNext();) {
            MBeanRelationshipMetadata relationship = (MBeanRelationshipMetadata) i.next();

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
            // Set the class loader
            try {
                newCL = ClassSpaceUtil.setContextClassLoader(server, metadata.getLoaderName());
            } catch (ClassSpaceException e) {
                throw new DeploymentException(e);
            }

            // Create and register the MBean
            try {
                // Get the constructor arguments
                List constructorTypeStrings = metadata.getConstructorTypes();
                List constructorTypes = new ArrayList(constructorTypeStrings.size());
                List constructorValues = metadata.getConstructorArgs();
                for (int i = 0; i < constructorTypeStrings.size(); i++) {
                    Class type = null;
                    try {
                        type = ParserUtil.loadClass((String) constructorTypeStrings.get(i), newCL);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException(e, metadata);
                    }
                    constructorTypes.add(type);

                    Object value = constructorValues.get(i);
                    if (value instanceof String) {
                        value = ParserUtil.getValue(type, (String) value, baseURI);
                        constructorValues.set(i, value);
                    }
                }

                // Create the mbean
                if (log.isTraceEnabled()) {
                    log.trace("Creating MBean name=" + metadata.getName() + " class=" + metadata.getCode());
                }

                Class mbeanClass = newCL.loadClass(metadata.getCode());
                Constructor mbeanConstructor = mbeanClass.getConstructor((Class[]) constructorTypes.toArray(new Class[constructorTypes.size()]));
                Object mbean = mbeanConstructor.newInstance(constructorValues.toArray());
                actualName = server.registerMBean(mbean, metadata.getName()).getObjectName();
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
                    MBeanDependencyMetadata dependency = (MBeanDependencyMetadata) i.next();
                    dependencies.add(new ObjectName(dependency.getName()));
                }
                dependencyService.addStartDependencies(actualName, dependencies);
                dependencyService.addStartDependency(actualName, metadata.getParentName());
                dependencyService.addRelationships(actualName, metadata.getRelationships());
            } catch (Exception e) {
                throw new DeploymentException(e, metadata);
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
        try {
            // Set the class loader
            try {
                ClassSpaceUtil.setContextClassLoader(server, metadata.getLoaderName());
            } catch (ClassSpaceException e) {
                log.warn("Could not set context class loader", e);
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

    public String toString() {
        return "CreateMBeanInstance " + metadata.getName();
    }
}
