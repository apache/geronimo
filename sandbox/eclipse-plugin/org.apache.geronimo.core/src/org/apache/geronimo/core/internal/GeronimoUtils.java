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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.enterprise.deploy.shared.ModuleType;

import org.apache.geronimo.xml.ns.web.DocumentRoot;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.jst.j2ee.internal.deployables.J2EEFlexProjDeployable;
import org.eclipse.jst.server.core.IJ2EEModule;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.common.componentcore.ArtifactEdit;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.server.core.IModule;

public class GeronimoUtils {

	public static String getConfigId(IModule module) {

		String configId = null;

		if (isWebModule(module)) {			
			WebAppType deploymentPlan = getWebDeploymentPlan(module);

			if (deploymentPlan != null)
				configId = deploymentPlan.getConfigId();

			if (configId == null)
				configId = getId(module);
		} else if (isEjbJarModule(module)) {
			configId = getId(module);
		}

		return configId;
	}

	public static boolean isWebModule(IModule module) {
		return "j2ee.web".equals(module.getModuleType().getId());
	}

	public static boolean isEjbJarModule(IModule module) {
		return "j2ee.ejb".equals(module.getModuleType().getId());
	}

	public static ModuleType getJSR88ModuleType(IModule module) {
		if (isWebModule(module)) {
			return ModuleType.WAR;
		} else if (isEjbJarModule(module)) {
			return ModuleType.EJB;
		}
		return null;
	}

	public static String getContextRoot(IModule module) {
		String contextRoot = null;
		
		WebAppType deploymentPlan = getWebDeploymentPlan(module);
		if (deploymentPlan != null)
			contextRoot = deploymentPlan.getContextRoot();

		if (contextRoot == null)
			contextRoot = getId(module);

		return contextRoot;
	}

	public static String getId(IModule module) {
		// use the module ID
		String moduleId = module.getId();

		IJ2EEModule j2eeModule = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, null);
		if (j2eeModule != null && j2eeModule instanceof J2EEFlexProjDeployable) {
			J2EEFlexProjDeployable j2eeFlex = (J2EEFlexProjDeployable) j2eeModule;
			// j2eeFlex
			ArtifactEdit edit = null;

			try {
				edit = ArtifactEdit.getArtifactEditForRead(j2eeFlex.getComponentHandle());
				XMIResource res = (XMIResource) edit.getContentModelRoot().eResource();
				moduleId = res.getID(edit.getContentModelRoot());
			} finally {
				if (edit != null)
					edit.dispose();
			}
		}

		if (moduleId != null && moduleId.length() > 0)
			return moduleId;

		// ...but if there is no defined module ID, pick the best alternative

		IPath moduleLocation = j2eeModule.getLocation();
		if (moduleLocation != null) {
			moduleId = moduleLocation.removeFileExtension().lastSegment();
		}

		if (j2eeModule instanceof IWebModule) {
			// A better choice is to use the context root
			// For wars most appservers use the module name
			// as the context root
			String contextRoot = ((IWebModule) j2eeModule).getContextRoot();
			if (contextRoot.charAt(0) == '/')
				moduleId = contextRoot.substring(1);
		}

		return moduleId;
	}

	// Temporary workaround - ensure the .deployable copy
	// of the file is also updated (WTP should be doing this)
	public static void copyDeploymentPlanToDeployable(IModule module) {
		IFile planFile = DeploymentPlanCreationOperation.getGeronimoWebDeploymentPlanFile(module);
		if (planFile.exists()) {
			try {
				IJ2EEModule j2eeModule = (IJ2EEModule) module.loadAdapter(IJ2EEModule.class, null);
				File deployableFile = j2eeModule.getLocation().addTrailingSeparator().append(
						"WEB-INF").addTrailingSeparator().append(DeploymentPlanCreationOperation.WEB_DEP_PLAN_NAME).toFile();
				InputStream input = planFile.getContents();
				FileOutputStream output = new FileOutputStream(deployableFile);
				byte[] buffer = new byte[1000];
				int bytesRead = 0;
				while (bytesRead > -1) {
					bytesRead = input.read(buffer);
					if (bytesRead > 0)
						output.write(buffer, 0, bytesRead);
				}
				output.close();
				input.close();
			} catch (Exception e) {
				Trace.trace(Trace.SEVERE, "Error copying deployment plan", e);
			}
		}
	}

	private static Resource load(IFile dpFile) {
		try {
			ResourceSet resourceSet = new ResourceSetImpl();
            DeploymentPlanCreationOperation.registerForWeb(resourceSet);
				
			URI uri = URI.createPlatformResourceURI(dpFile.getFullPath().toString());
			
			Resource resource = resourceSet.createResource(uri);
			if (!resource.isLoaded()) {
				resource.load(null);
			}
			return resource;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    public static WebAppType getWebDeploymentPlan(IFile dpFile) {       
        if (dpFile.exists()) {
            Resource resource = load(dpFile);
            if (resource != null) {
                return ((DocumentRoot) resource.getContents().get(0)).getWebApp();
            }
        } 
        return null;
    }

	private static WebAppType getWebDeploymentPlan(IModule module) {
        IFile dpFile = DeploymentPlanCreationOperation.getGeronimoWebDeploymentPlanFile(module);
		if (dpFile.exists()) {
			Resource resource = load(dpFile);
			if (resource != null) {
				return ((DocumentRoot) resource.getContents().get(0)).getWebApp();
			}
		} else {
			if (module != null) {
				return createWebDeploymentPlan(module);
			}
		}
		return null;
	}
    
    private static WebAppType createWebDeploymentPlan(IModule module) {
        IFlexibleProject flexProject = ComponentCore.createFlexibleProject(module.getProject());
        IVirtualComponent component = flexProject.getComponent(module.getName());
        DeploymentPlanCreationOperation op = new DeploymentPlanCreationOperation();
        return op.createGeronimoWebDeploymentPlan(component);
    }
}