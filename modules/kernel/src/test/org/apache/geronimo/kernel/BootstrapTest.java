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
package org.apache.geronimo.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.config.LocalConfigStore;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/04 05:42:57 $
 */
public class BootstrapTest extends TestCase {
    private File configRoot;
    private File tmpDir;
    private File kernelState;
    private GBeanInfo storeInfo;

    public void testCreate() throws Exception {
        Kernel kernel = new Kernel("test.kernel", "geronimo", storeInfo, configRoot);
        kernel.boot();
        kernel.shutdown();
    }

    public void testPersist() throws Exception {
        Kernel kernel = new Kernel("test.kernel", "geronimo", storeInfo, configRoot);
        kernel.boot();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(kernelState));
        oos.writeObject(kernel);
        oos.close();
        kernel.shutdown();

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(kernelState));
        kernel = (Kernel) ois.readObject();
        ois.close();
        kernel.boot();
        kernel.shutdown();
    }

    protected void setUp() throws Exception {
        tmpDir = new File(System.getProperty("java.io.tmpdir"));
        configRoot = new File(tmpDir, "config-store");
        kernelState = new File(tmpDir, "kernel.ser");
        storeInfo = LocalConfigStore.getGBeanInfo();

        configRoot.mkdir();
    }

    protected void tearDown() throws Exception {
        recursiveDelete(configRoot);
        kernelState.delete();
    }

    private static void recursiveDelete(File root) throws Exception {
        File[] files = root.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    recursiveDelete(file);
                } else {
                    file.delete();
                }
            }
        }
        root.delete();
    }
}
