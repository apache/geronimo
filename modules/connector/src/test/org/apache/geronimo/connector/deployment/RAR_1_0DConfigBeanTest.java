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
import org.apache.geronimo.connector.deployment.dconfigbean.ConfigPropertySettings;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionDConfigBean;
import org.apache.geronimo.connector.deployment.dconfigbean.ConnectionDefinitionInstance;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/21 01:10:50 $
 *
 * */
public class RAR_1_0DConfigBeanTest extends TestCase  {
    private URL j2eeDD;
    XmlOptions xmlOptions;
    private List errors;

    public void testDConfigBeans() throws Exception {
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
        connectionDefinitionInstance1.setBlockingTimeout(3000);

        //check the results
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        rarConfiguration.save(baos);
        baos.flush();
        byte[] bytes = baos.toByteArray();
        baos.close();
        InputStream is = new ByteArrayInputStream(bytes);
        GerConnectorDocument gcDoc = GerConnectorDocument.Factory.parse(is);
        GerResourceadapterType ra = gcDoc.getConnector().getResourceadapter();

        //connection definition
        GerConnectionDefinitionType connectionDefinitionType = ra.getOutboundResourceadapter().getConnectionDefinitionArray(0);
        GerConnectiondefinitionInstanceType connectiondefinitionInstanceType = connectionDefinitionType.getConnectiondefinitionInstanceArray(0);
        assertEquals("TestCDValue1", connectiondefinitionInstanceType.getConfigPropertySettingArray(0).getStringValue());
        //connection manager
        GerConnectionmanagerType connectionmanagerType = connectiondefinitionInstanceType.getConnectionmanager();
        assertEquals(3000, connectionmanagerType.getBlockingTimeout().intValue());

        //and read back into dconfigbeans
        rarConfiguration.restore(new ByteArrayInputStream(bytes));

        //outbound
        connectionDefinitionDConfigBean = (ConnectionDefinitionDConfigBean)root.getDConfigBean(connectionDefinitiondds[0]);
        assertNotNull(connectionDefinitionDConfigBean);
        ConnectionDefinitionInstance[] connectionDefinitionInstances = connectionDefinitionDConfigBean.getConnectionDefinitionInstance();
        connectionDefinitionSetting1 = connectionDefinitionInstances[0].getConfigProperty()[0];
        assertEquals("TestCDValue1", connectionDefinitionSetting1.getConfigPropertyValue());
        //connection manager
        assertEquals(3000, connectionDefinitionInstances[0].getBlockingTimeout());

    }

    protected void setUp() throws Exception {
        File docDir = new File("src/test-data/connector_1_0");
        j2eeDD = new File(docDir, "ra.xml").toURL();
        xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
    }

}
