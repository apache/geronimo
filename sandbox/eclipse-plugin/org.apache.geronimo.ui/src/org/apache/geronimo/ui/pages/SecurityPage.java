package org.apache.geronimo.ui.pages;

import org.apache.geronimo.ui.editors.DPEditor;
import org.apache.geronimo.ui.sections.SecuritySection;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class SecurityPage extends FormPage {

    public SecurityPage(FormEditor editor, String id, String title) {
        super(editor, id, title);
        // TODO Auto-generated constructor stub
    }

    public SecurityPage(String id, String title) {
        super(id, title);
        // TODO Auto-generated constructor stub
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
        layout.numColumns = 1;
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

        managedForm.addPart(new SecuritySection(plan, body, managedForm
                .getToolkit(), style));
    }

}
