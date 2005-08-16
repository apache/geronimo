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
package org.apache.geronimo.ui.pages;

import org.apache.geronimo.ui.editors.DPEditor;
import org.apache.geronimo.ui.internal.Messages;
import org.apache.geronimo.ui.sections.EjbLocalRefSection;
import org.apache.geronimo.ui.sections.ResourceEnvRefSection;
import org.apache.geronimo.ui.sections.ResourceRefSection;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class NamingFormPage extends FormPage {

    public NamingFormPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
    }

    public NamingFormPage(String id, String title) {
        super(id, title);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    protected void createFormContent(IManagedForm managedForm) {

        WebAppType plan = ((DPEditor) getEditor()).getPlan();

        ScrolledForm form = managedForm.getForm();
        form.setText(Messages.editorTitle);
        form.getBody().setLayout(new GridLayout());

        // create resource ref section
        ResourceRefSection sec = new ResourceRefSection(plan, form.getBody(),
                managedForm.getToolkit(), ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED
                        | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
                        | ExpandableComposite.FOCUS_TITLE);
        managedForm.addPart(sec);

        // create resource env ref section
        ResourceEnvRefSection sec2 = new ResourceEnvRefSection(plan, form
                .getBody(), managedForm.getToolkit(),
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
                        | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
                        | ExpandableComposite.FOCUS_TITLE);
        managedForm.addPart(sec2);

        // create ejb local ref section
        EjbLocalRefSection sec3 = new EjbLocalRefSection(plan, form.getBody(),
                managedForm.getToolkit(), ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED
                        | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
                        | ExpandableComposite.FOCUS_TITLE);
        managedForm.addPart(sec3);

        form.reflow(true);

    }

}
