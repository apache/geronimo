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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.NoDeployerException;
import org.apache.geronimo.deployment.URLDeployer;
import org.apache.geronimo.deployment.JARDeployer;
import org.apache.geronimo.deployment.service.ServiceDeployer;
import org.apache.geronimo.deployment.util.FileUtil;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.deployment.util.URLType;

/**
 *
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/06 02:57:23 $
 */
public class DeployCommand {
    private final File configFile;
    private final URLDeployer batcher;

    public DeployCommand(File configFile, URI configID, File workDir, List deployers) {
        this.configFile = configFile;
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        batcher = new URLDeployer(null, configID, deployers, workDir);
    }

    public DeployCommand(File configFile, URI configID, File workDir, List deployers, List urls) throws IOException, DeploymentException, NoDeployerException {
        this.configFile = configFile;
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        batcher = new URLDeployer(null, configID, deployers, workDir);
        for (Iterator iterator = urls.iterator(); iterator.hasNext();) {
            URL url = (URL) iterator.next();
            add(url);
        }
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

    //used by maven plugin
    public static void deploy(String configIDString, String outfile, String urlsString) throws IOException, URISyntaxException, DeploymentException, NoDeployerException {
        URI configID = null;
        configID = new URI(configIDString);
        List urls = new ArrayList();
        for (StringTokenizer st = new StringTokenizer(urlsString, ","); st.hasMoreTokens();) {
            String url = st.nextToken();
            File source = new File(url);
            urls.add(source.toURL());
        }
        File configFile = new File(outfile);
        File workDir = createWorkDir();
        List deployers = getDeployers();
        DeployCommand deployer = new DeployCommand(configFile, configID, workDir, deployers, urls);
        deployer.deploy();
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("usage: " + DeployCommand.class.getName() + " <configID> <outfile> <url>+");
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
        System.out.println("found configID: " + configID);
        File configFile = new File(args[1]);
        File workDir;
        try {
            workDir = createWorkDir();
        } catch (IOException e) {
            System.err.println("Unable to create working directory");
            System.exit(2);
            throw new AssertionError();
        }
        System.out.println("work dir: " + workDir.getAbsolutePath());

        List deployers = getDeployers();

        int status = 0;
        List urls = new ArrayList();
        try {
            for (int i = 2; i < args.length; i++) {
                File source = new File(args[i]);
                urls.add(source.toURL());
            }
        } catch (Exception e) {
            e.printStackTrace();
            status = 2;
        }
        try {
            DeployCommand deployer = new DeployCommand(configFile, configID, workDir, deployers, urls);
            deployer.deploy();
            System.out.println("deployed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            status = 3;
        } finally {
            FileUtil.recursiveDelete(workDir);
        }
        System.exit(status);
    }

    private static List getDeployers() {
        DocumentBuilder parser;
        try {
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError("Unable to instantiate XML Parser");
        }
        List deployers = new ArrayList();
        deployers.add(new ServiceDeployer(parser));
        deployers.add(new JARDeployer());
        return deployers;
    }

    private static File createWorkDir() throws IOException {
        File workDir = File.createTempFile("deployer", "");
        workDir.delete();
        workDir.mkdir();
        return workDir;
    }

}
