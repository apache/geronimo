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

import org.apache.geronimo.core.internal.GeronimoServer;
import org.apache.geronimo.ui.commands.SetPasswordCommand;
import org.apache.geronimo.ui.commands.SetUsernameCommand;
import org.apache.geronimo.ui.internal.Messages;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

/**
 * 
 * 
 */
public class ServerSecurityEditorSection extends ServerEditorSection {

    Text username;

    Text password;

    GeronimoServer gs;

    /**
     * 
     */
    public ServerSecurityEditorSection() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.wst.server.ui.editor.ServerEditorSection#createSection(org.eclipse.swt.widgets.Composite)
     */
    public void createSection(Composite parent) {
        super.createSection(parent);

        FormToolkit toolkit = getFormToolkit(parent.getDisplay());

        Section section = toolkit.createSection(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
                        | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION
                        | ExpandableComposite.FOCUS_TITLE);

        section.setText(Messages.editorSectionSecurityTitle);
        section.setDescription(Messages.editorSectionSecurityDescription);
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
        section.setClient(composite);

        // ------- Label and text field for the username -------
        createLabel(composite, Messages.username, toolkit);

        username = toolkit.createText(composite, getUserName(), SWT.BORDER);
        username.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        username.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                execute(new SetUsernameCommand(server, username.getText()));
            }
        });

        // ------- Label and text field for the password -------
        createLabel(composite, Messages.password, toolkit);

        password = toolkit.createText(composite, getPassword(), SWT.BORDER);
        password
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        password.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                execute(new SetPasswordCommand(server, password.getText()));
            }
        });

    }

    protected Label createLabel(Composite parent, String text,
            FormToolkit toolkit) {
        Label label = toolkit.createLabel(parent, text);
        label.setForeground(toolkit.getColors().getColor(FormColors.TITLE));
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.wst.server.ui.editor.ServerEditorSection#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        gs = (GeronimoServer) server.getAdapter(GeronimoServer.class);
        if (gs == null) {
            gs = (GeronimoServer) server.loadAdapter(GeronimoServer.class,
                    new NullProgressMonitor());
        }
    }

    private String getUserName() {
        if (gs != null) {
            return gs.getAdminID();
        }
        return "";
    }

    private String getPassword() {
        if (gs != null) {
            return gs.getAdminPassword();
        }
        return "";
    }

}
