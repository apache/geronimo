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

import java.util.Collection;
import java.util.ArrayList;
import java.net.URI;
import java.io.File;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GEndpointInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.kernel.config.ConfigurationParent;

/**
 * TODO this does not put the deployers in any particular order.  This may be a problem.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/19 06:40:07 $
 *
 * */
public class BatchDeployerFactory {

    private static final GBeanInfo GBEAN_INFO;

    private int id = 0;

    private static File tmpDir = new File(System.getProperty("java.io.tmpdir"), "geronimo");


    private Collection deployers;

    public Collection getDeployers() {
        return deployers;
    }

    public void setDeployers(Collection deployers) {
        this.deployers = deployers;
    }

    public BatchDeployer getBatchDeployer(ConfigurationParent configurationParent, URI configID, File workingDir) {
        return new BatchDeployer(configurationParent, configID, new ArrayList(deployers), workingDir);
    }

    public synchronized File createWorkDir() {
        while (true) {
            File result = new File(tmpDir, "package" + id++);
            if (!result.exists()) {
                result.mkdirs();
                return result;
            }
        }
    }


    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(BatchDeployerFactory.class.getName());
        infoFactory.addOperation(new GOperationInfo("getBatchDeployer", new String[] {ConfigurationParent.class.getName(), URI.class.getName(), File.class.getName()}));
        infoFactory.addOperation(new GOperationInfo("createWorkDir"));
        infoFactory.addEndpoint(new GEndpointInfo("Deployers", ModuleFactory.class.getName()));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
