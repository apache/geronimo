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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.service.ServiceDeployer;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.deployment.util.URLType;

/**
 * Helper class to bootstrap a Geronimo instance from a service archive.
 * This deploys the service definition to create a bootstrap Configuration,
 * and then creates a Kernel to run that configuration. This allows someone
 * to boot a Kernel without pre-deploying and installing the Configuration.
 *
 * @version $Revision: 1.5 $ $Date: 2004/02/04 05:43:31 $
 */
public class Bootstrap {
    public static final URI CONFIG_ID = URI.create("org/apache/geronimo/Bootstrap");

    /**
     * Entry point. Arguments are:
     * <li>directory to use for the ConfigurationStore</li>
     * <li>URL for initial service to deploy</li>
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("usage: " + Bootstrap.class.getName() + " <service-url>");
            System.exit(1);
        }
        URL serviceURL;
        try {
            serviceURL = new URL(args[0]);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
            throw new AssertionError();
        }

        List deployers = new ArrayList();
        try {
            deployers.add(new ServiceDeployer(DocumentBuilderFactory.newInstance().newDocumentBuilder()));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }

        File tmpDir = new File(System.getProperty("java.io.tmpdir"), "geronimo");
        FileUtil.recursiveDelete(tmpDir);
        tmpDir.mkdir();

        File storeDir = new File(tmpDir, "config");
        storeDir.mkdir();
        final Kernel kernel = new Kernel("geronimo.kernel", "geronimo", LocalConfigStore.GBEAN_INFO, storeDir);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread") {
            public void run() {
                kernel.shutdown();
            }
        });
        try {
            kernel.boot();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }

        try {
            File workDir = new File(tmpDir, "deployment");
            workDir.mkdir();
            URLDeployer deployer = new URLDeployer(null, CONFIG_ID, deployers, workDir);
            deployer.addSource(new URLInfo(serviceURL, URLType.getType(serviceURL)));
            deployer.deploy();

            ObjectName configName = kernel.load(deployer.getConfiguration(), workDir.toURL());
            kernel.getMBeanServer().invoke(configName, "startRecursive", null, null);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }

        // loop to keep the kernel alive
        while (kernel.isRunning()) {
            try {
                synchronized (kernel) {
                    kernel.wait();
                }
            } catch (InterruptedException e) {
                // continue
            }
        }
    }
}
