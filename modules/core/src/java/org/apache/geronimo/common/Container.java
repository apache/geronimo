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

import javax.management.ObjectName;

import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;
import org.apache.geronimo.common.Component;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:45:02 $
 */
public interface Container extends Component {
    //
    //  Main entry point
    //
    InvocationResult invoke(Invocation invocation) throws Exception;

    /**
     * Get the JMX object name of the logical plugin.
     * @param logicalPluginName the logical name of the desired plugin
     * @return the JMX object name associated with the logical plugin, or null if a name is not found
     */
    ObjectName getPlugin(String logicalPluginName);

    /**
     * Puts the objectName in the container.
     * @param logicalPluginName the logical name of the plugin to set
     * @param objectName the JMX object name to set
     */
    void putPlugin(String logicalPluginName, ObjectName objectName);

    /**
     * Gets the named plugin as an Object.
     * @deprecated Switch plugin to a JMX object an use 'ObjectName getPlugin(String name)' instead
     * @param logicalPluginName the name of the plugin to get
     * @return the actual plugin object
     */
    Object getPluginObject(String logicalPluginName);

    /**
     * Puts the named plugin Object in the container.
     * @deprecated Switch plugin to a JMX object an use 'void putPlugin(String name, ObjectName objectName)' instead
     * @param logicalPluginName the name of the plugin to get
     * @param plugin the plugin obect or null to remove an existing plugin
     */
    void putPluginObject(String logicalPluginName, Object plugin);
}
