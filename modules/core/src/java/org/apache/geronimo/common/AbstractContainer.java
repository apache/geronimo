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
package org.apache.geronimo.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.dependency.DependencyServiceMBean;
import org.apache.geronimo.jmx.JMXUtil;

/**
 * Abstract implementation of Container interface.
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/16 23:16:18 $
 *
 */
public abstract class AbstractContainer extends AbstractComponent implements Container {
    /**
     * The Dependency Service manager.  We register our components with the is service because
     * we need to stop if any of our services fail or stop.
     */
    private DependencyServiceMBean dependency;

    /**
     * The components owned by this container
     * @todo all accss to this must be synchronized
     */
    private ArrayList components = new ArrayList();

    public ObjectName preRegister(MBeanServer server, ObjectName objectName) throws Exception {
        dependency = JMXUtil.getDependencyService(server);
        return super.preRegister(server, objectName);
    }

    /**
     * A Container cannot be itself contained in a Container.
     *
     * NB. Do we agree this is the model?
     * @return always null
     */
    public Container getContainer() {
        return null;
    }

    /**
     * A Container cannot be itself contained in a Container.
     *
     * NB. Do we agree this is the model?
     * @throws java.lang.UnsupportedOperationException
     */
    public void setContainer() {
        throw new UnsupportedOperationException("Cannot call setContainer on a Container");
    }

    /**
     * Add a component to the set for a Container.
     * Subclasses might like to override this in order
     * to check their state before allowing the addition.
     *
     * @param component
     */
    public void addComponent(Component component) {
        if (component == null) {
            throw new NullArgumentException("component");
        }
        try {
            dependency.addStartDependency(objectName, new ObjectName(component.getObjectName()));
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Component does not have a valid object name: objectName=" + component.getObjectName());
        }
        components.add(component);
    }

    /**
     * Get all the Components known to the Container
     *
     * @return an immutable List of Components
     */
    public List getComponents() {
        return Collections.unmodifiableList(components);
    }

    /**
     * Remove a Component from the Container.
     * If the Component is not in the Container,
     * an Exception is thrown.
     *
     * Subclasses might want to override this, for
     * example to disallow Component addition/removal
     * after the Container is started.
     *
     * @param component the Component to remove
     */
    public void removeComponent(Component component) throws Exception {
        if (component == null) {
            throw new NullArgumentException("component");
        }
        try {
            dependency.removeStartDependency(objectName, new ObjectName(component.getObjectName()));
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Component does not have a valid object name: objectName=" + component.getObjectName());
        }
        components.remove(component);
    }
}
