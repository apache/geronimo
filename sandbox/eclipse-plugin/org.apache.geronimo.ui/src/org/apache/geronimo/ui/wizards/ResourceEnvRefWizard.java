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

public class ResourceEnvRefWizard extends DynamicAddEditWizard {

    /**
     * @param section
     */
    public ResourceEnvRefWizard(DynamicTableSection section) {
        super(section);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.geronimo.ui.wizards.DynamicAddEditWizard#getAddWizardWindowTitle()
     */
    public String getAddWizardWindowTitle() {
        return Messages.wizardPageTitle_ResEnvRef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.geronimo.ui.wizards.DynamicAddEditWizard#getEditWizardWindowTitle()
     */
    public String getEditWizardWindowTitle() {
        return Messages.wizardEditTitle_ResEnvRef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.geronimo.ui.wizards.DynamicAddEditWizard#getWizardFirstPageTitle()
     */
    public String getWizardFirstPageTitle() {
        return Messages.wizardPageTitle_ResEnvRef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.geronimo.ui.wizards.DynamicAddEditWizard#getWizardFirstPageDescription()
     */
    public String getWizardFirstPageDescription() {
        return Messages.wizardPageDescription_ResEnvRef;
    }

}
