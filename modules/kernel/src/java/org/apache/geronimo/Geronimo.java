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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.File;

import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/28 23:03:22 $
 *
 * */
public class Geronimo {

    private Geronimo() {

     }
    /**
     * Static entry point allowing a Kernel to be run from the command line.
     * Arguments are:
     * <li>the filename of the directory to use for the configuration store.
     *     This will be created if it does not exist.</li>
     * <li>the id of a configuation to load</li>
     * Once the Kernel is booted and the configuration is loaded, the process
     * will remain running until the shutdown() method on the kernel is
     * invoked or until the JVM exits.
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("usage: " + Geronimo.class.getName() + " <config-store-dir> <config-id>");
            System.exit(1);
        }
        String storeDirName = args[0];
        URI configID = null;
        try {
            configID = new URI(args[1]);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }
        String domain = "geronimo";

        Kernel kernel = null;
        try {
            kernel = createKernel(domain, storeDirName);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        try {
            kernel.load(configID);
        } catch (Exception e) {
            kernel.shutdown();
            e.printStackTrace();
            System.exit(3);
        }
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
