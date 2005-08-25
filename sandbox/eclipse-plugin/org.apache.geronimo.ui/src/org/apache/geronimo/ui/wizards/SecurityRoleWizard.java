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
package org.apache.geronimo.ui.wizards;

import org.apache.geronimo.ui.internal.Messages;
import org.apache.geronimo.ui.sections.DynamicTableSection;
import org.apache.geronimo.ui.sections.SecuritySection;
import org.apache.geronimo.xml.ns.security.DescriptionType;
import org.apache.geronimo.xml.ns.security.RoleMappingsType;
import org.apache.geronimo.xml.ns.security.RoleType;
import org.apache.geronimo.xml.ns.security.SecurityFactory;
import org.apache.geronimo.xml.ns.security.SecurityPackage;
import org.apache.geronimo.xml.ns.security.SecurityType;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

public class SecurityRoleWizard extends DynamicAddEditWizard {

    public SecurityRoleWizard(DynamicTableSection section) {
        super(section);
    }

    public String getAddWizardWindowTitle() {
        return Messages.wizardNewTitle_SecurityRole;
    }

    public String getEditWizardWindowTitle() {
        return Messages.wizardEditTitle_SecurityRole;
    }

    public String getWizardFirstPageTitle() {
        return Messages.wizardPageTitle_SecurityRole;
    }

    public String getWizardFirstPageDescription() {
        return Messages.wizardPageDescription_SecurityRole;
    }

    public boolean performFinish() {
        DynamicWizardPage page = (DynamicWizardPage) getPages()[0];

        boolean isNew = false;

        if (eObject == null) {
            eObject = section.getEFactory().create(
                    section.getTableColumnEAttributes()[0]
                            .getEContainingClass());
            EObject plan = section.getPlan();

            SecurityType securityType = (SecurityType) plan
                    .eGet(SecuritySection.securityERef);
            if (securityType == null) {
                securityType = SecurityFactory.eINSTANCE.createSecurityType();
                plan.eSet(SecuritySection.securityERef, securityType);
            }

            RoleMappingsType roleMappingsType = securityType.getRoleMappings();
            if (roleMappingsType == null) {
                roleMappingsType = SecurityFactory.eINSTANCE
                        .createRoleMappingsType();
                securityType.setRoleMappings(roleMappingsType);
            }

            roleMappingsType.getRole().add(eObject);
            isNew = true;
        }

        for (int i = 0; i < section.getTableColumnEAttributes().length; i++) {
            String value = page.textEntries[i].getText();
            EAttribute attribute = section.getTableColumnEAttributes()[i];
            if (attribute.getEContainingClass().equals(eObject.eClass())) {
                eObject.eSet(attribute, value);
            } else {
                if (attribute.getFeatureID() == SecurityPackage.DESCRIPTION_TYPE__LANG) {
                    DescriptionType type = null;
                    RoleType roleType = ((RoleType) eObject);
                    if (roleType.getDescription().isEmpty()) {
                        type = SecurityFactory.eINSTANCE
                                .createDescriptionType();
                        roleType.getDescription().add(type);
                    } else {
                        type = (DescriptionType) roleType.getDescription().get(
                                0);
                    }
                    type.setLang(value);
                }
            }
        }

        String[] tableText = section.getTableText(eObject);

        if (isNew) {
            TableItem item = new TableItem(section.getTableViewer().getTable(),
                    SWT.NONE);
            item.setImage(section.getImage());
            item.setData(eObject);
            item.setText((String) eObject.eGet(SecurityPackage.eINSTANCE
                    .getRoleType_RoleName()));
        } else {
            int index = section.getTableViewer().getTable().getSelectionIndex();
            if (index != -1) {
                TableItem item = section.getTableViewer().getTable().getItem(
                        index);
                item.setText(tableText);
            }
        }

        return true;
    }
}
