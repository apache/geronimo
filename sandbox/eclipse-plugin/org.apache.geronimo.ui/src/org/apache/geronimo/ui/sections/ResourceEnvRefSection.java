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
package org.apache.geronimo.ui.sections;

import org.apache.geronimo.ui.internal.Messages;
import org.apache.geronimo.xml.ns.naming.NamingFactory;
import org.apache.geronimo.xml.ns.naming.NamingPackage;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.apache.geronimo.xml.ns.web.WebFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ResourceEnvRefSection extends DynamicTableSection {

    public ResourceEnvRefSection(WebAppType plan, Composite parent,
            FormToolkit toolkit, int style) {
        super(plan, parent, toolkit, style);
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.ui.sections.DynamicTableSection#getTitle()
     */
    protected String getTitle() {
        return Messages.editorResourceEnvRefTitle;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.ui.sections.DynamicTableSection#getDescription()
     */
    protected String getDescription() {
        return Messages.editorResourceEnvRefDescription;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.ui.sections.DynamicTableSection#getEFactory()
     */
    protected EFactory getEFactory() {
        return NamingFactory.eINSTANCE;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.ui.sections.DynamicTableSection#getEReference()
     */
    protected EReference getEReference() {
        return WebFactory.eINSTANCE.getWebPackage()
                .getWebAppType_ResourceEnvRef();
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.ui.sections.DynamicTableSection#getTableColumnNames()
     */
    protected String[] getTableColumnNames() {
        return new String[] { Messages.editorResEnvRefNameTitle,
                Messages.editorResEnvRefMsgDestTitle };
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.ui.sections.DynamicTableSection#getTableColumnEAttributes()
     */
    protected EAttribute[] getTableColumnEAttributes() {
        return new EAttribute[] {
                NamingPackage.eINSTANCE.getResourceEnvRefType_RefName(),
                NamingPackage.eINSTANCE
                        .getResourceEnvRefType_MessageDestinationLink() };
    }

}
