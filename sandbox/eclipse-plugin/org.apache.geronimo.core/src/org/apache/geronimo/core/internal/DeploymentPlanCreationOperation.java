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
import org.apache.geronimo.xml.ns.j2ee.application.ApplicationType;
import org.apache.geronimo.xml.ns.web.DocumentRoot;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.apache.geronimo.xml.ns.web.WebFactory;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
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
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.properties.IFlexibleProjectCreationDataModelProperties;
import org.openejb.xml.ns.openejb.jar.JarFactory;
import org.openejb.xml.ns.openejb.jar.OpenejbJarType;

public class DeploymentPlanCreationOperation extends AbstractDataModelOperation {

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
                createGeronimoWebDeploymentPlan(GeronimoUtils
                        .getWebDeploymentPlanFile(comp));
            } else if (comp.getComponentTypeId().equals(
                    IModuleConstants.JST_EJB_MODULE)) {
                createOpenEjbDeploymentPlan(GeronimoUtils
                        .getOpenEjbDeploymentPlanFile(comp));
            } else if (comp.getComponentTypeId().equals(
                    IModuleConstants.JST_EAR_MODULE)) {
                createGeronimoApplicationDeploymentPlan(GeronimoUtils
                        .getApplicationDeploymentPlanFile(comp));
            }
        }

        return Status.OK_STATUS;

    }

    public ApplicationType createGeronimoApplicationDeploymentPlan(IFile dpFile) {
        URI uri = URI
                .createPlatformResourceURI(dpFile.getFullPath().toString());

        ResourceSet resourceSet = new ResourceSetImpl();
        GeronimoUtils.registerAppFactoryAndPackage(resourceSet);

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
        GeronimoUtils.registerWebFactoryAndPackage(resourceSet);

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
        GeronimoUtils.registerEjbFactoryAndPackage(resourceSet);

        Resource resource = resourceSet.createResource(uri);
        org.openejb.xml.ns.openejb.jar.DocumentRoot documentRoot = JarFactory.eINSTANCE
                .createDocumentRoot();
        OpenejbJarType root = JarFactory.eINSTANCE.createOpenejbJarType();

        root.setConfigId(getProject().getName() + "/" + getComponentName());

        documentRoot.setOpenejbJar(root);
        resource.getContents().add(documentRoot);

        doSave(resource);

        return root;
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
