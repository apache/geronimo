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
import org.apache.geronimo.ui.sections.EjbLocalRefSection;
import org.apache.geronimo.ui.sections.EjbRefSection;
import org.apache.geronimo.ui.sections.ResourceEnvRefSection;
import org.apache.geronimo.ui.sections.ResourceRefSection;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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

        ScrolledForm form = managedForm.getForm();
        form.setText(getTitle());
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 20;

        form.getBody().setLayout(layout);

        fillBody(managedForm);

        form.reflow(true);

    }

    private void fillBody(IManagedForm managedForm) {

        WebAppType plan = ((DPEditor) getEditor()).getPlan();

        Composite body = managedForm.getForm().getBody();

        int style = ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
                | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
                | ExpandableComposite.FOCUS_TITLE;

        managedForm.addPart(new ResourceRefSection(plan, body, managedForm
                .getToolkit(), style));

        managedForm.addPart(new ResourceEnvRefSection(plan, body, managedForm
                .getToolkit(), style));

        managedForm.addPart(new EjbRefSection(plan, body, managedForm
                .getToolkit(), style));

        managedForm.addPart(new EjbLocalRefSection(plan, body, managedForm
                .getToolkit(), style));
    }

}
