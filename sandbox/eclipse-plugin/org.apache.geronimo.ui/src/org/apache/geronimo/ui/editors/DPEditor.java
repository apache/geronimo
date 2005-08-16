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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.geronimo.core.internal.GeronimoUtils;
import org.apache.geronimo.ui.internal.Messages;
import org.apache.geronimo.ui.internal.Trace;
import org.apache.geronimo.ui.pages.NamingFormPage;
import org.apache.geronimo.ui.pages.WebGeneralPage;
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class DPEditor extends FormEditor {

    protected FormToolkit toolkit;

    protected WebAppType plan;

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        if (isDirty()) {
            InputStream is = null;
            try {
                IEditorInput input = getEditorInput();
                if (input instanceof IFileEditorInput) {
                    plan.eResource().save(Collections.EMPTY_MAP);
                    commitFormPages(true);
                    editorDirtyStateChanged();
                }
            } catch (Exception e) {
                Trace.trace(Trace.SEVERE, "Error saving", e);
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
    }

    private void commitFormPages(boolean onSave) {
        IFormPage[] pages = getPages();
        for (int i = 0; i < pages.length; i++) {
            IFormPage page = pages[i];
            IManagedForm mform = page.getManagedForm();
            if (mform != null && mform.isDirty())
                mform.commit(true);
        }
    }

    private IFormPage[] getPages() {
        ArrayList formPages = new ArrayList();
        for (int i = 0; i < pages.size(); i++) {
            Object page = pages.get(i);
            if (page instanceof IFormPage)
                formPages.add(page);
        }
        return (IFormPage[]) formPages.toArray(new IFormPage[formPages.size()]);
    }

    public void doSaveAs() {
        // ignore
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            addPage(new WebGeneralPage(this, "webgeneralpage",
                    Messages.editorTabGeneral));
            addPage(new NamingFormPage(this, "namingformpage",
                    Messages.editorTabNaming));
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        createPageSecurity();
        createPageDependencies();
    }

    protected void createPageSecurity() {

        ScrolledForm form = getToolkit().createScrolledForm(getContainer());

        form.setText(Messages.editorTitle);
        form.getBody().setLayout(new GridLayout());

        form.reflow(true);

        int index = addPage(form);
        setPageText(index, Messages.editorTabSecurity); //$NON-NLS-1$

    }

    protected void createPageDependencies() {

        ScrolledForm form = getToolkit().createScrolledForm(getContainer());

        form.setText(Messages.editorTitle);
        form.getBody().setLayout(new GridLayout());

        form.reflow(true);

        int index = addPage(form);
        setPageText(index, Messages.editorTabDependencies); //$NON-NLS-1$

    }

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        if (input instanceof IFileEditorInput) {
            IFileEditorInput fei = (IFileEditorInput) input;
            plan = GeronimoUtils.getWebDeploymentPlan(fei.getFile());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    public WebAppType getPlan() {
        return plan;
    }
}