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

import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;

/**
 * 
 * 
 */
public class ExportDeploymentPlanDataModelProvider extends
        AbstractDataModelProvider implements
        IExportDeploymentPlanDataModelProperties {

    /**
     * 
     */
    public ExportDeploymentPlanDataModelProvider() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider#getPropertyNames()
     */
    public String[] getPropertyNames() {
        return new String[]{IExportDeploymentPlanDataModelProperties.COMPONENT_NAME, IExportDeploymentPlanDataModelProperties.PROJECT_NAME};
    }

}
