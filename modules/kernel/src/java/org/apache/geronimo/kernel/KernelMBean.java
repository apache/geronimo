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
package org.apache.geronimo.kernel;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.InvalidConfigException;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/24 06:05:37 $
 */
public interface KernelMBean {
    /**
     * Get the MBeanServer used by this kernel
     * @return the MBeanServer used by this kernel
     */
    MBeanServer getMBeanServer();

    /**
     * Get the name of this kernel
     * @return the name of this kernel
     */
    String getKernelName();

    /**
     * Load a specific GBean into this kernel.
     * This is intended for applications that are embedding the kernel.
     * @param name the name to register the GBean under
     * @param gbean the GBean to register
     * @throws InstanceAlreadyExistsException if the name is already used
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException if there is a problem during registration
     */
    void loadGBean(ObjectName name, GBeanMBean gbean) throws InstanceAlreadyExistsException, InvalidConfigException;

    /**
     * Start a specific GBean.
     * @param name the GBean to start
     * @throws InstanceNotFoundException if the GBean could not be found
     */
    void startGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException;

    /**
     * Start a specific GBean and its children.
     * @param name the GBean to start
     * @throws javax.management.InstanceNotFoundException if the GBean could not be found
     */
    void startRecursiveGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException;

    /**
     * Stop a specific GBean.
     * @param name the GBean to stop
     * @throws javax.management.InstanceNotFoundException if the GBean could not be found
     */
    void stopGBean(ObjectName name) throws InstanceNotFoundException, InvalidConfigException;

    /**
     * Unload a specific GBean.
     * This is intended for applications that are embedding the kernel.
     * @param name the name of the GBean to unregister
     * @throws javax.management.InstanceNotFoundException if the GBean could not be found
     */
    void unloadGBean(ObjectName name) throws InstanceNotFoundException;

    boolean isRunning();

    ConfigurationManager getConfigurationManager();

    Object getAttribute(ObjectName objectName, String attributeName) throws Exception;

    void setAttribute(ObjectName objectName, String attributeName, Object attributeValue) throws Exception;

    Object invoke(ObjectName objectName, String methodName) throws Exception;

    Object invoke(ObjectName objectName, String methodName, Object[] args, String[] types) throws Exception;

    boolean isLoaded(ObjectName name);
}
