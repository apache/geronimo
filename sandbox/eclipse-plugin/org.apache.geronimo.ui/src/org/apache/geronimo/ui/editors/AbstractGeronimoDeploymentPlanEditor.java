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

import org.apache.geronimo.ui.internal.Trace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * 
 * 
 */
public abstract class AbstractGeronimoDeploymentPlanEditor extends FormEditor {

    private EObject deploymentPlan;

    /**
     * 
     */
    public AbstractGeronimoDeploymentPlanEditor() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        InputStream is = null;
        try {
            IEditorInput input = getEditorInput();
            if (input instanceof IFileEditorInput) {

                deploymentPlan.eResource().save(Collections.EMPTY_MAP);
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
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    protected void addPages() {
        try {
            doAddPages();
        } catch (PartInitException e1) {          
            e1.printStackTrace();
        }   
    }
    
    abstract public void doAddPages() throws PartInitException;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    public final void doSaveAs() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    public void commitFormPages(boolean onSave) {
        IFormPage[] pages = getPages();
        for (int i = 0; i < pages.length; i++) {
            IFormPage page = pages[i];
            IManagedForm mform = page.getManagedForm();
            if (mform != null && mform.isDirty())
                mform.commit(true);
        }
    }

    public IFormPage[] getPages() {
        ArrayList formPages = new ArrayList();
        for (int i = 0; i < pages.size(); i++) {
            Object page = pages.get(i);
            if (page instanceof IFormPage)
                formPages.add(page);
        }
        return (IFormPage[]) formPages.toArray(new IFormPage[formPages.size()]);
    }

    public EObject getDeploymentPlan() {
        return deploymentPlan;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        if (input instanceof IFileEditorInput) {
            IFileEditorInput fei = (IFileEditorInput) input;
            deploymentPlan = loadDeploymentPlan(fei.getFile());
        }
    }

    abstract public EObject loadDeploymentPlan(IFile file);

}
