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
package org.apache.geronimo.deployment;

import java.util.List;
import java.util.Map;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * A module representing a resource being deployed. The deployer will call each
 * method once in the sequence:
 * <li>init</li>
 * <li>generateClassPath</li>
 * <li>defineGBeans</li>
 * <li>complete</li>
 *
 * Once deployment starts, complete() method must always be called even if
 * problems in the deployment process prevent the other methods being called.
 * complete() may be called without a prior call to init().
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/22 08:10:26 $
 */
public interface DeploymentModule {
    /**
     * Indication to this module that the deployment process is starting.
     */
    void init() throws DeploymentException;

    /**
     * Perform any callbacks needed to define the classpath this module needs
     * the resulting Configuration to contain. This would typically involve
     * callbacks to add files to the Configuration or to add URLs for external
     * resources
     * @param callback the callback to use to interact with the deployer
     * @throws DeploymentException if there was a problem generating the classpath
     */
    void generateClassPath(ConfigurationCallback callback) throws DeploymentException;

    /**
     * Perform callbacks needed to define GBeans in the resulting Configuration.
     * @param callback the callback to use to interact with the deployer
     * @param cl a ClassLoader created by the deployer which is guaranteed to
     *           contain all the classpath entries this module added in the prior
     *           call to generateClassPath
     * @throws DeploymentException
     */
    void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException;

    /**
     * Indication from the deployer that its use of this module is complete.
     */
    void complete();
}
