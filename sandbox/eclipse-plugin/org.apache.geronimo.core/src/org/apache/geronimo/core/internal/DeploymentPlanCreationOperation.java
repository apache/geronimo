/**
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.core.internal;

import java.io.IOException;
import java.util.Collections;

import org.apache.geronimo.xml.ns.j2ee.application.ApplicationFactory;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationPackage;
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationType;
import org.apache.geronimo.xml.ns.j2ee.application.util.ApplicationResourceFactoryImpl;
import org.apache.geronimo.xml.ns.web.DocumentRoot;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.apache.geronimo.xml.ns.web.WebFactory;
import org.apache.geronimo.xml.ns.web.WebPackage;
import org.apache.geronimo.xml.ns.web.util.WebResourceFactoryImpl;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.jst.j2ee.datamodel.properties.IJavaComponentCreationDataModelProperties;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.properties.IFlexibleProjectCreationDataModelProperties;
import org.eclipse.wst.server.core.IModule;
import org.openejb.xml.ns.openejb.jar.JarFactory;
import org.openejb.xml.ns.openejb.jar.JarPackage;
import org.openejb.xml.ns.openejb.jar.OpenejbJarType;
import org.openejb.xml.ns.openejb.jar.util.JarResourceFactoryImpl;

public class DeploymentPlanCreationOperation extends AbstractDataModelOperation {

    public static final String WEB_DEP_PLAN_NAME = "geronimo-web.xml";

    public static final String EJB_DEP_PLAN_NAME = "openejb-jar.xml";

    public static final String APP_DEP_PLAN_NAME = "geronimo-application.xml";

    public DeploymentPlanCreationOperation() {
    }

    public DeploymentPlanCreationOperation(IDataModel model) {
        super(model);
    }

    public IStatus execute(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {

        if (isGeronimoRuntimeTarget()) {

            IVirtualComponent comp = ComponentCore.createComponent(
                    getProject(), getComponentName());

            if (comp.getComponentTypeId().equals(
                    IModuleConstants.JST_WEB_MODULE)) {
                createGeronimoWebDeploymentPlan(comp);
            } else if (comp.getComponentTypeId().equals(
                    IModuleConstants.JST_EJB_MODULE)) {
                createOpenEjbDeploymentPlan(comp);
            } else if (comp.getComponentTypeId().equals(
                    IModuleConstants.JST_EAR_MODULE)) {
                createGeronimoApplicationDeploymentPlan(comp);
            }
        }

        return Status.OK_STATUS;

    }

    public ApplicationType createGeronimoApplicationDeploymentPlan(
            IVirtualComponent comp) {
        IPath deployPlanPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath().append("META-INF").append(
                        APP_DEP_PLAN_NAME);

        IFile planFile = getProject().getFile(deployPlanPath);
        return createGeronimoApplicationDeploymentPlan(planFile);
    }

    public WebAppType createGeronimoWebDeploymentPlan(IVirtualComponent comp) {
        IPath deployPlanPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath().append("WEB-INF").append(
                        WEB_DEP_PLAN_NAME);

        IFile planFile = getProject().getFile(deployPlanPath);
        return createGeronimoWebDeploymentPlan(planFile);
    }

    public OpenejbJarType createOpenEjbDeploymentPlan(IVirtualComponent comp) {
        IPath deployPlanPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath().append("META-INF").append(
                        EJB_DEP_PLAN_NAME);

        IFile planFile = getProject().getFile(deployPlanPath);
        return createOpenEjbDeploymentPlan(planFile);
    }

    public ApplicationType createGeronimoApplicationDeploymentPlan(IFile dpFile) {
        URI uri = URI
                .createPlatformResourceURI(dpFile.getFullPath().toString());

        ResourceSet resourceSet = new ResourceSetImpl();
        // registerForWeb(resourceSet);

        Resource resource = resourceSet.createResource(uri);
        org.apache.geronimo.xml.ns.j2ee.application.DocumentRoot documentRoot = ApplicationFactory.eINSTANCE
                .createDocumentRoot();
        ApplicationType root = ApplicationFactory.eINSTANCE
                .createApplicationType();

        root.setApplicationName(getComponentName());
        root.setConfigId(getProject().getName() + "/" + getComponentName());

        documentRoot.setApplication(root);
        resource.getContents().add(documentRoot);

        doSave(resource);

        return root;
    }

    public WebAppType createGeronimoWebDeploymentPlan(IFile dpFile) {

        URI uri = URI
                .createPlatformResourceURI(dpFile.getFullPath().toString());

        ResourceSet resourceSet = new ResourceSetImpl();
        registerForWeb(resourceSet);

        Resource resource = resourceSet.createResource(uri);
        DocumentRoot documentRoot = WebFactory.eINSTANCE.createDocumentRoot();
        WebAppType root = WebFactory.eINSTANCE.createWebAppType();

        root.setConfigId(getProject().getName() + "/" + getComponentName());
        root.setContextRoot("/" + getComponentName());
        root.setContextPriorityClassloader(false);

        documentRoot.setWebApp(root);
        resource.getContents().add(documentRoot);

        doSave(resource);

        return root;
    }

    public OpenejbJarType createOpenEjbDeploymentPlan(IFile dpFile) {
        URI uri = URI
                .createPlatformResourceURI(dpFile.getFullPath().toString());

        ResourceSet resourceSet = new ResourceSetImpl();
        registerForWeb(resourceSet);

        Resource resource = resourceSet.createResource(uri);
        org.openejb.xml.ns.openejb.jar.DocumentRoot documentRoot = JarFactory.eINSTANCE.createDocumentRoot();
        OpenejbJarType root = JarFactory.eINSTANCE.createOpenejbJarType();
        
        root.setConfigId(getProject().getName() + "/" + getComponentName());
       
        documentRoot.setOpenejbJar(root);
        resource.getContents().add(documentRoot);

        doSave(resource);

        return root;
    }

    public static void registerForWeb(ResourceSet resourceSet) {
        // Register the appropriate resource factory to handle all file
        // extentions.
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                        new WebResourceFactoryImpl());

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                        new ApplicationResourceFactoryImpl());

        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                        new JarResourceFactoryImpl());

        // Register the package to ensure it is available during loading.
        resourceSet.getPackageRegistry().put(JarPackage.eNS_URI,
                JarPackage.eINSTANCE);

        resourceSet.getPackageRegistry().put(WebPackage.eNS_URI,
                WebPackage.eINSTANCE);

        resourceSet.getPackageRegistry().put(ApplicationPackage.eNS_URI,
                ApplicationPackage.eINSTANCE);
    }

    public static IFile getGeronimoWebDeploymentPlanFile(IModule module) {
        IProject project = module.getProject();

        IFlexibleProject flexProject = ComponentCore
                .createFlexibleProject(project);
        IVirtualComponent component = flexProject
                .getComponent(module.getName());
        IPath deployPlanPath = component.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath().append("WEB-INF").append(
                        WEB_DEP_PLAN_NAME);

        IFile planFile = project.getFile(deployPlanPath);
        return planFile;
    }

    public boolean isGeronimoRuntimeTarget() {

        String runtimeTarget = model.getProperty(
                IJavaComponentCreationDataModelProperties.RUNTIME_TARGET_ID)
                .toString();

        return runtimeTarget.startsWith("Apache Geronimo");

    }

    public IStatus redo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        return null;
    }

    public IStatus undo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        return null;
    }

    public String getComponentName() {
        return model.getProperty(
                IComponentCreationDataModelProperties.COMPONENT_NAME)
                .toString();
    }

    public IProject getProject() {
        String projectName = model.getProperty(
                IFlexibleProjectCreationDataModelProperties.PROJECT_NAME)
                .toString();
        if (projectName != null) {
            return ResourcesPlugin.getWorkspace().getRoot().getProject(
                    projectName);
        }
        return null;
    }

    private void doSave(Resource resource) {
        if (resource instanceof XMIResource) {
            ((XMIResource) resource).setEncoding("UTF-8");
        }

        try {
            resource.save(Collections.EMPTY_MAP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
