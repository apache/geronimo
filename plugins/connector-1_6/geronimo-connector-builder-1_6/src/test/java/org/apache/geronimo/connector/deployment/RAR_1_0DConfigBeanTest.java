/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.connector.deployment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;

import org.apache.geronimo.testsupport.TestSupport;

import org.apache.geronimo.connector.deployment.dconfigbean.ConfigPropertySettings;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionInstance;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class RAR_1_0DConfigBeanTest extends TestSupport {
    private URL j2eeDD;
    XmlOptions xmlOptions;
    private List errors;

    public void testNothing() {}

    public void XtestDConfigBeans() throws Exception {
        MockRARDeployable deployable = new MockRARDeployable(j2eeDD);
        DDBeanRoot ddroot = deployable.getDDBeanRoot();
        DeploymentConfiguration rarConfiguration = new RARConfigurer().createConfiguration(deployable);
        DConfigBeanRoot root = rarConfiguration.getDConfigBeanRoot(ddroot);
        assertNotNull(root);

        //outbound
        DDBean[] connectionDefinitiondds = ddroot.getChildBean(root.getXpaths()[0]);
        assertEquals(1, connectionDefinitiondds.length);
        ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)root.getDConfigBean(connectionDefinitiondds[0]);
        assertNotNull(connectionDefinitionDConfigBean);
        ConnectionDefinitionInstance connectionDefinitionInstance1 = new ConnectionDefinitionInstance();
        connectionDefinitionDConfigBean.setConnectionDefinitionInstance(new ConnectionDefinitionInstance[] {connectionDefinitionInstance1});
        DDBean[] connectionDefinitionConfigPropDDs = connectionDefinitiondds[0].getChildBean("config-property");
        assertEquals(4, connectionDefinitionConfigPropDDs.length);
        ConfigPropertySettings connectionDefinitionSetting1 = connectionDefinitionInstance1.getConfigProperty()[0];
        connectionDefinitionSetting1.setConfigPropertyValue("TestCDValue1");
        //connection manager properties
//        connectionDefinitionInstance1.setBlockingTimeout(3000);

        //check the results
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rarConfiguration.save(baos);
        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        InputStream is = new ByteArrayInputStream(bytes);
        GerConnectorDocument gcDoc = GerConnectorDocument.Factory.parse(is);
        GerResourceadapterType ra = gcDoc.getConnector().getResourceadapterArray()[0];

        //connection definition
        GerConnectionDefinitionType connectionDefinitionType = ra.getOutboundResourceadapter().getConnectionDefinitionArray(0);
        GerConnectiondefinitionInstanceType connectiondefinitionInstanceType = connectionDefinitionType.getConnectiondefinitionInstanceArray(0);
        assertEquals("TestCDValue1", connectiondefinitionInstanceType.getConfigPropertySettingArray(0).getStringValue());
        //connection manager
        GerConnectionmanagerType connectionmanagerType = connectiondefinitionInstanceType.getConnectionmanager();
//        assertEquals(3000, connectionmanagerType.getBlockingTimeout().intValue());

        //and read back into dconfigbeans
        rarConfiguration.restore(new ByteArrayInputStream(bytes));

        //outbound
        connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)root.getDConfigBean(connectionDefinitiondds[0]);
        assertNotNull(connectionDefinitionDConfigBean);
        ConnectionDefinitionInstance[] connectionDefinitionInstances = connectionDefinitionDConfigBean.getConnectionDefinitionInstance();
        connectionDefinitionSetting1 = connectionDefinitionInstances[0].getConfigProperty()[0];
        assertEquals("TestCDValue1", connectionDefinitionSetting1.getConfigPropertyValue());
        //connection manager
//        assertEquals(3000, connectionDefinitionInstances[0].getBlockingTimeout());

    }

    protected void XsetUp() throws Exception {
        File docDir = new File(BASEDIR, "src/test/data/connector_1_0");
        j2eeDD = new File(docDir, "ra.xml").toURI().toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }

}
