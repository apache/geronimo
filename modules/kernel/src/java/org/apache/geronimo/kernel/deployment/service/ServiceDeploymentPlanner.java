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
package org.apache.geronimo.kernel.deployment.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.DeploymentGoal;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.task.CreateClassSpace;
import org.apache.geronimo.kernel.deployment.task.CreateMBeanInstance;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.task.DestroyMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.InitializeMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.RegisterMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StopMBeanInstance;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfoXMLLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Plans deployment of MBean services.
 *
 * @jmx:mbean extends="org.apache.geronimo.kernel.deployment.DeploymentPlanner"
 *
 * @version $Revision: 1.4 $ $Date: 2003/10/27 21:29:46 $
 */
public class ServiceDeploymentPlanner implements ServiceDeploymentPlannerMBean, MBeanRegistration {
    private MBeanServer server;
    private ObjectName objectName;
    private RelationServiceMBean relationService;
    private final DocumentBuilder parser;
    private final MBeanMetadataXMLLoader mbeanLoader;

    public ServiceDeploymentPlanner() throws DeploymentException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            parser = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError("No XML parser available");
        }
        mbeanLoader = new MBeanMetadataXMLLoader();
    }

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        this.server = mBeanServer;
        relationService = JMXUtil.getRelationService(server);

        this.objectName = objectName;
        return objectName;
    }

    public void postRegister(Boolean aBoolean) {
        try {
            List planners = relationService.getRole("DeploymentController-DeploymentPlanner", "DeploymentPlanner");
            planners.add(objectName);
            relationService.setRole("DeploymentController-DeploymentPlanner", new Role("DeploymentPlanner", planners));
        } catch (Exception e) {
            IllegalStateException e1 = new IllegalStateException();
            e1.initCause(e);
            throw e1;
        }
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public boolean plan(Set goals, Set plans) throws DeploymentException {
        boolean progress = false;
        Set x = new HashSet(goals);
        for (Iterator i = x.iterator(); i.hasNext();) {
            DeploymentGoal goal = (DeploymentGoal) i.next();
            if (goal instanceof DeployURL) {
                progress = addURL((DeployURL) goal, goals, plans);
            } else if (goal instanceof RedeployURL) {
                progress = verifyURL((RedeployURL) goal, goals);
            } else if (goal instanceof UndeployURL) {
                progress = removeURL((UndeployURL) goal, goals, plans);
            }
        }
        return progress;
    }

    private boolean addURL(DeployURL goal, Set goals, Set plans) throws DeploymentException {
        InputStream is;
        URL url = goal.getUrl();
        URI baseURI = URI.create(url.toString()).normalize();

        URLType type = goal.getType();
        if (type == URLType.RESOURCE) {
            if (!url.getPath().endsWith("-service.xml")) {
                return false;
            }
            try {
                is = url.openConnection().getInputStream();
            } catch (IOException e) {
                throw new DeploymentException("Failed to open stream for URL: " + url, e);
            }
        } else if (type == URLType.UNPACKED_ARCHIVE) {
            try {
                URL serviceURL = new URL(url, "META-INF/geronimo-service.xml");
                is = serviceURL.openConnection().getInputStream();
            } catch (IOException e) {
                // assume resource does not exist
                return false;
            }
        } else {
            return false;
        }

        Document doc;
        try {
            doc = parser.parse(is);
        } catch (Exception e) {
            throw new DeploymentException("Failed to parse document", e);
        }

        // Create a plan to register the deployment unit and create the class loader
        DeploymentPlan createDeploymentUnitPlan = new DeploymentPlan();
        ObjectName deploymentName = null;
        try {
            deploymentName = new ObjectName("geronimo.deployment:role=DeploymentUnit,type=Service,url=" + ObjectName.quote(url.toString()));
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }
        ServiceDeployment serviceInfo = new ServiceDeployment(deploymentName, null, url);
        createDeploymentUnitPlan.addTask(new RegisterMBeanInstance(server, deploymentName, serviceInfo));
        MBeanMetadata metadata = new MBeanMetadata(deploymentName);
        createDeploymentUnitPlan.addTask(new StartMBeanInstance(server, metadata));

        // add a plan to create a class space
        ClassSpaceMetadata md = createClassSpaceMetadata((Element) doc.getElementsByTagName("class-space").item(0), deploymentName, url);
        createDeploymentUnitPlan.addTask(new CreateClassSpace(server, md));
        plans.add(createDeploymentUnitPlan);
        ObjectName loaderName = md.getName();

        // register a plan to create each mbean
        NodeList nl = doc.getElementsByTagName("mbean");
        for (int i = 0; i < nl.getLength(); i++) {
            DeploymentPlan createPlan = new DeploymentPlan();

            Element mbeanElement = (Element) nl.item(i);
            metadata = mbeanLoader.loadXML(baseURI, mbeanElement);
            if (server.isRegistered(metadata.getName())) {
                throw new DeploymentException("MBean already exists " + metadata.getName());
            }
            metadata.setLoaderName(loaderName);
            metadata.setParentName(deploymentName);
            metadata.setBaseURI(baseURI);
            CreateMBeanInstance createTask = new CreateMBeanInstance(server, metadata);
            createPlan.addTask(createTask);
            InitializeMBeanInstance initTask = new InitializeMBeanInstance(server, metadata);
            createPlan.addTask(initTask);
            StartMBeanInstance startTask = new StartMBeanInstance(server, metadata);
            createPlan.addTask(startTask);

            plans.add(createPlan);
        }

        goals.remove(goal);
        return true;
    }

    private ClassSpaceMetadata createClassSpaceMetadata(Element classSpaceElement, ObjectName deploymentName, URL deploymentURL) throws DeploymentException {
        ClassSpaceMetadata classSpaceMetadata;
        if (classSpaceElement == null) {
            classSpaceMetadata = new ClassSpaceMetadata();
            try {
                classSpaceMetadata.setName(new ObjectName("geronimo.system:role=ClassSpace,name=Application,url=" + ObjectName.quote(deploymentURL.toString())));
            } catch (MalformedObjectNameException e) {
                // this will never happen as above is a valid object name
                throw new AssertionError(e);
            }
            classSpaceMetadata.setGeronimoMBeanInfo(GeronimoMBeanInfoXMLLoader.loadMBean(
                    ClassLoader.getSystemResource("org/apache/geronimo/kernel/classspace/classspace-mbean.xml")
            ));
            classSpaceMetadata.setCreate(ClassSpaceMetadata.CREATE_IF_NECESSARY);
        } else {
            ClassSpaceMetadataXMLLoader classSpaceLoader = new ClassSpaceMetadataXMLLoader(deploymentURL);
            classSpaceMetadata = classSpaceLoader.loadXML(classSpaceElement);
        }
        classSpaceMetadata.setDeploymentName(deploymentName);
        return classSpaceMetadata;
    }

    private boolean removeURL(UndeployURL goal, Set goals, Set plans) throws DeploymentException {

        URL url = goal.getUrl();

        //TODO: better method of determining whether this deployer should
        //handle this undeploy call. This deployer should only handle the undeploy
        //if it is something that it would have deployed (ie a service). More context
        //information needs to be available to make this decision. For now, just handle
        //the case of the unpacked service, just to prevent this deployer from undeploying everything.

        if (!url.getPath().endsWith("-service.xml")) {
            return false;
        }


        ObjectName deploymentName = null;
        try {
            deploymentName = new ObjectName("geronimo.deployment:role=DeploymentUnit,type=Service,url=" + ObjectName.quote(url.toString()));
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }

        Collection mbeans;
        try {
            mbeans = (Collection) server.getAttribute(deploymentName, "Children");
        } catch (InstanceNotFoundException e) {
            return false;
        } catch (AttributeNotFoundException e) {
            throw new DeploymentException(e);
        } catch (MBeanException e) {
            throw new DeploymentException(e);
        } catch (ReflectionException e) {
            throw new DeploymentException(e);
        }

        // Stop the main deployment which stopps all dependents including its children
        DeploymentPlan stopPlan = new DeploymentPlan();
        stopPlan.addTask(new StopMBeanInstance(server, deploymentName));
        plans.add(stopPlan);

        // Plan the destruction of all the children and then the deployment
        DeploymentPlan destroyPlan = new DeploymentPlan();
        for (Iterator i = mbeans.iterator(); i.hasNext();) {
            ObjectName name = (ObjectName) i.next();
            destroyPlan.addTask(new DestroyMBeanInstance(server, name));

        }

        destroyPlan.addTask(new DestroyMBeanInstance(server, deploymentName));
        plans.add(destroyPlan);

        goals.remove(goal);

        return true;
    }

    private boolean verifyURL(RedeployURL goal, Set goals) throws DeploymentException {
        URL url = goal.getUrl();
        try {
            new ObjectName("geronimo.deployment:role=DeploymentUnit,type=Service,url=" + ObjectName.quote(url.toString()));
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }
        goals.remove(goal);
        return true;
    }
}
