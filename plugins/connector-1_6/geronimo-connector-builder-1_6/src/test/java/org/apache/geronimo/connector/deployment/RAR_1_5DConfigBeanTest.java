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

import junit.framework.TestCase;
import org.apache.geronimo.connector.deployment.dconfigbean.AdminObjectDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.AdminObjectInstance;
import org.apache.geronimo.connector.deployment.dconfigbean.ConfigPropertySettingDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ConfigPropertySettings;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionInstance;
import org.apache.geronimo.connector.deployment.dconfigbean.ResourceAdapterDConfigBean;
import org.apache.geronimo.xbeans.connector.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.connector.GerAdminobjectType;
import org.apache.geronimo.xbeans.connector.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.connector.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.connector.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.connector.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.connector.GerConnectorDocument;
import org.apache.geronimo.xbeans.connector.GerResourceadapterInstanceType;
import org.apache.geronimo.xbeans.connector.GerResourceadapterType;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class RAR_1_5DConfigBeanTest extends TestCase {
    public void testNothing() {}
    /* In the process of replacing the Connector 1.5 DConfigBeans
    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));
    private URL j2eeDD;
    XmlOptions xmlOptions;
    private List errors;


    public void testDConfigBeans() throws Exception {
        MockRARDeployable deployable = new MockRARDeployable(j2eeDD);
        DDBeanRoot ddroot = deployable.getDDBeanRoot();
        DeploymentConfiguration rarConfiguration = new RARConfigurer().createConfiguration(deployable);
        DConfigBeanRoot root = rarConfiguration.getDConfigBeanRoot(ddroot);
        assertNotNull(root);

        //resource adapter
        DDBean resourceAdapterdd = ddroot.getChildBean(root.getXpaths()[0])[0];
        ResourceAdapterDConfigBean resourceAdapterDConfigBean = (ResourceAdapterDConfigBean) root.getDConfigBean(resourceAdapterdd);
        assertNotNull(resourceAdapterDConfigBean);
        resourceAdapterDConfigBean.setResourceAdapterName("TestRAName");
        DDBean[] resourceAdapterProperties = resourceAdapterdd.getChildBean(resourceAdapterDConfigBean.getXpaths()[0]);
        assertEquals(1, resourceAdapterProperties.length);
        ConfigPropertySettingDConfigBean resourceAdapterSetting = (ConfigPropertySettingDConfigBean)resourceAdapterDConfigBean.getDConfigBean(resourceAdapterProperties[0]);
        assertNotNull(resourceAdapterSetting);
        assertEquals("StringValue", resourceAdapterSetting.getConfigPropertyValue());
        resourceAdapterSetting.setConfigPropertyValue("TestRAValue");

//        //admin objects
//        DDBean[] adminObjectdds = resourceAdapterdd.getChildBean(resourceAdapterDConfigBean.getXpaths()[2]);
//        assertEquals(1, adminObjectdds.length);
//        AdminObjectDConfigBean adminObjectDConfigBean = (AdminObjectDConfigBean)resourceAdapterDConfigBean.getDConfigBean(adminObjectdds[0]);
//        assertNotNull(adminObjectDConfigBean);
//        AdminObjectInstance adminObjectInstance1 = new AdminObjectInstance();
//        adminObjectDConfigBean.setAdminObjectInstance(new AdminObjectInstance[] {adminObjectInstance1});
//        ConfigPropertySettings adminObjectSetting1 = adminObjectInstance1.getConfigProperty()[0];
//        adminObjectSetting1.setConfigPropertyValue("TestAOValue1");
//
//        //add a second admin object in first position
//        AdminObjectInstance adminObjectInstance2 = new AdminObjectInstance();
//        adminObjectDConfigBean.setAdminObjectInstance(new AdminObjectInstance[] {adminObjectInstance2, adminObjectInstance1});
//        ConfigPropertySettings adminObjectSetting2 = adminObjectInstance2.getConfigProperty()[0];
//        adminObjectSetting2.setConfigPropertyValue("TestAOValue2");

        //outbound
        DDBean[] connectionDefinitiondds = resourceAdapterdd.getChildBean(resourceAdapterDConfigBean.getXpaths()[1]);
        assertEquals(2, connectionDefinitiondds.length);
        ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)resourceAdapterDConfigBean.getDConfigBean(connectionDefinitiondds[0]);
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
        GerResourceadapterInstanceType rai = ra.getResourceadapterInstance();
        assertEquals("TestRAName", rai.getResourceadapterName());
        GerConfigPropertySettingType rasetting = rai.getConfigPropertySettingArray(0);
        assertEquals("TestRAValue", rasetting.getStringValue());

        //admin object
//        GerAdminobjectType adminobjectType1 = ra.getAdminobjectArray(0);
//        GerAdminobjectInstanceType adminobjectInstanceType2 = adminobjectType1.getAdminobjectInstanceArray(0);
//        assertEquals("TestAOValue1", adminobjectInstanceType2.getConfigPropertySettingArray(0).getStringValue());
//        GerAdminobjectInstanceType adminobjectInstanceType1 = adminobjectType1.getAdminobjectInstanceArray(1);
//        assertEquals("TestAOValue2", adminobjectInstanceType1.getConfigPropertySettingArray(0).getStringValue());

        //connection definition
        GerConnectionDefinitionType connectionDefinitionType = ra.getOutboundResourceadapter().getConnectionDefinitionArray(0);
        GerConnectiondefinitionInstanceType connectiondefinitionInstanceType = connectionDefinitionType.getConnectiondefinitionInstanceArray(0);
        assertEquals("TestCDValue1", connectiondefinitionInstanceType.getConfigPropertySettingArray(0).getStringValue());
        //connection manager
        GerConnectionmanagerType connectionmanagerType = connectiondefinitionInstanceType.getConnectionmanager();
//        assertEquals(3000, connectionmanagerType.getBlockingTimeout().intValue());

        //and read back into dconfigbeans
        rarConfiguration.restore(new ByteArrayInputStream(bytes));
        //resource adapter
        resourceAdapterDConfigBean = (ResourceAdapterDConfigBean) root.getDConfigBean(resourceAdapterdd);
        assertNotNull(resourceAdapterDConfigBean);
        assertEquals("TestRAName", resourceAdapterDConfigBean.getResourceAdapterName());
        resourceAdapterSetting = (ConfigPropertySettingDConfigBean)resourceAdapterDConfigBean.getDConfigBean(resourceAdapterProperties[0]);
        assertNotNull(resourceAdapterSetting);
        assertEquals("TestRAValue", resourceAdapterSetting.getConfigPropertyValue());

//        //admin objects
//        adminObjectDConfigBean = (AdminObjectDConfigBean)resourceAdapterDConfigBean.getDConfigBean(adminObjectdds[0]);
//        assertNotNull(adminObjectDConfigBean);
//        AdminObjectInstance[] adminObjectInstances = adminObjectDConfigBean.getAdminObjectInstance();
//        assertEquals(2, adminObjectInstances.length);
//        adminObjectSetting1 = adminObjectInstances[1].getConfigProperty()[0];
//        assertEquals("TestAOValue2", adminObjectSetting1.getConfigPropertyValue());
//
//        //second admin object is in first position ..not any longer:-(((
//        adminObjectSetting2 = adminObjectInstances[0].getConfigProperty()[0];
//        assertEquals("TestAOValue1", adminObjectSetting2.getConfigPropertyValue());

        //outbound
        connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)resourceAdapterDConfigBean.getDConfigBean(connectionDefinitiondds[0]);
        assertNotNull(connectionDefinitionDConfigBean);
        ConnectionDefinitionInstance[] connectionDefinitionInstances = connectionDefinitionDConfigBean.getConnectionDefinitionInstance();
        connectionDefinitionSetting1 = connectionDefinitionInstances[0].getConfigProperty()[0];
        assertEquals("TestCDValue1", connectionDefinitionSetting1.getConfigPropertyValue());
        //connection manager
//        assertEquals(3000, connectionDefinitionInstances[0].getBlockingTimeout());

    }



    protected void setUp() throws Exception {
        File docDir = new File(basedir, "src/test-data/connector_1_5");
        j2eeDD = new File(docDir, "ra.xml").toURI().toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }
*/
}
