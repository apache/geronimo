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
import java.util.List;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.loader.ClassSpace;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/10/22 02:04:31 $
 */
public class CreateClassSpace implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final MBeanServer server;
    private final ClassSpaceMetadata metadata;
    private ObjectName actualName;

    public CreateClassSpace(MBeanServer server, ClassSpaceMetadata metadata) {
        this.server = server;
        this.metadata = metadata;
    }

    public boolean canRun() throws DeploymentException {
        return true;
    }

    public void perform() throws DeploymentException {
        ObjectName name = metadata.getName();
        List urls = metadata.getUrls();
        if (!server.isRegistered(name)) {
            // Get the class object for the class space
            // Class must be available from the JMX classloader repoistory
            Class clazz = null;
            try {
                clazz = server.getClassLoaderRepository().loadClass(metadata.getClassName());
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(e);
            }
            if (!ClassSpace.class.isAssignableFrom(clazz)) {
                throw new DeploymentException("Class does not implement ClassSpace: " + clazz.getName());
            }
            if (!ClassLoader.class.isAssignableFrom(clazz)) {
                throw new DeploymentException("Class is not a ClassLoader: " + clazz.getName());
            }

            // Get the constructor
            Constructor constructor = null;
            try {
                constructor = clazz.getConstructor(new Class[]{ClassLoader.class, ObjectName.class});
            } catch (Exception e) {
                throw new DeploymentException("Class does not have the constructor " +
                        clazz.getName() + "(Classloader parent, String name)");
            }

            // Determine the parent classloader
            ObjectName parentName = metadata.getParent();
            ClassLoader parent = null;
            if (parentName != null) {
                try {
                    parent = server.getClassLoader(parentName);
                } catch (InstanceNotFoundException e) {
                    throw new DeploymentException("Parent class loader not found", e);
                }
            } else {
                Thread.currentThread().getContextClassLoader();
                if (parent == null) {
                    parent = ClassLoader.getSystemClassLoader();
                }
            }

            // Construct a class space instance
            ClassSpace space = null;
            try {
                space = (ClassSpace) constructor.newInstance(new Object[]{parent, metadata.getName()});
            } catch (Exception e) {
                // @todo use a typed exception which carries the object name and class type
                throw new DeploymentException("Could not create class space instance", e);
            }

            // Add the URLs from the deployment to the class space
            space.addDeployment(metadata.getDeploymentName(), metadata.getUrls());

            // Register the class loader witht the MBeanServer
            try {
                actualName = server.registerMBean(space, name).getObjectName();
            } catch (Exception e) {
                // @todo use a typed exception which carries the object name and class type
                throw new DeploymentException("Could not register class space with MBeanServer", e);
            }
        } else {
            try {
                server.invoke(name, "addDeployment", new Object[]{metadata.getDeploymentName(), urls}, new String[]{"javax.management.ObjectName", "java.util.List"});
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (MBeanException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            }
        }
    }

    public void undo() {
        // @todo  we have a problem here... class space may have been used so it may now contain bad classes (not a problem when constructing a new space)
        if (actualName != null) {
            try {
                server.unregisterMBean(actualName);
            } catch (InstanceNotFoundException e) {
                log.warn("ClassSpace MBean was already removed " + actualName, e);
                return;
            } catch (MBeanRegistrationException e) {
                log.error("Error while unregistering ClassSpace MBean " + actualName, e);
            }
        }
    }

    public String toString() {
        return "CreateClassSpace " + metadata.getName();
    }
}
