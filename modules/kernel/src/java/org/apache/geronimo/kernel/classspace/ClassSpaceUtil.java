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
package org.apache.geronimo.kernel.classspace;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * Helper methods for class spaces.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/27 20:58:38 $
 */
public class ClassSpaceUtil {
    private ClassSpaceUtil() {
    }

    /**
     * Gets the class loader associated with the specified class space.
     * @param server the MBean server to search for the class space
     * @param classSpaceName name of the class space
     * @return the class loader associated with the class space
     * @throws ClassSpaceException if the class space was not found or if the class loader is not running
     */
    public static ClassLoader getClassLoader(MBeanServer server, ObjectName classSpaceName) throws ClassSpaceException {
        if(classSpaceName != null) {
            ClassLoader cl = null;
            try {
                cl = (ClassLoader) server.invoke(classSpaceName, "getClassLoader", null, null);
            } catch (JMException e) {
                throw new ClassSpaceException("Could not get class loader from class space", e);
            }
            if(cl == null) {
                throw new ClassSpaceException("Class space is not in the running or stopping state");
            }
            return cl;
        } else {
            return ClassLoader.getSystemClassLoader();
        }
    }

    /**
     * Sets the class loader associated with the specified class space into the thread context class loader.
     * @param server the MBean server to search for the class space
     * @param classSpaceName name of the class space
     * @return the class loader that was set into the thread context class loader
     * @throws ClassSpaceException if the class space was not found or if the class loader is not running
     */
    public static ClassLoader setContextClassLoader(MBeanServer server, ObjectName classSpaceName) throws ClassSpaceException {
        ClassLoader newCL = getClassLoader(server, classSpaceName);
        Thread.currentThread().setContextClassLoader(newCL);
        return newCL;
    }
}
