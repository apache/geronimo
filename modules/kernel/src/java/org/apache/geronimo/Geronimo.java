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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.kernel.log.GeronimoLogFactory;

/**
 * @version $Revision: 1.8 $ $Date: 2004/02/12 18:23:58 $
 */
public class Geronimo {
    static {
        // This MUST be done before the first log is acquired
        System.setProperty(LogFactory.FACTORY_PROPERTY, GeronimoLogFactory.class.getName());
    }

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
     * @todo commons-cli support
     * @todo save list of started configurations and restart them next time
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("usage: " + Geronimo.class.getName() + " <config-store-dir> <config-id>...");
            System.exit(1);
        }
        String storeDirName = args[0];
        List configs = new ArrayList();
        for (int i = 1; i < args.length; i++) {
            URI configID;
            try {
                configID = new URI(args[i]);
            } catch (URISyntaxException e) {
                System.err.println("Invalid config-id: " + args[i]);
                e.printStackTrace();
                System.exit(1);
                throw new AssertionError();
            }
            configs.add(configID);
        }

        final Kernel kernel;
        File storeDir = new File(storeDirName);
        if (storeDir.exists()) {
            if (!storeDir.isDirectory() || !storeDir.canWrite()) {
                System.err.println("Store location is not a writable directory: " + storeDir);
                System.exit(2);
                throw new AssertionError();
            }
        } else {
            if (!storeDir.mkdirs()) {
                System.err.println("Could not create store directory: " + storeDir);
                System.exit(2);
                throw new AssertionError();
            }
        }

        kernel = new Kernel("geronimo.kernel", "geronimo", LocalConfigStore.GBEAN_INFO, storeDir);
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
        }

        try {
            for (Iterator i = configs.iterator(); i.hasNext();) {
                URI configID = (URI) i.next();
                kernel.load(configID);
                kernel.startRecursiveGBean(Kernel.getConfigObjectName(configID));
            }
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
}
