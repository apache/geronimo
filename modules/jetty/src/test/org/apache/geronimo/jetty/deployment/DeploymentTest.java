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
package org.apache.geronimo.jetty.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.jar.JarOutputStream;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.shared.StateType;

import org.apache.geronimo.deployment.URLDeployer;
import org.apache.geronimo.deployment.plugin.local.LocalServer;
import org.apache.geronimo.deployment.service.ServiceDeployer;
import org.apache.geronimo.deployment.util.URLInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/26 05:55:27 $
 */
public class DeploymentTest extends DeployerTestCase {
//    private byte[] plan;
    private File configFile;
    private Target[] targets;

    public void testUnpacked() throws Exception {
        File war = new File(URI.create(classLoader.getResource("deployables/war1/").toString()));

        ProgressObject result = manager.distribute(targets, war, new File(war, "WEB-INF/geronimo-web.xml"));
        waitFor(result);
        TargetModuleID[] ids = result.getResultTargetModuleIDs();
        assertEquals(1, ids.length);

        result = manager.start(ids);
        waitFor(result);
        checkHaveContent();

        result = manager.stop(ids);
        waitFor(result);

        checkNoContent();
    }

    public void testPacked() throws Exception {
        File war1 = new File(URI.create(classLoader.getResource("deployables/war1/").toString()));
        File war2 = new File(URI.create(classLoader.getResource("deployables/war2.war").toString()));

        ProgressObject result = manager.distribute(targets, war2, new File(war1, "WEB-INF/geronimo-web.xml"));
        waitFor(result);
        TargetModuleID[] ids = result.getResultTargetModuleIDs();
        assertEquals(1, ids.length);

        result = manager.start(ids);
        waitFor(result);
        checkHaveContent();

        result = manager.stop(ids);
        waitFor(result);

        checkNoContent();
    }

    private void checkNoContent() throws IOException {
        HttpURLConnection connection;
        connection = (HttpURLConnection) new URL("http://localhost:5678/test/hello.txt").openConnection();
        try {
            connection.getInputStream();
            fail();
        } catch (IOException e) {
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
        }
        connection.disconnect();
    }

    private void checkHaveContent() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:5678/test/hello.txt").openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        assertEquals("Hello World", reader.readLine());
        connection.disconnect();
    }

    private void waitFor(ProgressObject result) throws InterruptedException {
        result.addProgressListener(new ProgressListener() {
            public void handleProgressEvent(ProgressEvent event) {
                synchronized (DeploymentTest.this) {
                    DeploymentTest.this.notify();
                }
            }
        });
        synchronized (this) {
            while (result.getDeploymentStatus().isRunning()) {
                wait();
            }
        }
        assertEquals(StateType.COMPLETED, result.getDeploymentStatus().getState());
    }

    protected void setUp() throws Exception {
        super.setUp();

        URI localID = URI.create("local");
        File workDir = new File(System.getProperty("java.io.tmpdir"), "workdir");
        workDir.mkdir();
        URLDeployer deployer = new URLDeployer(null, localID, Collections.singletonList(new ServiceDeployer(parser)), workDir);
        deployer.addSource(new URLInfo(classLoader.getResource("services/local.xml")));
        deployer.deploy();
        configFile = new File(System.getProperty("java.io.tmpdir"), "local.car");
        JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(configFile)));
        deployer.saveConfiguration(jos);
        jos.close();
        kernel.install(configFile.toURL());

        GBeanMBean serverGBean = new GBeanMBean(LocalServer.GBEAN_INFO);
        serverGBean.setAttribute("ConfigID", localID);
        serverGBean.setAttribute("ConfigStore", configStore);
        kernel.loadGBean(serverName, serverGBean);
        kernel.startGBean(serverName);

//        WebDeployable deployable = new WebDeployable(war);
//        DeploymentConfiguration config = manager.createConfiguration(deployable);
//        DConfigBeanRoot configRoot = config.getDConfigBeanRoot(deployable.getDDBeanRoot());
//        WebAppDConfigBean contextBean = (WebAppDConfigBean) configRoot.getDConfigBean(deployable.getChildBean("/web-app")[0]);
//        contextBean.setContextRoot("/war2");
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        config.save(baos);
//        plan = baos.toByteArray();

        targets = manager.getTargets();
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(serverName);
        kernel.unloadGBean(serverName);
        super.tearDown();
    }
}
