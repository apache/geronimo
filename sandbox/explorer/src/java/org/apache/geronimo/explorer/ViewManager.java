/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.explorer;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.swing.JPanel;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Manages views of selected tree nodes
 *
 * @version <code>$Rev$ $Date$</code>
 */
public class ViewManager {

    private JPanel panel;
    private Object explorer;
    private MBeanServer server;

    public ViewManager(Object explorer) {
        this.explorer = explorer;
        this.server = (MBeanServer) InvokerHelper.getProperty(explorer, "MBeanServer");
    }

    public void setSelectedTreeNode(Object node) {
        if (node instanceof MBeanNode) {
            MBeanNode mbeanNode = (MBeanNode) node;

            // lets make the mbean view
            ObjectName name = mbeanNode.getObjectName();
            System.out.println("About to call method: createMBeanView");
            
            Component component = (Component) InvokerHelper.invokeMethod(explorer, "createMBeanView", name);
            setViewComponent(component);
        }
        else {
            setViewComponent(null);
        }
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
        panel.setLayout(new BorderLayout());
    }

    protected void setViewComponent(Component component) {
        panel.removeAll();
        if (component != null) {
            panel.add(component, BorderLayout.CENTER);
        }
        panel.invalidate();
        panel.revalidate();
        panel.repaint();
    }
}
