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
package org.apache.geronimo.enterprise.deploy.server;

import java.rmi.RemoteException;

/**
 * The methods a DConfigBean may need to get information from the server.
 * Whether this turns out to be useful depends on whether the current
 * deployment managed is connected or not, among other things.  In truth, this
 * will be used more by the DConfigBean property editors than by the DCBs
 * themselves.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/06 14:35:33 $
 */
public interface DConfigBeanLookup {
    /**
     * Used to provide a list of security users/groups/roles that the deployer
     * can map a J2EE security role to.
     *
     * @param securityRealm The security realm in use by the application
     *
     * @return A list of security mapping options, or null if the current user
     *         is not authorized to retrieve that information, or the
     *         information is not available.
     */
    public String[] getSecurityRoleOptions(String securityRealm);

    /**
     * Gets a list of the JNDI names of global resources of a particular type
     * defined in the server.  For example, a list of all javax.sql.DataSource
     * resources.  Note that any resources tied to a particular application
     * will not be included.
     *
     * @param resourceClassName The name of the interface that the resource
     *                          should implement (e.g. javax.sql.DataSource).
     *
     * @return A list of the JNDI names of the available resources.  Returns
     *         null of no such resources are available, the current user is
     *         not authorized to retrieve the list, etc.
     */
    public String[] getResourceJndiNames(String resourceClassName);
}
