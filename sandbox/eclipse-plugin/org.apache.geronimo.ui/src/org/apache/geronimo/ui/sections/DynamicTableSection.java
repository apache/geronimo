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

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.ui.internal.GeronimoUIPlugin;
import org.apache.geronimo.ui.internal.Messages;
import org.apache.geronimo.ui.wizards.DynamicAddEditWizard;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

public abstract class DynamicTableSection extends SectionPart {

    private EObject plan;

    protected Table table;

    protected TableViewer tableViewer;

    private ImageDescriptor defaultDescriptor = GeronimoUIPlugin
            .imageDescriptorFromPlugin("org.apache.geronimo.ui",
                    "icons/obj16/geronimo.gif");

    private Image defaultImage = defaultDescriptor.createImage();

    public DynamicTableSection(Section section) {
        super(section);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
     * 
     * Overriding this method as a workaround as switching tabs on a dirty
     * editor commits the page and marks the part as not dirty.
     */
    public void commit(boolean onSave) {
        boolean currentDirtyState = isDirty();
        super.commit(onSave);
        if (!onSave && currentDirtyState) {
            markDirty();
        }
    }

    public DynamicTableSection(EObject plan, Composite parent,
            FormToolkit toolkit, int style) {
        super(parent, toolkit, style);
        this.plan = plan;

        if (isValid()) {
            createClient(getSection(), toolkit);
        }
    }

    private boolean isValid() {
        return getEFactory() != null && getEReference() != null
                && getTableColumnEAttributes() != null
                && getTableColumnNames() != null;
    }

    public void createClient(Section section, FormToolkit toolkit) {

        section.setText(getTitle());
        section.setDescription(getDescription());
        
        configureSection(section);

        Composite composite = createTableComposite(section, toolkit);
        createTable(composite);
        fillTableItems();

        tableViewer = new TableViewer(table);

        if (getTableColumnNames().length > 0) {
            tableViewer.setColumnProperties(getTableColumnNames());
        }

        Composite buttonComp = createButtonComposite(toolkit, composite);
        createAddButton(toolkit, buttonComp);
        createRemoveButton(toolkit, buttonComp);
        createEditButton(toolkit, buttonComp);

    }

    protected Composite createTableComposite(Section section,
            FormToolkit toolkit) {
        Composite composite = toolkit.createComposite(section);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 5;
        layout.marginWidth = 10;
        layout.verticalSpacing = 5;
        layout.horizontalSpacing = 15;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        section.setClient(composite);
        return composite;
    }

    protected void configureSection(Section section) {       
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    }

    protected void fillTableItems() {
        EList list = (EList) plan.eGet(getEReference());

        for (int j = 0; j < list.size(); j++) {
            TableItem item = new TableItem(table, SWT.NONE);
            String[] tableTextData = getTableText((EObject) list.get(j));
            item.setImage(getImage());
            item.setText(tableTextData);
            item.setData((EObject) list.get(j));
        }
    }

    protected void createTable(Composite composite) {
        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.V_SCROLL | SWT.SINGLE);
        if (getTableColumnNames().length > 0) {
            table.setHeaderVisible(true);
        }

        GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
        data.heightHint = 60;
        data.widthHint = 400;
        table.setLayoutData(data);

        TableLayout tableLayout = new TableLayout();
        table.setLayout(tableLayout);

        for (int i = 0; i < getTableColumnNames().length; i++) {
            tableLayout.addColumnData(new ColumnWeightData(35));
            TableColumn tableColumn = new TableColumn(table, SWT.NONE);
            tableColumn.setText(getTableColumnNames()[i]);

        }

    }

    protected Composite createButtonComposite(FormToolkit toolkit,
            Composite parent) {
        GridLayout layout;
        Composite buttonComp = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.horizontalSpacing = 2;
        layout.verticalSpacing = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 1;
        buttonComp.setLayout(layout);
        buttonComp.setBackground(toolkit.getColors().getBackground());
        buttonComp
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        return buttonComp;
    }

    protected void createRemoveButton(FormToolkit toolkit, Composite buttonComp) {
        Button del = toolkit
                .createButton(buttonComp, Messages.remove, SWT.NONE);
        del.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                int[] selectedIndices = table.getSelectionIndices();
                for (int i = 0; i < selectedIndices.length; i++) {
                    TableItem tableItem = table.getItem(selectedIndices[i]);
                    EObject type = (EObject) (tableItem.getData());
                    table.remove(selectedIndices[i]);
                    ((EList) plan.eGet(getEReference())).remove(type);
                    markDirty();
                }
            }
        });
        del.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    protected void createAddButton(FormToolkit toolkit, Composite buttonComp) {
        Button add = toolkit.createButton(buttonComp, Messages.add, SWT.NONE);

        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                DynamicAddEditWizard wizard = getWizard();
                if (wizard != null) {
                    WizardDialog dialog = new WizardDialog(Display.getCurrent()
                            .getActiveShell(), wizard);

                    dialog.open();

                    if (dialog.getReturnCode() == Dialog.OK) {
                        markDirty();
                    }
                }
            }
        });

        add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    protected void createEditButton(FormToolkit toolkit, Composite buttonComp) {
        Button add = toolkit.createButton(buttonComp, Messages.edit, SWT.NONE);

        add.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Object o = ((StructuredSelection) getTableViewer()
                        .getSelection()).getFirstElement();

                if (o != null) {

                    DynamicAddEditWizard wizard = getWizard();
                    if (wizard != null) {
                        wizard.setEObject((EObject) o);

                        WizardDialog dialog = new WizardDialog(Display
                                .getCurrent().getActiveShell(), wizard);

                        dialog.open();

                        if (dialog.getReturnCode() == Dialog.OK) {
                            markDirty();
                        }
                    }
                }

            }
        });
        add.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    public String[] getTableText(EObject eObject) {
        List tableText = new ArrayList();
        for (int i = 0; i < getTableColumnEAttributes().length; i++) {
            String value = (String) eObject
                    .eGet(getTableColumnEAttributes()[i]);
            if (value != null) {
                tableText.add(value);
            } else {
                tableText.add("");
            }
        }
        return (String[]) tableText.toArray(new String[tableText.size()]);
    }

    public Image getImage() {
        return defaultImage;
    }

    public TableViewer getTableViewer() {
        return tableViewer;
    }

    public EObject getPlan() {
        return plan;
    }

    /**
     * @return
     */
    abstract public String getTitle();

    /**
     * @return
     */
    abstract public String getDescription();

    /**
     * @return
     */
    abstract public EFactory getEFactory();

    /**
     * @return
     */
    abstract public EReference getEReference();

    /**
     * @return
     */
    abstract public String[] getTableColumnNames();

    /**
     * @return
     */
    abstract public EAttribute[] getTableColumnEAttributes();

    /**
     * @return
     */
    abstract public DynamicAddEditWizard getWizard();

}
