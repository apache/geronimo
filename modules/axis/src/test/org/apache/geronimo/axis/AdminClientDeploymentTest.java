/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import org.apache.axis.client.AdminClient;
import org.apache.axis.client.Call;
import org.apache.axis.utils.ClassUtils;
import org.apache.geronimo.ews.ws4j2ee.toWs.Ws4J2ee;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

import javax.management.ObjectName;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>This test case shows the possible two ways of add a entry to the Axis
 * server-config.wsdd Deployment Discrypter.</p>
 */

public class AdminClientDeploymentTest extends AbstractTestCase {
    private ObjectName name;
    private Kernel kernel;
    private File jarFile;

    /**
     * @param testName
     */
    public AdminClientDeploymentTest(String testName) {
        super(testName);
    }
//I leave it like this .. this feature is not used now. 
    public void XtestDeployWithAdminClientStatically() throws Exception {
//      URLClassLoader cl = new  URLClassLoader(new URL[]{jarFile.toURL()});
//      InputStream deplydd = cl.getResourceAsStream("deploy.wsdd");
//      assertNotNull(deplydd);
//
//      Admin admin = new Admin();
//      InputStream wsddconf = getClass().getClassLoader().getResourceAsStream("org/apache/axis/server/server-config.wsdd");
//      assertNotNull(wsddconf);
//      WSDDDocument wsddDoc = new WSDDDocument(XMLUtils.newDocument(wsddconf));
//      WSDDDeployment deployment = wsddDoc.getDeployment();
//      AxisEngine engine = new AxisServer(deployment);
//      engine.setShouldSaveConfig(true);
//      engine.init();
//      MessageContext msgContext = new MessageContext(engine);
//
//      Document doc = XMLUtils.newDocument(deplydd);
//      Document result = admin.process(msgContext, doc.getDocumentElement());
//      System.out.println(XMLUtils.DocumentToString(result));
//      PrintWriter w = new PrintWriter(System.out);
//      deployment.writeToContext(new SerializationContextImpl(w));
//      w.close();
    }

    public void testDeployWithAdminClientDinamically() throws Exception {
        URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURL()});
        InputStream deplydd = cl.getResourceAsStream("deploy.wsdd");
        assertNotNull(deplydd);

        ClassLoader parentClassLoder = ClassUtils.getDefaultClassLoader();
        ClassUtils.setDefaultClassLoader(cl);
        AdminClient adminClient = new AdminClient();

        URL requestUrl = new URL("http://localhost:"
                + AxisGeronimoConstants.AXIS_SERVICE_PORT
                + "/axis/services/AdminService");
        Call call = adminClient.getCall();
        call.setTargetEndpointAddress(requestUrl);
        String result = adminClient.process(null, deplydd);
        System.out.println(result);

        URL wsdlrequestUrl = new URL("http://localhost:"
                + AxisGeronimoConstants.AXIS_SERVICE_PORT

                + "/axis/services/echoPort?wsdl");
        //+"/axis/services/AdminService?wsdl");
        
        HttpURLConnection connection = (HttpURLConnection) wsdlrequestUrl.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        connection.getResponseCode();
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }

    }

    public void testURLFileTest() throws MalformedURLException {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        URL url = contextLoader.getResource("deployables/axis/WEB-INF/web.xml");
        assertNotNull(url);
        File file = new File(url.getFile());
        assertTrue(file.exists());
        assertTrue(url.sameFile(file.toURL()));
    }

    protected void setUp() throws Exception {
        new File(outDir).mkdirs();
        name = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanMBean gbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        gbean.setAttribute("Name", "Test");
        kernel.loadGBean(name, gbean);
        kernel.startGBean(name);

        jarFile = new File(outDir + "/echo-ewsimpl.jar");
        if (!jarFile.exists()) {
            GeronimoWsDeployContext deployContext =
                    new GeronimoWsDeployContext(getTestFile("target/samples/echo.jar"),
                            outDir);
            Ws4J2ee ws4j2ee = new Ws4J2ee(deployContext, null);
            ws4j2ee.generate();
        }
    }

    protected void tearDown() throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
        kernel.shutdown();
    }
}
