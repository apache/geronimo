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
package org.apache.geronimo.deployment.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.deployment.BatchDeployer;
import org.apache.geronimo.deployment.NoDeployerException;
import org.apache.geronimo.deployment.service.ServiceDeployer;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLInfo;
import org.apache.geronimo.kernel.deployment.scanner.URLType;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/01/17 03:44:38 $
 */
public class DeployCommand {
    private final File configFile;
    private final BatchDeployer batcher;

    public DeployCommand(File configFile, URI configID, File workDir, List deployers) {
        this.configFile = configFile;
        batcher = new BatchDeployer(null, configID, deployers, workDir);
    }

    public void add(URL url) throws IOException, DeploymentException, NoDeployerException {
        URLType type = URLType.getType(url);
        batcher.addSource(new URLInfo(url, type));
    }

    public void deploy() throws IOException, DeploymentException {
        batcher.deploy();

        JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(configFile)));
        batcher.saveConfiguration(jos);
        jos.close();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("usage: "+DeployCommand.class.getName()+" <configID> <outfile> <url>+");
            System.exit(1);
            throw new AssertionError();
        }
        URI configID = null;
        try {
            configID = new URI(args[0]);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
            throw new AssertionError();
        }
        File configFile = new File(args[1]);
        File workDir;
        try {
            workDir = File.createTempFile("deployer", "");
            workDir.delete();
            workDir.mkdir();
        } catch (IOException e) {
            System.err.println("Unable to create working directory");
            System.exit(2);
            throw new AssertionError();
        }

        List deployers = getDeployers();

        DeployCommand deployer = new DeployCommand(configFile, configID, workDir, deployers);
        int status = 0;
        try {
            for (int i=2; i < args.length; i++) {
                File source = new File(args[i]);
                deployer.add(source.toURL());
            }
            deployer.deploy();
        } catch (Exception e) {
            e.printStackTrace();
            status = 2;
        } finally {
            try {
                recursiveDelete(workDir);
            } catch (IOException e) {
                // ignore
            }
        }
        System.exit(status);
    }

    private static List getDeployers() {
        try {
            List deployers = new ArrayList();
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            deployers.add(new ServiceDeployer(parser));
            return deployers;
        } catch (ParserConfigurationException e) {
            throw new AssertionError("Unable to instanciate XML Parser");
        }
    }

    private static void recursiveDelete(File root) throws IOException {
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
