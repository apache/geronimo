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

import java.util.Iterator;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.ParserUtil;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.classspace.ClassSpaceUtil;
import org.apache.geronimo.kernel.classspace.ClassSpaceException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/10/27 21:32:20 $
 */
public class InitializeMBeanInstance implements DeploymentTask {
    private final Log log = LogFactory.getLog(this.getClass());
    private final MBeanServer server;
    private final MBeanMetadata metadata;

    public InitializeMBeanInstance(MBeanServer server, MBeanMetadata metadata) {
        this.server = server;
        this.metadata = metadata;
    }

    public boolean canRun() throws DeploymentException {
        return true;
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


            MBeanInfo mbeanInfo;
            try {
                mbeanInfo = server.getMBeanInfo(metadata.getName());
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IntrospectionException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            }
            MBeanAttributeInfo[] attributeInfos = mbeanInfo.getAttributes();
            Map attributeValues = metadata.getAttributeValues();
            AttributeList attributeList = new AttributeList(attributeValues.size());
            for (int i = 0; i < attributeInfos.length; i++) {
                MBeanAttributeInfo attributeInfo = attributeInfos[i];
                String attributeName = attributeInfo.getName();
                if (!attributeValues.containsKey(attributeName)) {
                    continue;
                }
                Object value = attributeValues.get(attributeName);
                if (value instanceof String) {
                    try {
                        value = ParserUtil.getValue(newCL, attributeInfo.getType(), (String) value, metadata.getBaseURI());
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException(e);
                    }
                }

                attributeList.add(new Attribute(attributeName, value));
            }

            if (log.isTraceEnabled()) {
                for (Iterator i = attributeList.iterator(); i.hasNext();) {
                    Attribute attr = (Attribute) i.next();
                    log.trace("Attribute " + attr.getName() + " will be set to " + attr.getValue());
                }
            }
            try {
                AttributeList attributeResults = server.setAttributes(metadata.getName(), attributeList);
                if (attributeResults.size() != attributeList.size()) {
                    throw new DeploymentException("Unable to set all supplied attributes");
                }
            } catch (InstanceNotFoundException e) {
                throw new DeploymentException(e);
            } catch (ReflectionException e) {
                throw new DeploymentException(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }

    }

    public void undo() {
    }

    public String toString() {
        return "InitailizeMBeanInstance " + metadata.getName();
    }
}
