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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.model.connector.ConfigProperty;
import org.apache.geronimo.deployment.model.connector.ConnectorDocument;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectionDefinition;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectionManagerFactory;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectorDocument;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoResourceAdapter;
import org.apache.geronimo.kernel.deployment.AbstractDeploymentPlanner;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.DeploymentInfo;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.CreateClassSpace;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.task.RegisterMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.xml.deployment.ConnectorLoader;
import org.apache.geronimo.xml.deployment.GeronimoConnectorLoader;
import org.apache.geronimo.xml.deployment.LoaderUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * DeploymentPlanner in charge of the plannification of Connector deployments.
 *
 *
 * @version $Revision: 1.9 $ $Date: 2003/12/28 23:06:42 $
 */
public class ConnectorDeploymentPlanner
        extends AbstractDeploymentPlanner {

    private static final Log log = LogFactory.getLog(ConnectorDeploymentPlanner.class);

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        return AbstractDeploymentPlanner.getGeronimoMBeanInfo(ConnectorDeploymentPlanner.class.getName());
    }


    /**
     * Deploys the specified URL. If this deployer can deploy the specified
     * URL, then two plans are added to the current plan set. The first plan
     * is in charge of registering the deployment unit and the second one - a
     * child of the first one - in charge of bootstrapping the Connector
     * deployment.
     *
     * @param goal URL to be deployed.
     * @param goals Current deployment goals.
     * @param plans Current deployment plan.
     *
     * @return true if the deployment has progressed and false otherwise.
     *
     * @throws DeploymentException
     */
    protected boolean addURL(DeployURL goal, Set goals, Set plans)
            throws DeploymentException {
        URL url = goal.getUrl();
        ConnectorDeploymentHelper dHelper =
                new ConnectorDeploymentHelper(url, goal.getType());
        URL raURL = dHelper.locateJ2EEDD();
        URL graURL = dHelper.locateGeronimoDD();
        // Is the specific URL deployable?
        if (null == raURL) {
            return false;
        }
        URI baseURI = URI.create(url.toString()).normalize();

        log.trace("Planning the connector deployment " + url);

        // One can deploy the specified URL. One removes it from the current
        // goal set.
        goals.remove(goal);

        ObjectName deploymentUnitName = dHelper.buildDeploymentName();

        // Define a deployment plan for the deployment unit.
        DeploymentPlan deploymentInfoPlan = DeploymentInfo.planDeploymentInfo(getServer(), null, deploymentUnitName, null, url);

        // Define the ClassSpace for the Connector archives.
        ClassSpaceMetadata raCS = dHelper.buildClassSpace();
        deploymentInfoPlan.addTask(new CreateClassSpace(getServer(), raCS));//parent???
        plans.add(deploymentInfoPlan);

        //now another plan for the tasks that depend on the class space.
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        // Load the deployment descriptor into our POJO
        URI raURI = URI.create(raURL.toString()).normalize();
        log.trace("Loading deployment descriptor " + raURI);

        GeronimoConnectorDocument gconDoc = null;
        try {
            Document raDocument =
                    LoaderUtil.parseXML(new InputStreamReader(raURL.openStream()));
            ConnectorDocument conDoc = ConnectorLoader.load(raDocument);
            Document graDocument =
                    LoaderUtil.parseXML(new InputStreamReader(graURL.openStream()));
            gconDoc = GeronimoConnectorLoader.load(graDocument, conDoc);
        } catch (FileNotFoundException e1) {
            throw new DeploymentException("Deployment descriptor not found", e1);
        } catch (SAXException e1) {
            throw new DeploymentException("[geronimo-]ra.xml malformed", e1);
        } catch (IOException e1) {
            throw new DeploymentException("Deployment descriptor not readable", e1);
        }
        GeronimoResourceAdapter gra = gconDoc.getGeronimoConnector().getGeronimoResourceAdapter();
        //deploy ra
        ObjectName resourceAdapterName = null;
        if (gra.getResourceAdapterClass() != null) {
            resourceAdapterName = planResourceAdapter(deploymentPlan, gra, raCS, deploymentUnitName, baseURI);
        }


        //deploy mcfs
        for (int i = 0; i < gra.getGeronimoOutboundResourceAdapter().getGeronimoConnectionDefinition().length; i++) {
            GeronimoConnectionDefinition gcd = gra.getGeronimoOutboundResourceAdapter().getGeronimoConnectionDefinition(i);
            assert gcd != null: "Null GeronimoConnectionDefinition";
            //deploy cm factory
            planManagedConnectionFactory(deploymentPlan, gcd, raCS, deploymentUnitName, baseURI, resourceAdapterName);


        }
        plans.add(deploymentPlan);

        return true;
    }

    //Public access is bad, but these need to be accessible for testing from other packages.
    //TODO find a way to make these package access
    public void planManagedConnectionFactory(DeploymentPlan deploymentPlan, GeronimoConnectionDefinition gcd, ClassSpaceMetadata raCS, ObjectName deploymentUnitName, URI baseURI, ObjectName resourceAdapterName) throws DeploymentException {
        GeronimoConnectionManagerFactory gcmf = gcd.getGeronimoConnectionManagerFactory();
        assert gcmf != null: "Null GeronimoConnectionManagerFactory";
        MBeanMetadata cmfMD = getMBeanMetadata(raCS.getName(), deploymentUnitName, baseURI);
        cmfMD.setGeronimoMBeanDescriptor(gcmf.getConnectionManagerFactoryDescriptor());
        cmfMD.setName(buildConnectionManagerFactoryDeploymentName(gcd));
        adaptConfigProperties(gcmf.getConfigProperty(), null, cmfMD.getAttributeValues());
        addTasks(cmfMD, deploymentPlan);


        MBeanMetadata mcfMD = getMBeanMetadata(raCS.getName(), deploymentUnitName, baseURI);
        mcfMD.setCode(gcd.getManagedConnectionFactoryClass());
        mcfMD.setName(buildManagedConnectionFactoryDeploymentName(gcd));
        configureMBeanMetadata(gcd.getConfigProperty(), mcfMD);
        ManagedConnectionFactoryHelper.addMBeanInfo(mcfMD.getGeronimoMBeanInfo(), resourceAdapterName, cmfMD.getName());
        Map attributes = mcfMD.getAttributeValues();
        attributes.put("ConnectionFactoryImplClass", gcd.getConnectionFactoryImplClass());
        attributes.put("ConnectionFactoryInterface", gcd.getConnectionFactoryInterface());
        attributes.put("ConnectionImplClass", gcd.getConnectionImplClass());
        attributes.put("ConnectionInterface", gcd.getConnectionInterface());
        attributes.put("ManagedConnectionFactoryClass", gcd.getManagedConnectionFactoryClass());
        if (resourceAdapterName != null) {
            attributes.put("ResourceAdapterName", resourceAdapterName);
        }
        attributes.put("ConnectionManagerFactoryName", cmfMD.getName());
        addTasks(mcfMD, deploymentPlan);
    }

    //Public access is bad, but these need to be accessible for testing from other packages.
    //TODO find a way to make these package access
    public ObjectName planResourceAdapter(DeploymentPlan deploymentPlan, GeronimoResourceAdapter gra, ClassSpaceMetadata raCS, ObjectName deploymentUnitName, URI baseURI) throws DeploymentException {
        ObjectName resourceAdapterName;
        MBeanMetadata raMD = getMBeanMetadata(raCS.getName(), deploymentUnitName, baseURI);
        raMD.setCode(gra.getResourceAdapterClass());
        raMD.setName(buildResourceAdapterDeploymentName(gra));
        configureMBeanMetadata(gra.getConfigProperty(), raMD);
        addTasks(raMD, deploymentPlan);
        resourceAdapterName = raMD.getName();
        ObjectName bootstrapContextName = buildBootstrapContextName(gra);
        ResourceAdapterHelperImpl.addMBeanInfo(raMD.getGeronimoMBeanInfo(), bootstrapContextName);
        return resourceAdapterName;
    }

    /**
     *
     * @param redeployURL
     * @param goals
     * @return
     * @throws DeploymentException
     * todo implement this
     */
    protected boolean redeployURL(RedeployURL redeployURL, Set goals) throws DeploymentException {
        return false;
    }

    /**
     *
     * @param undeployURL
     * @param goals
     * @param plans
     * @return
     * @throws DeploymentException
     * todo implement this
     */
    protected boolean removeURL(UndeployURL undeployURL, Set goals, Set plans) throws DeploymentException {
        return false;
    }

    private void configureMBeanMetadata(ConfigProperty[] props, MBeanMetadata metadata) {
        GeronimoMBeanInfo info = new GeronimoMBeanInfo();
        info.setTargetClass(metadata.getCode());
        Map attributes = metadata.getAttributeValues();
        adaptConfigProperties(props, info, attributes);
        metadata.setGeronimoMBeanInfo(info);

    }

    private void adaptConfigProperties(
            ConfigProperty[] configProperty,
            GeronimoMBeanInfo mbeanInfo,
            Map attributes) {
        for (int i = 0; i < configProperty.length; i++) {
            if (mbeanInfo != null) {
                GeronimoAttributeInfo attInfo = new GeronimoAttributeInfo();
                attInfo.setName(configProperty[i].getConfigPropertyName());
                if (configProperty[i].getConfigPropertyValue() != null) {
                    attInfo.setInitialValue(configProperty[i].getConfigPropertyValue());
                }
                //descriptions are now multilingual, so we'll leave them to
                // someone who knows how to determine the locale.
                mbeanInfo.addAttributeInfo(attInfo);
            } else if (configProperty[i].getConfigPropertyValue() != null) {
                attributes.put(configProperty[i].getConfigPropertyName(), configProperty[i].getConfigPropertyValue());
            }
        }
    }

    private void addTasks(MBeanMetadata metadata, DeploymentPlan plan) {
        DeployGeronimoMBean createTask =
                new DeployGeronimoMBean(getServer(), metadata);
        plan.addTask(createTask);
        StartMBeanInstance startTask =
                new StartMBeanInstance(getServer(), metadata);
        plan.addTask(startTask);
    }


    /**
     * Build the name of the Connector deployment related to this URL.
     *
     * @return Connector deployment name.
     */

    private ObjectName buildResourceAdapterDeploymentName(GeronimoResourceAdapter gra) {
        return JMXUtil.getObjectName(
                "geronimo.j2ee:J2eeType=ResourceAdapter,name="
                + gra.getName());
    }

    private ObjectName buildManagedConnectionFactoryDeploymentName(GeronimoConnectionDefinition gcd) {
        return JMXUtil.getObjectName(
                "geronimo.management:j2eeType=JCAManagedConnectionFactory,name="
                + gcd.getName());
    }


    private ObjectName buildConnectionManagerFactoryDeploymentName(GeronimoConnectionDefinition gcd) {
        return JMXUtil.getObjectName(
                "geronimo.management:j2eeType=ConnectionManager,name="
                + gcd.getName());
    }


    private ObjectName buildBootstrapContextName(GeronimoResourceAdapter gra) {
        return JMXUtil.getObjectName(
                gra.getBootstrapContext());
    }


}
