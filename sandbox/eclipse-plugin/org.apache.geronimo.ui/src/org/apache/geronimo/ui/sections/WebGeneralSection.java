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
import org.apache.geronimo.xml.ns.web.WebAppType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public class WebGeneralSection extends SectionPart {
    
    protected Text configId;

    protected Text parentId;

    protected Text contextRoot;

    protected Button cpClassLoaderWebAppFirst;

    protected Button cpClassLoaderServerFirst;
    
    WebAppType plan;

    public WebGeneralSection(Section section) {
        super(section);
    }

    public WebGeneralSection(Composite parent, FormToolkit toolkit, int style) {
        super(parent, toolkit, style);
    }
    
    public WebGeneralSection(WebAppType plan, Composite parent, FormToolkit toolkit, int style) {
        this(parent, toolkit, style);        
        this.plan = plan;
        createClient(getSection(), toolkit);
    }
    
    private void createClient(Section section, FormToolkit toolkit) {
        
        section.setText(Messages.editorSectionGeneralTitle);
        section.setDescription(Messages.editorSectionGeneralDescription);
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        // toolkit.paintBordersFor(composite);
        section.setClient(composite);

        // ------- Label and text field for the config Id -------
        createLabel(composite, Messages.editorConfigId, toolkit);

        configId = toolkit.createText(composite, plan.getConfigId(),
                SWT.BORDER);
        configId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        configId.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                plan.setConfigId(configId.getText());                             
                markDirty();
            }
        });

        // ------- Label and text field for the parent Id -------
        createLabel(composite, Messages.editorParentId, toolkit);

        parentId = toolkit.createText(composite, plan.getParentId(),
                SWT.BORDER);
        parentId
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        parentId.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                plan.setParentId(parentId.getText());
                markDirty();
            }
        });

        // ------- Label and text field for the context root -------
        createLabel(composite, Messages.editorContextRoot, toolkit);

        contextRoot = toolkit.createText(composite, plan.getContextRoot(),
                SWT.BORDER);
        contextRoot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
                false));
        contextRoot.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                plan.setContextRoot(contextRoot.getText());
                markDirty();
            }
        });

        // ------- Label and check box for the Context Priority Classloader
        // -------
        Label classLoaderLabel = toolkit.createLabel(composite,
                Messages.editorClassloader);
        GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        classLoaderLabel.setLayoutData(data);

        cpClassLoaderServerFirst = toolkit.createButton(composite,
                Messages.editorClassloaderServer, SWT.RADIO);
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        cpClassLoaderServerFirst.setLayoutData(data);
        cpClassLoaderServerFirst.setSelection(!plan
                .isContextPriorityClassloader());

        cpClassLoaderWebAppFirst = toolkit.createButton(composite,
                Messages.editorClassloaderWebApp, SWT.RADIO);
        data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        cpClassLoaderWebAppFirst.setLayoutData(data);
        cpClassLoaderWebAppFirst.setSelection(plan
                .isContextPriorityClassloader());

        cpClassLoaderWebAppFirst.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                plan.setContextPriorityClassloader(cpClassLoaderWebAppFirst
                        .getSelection());
                markDirty();
               
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                // Not invoked
            }
        });

        
    }
    
    protected Label createLabel(Composite parent, String text, FormToolkit toolkit) {
        Label label = toolkit.createLabel(parent, text);
        label
                .setForeground(toolkit.getColors().getColor(
                        FormColors.TITLE));
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        return label;
    }

}
