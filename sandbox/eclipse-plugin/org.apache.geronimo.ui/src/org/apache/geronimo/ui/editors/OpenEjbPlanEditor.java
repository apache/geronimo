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
package org.apache.geronimo.ui.editors;

import org.apache.geronimo.core.internal.GeronimoUtils;
import org.apache.geronimo.ui.internal.Messages;
import org.apache.geronimo.ui.pages.EjbOverviewPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.PartInitException;
/**
 * 
 * 
 */
public class OpenEjbPlanEditor extends AbstractGeronimoDeploymentPlanEditor {

    /**
     * 
     */
    public OpenEjbPlanEditor() {
        super();
    }

    public void doAddPages() throws PartInitException {
        addPage(new EjbOverviewPage(this, "ejboverview",
                Messages.editorTabGeneral));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.geronimo.ui.editors.AbstractGeronimoDeploymentPlanEditor#loadDeploymentPlan(org.eclipse.core.resources.IFile)
     */
    public EObject loadDeploymentPlan(IFile file) {
        return GeronimoUtils.getOpenEjbDeploymentPlan(file);
    }

}
