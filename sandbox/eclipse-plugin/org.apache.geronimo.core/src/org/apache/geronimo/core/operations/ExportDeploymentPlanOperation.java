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
package org.apache.geronimo.core.operations;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.geronimo.core.internal.DeploymentPlanCreationOperation;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
import org.eclipse.jst.j2ee.ejb.componentcore.util.EJBArtifactEdit;
import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

/**
 * 
 * 
 */
public class ExportDeploymentPlanOperation extends AbstractDataModelOperation
        implements IExportDeploymentPlanDataModelProperties {

    /**
     * 
     */
    public ExportDeploymentPlanOperation() {
        super();
    }

    /**
     * @param model
     */
    public ExportDeploymentPlanOperation(IDataModel model) {
        super(model);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor,
     *      org.eclipse.core.runtime.IAdaptable)
     */
    public IStatus execute(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {

        IProject project = ProjectUtilities
                .getProject(model
                        .getStringProperty(IExportDeploymentPlanDataModelProperties.PROJECT_NAME));

        IVirtualComponent component = ComponentCore.createComponent(project,
                IExportDeploymentPlanDataModelProperties.COMPONENT_NAME);

        if (component.getComponentTypeId().equals(EARArtifactEdit.TYPE_ID)) {
            IVirtualReference[] refs = component.getReferences();
            for (int i = 0; i < refs.length; i++) {
                IVirtualComponent refComp = refs[i].getReferencedComponent();
                EObject plan = getDeploymentPlanForComponent(refComp);
                if (plan != null) {
                    addToGeronimoApplicationPlan(plan, refComp);
                }
            }
        }

        return null;
    }

    // TODO
    private void addToGeronimoApplicationPlan(EObject eObject,
            IVirtualComponent component) {

    }

    private EObject getDeploymentPlanForComponent(IVirtualComponent comp) {
        if (comp.getComponentTypeId().equals(EJBArtifactEdit.TYPE_ID)) {
            return getWebDeploymentPlan(comp);
        }

        if (comp.getComponentTypeId().equals(WebArtifactEdit.TYPE_ID)) {
            return getWebDeploymentPlan(comp);
        }
        return null;
    }

    private EObject getWebDeploymentPlan(IVirtualComponent comp) {
        IPath deployPlanPath = comp.getRootFolder().getUnderlyingFolder()
                .getProjectRelativePath().append("WEB-INF").append(
                        WEB_PLAN_NAME);        
        return load(comp.getProject().getFile(deployPlanPath));
    }

    private EObject getOpenEjbDeploymentPlan(IVirtualComponent comp) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.operations.AbstractOperation#redo(org.eclipse.core.runtime.IProgressMonitor,
     *      org.eclipse.core.runtime.IAdaptable)
     */
    public IStatus redo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.operations.AbstractOperation#undo(org.eclipse.core.runtime.IProgressMonitor,
     *      org.eclipse.core.runtime.IAdaptable)
     */
    public IStatus undo(IProgressMonitor monitor, IAdaptable info)
            throws ExecutionException {
        return null;
    }

    public EObject load(IFile file) {
        if (file.exists()) {
            try {
                ResourceSet resourceSet = new ResourceSetImpl();
                DeploymentPlanCreationOperation.registerForWeb(resourceSet);

                URI uri = URI.createPlatformResourceURI(file.getFullPath()
                        .toString());

                Resource resource = resourceSet.createResource(uri);
                if (!resource.isLoaded()) {
                    resource.load(null);
                }
                return (EObject) resource.getContents().get(0);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
