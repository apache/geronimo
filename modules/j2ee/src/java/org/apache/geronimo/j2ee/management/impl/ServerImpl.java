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
package org.apache.geronimo.j2ee.management.impl;

import java.util.Collection;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.j2ee.management.J2EEDeployedObject;
import org.apache.geronimo.j2ee.management.J2EEResource;
import org.apache.geronimo.j2ee.management.JVM;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/22 05:19:10 $
 */
public class ServerImpl {
    private static final String SERVER_VENDOR = "The Apache Software Foundation";
    private final ServerInfo serverInfo;
    private final Collection deployedObjects;
    private final Collection resources;
    private final Collection jvms;

    public ServerImpl(ServerInfo serverInfo, Collection deployedObjects, Collection resources, Collection jvms) {
        this.serverInfo = serverInfo;
        this.deployedObjects = deployedObjects;
        this.resources = resources;
        this.jvms = jvms;
    }

    public String[] getdeployedObjects() {
        return Util.getObjectNames(deployedObjects);
    }

    public String[] getresources() {
        return Util.getObjectNames(resources);
    }

    public String[] getjavaVMs() {
        return Util.getObjectNames(jvms);
    }

    public String getserverVendor() {
        return SERVER_VENDOR;
    }

    public String getserverVersion() {
        return serverInfo.getVersion();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ServerImpl.class);
        infoFactory.addAttribute("deployedObjects", false);
        infoFactory.addAttribute("resources", false);
        infoFactory.addAttribute("javaVMs", false);
        infoFactory.addAttribute("serverVendor", false);
        infoFactory.addAttribute("serverVersion", false);
        infoFactory.addReference("ServerInfo", ServerInfo.class);
        infoFactory.addReference("DeployedObjects", J2EEDeployedObject.class);
        infoFactory.addReference("Resources", J2EEResource.class);
        infoFactory.addReference("JVMs", JVM.class);
        infoFactory.setConstructor(
                new String[]{"ServerInfo", "DeployedObjects", "Resources", "JVMs"},
                new Class[]{ServerInfo.class, Collection.class, Collection.class, Collection.class}
        );
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
