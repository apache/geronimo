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
package org.apache.geronimo.console.cli.controller;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.cli.TextController;
import org.apache.geronimo.console.cli.DeploymentContext;
import org.apache.geronimo.console.cli.module.EJBJARInfo;
import org.apache.geronimo.enterprise.deploy.tool.EjbDeployableObject;

/**
 * Loads the deployment descriptor information from the specific EJB JAR
 * file.
 *
 * @version $Revision: 1.1 $ $Date: 2003/10/19 01:56:14 $
 */
public class InitializeEJBJAR extends TextController {
    private static final Log log = LogFactory.getLog(InitializeEJBJAR.class);

    public InitializeEJBJAR(DeploymentContext context) {
        super(context);
    }

    public void execute() {
        if(!(context.moduleInfo instanceof EJBJARInfo)) {
            throw new IllegalStateException("Tried to load an EJB JAR but the current module is "+context.moduleInfo.getClass().getName());
        }
        EJBJARInfo jarInfo = (EJBJARInfo)context.moduleInfo;
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{jarInfo.file.toURL()}, ClassLoader.getSystemClassLoader());
            context.standardModule = new EjbDeployableObject(jarInfo.jarFile, loader);
        } catch(MalformedURLException e) {
            context.out.println("ERROR: "+jarInfo.file+" is not a valid JAR file!");
            context.moduleInfo = null;
            return;
        }
        try {
            context.serverModule = context.deployer.createConfiguration(context.standardModule);
        } catch(InvalidModuleException e) {
            context.out.println("ERROR: Unable to initialize a Geronimo DD for EJB JAR "+jarInfo.file);
            context.moduleInfo = null;
            return;
        }
        jarInfo.ejbJar = context.standardModule.getDDBeanRoot();
        jarInfo.editingEjbJar = true;
        try {
            jarInfo.ejbJarConfig = context.serverModule.getDConfigBeanRoot(jarInfo.ejbJar);
            initializeDConfigBean(jarInfo.ejbJarConfig);
        } catch(ConfigurationException e) {
            log.error("Unable to initialize server-specific deployment information", e);
            context.moduleInfo = null;
            return;
        }
    }
}
