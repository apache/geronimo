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
package org.apache.geronimo.axis.preconditions;

import org.apache.axis.client.AdminClient;
import org.apache.axis.client.Call;
import org.apache.axis.utils.ClassUtils;
import org.apache.geronimo.axis.AbstractWebServiceTest;
import org.apache.geronimo.axis.AxisGeronimoUtils;
import org.apache.geronimo.gbean.WaitingException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p>This test case shows the possible two ways of add a entry to the Axis
 * server-config.wsdd Deployment Discrypter.</p>
 * @version $Rev: $ $Date: $
 */

public class AdminClientDeploymentTest extends AbstractWebServiceTest {
    public AdminClientDeploymentTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);
    }

    public void xtestDeployWithAdminClientDinamically() throws Exception {
        File jarFile = new File(outDir , "echo-jar/echo-ewsimpl.jar");
        URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURL()});
        InputStream deplydd = cl.getResourceAsStream("deploy.wsdd");
        assertNotNull(deplydd);
        ClassLoader parentClassLoder = ClassUtils.getDefaultClassLoader();
        ClassUtils.setDefaultClassLoader(cl);
        AdminClient adminClient = new AdminClient();
        URL requestUrl = AxisGeronimoUtils.getURL("/axis/services/AdminService");
        Call call = adminClient.getCall();
        call.setTargetEndpointAddress(requestUrl);
        String result = adminClient.process(null, deplydd);
        URL wsdlrequestUrl = AxisGeronimoUtils.getURL("/axis/services/echoPort?wsdl");
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

}
