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
package org.apache.geronimo.deployment.service;

import java.io.IOException;
import java.io.InputStream;
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

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.goal.DeployURL;
import org.apache.geronimo.deployment.goal.DeploymentGoal;
import org.apache.geronimo.deployment.goal.RedeployURL;
import org.apache.geronimo.deployment.goal.UndeployURL;
import org.apache.geronimo.deployment.plan.CreateClassSpace;
import org.apache.geronimo.deployment.plan.CreateMBeanInstance;
import org.apache.geronimo.deployment.plan.DeploymentPlan;
import org.apache.geronimo.deployment.plan.DeploymentTask;
import org.apache.geronimo.deployment.plan.DestroyMBeanInstance;
import org.apache.geronimo.deployment.plan.RegisterMBeanInstance;
import org.apache.geronimo.deployment.plan.InitializeMBeanInstance;
import org.apache.geronimo.deployment.plan.StartMBeanInstance;
import org.apache.geronimo.deployment.scanner.URLType;
import org.apache.geronimo.jmx.JMXUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/16 23:16:34 $
 */
public class ServiceDeploymentPlanner implements ServiceDeploymentPlannerMBean, MBeanRegistration {
    private MBeanServer server;
    private ObjectName objectName;
    private RelationServiceMBean relationService;
    private final DocumentBuilder parser;
    private final MBeanMetadataXMLLoader mbeanLoader;

    public ServiceDeploymentPlanner() {
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
        URLType type = goal.getType();
        if (type == URLType.RESOURCE) {
            if (!url.getPath().endsWith("-service.xml")) {
                return false;
            }
            try {
                is = url.openConnection().getInputStream();
            } catch (IOException e) {
                throw new DeploymentException(e);
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
        } catch (SAXException e) {
            throw new DeploymentException(e);
        } catch (IOException e) {
            throw new DeploymentException(e);
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

        ObjectName loaderName = addClassSpaces(doc.getElementsByTagName("class-space"), createDeploymentUnitPlan, url);
        plans.add(createDeploymentUnitPlan);

        // register a plan to create each mbean
        NodeList nl = doc.getElementsByTagName("mbean");
        for (int i = 0; i < nl.getLength(); i++) {
            DeploymentPlan createPlan = new DeploymentPlan();

            Element mbeanElement = (Element) nl.item(i);
            MBeanMetadata md = mbeanLoader.loadXML(mbeanElement);
            if (server.isRegistered(md.getName())) {
                throw new DeploymentException("MBean already exists " + md.getName());
            }
            md.setLoaderName(loaderName);
            md.setParentName(deploymentName);
            CreateMBeanInstance createTask = new CreateMBeanInstance(server, md);
            createPlan.addTask(createTask);
            InitializeMBeanInstance initTask = new InitializeMBeanInstance(server, md);
            createPlan.addTask(initTask);
            StartMBeanInstance startTask = new StartMBeanInstance(server, md);
            createPlan.addTask(startTask);

            plans.add(createPlan);
        }

        goals.remove(goal);
        return true;
    }

    private ObjectName addClassSpaces(NodeList nl, DeploymentPlan plan, URL baseURL) throws DeploymentException {
        Element classSpaceElement = (Element) nl.item(0);
        if (classSpaceElement == null) {
            return null;
        }

        ClassSpaceMetadataXMLLoader classSpaceLoader = new ClassSpaceMetadataXMLLoader(baseURL);
        ClassSpaceMetadata md = classSpaceLoader.loadXML(classSpaceElement);
        CreateClassSpace createTask = new CreateClassSpace(server, md);
        plan.addTask(createTask);
        return md.getName();
    }

    private boolean removeURL(UndeployURL goal, Set goals, Set plans) throws DeploymentException {
        URL url = goal.getUrl();
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

        for (Iterator i = mbeans.iterator(); i.hasNext();) {
            ObjectName name = (ObjectName) i.next();
            DeploymentTask task = new DestroyMBeanInstance(server, name);
            DeploymentPlan destroyPlan = new DeploymentPlan();
            destroyPlan.addTask(task);
            plans.add(destroyPlan);
        }

        DeploymentPlan undeployPlan = new DeploymentPlan();
        undeployPlan.addTask(new DestroyMBeanInstance(server, deploymentName));
        plans.add(undeployPlan);

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
