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

package org.apache.geronimo;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.LocalConfigStore;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/27 19:33:40 $
 *
 * */
public class Geronimo {

    private Geronimo() {

     }
    /**
     * Static entry point allowing a Kernel to be run from the command line.
     * Arguments are:
     * <li>the id of a configuation to load</li>
     * <li>deploy output file</li>
     * <li>deploy urls</li>
     * <li>domain</li>
     * <li>the filename of the directory to use for the configuration store.
     *     This will be created if it does not exist.</li>
     * Once the Kernel is booted and the configuration is loaded, the process
     * will remain running until the shutdown() method on the kernel is
     * invoked or until the JVM exits.
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("usage: " + Geronimo.class.getName() + " <config-id> <deploy-out-file> <deploy-urls> <domain> <config-store>");
            System.exit(1);
        }
        String configIDString = args[0];
        String outfile = args[1];
        String urlsString = args[2];
        String domain = args[3];
        String storeDirName = args[4];

        Class clazz = Class.forName("org.apache.geronimo.deployment.tools.DeployCommand");
        Method m = clazz.getMethod("deploy", new Class[]{String.class,String.class,String.class});
        m.invoke(null, new Object[]{configIDString, outfile, urlsString});

        installPackage(domain, storeDirName, outfile);
        loadAndWait(domain, storeDirName, configIDString);
    }

    public static Kernel createKernel(String domain, String storeDirName) throws Exception {
        File storeDir = new File(storeDirName);
        if (storeDir.exists()) {
            if (!storeDir.isDirectory() || !storeDir.canWrite()) {
                throw new IllegalArgumentException("Store location is not a writable directory: " + storeDir);
            }
        } else {
            if (!storeDir.mkdirs()) {
                throw new IllegalArgumentException("Could not create store directory: " + storeDir);
            }
        }

        final Kernel kernel = new Kernel(domain, LocalConfigStore.GBEAN_INFO, storeDir);
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread") {
            public void run() {
                kernel.shutdown();
            }
        });
        //Kernel may not be loaded by SystemClassLoader if run embedded.
        Thread.currentThread().setContextClassLoader(kernel.getClass().getClassLoader());
        kernel.boot();
        return kernel;
    }

    public static Kernel installPackage(String domain, String storeDirName, String packageURLName) throws Exception {
        Kernel kernel = createKernel(domain, storeDirName);
        URL packageURL = new File(packageURLName).toURL();
        kernel.install(packageURL);
        return kernel;
    }

    public static Kernel load(String domain, String storeDirName, String configIDString) throws Exception {
        Kernel kernel = createKernel(domain, storeDirName);
        URI configID = new URI(configIDString);
        kernel.load(configID);
        return kernel;
    }

    public static void loadAndWait(String domain, String storeDirName, String configIDString) throws Exception {
        Kernel kernel = load(domain, storeDirName, configIDString);
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
